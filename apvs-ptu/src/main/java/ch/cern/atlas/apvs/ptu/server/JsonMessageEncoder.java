package ch.cern.atlas.apvs.ptu.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import ch.cern.atlas.apvs.domain.Packet;

public class JsonMessageEncoder extends MessageToByteEncoder<Packet> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public JsonMessageEncoder() {
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out)
			throws Exception {
		ByteBuf encoded = Unpooled.buffer();
		encoded.clear();
		ByteBufOutputStream os = new ByteBufOutputStream(encoded);
//		PtuJsonWriter writer = new PtuJsonWriter(os);		
//		writer.write(packet);
//		writer.close();
//		os.close();
		
		String s = PtuJsonWriter.toJson(packet);
//		log.info(getClass()+" "+s);
		os.write(s.getBytes());
		os.flush();
		os.close();
		
//		log.info("Encoded  "+encoded.toString(CharsetUtil.UTF_8));
		out.writeBytes(encoded);
		
		ctx.flush();
	}	
}
