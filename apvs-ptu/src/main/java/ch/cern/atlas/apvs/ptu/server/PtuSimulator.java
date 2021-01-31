package ch.cern.atlas.apvs.ptu.server;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.APVSException;
import ch.cern.atlas.apvs.domain.Device;
import ch.cern.atlas.apvs.domain.Event;
import ch.cern.atlas.apvs.domain.InetAddress;
import ch.cern.atlas.apvs.domain.MacAddress;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.domain.Message;
import ch.cern.atlas.apvs.domain.Packet;
import ch.cern.atlas.apvs.domain.Ptu;
import ch.cern.atlas.apvs.domain.Report;

public class PtuSimulator extends Thread {

	private static final boolean DEBUG_PARTIAL_MESSAGES;

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private final Channel channel;
	private final Random random = new Random();
	private final int defaultWait;
	private final int extraWait;
	private final int deltaStartTime = 12 * 3600 * 1000;
	private final String ptuId;
	private Ptu ptu;

	// encoding stack uses packet, so cannot include markers at this time
	private final static boolean WRITE_MARKERS = false;

	public PtuSimulator(String ptuId, int refresh) {
		this(ptuId, refresh, null);
	}

	public PtuSimulator(String ptuId, int refresh, Channel channel) {
		this.defaultWait = refresh;
		this.extraWait = refresh / 3;
		this.channel = channel;
		this.ptuId = ptuId;
	}

	@Override
	public void run() {
		try {
			long now = new Date().getTime();
			long then = now - deltaStartTime;
			Date start = new Date(then);

			Device device = new Device(ptuId,
					InetAddress.getByName("localhost"), "Test Device",
					new MacAddress("00:00:00:00:00:00"), "localhost", false);
			ptu = new Ptu(device);
			log.info("Creating " + ptuId + " delay " + (defaultWait / 1000)
					+ "s");

			try {
				ptu.addMeasurement(new Temperature(device, 25.7, start));
				ptu.addMeasurement(new Humidity(device, 31.4, start));
				ptu.addMeasurement(new CO2(device, 2.5, start));
				ptu.addMeasurement(new BodyTemperature(device, 37.2, start));
				ptu.addMeasurement(new HeartRate(device, 120, start));
				ptu.addMeasurement(new DoseAccum(device, 0.042, start));
				ptu.addMeasurement(new DoseRate(device, 0.001, start));
				ptu.addMeasurement(new O2(device, 85.2, start));
			} catch (APVSException e) {
				log.warn("Could not add measurement", e);
			}
			log.info(ptuId);

			then += defaultWait + random.nextInt(extraWait);

			int i = 1;
			try {
				// now loop at current time
				while (!isInterrupted()) {
					Message msg;

					if (i % 5 == 0) {
						msg = nextEvent(ptu, new Date());
					} else {
						msg = nextMeasurement(ptu, new Date());
					}
					Packet packet = new Packet(device.getName(), "Broadcast",
							i, false);
					packet.addMessage(msg);
//					String json = PtuJsonWriter.objectToJson(packet); // new
																		// JsonHeader(msg));
					// log.info(json +" "+json.length());

					if (WRITE_MARKERS) {
						StringBuffer b = new StringBuffer();
						b.append((char) 0x10);
//						b.append(json);
						b.append((char) 0x00);
						b.append((char) 0x13);
//						json = b.toString();
					}

					if (channel != null) {
//						if (DEBUG_PARTIAL_MESSAGES && (json.length() > 75)) {
//							write(json.substring(0, 75));
//							json = json.substring(75, json.length());
//							Thread.sleep(1000);
//						}
//						write(json);
						write(packet);
					}

//					log.info(""+packet);
					Thread.sleep(defaultWait + random.nextInt(extraWait));
					i++;
				}
			} catch (InterruptedException e) {
				// ignored
			}
		} finally {
			if (channel != null) {
				log.info("Closing");
				channel.close();
			}
		}
	}

	private void write(final Packet msg) {
		channel.write(msg).addListener(
				new GenericFutureListener<Future<? super Void>>() {
					@Override
					public void operationComplete(Future<? super Void> future)
							throws Exception {
						log.info("Sent "+future.isSuccess());
					}
				});
		channel.flush();
	}

	private Measurement nextMeasurement(Ptu ptu, Date d) {
		int index = random.nextInt(ptu.getSize());
		String name = ptu.getMeasurementNames().get(index);
		Measurement measurement = nextMeasurement(ptu.getMeasurement(name), d);
		try {
			ptu.addMeasurement(measurement);
		} catch (APVSException e) {
			log.warn("Could not add measurement", e);
		}
		return measurement;
	}

	private Measurement nextMeasurement(Measurement m, Date d) {
		return new Measurement(m.getDevice(), m.getSensor(), m.getValue()
				.doubleValue() + random.nextGaussian(), m.getDownThreshold(),
				m.getUpThreshold(), m.getUnit(), m.getSamplingRate(),
				m.getMethod(), d);
	}

	@SuppressWarnings("unused")
	private Report nextReport(Ptu ptu, Date d) {
		return new Report(ptu.getDevice(), random.nextGaussian(),
				random.nextBoolean(), random.nextBoolean(),
				random.nextBoolean(), d);
	}

	private Event nextEvent(Ptu ptu, Date d) {
		int index = random.nextInt(ptu.getSize());
		String name = ptu.getMeasurementNames().get(index);
		double d1 = random.nextDouble();
		double d2 = random.nextDouble();
		String unit = "";

		return new Event(ptu.getDevice(), name, "UpLevel", Math.max(d1, d2),
				Math.min(d1, d2), unit, d);
	}
}
