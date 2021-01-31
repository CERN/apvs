package ch.cern.atlas.apvs.dosimeter.server;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DosimeterServerHandler extends SimpleChannelUpstreamHandler {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private final Map<Channel, DosimeterSimulator> simulators = new HashMap<Channel, DosimeterSimulator>();

	public DosimeterServerHandler() {
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		log.info("Connected from " + e.getChannel().getRemoteAddress());

		DosimeterSimulator simulator = new DosimeterSimulator(e.getChannel());
		simulators.put(e.getChannel(), simulator);
		simulator.start();

		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		log.info("Disconnected from " + e.getChannel().getRemoteAddress());
		DosimeterSimulator simulator = simulators.get(e.getChannel());
		if (simulator != null) {
			log.info("Interrupting Thread...");
			simulators.remove(e.getChannel());
			simulator.interrupt();
		}

		super.channelDisconnected(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

		String request = (String) e.getMessage();
		log.info(request);
		String response = request;
		e.getChannel().write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		// Close the connection when an exception is raised.
		log.warn("Unexpected exception from downstream.", e.getCause());
		e.getChannel().close();
	}
}
