package ch.cern.atlas.apvs.ptu.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.Device;
import ch.cern.atlas.apvs.domain.Message;
import ch.cern.atlas.apvs.domain.Packet;

public class JsonMessageDecoder extends ReplayingDecoder<Void> {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());
	private final Map<String, Device> devices;
	
	public JsonMessageDecoder(Map<String, Device> devices) {
		this.devices = devices;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		if (ctx.channel().isActive()) {
			ByteBufInputStream is = new ByteBufInputStream(in);
			// log.debug("RIN/WIN: "+in.readerIndex()+" "+in.writerIndex());
			PtuJsonReader reader = new PtuJsonReader(is);
			JsonHeader header = (JsonHeader)reader.readObject();
//			log.info("*** " + header);
			String deviceName = header.getSender();
			Packet packet = new Packet(deviceName, header.getReceiver(), header.getFrameId(), header.getAcknowledge());
			Device device = devices.get(deviceName);
			if (device != null) {			
				for (Message msg : header.getMessages(device)) {
					packet.addMessage(msg);
				}
				out.add(packet);
			} else {
				log.warn("Device unknown in DB: "+deviceName);
			}
			reader.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		// super.exceptionCaught(ctx, cause);

		log.trace(getClass()+" EX: ", cause);
	}
}
