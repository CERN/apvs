package ch.cern.atlas.apvs.ptu.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;

public class PtuJsonReader extends JsonReader {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());
	
	public PtuJsonReader(InputStream in) {
		this(in, true);
	}

	public PtuJsonReader(InputStream in, boolean noObjects) {
		super(in, true);
				
		addReader(Date.class, new JsonClassReader() {

			@Override
			public Object read(Object o,
					LinkedList<JsonObject<String, Object>> stack)
					throws IOException {
				return JsonMessage.toDate(o);
			}
		});
	}

	@Override
	public Object readObject() throws IOException {
		JsonObject<?, ?> jsonObj = (JsonObject<?, ?>) super.readObject();
		String sender = (String) jsonObj.get("Sender");
		String receiver = (String) jsonObj.get("Receiver");
		Integer frameID = JsonMessage.toInteger(jsonObj.get("FrameID"));
		Boolean acknowledge = JsonMessage.toBoolean(jsonObj.get("Acknowledge"));
		
		JsonHeader header = new JsonHeader(sender, receiver, frameID, acknowledge);
		
		Object[] msgs = ((JsonObject<?, ?>)jsonObj.get("Messages")).getArray();
		// fix for #497
		if (msgs == null) {
			log.warn("No messages in JSON from " + sender);
			return header;
		}

		for (int i = 0; i < msgs.length; i++) {
			@SuppressWarnings("unchecked")
			JsonObject<String, Object> msg = (JsonObject<String, Object>)msgs[i];
			header.addMessage(new JsonMessage(msg));
		}

		// returns header with a list of messages
		return header;
	}


	public static JsonHeader jsonToJava(String json) throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(
				json.getBytes(StandardCharsets.UTF_8));
		// NOTE: noObjects as we decode ourselves
		PtuJsonReader jr = new PtuJsonReader(ba, true);
		JsonHeader result = (JsonHeader) jr.readObject();
		jr.close();
		return result;
	}

	public static JsonHeader toJava(String json) {
		try {
			return jsonToJava(json);
		} catch (Exception ignored) {
			return null;
		}
	}
}
