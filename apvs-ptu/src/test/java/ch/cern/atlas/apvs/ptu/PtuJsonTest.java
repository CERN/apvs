package ch.cern.atlas.apvs.ptu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.Device;
import ch.cern.atlas.apvs.domain.InetAddress;
import ch.cern.atlas.apvs.domain.MacAddress;
import ch.cern.atlas.apvs.domain.Measurement;
import ch.cern.atlas.apvs.domain.Message;
import ch.cern.atlas.apvs.domain.Packet;
import ch.cern.atlas.apvs.ptu.server.Humidity;
import ch.cern.atlas.apvs.ptu.server.JsonHeader;
import ch.cern.atlas.apvs.ptu.server.PtuJsonReader;
import ch.cern.atlas.apvs.ptu.server.PtuJsonWriter;
import ch.cern.atlas.apvs.ptu.server.PtuServerConstants;

public class PtuJsonTest {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	String json = "{"
			+ "\"Sender\":\"PTU_88\",\"Receiver\":\"Broadcast\",\"FrameID\":\"0\",\"Acknowledge\":\"False\",\"Messages\":["
			+ "{\"Type\":\"Measurement\",\"Sensor\":\"Humidity\",\"Time\":\"11/09/2012 10:02:25\",\"Method\":\"OneShoot\",\"Value\":\"33.19684099267707\",\"SamplingRate\":\"10000\",\"Unit\":\"ppm\",\"DownThreshold\":\"33.0\",\"UpThreshold\":\"35.7\"},"
			+ "{\"Type\":\"Measurement\",\"Sensor\":\"Humidity\",\"Time\":\"11/09/2012 10:07:10\",\"Method\":\"OneShoot\",\"Value\":\"35.45927608218701\",\"SamplingRate\":\"15000\",\"Unit\":\"ppm\",\"DownThreshold\":\"33.0\",\"UpThreshold\":\"35.7\"}"
			+ "]}";

	String msg0 = "Measurement(PTU_88): sensor:Humidity, value:33.19684099267707, unit:ppm, sampling rate:10000"; // , date:11/09/2012 10:02:25";
	String msg1 = "Measurement(PTU_88): sensor:Humidity, value:35.45927608218701, unit:ppm, sampling rate:15000"; // , date:11/09/2012 10:07:10";

	String parsedJson = "{"
			+ "\"Sender\":\"PTU_88\",\"Receiver\":\"Broadcast\",\"FrameID\":\"0\",\"Acknowledge\":\"False\",\"Messages\":["
			+ "{\"Time\":\"04/07/2013 15:42:53\",\"Value\":\"33.19684099267707\",\"Unit\":\"ppm\",\"Method\":\"OneShoot\",\"SamplingRate\":\"60000\",\"Sensor\":\"Humidity\",\"UpThreshold\":\"130.0\",\"DownThreshold\":\"50.0\",\"Connected\":\"True\",\"Type\":\"Measurement\"},"
			+ "{\"Time\":\"04/07/2013 21:16:13\",\"Value\":\"35.45927608218701\",\"Unit\":\"ppm\",\"Method\":\"OneShoot\",\"SamplingRate\":\"60000\",\"Sensor\":\"Humidity\",\"UpThreshold\":\"130.0\",\"DownThreshold\":\"50.0\",\"Connected\":\"True\",\"Type\":\"Measurement\"}"
			+ "]}";
	
	@Test
	public void readerTest() throws IOException {
		Device device = new Device("PTU_88", InetAddress.getByName("localhost"), "Test Device", new MacAddress("00:00:00:00:00:00"), "localhost", false);
		
		JsonHeader header = PtuJsonReader.jsonToJava(json);
		// log.info(""+packet);

		List<Message> list = header.getMessages(device);
		Assert.assertEquals(2, list.size());
//		 log.info(list.get(0).toString());
//		 log.info(list.get(1).toString());
		Assert.assertEquals(msg0, ((Measurement)list.get(0)).toShortString());
		Assert.assertEquals(msg1, ((Measurement)list.get(1)).toShortString());
	}

	@Test
	public void streamReaderTest() throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(
				(json + json).getBytes(StandardCharsets.UTF_8));
//		log.info("Len "+json.length());
		PtuJsonReader jr = new PtuJsonReader(ba, true);
		JsonHeader packet1 = (JsonHeader) jr.readObject();
//		log.info(""+packet1);
		Assert.assertEquals("PTU_88", packet1.getSender());
		JsonHeader packet2 = (JsonHeader) jr.readObject();
//		log.info(""+packet2);
		Assert.assertEquals("PTU_88", packet2.getSender());
		ba.close();
		jr.close();
	}

	@Test
	public void writerTest() throws ParseException, UnknownHostException {
		Device device = new Device("PTU_88", InetAddress.getByName("localhost"), "Test Device", new MacAddress("00:00:00:00:00:00"), "localhost", false);
		Packet packet = new Packet(device.getName(), "Broadcast", 0, false);
		packet.addMessage(new Humidity(device, 33.19684099267707,
				PtuServerConstants.dateFormat.parse("04/07/2013 15:42:53")));
		packet.addMessage(new Humidity(device, 35.45927608218701,
				PtuServerConstants.dateFormat.parse("04/07/2013 21:16:13")));

		String output = PtuJsonWriter.toJson(packet);
		log.info(output);
		Assert.assertEquals(parsedJson, output);
	}
}
