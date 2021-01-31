package ch.cern.atlas.apvs.ptu.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.SocketAddress;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.Device;
import ch.cern.atlas.apvs.domain.Event;
import ch.cern.atlas.apvs.domain.Packet;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

@Sharable
public class MessageToBus extends SimpleChannelInboundHandler<Packet> {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private final String prefix;
	private final EventBus bus;
	private ChannelHandlerContext ctx;
	private final HandlerRegistration handler;
	private Device device;

	public MessageToBus(final String prefix, EventBus bus) {
		this.prefix = prefix;
		this.bus = bus;

		handler = bus.addHandler(MessageEvent.TYPE, new MessageEvent.Handler() {

			@Override
			public void onMessageReceived(MessageEvent event) {
				if (event.getPrefix().equals(prefix)) {
					return;
				}

//				log.info("Will send to " + prefix + " "
//						+ ctx.channel().remoteAddress());
				if ((ctx != null) && ctx.channel().isActive()) {
					ctx.channel()
							.write(event.getPacket())
							.addListener(
									new GenericFutureListener<Future<? super Void>>() {

										@Override
										public void operationComplete(
												Future<? super Void> future)
												throws Exception {
											if (!future.isSuccess()) {
												log.error("ERROR: "
														+ future.cause());
											}
										}

									});
					ctx.flush();
				} else {
					log.info("Channel closed "+ctx.channel().remoteAddress());
				}
			}
		});
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Packet packet)
			throws Exception {

		if ((device == null) && (packet.getMessages().size() > 0)) {
			device = packet.getMessages().get(0).getDevice();
			
			Packet connectionPacket = new Packet(device.getName(), "Broadcast", 0, false);
			connectionPacket.addMessage(new Event(device, ctx.channel().remoteAddress().toString(), "Connect", new Date()));
			bus.fireEvent(new MessageEvent(prefix, connectionPacket));
		}
		
		bus.fireEvent(new MessageEvent(prefix, packet));
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		log.info(prefix + " opened from "
				+ ctx.channel().remoteAddress());
		this.ctx = ctx;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);

		if (device != null) {
			Packet connectionPacket = new Packet(device.getName(), "Broadcast", 0, false);
			connectionPacket.addMessage(new Event(device, ctx.channel().remoteAddress().toString(), "Disconnect", new Date()));
			bus.fireEvent(new MessageEvent(prefix, connectionPacket));
			
			device = null;
		}

		if (handler != null) {
			log.info("Removed handler for "+ctx.channel().remoteAddress());
			handler.removeHandler();
		}
		log.info(prefix + " closed by "
				+ ctx.channel().remoteAddress());
		
		this.ctx = ctx;
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		SocketAddress remote = ctx.channel().remoteAddress();
		if (evt instanceof IdleState) {
			switch ((IdleState) evt) {
			case ALL_IDLE:
			case READER_IDLE:
				log.warn("Channel read-idle or all-idle, closing "+remote);
				ctx.channel().close();
				break;
			case WRITER_IDLE:
				log.warn("Channel write-idle, pinging "+remote+"...");
				ctx.channel().write(new Packet("DaqServer", "Ping", 0, false));
				break;
			default:
				log.warn("Unknown idle state "+evt+" for "+remote);
				break;
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}
}
