package ch.cern.atlas.apvs.ptu.server;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.Device;
import ch.cern.atlas.apvs.domain.Error;
import ch.cern.atlas.apvs.domain.Event;
import ch.cern.atlas.apvs.domain.GeneralConfiguration;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.domain.MeasurementConfiguration;
import ch.cern.atlas.apvs.domain.Message;
import ch.cern.atlas.apvs.domain.Order;
import ch.cern.atlas.apvs.domain.Report;
import ch.cern.atlas.apvs.domain.SensorOrder;

import com.cedarsoftware.util.io.JsonObject;

public class JsonMessage {

	private static final Logger log = LoggerFactory.getLogger(JsonMessage.class
			.getName());
	
	private static final String TYPE = "Type";
	private static final String SENSOR = "Sensor";
	private static final String UNIT = "Unit";
	private static final String VALUE = "Value";
	private static final String TIME = "Time";
	private static final String SAMPLING_RATE = "SamplingRate";
	private static final String UP_THRESHOLD = "UpThreshold";
	private static final String DOWN_THRESHOLD = "DownThreshold";
	private static final String METHOD = "Method";
	private static final String THRESHOLD = "Threshold";
	private static final String DESCRIPTION = "Description";
	private static final String CRITICALITY = "Criticality";
	private static final String DOSIMETER_ID = "DosimeterID";
	private static final String BSSID = "bssid";
	private static final String SLOPE = "Slope";
	private static final String OFFSET = "Offset";
	private static final String NAME = "Name";
	private static final String PARAMETER = "Parameter";
	private static final String BATTERY_LEVEL = "BatteryLevel";
	private static final String CAMERA_HANDHELD = "CameraHandheld";
	private static final String CAMERA_HELMET = "CameraHelmet";
	private static final String AUDIO = "Audio";
	
	protected JsonObject<String, Object> msg;

	public JsonMessage(JsonObject<String, Object> msg) {
		this.msg = msg;
	}

	// FIXME add proper conversions (Date, Int, Double)
	public JsonMessage(Message message) {
		msg = new JsonObject<String, Object>();
		if (message instanceof Measurement) {
			Measurement m = (Measurement) message;
			msg.put(TYPE, m.getType());
			msg.put(SENSOR, m.getSensor());
			msg.put(UNIT, m.getUnit());
			msg.put(VALUE, m.getValue().toString());
			msg.put(TIME, m.getTime());
			msg.put(SAMPLING_RATE, m.getSamplingRate());
			msg.put(UP_THRESHOLD, m.getUpThreshold());
			msg.put(DOWN_THRESHOLD, m.getDownThreshold());
			msg.put(METHOD, m.getMethod());
		} else if (message instanceof Event) {
			Event m = (Event) message;
			msg.put(TYPE, m.getType());
			msg.put(VALUE, m.getValue());
			msg.put(THRESHOLD, m.getThreshold());
			msg.put(UNIT, m.getUnit());
			msg.put(TIME, m.getTime());
		} else if (message instanceof Error) {
			Error m = (Error) message;
			msg.put(TYPE, m.getType());
			msg.put(DESCRIPTION, m.getDescription());
			msg.put(CRITICALITY, m.getCriticality());
			msg.put(TIME, m.getTime());
		} else if (message instanceof GeneralConfiguration) {
			GeneralConfiguration m = (GeneralConfiguration) message;
			msg.put(TYPE, m.getType());
			msg.put(DOSIMETER_ID, m.getDosimeterId());
			msg.put(BSSID, m.getBSSID());
		} else if (message instanceof MeasurementConfiguration) {
			MeasurementConfiguration m = (MeasurementConfiguration) message;
			msg.put(TYPE, m.getType());
			msg.put(SENSOR, m.getSensor());
			msg.put(UNIT, m.getUnit());
			msg.put(TIME, m.getTime());
			msg.put(SAMPLING_RATE, m.getSamplingRate());
			msg.put(UP_THRESHOLD, m.getUpThreshold());
			msg.put(DOWN_THRESHOLD, m.getDownThreshold());
			msg.put(SLOPE, m.getSlope());
			msg.put(OFFSET, m.getOffset());
		} else if (message instanceof SensorOrder) {
			SensorOrder m = (SensorOrder) message;
			msg.put(TYPE, m.getType());
			msg.put(NAME, m.getName());
			msg.put(PARAMETER, m.getParameter());
			msg.put(VALUE, m.getValue());
		} else if (message instanceof Order) {
			Order m = (Order) message;
			msg.put(TYPE, m.getType());
			msg.put(PARAMETER, m.getParameter());
			msg.put(VALUE, m.getValue());
		} else if (message instanceof Report) {
			Report m = (Report) message;
			msg.put(TYPE, m.getType());
			msg.put(BATTERY_LEVEL, m.getBatteryLevel());
			msg.put(CAMERA_HANDHELD, m.getCameraHandheld());
			msg.put(CAMERA_HELMET, m.getCameraHelmet());
			msg.put(AUDIO, m.getAudio());
			msg.put(TIME, m.getTime());
		} else {
			throw new RuntimeException("Cannot find JsonMessage for '"
					+ message + "'");
		}
	}

	public Message toMessage(Device device) {
		String type = (String) msg.get(TYPE);
		if (type.equals("Measurement")) {
			String sensor = getString(SENSOR);
			String unit = getString(UNIT);
			Double value = getDouble(VALUE);
			Date time = getDate(TIME);
			Integer samplingRate = getInteger(SAMPLING_RATE);

			// fix for #486 and #490
			if ((sensor == null) || (value == null) || (unit == null)
					|| (time == null)) {
				log.warn("PTU "
						+ device.getName()
						+ ": Measurement contains <null> sensor, value, samplingrate, unit or time ("
						+ sensor + ", " + value + ", " + unit + ", "
						+ samplingRate + ", " + time + ")");
				return null;
			}

			return new Measurement(device, sensor, value,
					getDouble(DOWN_THRESHOLD), getDouble(UP_THRESHOLD), unit,
					samplingRate, getString(METHOD), time);
		} else if (type.equals("Event")) {
			return new Event(device, getString(SENSOR),
					getString("EventType"), getDouble(VALUE),
					getDouble(THRESHOLD), getString(UNIT), getDate(TIME));
		} else if (type.equals("Error")) {
			return new Error(device, getInteger("ErrNo"),
					getString(DESCRIPTION), getString(CRITICALITY),
					getDate(TIME));
		} else if (type.equals("GeneralConfiguration")) {
			return new GeneralConfiguration(device, getString(DOSIMETER_ID), getString(BSSID), getDate(TIME));
		} else if (type.equals("MeasurementConfiguration")) {
			return new MeasurementConfiguration(device, getString(SENSOR), 
					getDouble(DOWN_THRESHOLD), getDouble(UP_THRESHOLD), getString(UNIT),
					getInteger(SAMPLING_RATE), getDouble(SLOPE), getDouble(OFFSET), getDate(TIME));
		} else if (type.equals("SensorOrder")) {
			return new SensorOrder(device, getString(NAME),
					getString(PARAMETER), getString(VALUE));
		} else if (type.equals("Order")) {
			return new Order(device, getString(PARAMETER), getString(VALUE));
		} else if (type.equals("Report")) {
			return new Report(device, getDouble(BATTERY_LEVEL),
					getBoolean(CAMERA_HANDHELD), getBoolean(CAMERA_HELMET),
					getBoolean(AUDIO), getDate(TIME));
		}

		log.warn("Ignoring message with unknown type: " + type + " " + msg);

		return null;
	}

	private String getString(String key) {
		return toString(msg.get(key));
	}

	private Double getDouble(String key) {
		return toDouble(msg.get(key));
	}

	private Integer getInteger(String key) {
		return toInteger(msg.get(key));
	}

	private Boolean getBoolean(String key) {
		return toBoolean(msg.get(key));
	}

	private Date getDate(String key) {
		return toDate(msg.get(key));
	}
	
	public static String toString(Object string) {
		if ((string == null) || !(string instanceof String)) {
			return null;
		}
		
		return string.toString();
	}

	public static Double toDouble(Object number) {
		if ((number == null) || !(number instanceof String)) {
			return null;
		}
		try {
			return Double.parseDouble((String) number);
		} catch (NumberFormatException e) {
			log.warn("NumberFormatException " + number + " " + e);
			return null;
		}
	}

	public static Integer toInteger(Object number) {
		if ((number == null) || !(number instanceof String)) {
			return null;
		}
		try {
			return Integer.parseInt((String) number);
		} catch (NumberFormatException e) {
			log.warn("NumberFormatException " + number + " " + e);
			return null;
		}
	}

	public static boolean toBoolean(Object state) {
		if ((state == null) || !(state instanceof String)) {
			return false;
		}
		try {
			String b = (String) state;
			b = b.equals("1") ? "true" : b.equals("0") ? "false" : b;
			return Boolean.parseBoolean((String) state);
		} catch (NumberFormatException e) {
			log.warn("NumberFormatException " + state + " " + e);
			return false;
		}
	}

	public static Date toDate(Object date) {
		try {
			return PtuServerConstants.dateFormat.parse((String) date);
		} catch (ParseException e) {
			return null;
		}
	}

	public String toString() {
		return msg.toString();
	}

	@SuppressWarnings("unused")
	private String limit(String value, int length) {
		StringBuilder buf = new StringBuilder(value);
		if (buf.length() > length) {
			buf.setLength(length);
		}

		return buf.toString();
	}
}
