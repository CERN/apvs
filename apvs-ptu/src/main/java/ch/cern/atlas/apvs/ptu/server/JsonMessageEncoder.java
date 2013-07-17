package ch.cern.atlas.apvs.ptu.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

import ch.cern.atlas.apvs.domain.Packet;

public class JsonMessageEncoder extends MessageToByteEncoder<Packet> {

	private final Charset charset;

	public JsonMessageEncoder() {
		this.charset = Charset.defaultCharset();
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out)
			throws Exception {
		ByteBuf encoded = Unpooled.buffer();
		ByteBufOutputStream os = new ByteBufOutputStream(encoded);
		PtuJsonWriter writer = new PtuJsonWriter(os);
		writer.write(packet);
		writer.close();
		out.writeBytes(encoded);
	}
}
