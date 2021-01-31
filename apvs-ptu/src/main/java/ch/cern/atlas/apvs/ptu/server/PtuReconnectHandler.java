package ch.cern.atlas.apvs.ptu.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.Packet;

public abstract class PtuReconnectHandler extends SimpleChannelInboundHandler<Packet> {
	private static final int RECONNECT_DELAY = 20;
	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private final Bootstrap bootstrap;
	private final String name;

	private InetSocketAddress address;
	private ChannelHandlerContext ctx;
	private Channel channel;
	private Timer reconnectTimer;
	
	private String cause;

	public PtuReconnectHandler(Bootstrap bootstrap, String name) {
		this.bootstrap = bootstrap;
		this.name = name;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("Connected to "+name+" on "+address);
		this.ctx = ctx;
		channel = ctx.channel();
		cause = "";
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// handle closed connection
		log.info("Closed "+name+" socket on "+address);

		channel = null;
		cancelTimer();
		reconnect();

		super.channelInactive(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if (evt instanceof IdleState) {
			IdleState e = (IdleState) evt;
			if (e == IdleState.READER_IDLE) {
				ctx.channel().close();
			} else if (e == IdleState.WRITER_IDLE) {
				// ctx.channel().write(new PingMessage());
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable caught) {
		if (caught instanceof ConnectException) {
			cause = "Connection Refused";
			log.warn(cause);
		} else if (caught instanceof SocketException) {
			cause = "Network is unreachable";
			log.warn(cause);
		} else {
			cause = caught.getMessage();
			log.warn("Unexpected exception from downstream.", caught);
		}
		ctx.channel().close();
	}

	public void connect(InetSocketAddress newAddress) {
		if (newAddress.equals(address))
			return;

		address = newAddress;
		cancelTimer();
		reconnect();
	}
	
	private void cancelTimer() {
		if (reconnectTimer != null) {
			reconnectTimer.stop();
			reconnectTimer = null;
		}		
	}
	
	private void reconnect() {
	
		if (channel != null) {
			channel.disconnect();
			channel = null;
		}
		
		log.info("Reconnecting to "+name+" on " + address);
		bootstrap.connect(address).addListener(new GenericFutureListener<Future<? super Void>>() {

			@Override
			public void operationComplete(Future<? super Void> arg)
					throws Exception {
				if (!arg.isSuccess()) {
					log.warn(arg.cause().toString());
					log.info("Sleeping for " + RECONNECT_DELAY + "s before reconnect "+name+" on "+address);
					
					reconnectTimer = new HashedWheelTimer();
					reconnectTimer.newTimeout(new TimerTask() {
						public void run(Timeout timeout) throws Exception {
							reconnect();
						}
					}, RECONNECT_DELAY, TimeUnit.SECONDS);
				} else {
					log.info("Being connected");
				}
			}
		});
	}

	public boolean isConnected() {
		return channel != null && channel.isActive();
	}
	
	public String getCause() {
		return cause;
	}

	public Channel getChannel() {
		return channel;
	}
	
	public ChannelHandlerContext getContext() {
		return ctx;
	}

}
