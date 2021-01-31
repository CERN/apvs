package ch.cern.atlas.apvs.ptu.server;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;

import ch.cern.atlas.apvs.domain.Device;

public class PtuChannelInitializer extends ChannelInitializer<SocketChannel> {

	private final boolean handleIdle;
	private final ChannelInboundHandlerAdapter handler;
	private final Map<String, Device> devices;

	public PtuChannelInitializer(ChannelInboundHandlerAdapter handler, Map<String, Device> devices, boolean handleIdle) {
		this.handleIdle = handleIdle;
		this.handler = handler;
		this.devices = devices;
	}
	
	protected void initChannel(SocketChannel ch) throws Exception {
		if (handleIdle) {
			ch.pipeline().addLast(new IdleStateHandler(60, 30, 0));			
		}
		if (devices != null) {
			ch.pipeline().addLast(new RemoveDelimiterDecoder());
			ch.pipeline().addLast(new JsonMessageDecoder(devices));
		}
		ch.pipeline().addLast(new JsonMessageEncoder());
		if (handler != null) {
			ch.pipeline().addLast(handler);
		}
	}
}
