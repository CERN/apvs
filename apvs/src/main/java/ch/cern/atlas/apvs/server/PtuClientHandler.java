package ch.cern.atlas.apvs.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.APVSException;
import ch.cern.atlas.apvs.domain.Error;
import ch.cern.atlas.apvs.domain.Event;
import ch.cern.atlas.apvs.domain.History;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.domain.Message;
import ch.cern.atlas.apvs.domain.Ptu;
import ch.cern.atlas.apvs.domain.Report;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
import ch.cern.atlas.apvs.ptu.server.PtuJsonReader;
import ch.cern.atlas.apvs.ptu.server.PtuReconnectHandler;
import ch.cern.atlas.apvs.ptu.shared.EventChangedEvent;
import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;

public class PtuClientHandler extends PtuReconnectHandler {

	private Logger log = LoggerFactory.getLogger(getClass().getName());
	private final RemoteEventBus eventBus;

	private Ptus ptus = Ptus.getInstance();

	private Map<String, Set<String>> measurementChanged = new HashMap<String, Set<String>>();

	public PtuClientHandler(ClientBootstrap bootstrap,
			final RemoteEventBus eventBus) {
		super(bootstrap);
		this.eventBus = eventBus;

		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {

			@Override
			public void onRequestEvent(RequestRemoteEvent event) {
				String type = event.getRequestedClassName();

				if (type.equals(MeasurementChangedEvent.class.getName())) {
					List<Measurement> m = getMeasurements();
					log.info("Getting all meas " + m.size());
					for (Iterator<Measurement> i = m.iterator(); i.hasNext();) {
						eventBus.fireEvent(new MeasurementChangedEvent(i.next()));
					}
				} else if (type.equals(EventChangedEvent.class.getName())) {
					eventBus.fireEvent(new EventChangedEvent(null));
				}
			}
		});
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) {
		// Print out the line received from the server.
		String line = (String) event.getMessage();
		// log.info(line);

		List<Message> list;
		try {
			list = PtuJsonReader.jsonToJava(line);
			for (Iterator<Message> i = list.iterator(); i.hasNext();) {
				Message message = i.next();
				String ptuId = message.getPtuId();
				Ptu ptu = ptus.get(ptuId);
				if (ptu != null) {
					try {
						if (message instanceof Measurement) {
							handleMessage(ptu, (Measurement) message);
						} else if (message instanceof Report) {
							handleMessage(ptu, (Report) message);
						} else if (message instanceof Event) {
							handleMessage(ptu, (Event) message);
						} else if (message instanceof Error) {
							handleMessage(ptu, (Error) message);
						} else {
							log.warn("Error: unknown Message Type: "
									+ message.getType());
						}
					} catch (APVSException e) {
						log.warn("Could not add measurement", e);
					}
				}
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
			ioe.printStackTrace();
		}

	}

	private void handleMessage(Ptu ptu, Measurement message)
			throws APVSException {

		String ptuId = message.getPtuId();
		String sensor = message.getName();
		History history = ptus.setHistory(ptuId, sensor, message.getUnit());

		ptu.addMeasurement(message);
		Set<String> changed = measurementChanged.get(ptuId);
		if (changed == null) {
			changed = new HashSet<String>();
			measurementChanged.put(ptuId, changed);
		}
		changed.add(message.getName());

		// duplicate entries will not be added
		if (history != null) {
			history.addEntry(message.getDate().getTime(), message.getValue());
		}

		sendEvents();
	}

	private void handleMessage(Ptu ptu, Event message) {
		String ptuId = message.getPtuId();
		String sensor = message.getName();

		log.info("EVENT " + message);

		eventBus.fireEvent(new EventChangedEvent(new Event(ptuId, sensor,
				message.getEventType(), message.getValue(), message
						.getTheshold(), message.getDate())));
	}

	private void handleMessage(Ptu ptu, Report report) {
		log.warn(report.getType() + " NOT YET IMPLEMENTED, see #23 and #112");
	}

	private void handleMessage(Ptu ptu, Error error) {
		log.warn(error.getType() + " NOT YET IMPLEMENTED, see #114");
	}

	private synchronized void sendEvents() {

		for (Iterator<String> i = measurementChanged.keySet().iterator(); i
				.hasNext();) {
			String id = i.next();
			for (Iterator<String> j = measurementChanged.get(id).iterator(); j
					.hasNext();) {
				Measurement m = ptus.get(id).getMeasurement(j.next());
				eventBus.fireEvent(new MeasurementChangedEvent(m));
			}
		}

		measurementChanged.clear();
	}

	public Ptu getPtu(String ptuId) {
		return ptus.get(ptuId);
	}

	public List<String> getPtuIds() {
		List<String> list = new ArrayList<String>();
		list.addAll(ptus.getPtuIds());
		Collections.sort(list);
		return list;
	}

	public List<Measurement> getMeasurements() {
		List<Measurement> m = new ArrayList<Measurement>();
		for (Iterator<Ptu> i = ptus.getPtus().iterator(); i.hasNext();) {
			m.addAll(i.next().getMeasurements());
		}
		return m;
	}
}