package ch.cern.atlas.apvs.ptu.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;

import ch.cern.atlas.apvs.domain.History;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.domain.Message;
import ch.cern.atlas.apvs.domain.Ptu;
import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
import ch.cern.atlas.apvs.ptu.shared.PtuIdsChangedEvent;

public class PtuClientHandler extends PtuReconnectHandler {

	private static final Logger logger = Logger
			.getLogger(PtuClientHandler.class.getName());
	private final RemoteEventBus eventBus;

	private SortedMap<String, Ptu> ptus;

	private boolean ptuIdsChanged = false;
	private Map<String, Set<String>> measurementChanged = new HashMap<String, Set<String>>();

	PreparedStatement historyQueryCount;
	PreparedStatement historyQuery;

	public PtuClientHandler(ClientBootstrap bootstrap,
			final RemoteEventBus eventBus) {
		super(bootstrap);
		this.eventBus = eventBus;

		init();

		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {

			@Override
			public void onRequestEvent(RequestRemoteEvent event) {
				String type = event.getRequestedClassName();

				if (type.equals(PtuIdsChangedEvent.class.getName())) {
					eventBus.fireEvent(new PtuIdsChangedEvent(getPtuIds()));
				} else if (type.equals(MeasurementChangedEvent.class.getName())) {
					List<Measurement<Double>> m = getMeasurements();
					System.err.println("Getting all meas " + m.size());
					for (Iterator<Measurement<Double>> i = m.iterator(); i
							.hasNext();) {
						eventBus.fireEvent(new MeasurementChangedEvent(i.next()));
					}
				}
			}
		});
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		if (e instanceof ChannelStateEvent) {
			logger.info(e.toString());
		}
		super.handleUpstream(ctx, e);
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// handle closed connection
		init();
		eventBus.fireEvent(new PtuIdsChangedEvent(new ArrayList<String>()));

		super.channelClosed(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		// Print out the line received from the server.
		String line = (String) e.getMessage();
		// System.err.println(line);
		List<Message> list = PtuJsonReader.toJava(line);
		for (Iterator<Message> i = list.iterator(); i.hasNext();) {
			Message message = i.next();
			if (message instanceof Measurement<?>) {
				@SuppressWarnings("unchecked")
				Measurement<Double> measurement = (Measurement<Double>) message;

				String ptuId = measurement.getPtuId();
				Ptu ptu = ptus.get(ptuId);
				if (ptu == null) {
					ptu = new Ptu(ptuId);
					ptus.put(ptuId, ptu);
					ptuIdsChanged = true;
				}

				String sensor = measurement.getName();
				History history = ptu.getHistory(sensor);
				if (history == null) {
					// check on history and load from DB
					if ((historyQuery != null) && (historyQueryCount != null)) {

						try {
							historyQueryCount.setString(1, sensor);
							historyQueryCount.setString(2, ptuId);
							historyQuery.setString(1, sensor);
							historyQuery.setString(2, ptuId);

							ResultSet result = historyQueryCount.executeQuery();
							result.next();

							int n = result.getInt(1);
							result.close();

							int MAX_ENTRIES = 1000;
							long MIN_INTERVAL = 5000; // ms

							if (n > 0) {
								// limit entries
								if (n > MAX_ENTRIES)
									n = 1000;

								Deque<Number[]> data = new ArrayDeque<Number[]>(
										n);

								long lastTime = new Date().getTime();
								result = historyQuery.executeQuery();
								while (result.next() && (data.size() <= n)) {
									long time = result.getTimestamp(1)
											.getTime();

									// limit entry separation (reverse order
									// !!!)
									if (lastTime - time > MIN_INTERVAL) {
										lastTime = time;

										Number[] entry = new Number[2];
										entry[0] = time;
										entry[1] = Double.parseDouble(result
												.getString(2));
										data.addFirst(entry);
									}
								}
								result.close();

								System.err.println("Creating history for "
										+ ptuId + " " + sensor + " "
										+ data.size() + " entries");
								history = new History(
										data.toArray(new Number[data.size()][]),
										measurement.getUnit());

								ptu.setHistory(sensor, history);
							}
						} catch (SQLException ex) {
							System.err.println(ex);
						}
					}
				}

				ptu.addMeasurement(measurement);
				Set<String> changed = measurementChanged.get(ptuId);
				if (changed == null) {
					changed = new HashSet<String>();
					measurementChanged.put(ptuId, changed);
				}
				changed.add(measurement.getName());

				// duplicate entries will not be added
				if (history != null) {
					history.addEntry(measurement.getDate().getTime(),
							measurement.getValue());
				}
				
				sendEvents();
			}
		}
	}

	private void init() {
		ptus = new TreeMap<String, Ptu>();
	}

	private synchronized void sendEvents() {
		// fire all at the end
		// FIXME we can still add MeasurementNamesChanged
		if (ptuIdsChanged) {
			eventBus.fireEvent(new PtuIdsChangedEvent(new ArrayList<String>(
					ptus.keySet())));
			ptuIdsChanged = false;
		}

		for (Iterator<String> i = measurementChanged.keySet().iterator(); i
				.hasNext();) {
			String id = i.next();
			for (Iterator<String> j = measurementChanged.get(id).iterator(); j
					.hasNext();) {
				Measurement<Double> m = ptus.get(id).getMeasurement(j.next());
				eventBus.fireEvent(new MeasurementChangedEvent(m));
			}
		}

		measurementChanged.clear();
	}

	public Ptu getPtu(String ptuId) {
		return ptus.get(ptuId);
	}

	public List<String> getPtuIds() {
		List<String> list = new ArrayList<String>(ptus.keySet());
		Collections.sort(list);
		return list;
	}

	public List<Measurement<Double>> getMeasurements() {
		List<Measurement<Double>> m = new ArrayList<Measurement<Double>>();
		for (Iterator<Ptu> i = ptus.values().iterator(); i.hasNext();) {
			m.addAll(i.next().getMeasurements());
		}
		return m;
	}

	public void connect(String dbUrl) {
		System.err.println("Connecting to DB " + dbUrl);
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			Connection connection = DriverManager.getConnection(dbUrl);

			historyQueryCount = connection
					.prepareStatement("select count(*) from tbl_measurements "
							+ "join tbl_devices on tbl_measurements.device_id = tbl_devices.id "
							+ "where sensor = ? " + "and name = ?");

			historyQuery = connection
					.prepareStatement("select DATETIME, VALUE from tbl_measurements "
							+ "join tbl_devices on tbl_measurements.device_id = tbl_devices.id "
							+ "where SENSOR = ? "
							+ "and NAME = ? "
							+ "order by DATETIME desc");
		} catch (SQLException e) {
			System.err.println(e);
		}
	}
}
