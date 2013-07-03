package ch.cern.atlas.apvs.ptu.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.Event;
import ch.cern.atlas.apvs.domain.GeneralConfiguration;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.domain.Packet;

import com.cedarsoftware.util.io.JsonReader;

public class PtuJsonReader extends JsonReader {

	private Logger log = LoggerFactory.getLogger(getClass().getName());

	public PtuJsonReader(InputStream in) {
		super(in);
	}

	public PtuJsonReader(InputStream in, boolean noObjects) {
		super(in, noObjects);
	}

	@Override
	public Object readObject() throws IOException {
		@SuppressWarnings("rawtypes")
		JsonObject jsonObj = (JsonObject) readIntoJsonMaps();

		String sender = (String) jsonObj.get("Sender");
		String receiver = (String) jsonObj.get("Receiver");
		Integer frameID = convertToInteger(jsonObj.get("FrameID"));
		Boolean acknowledge = convertToBoolean(jsonObj.get("Acknowledge"));
		
		Packet packet = new Packet(sender, receiver, frameID, acknowledge);

		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<JsonObject> msgs = (List<JsonObject>) jsonObj.get("Messages");
		// fix for #497
		if (msgs == null) {
			log.warn("No messages in JSON from "+sender);
			return packet;
		}
		JsonMessage[] messages = new JsonMessage[msgs.size()];

		for (int i = 0; i < messages.length; i++) {
			@SuppressWarnings("rawtypes")
			JsonObject msg = msgs.get(i);
			String type = (String) msg.get("Type");
			if (type.equals("Measurement")) {
				String sensor = (String) msg.get("Sensor");
				String unit = (String) msg.get("Unit");
				Number value = convertToDouble(msg.get("Value"));
				String time = (String)msg.get("Time");
				String samplingRate = (String) msg.get("SamplingRate");
				
				// fix for #486 and #490
				if ((sensor == null) || (value == null) || (unit == null) || (time == null)) {
					log.warn("PTU "+sender+": Measurement contains <null> sensor, value, samplingrate, unit or time ("+sensor+", "+value+", "+unit+", "+samplingRate+", "+time+")");
					continue;
				}

				Number low = convertToDouble(msg.get("DownThreshold"));
				Number high = convertToDouble(msg.get("UpThreshold"));

				// Scale down to microSievert
				value = Scale.getValue(value, unit);
				low = Scale.getLowLimit(low, unit);
				high = Scale.getHighLimit(high, unit);
				unit = Scale.getUnit(unit);

				packet.addMessage(new Measurement(sender, sensor, value, low, high,
						unit,
						Integer.parseInt(samplingRate),
						convertToDate(time)));
			} else if (type.equals("Event")) {
				packet.addMessage(new Event(sender, (String) msg.get("Sensor"),
						(String) msg.get("EventType"), convertToDouble(msg
								.get("Value")), convertToDouble(msg
								.get("Threshold")), (String) msg.get("Unit"),
						convertToDate(msg.get("Time"))));
			} else if (type.equals("GeneralConfiguration")) {
				packet.addMessage(new GeneralConfiguration(sender, (String) msg.get("DosimeterID")));
			} else {
				log.warn("Message type not implemented: " + type);
			}
			// FIXME add other types of messages, #115 #112 #114
		}

		// returns packet with a list of messages
		return packet;
	}

	private Double convertToDouble(Object number) {
		if ((number == null) || !(number instanceof String)) {
			return null;
		}
		try {
			return Double.parseDouble((String) number);
		} catch (NumberFormatException e) {
			log.warn("NumberFormatException "+number+" "+e);
			return null;
		}
	}

	private Integer convertToInteger(Object number) {
		if ((number == null) || !(number instanceof String)) {
			return null;
		}
		try {
			return Integer.parseInt((String) number);
		} catch (NumberFormatException e) {
			log.warn("NumberFormatException "+number+" "+e);
			return null;
		}
	}

	private boolean convertToBoolean(Object state) {
		if ((state == null) || !(state instanceof String)) {
			return false;
		}
		try {
			return Boolean.parseBoolean((String) state);
		} catch (NumberFormatException e) {
			log.warn("NumberFormatException "+state+" "+e);
			return false;
		}
	}

	@Override
	protected Date convertToDate(Object rhs) {
		try {
			return PtuServerConstants.dateFormat.parse((String) rhs);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Packet jsonToJava(String json) throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(
				json.getBytes("UTF-8"));
		PtuJsonReader jr = new PtuJsonReader(ba, false);
		@SuppressWarnings("unchecked")
		Packet result = (Packet) jr.readObject();
		jr.close();
		return result;
	}

	public static Packet toJava(String json) {
		try {
			return jsonToJava(json);
		} catch (Exception ignored) {
			return null;
		}
	}
}
