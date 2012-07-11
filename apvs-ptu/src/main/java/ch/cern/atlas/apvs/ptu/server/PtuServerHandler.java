package ch.cern.atlas.apvs.ptu.server;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;


public class PtuServerHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(
            PtuServerHandler.class.getName());

	private final boolean json;
	private Map<Channel, PtuSimulator> simulators = new HashMap<Channel, PtuSimulator>();

    public PtuServerHandler(boolean json) {
		this.json = json;
	}
    
    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    	System.err.println("Connected from "+e.getChannel().getRemoteAddress());
    	
    	PtuSimulator simulator = new PtuSimulator(e.getChannel(), json);
    	simulators.put(e.getChannel(), simulator);
    	simulator.start();
    	
    	super.channelConnected(ctx, e);
    }
    
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
    		ChannelStateEvent e) throws Exception {
    	System.err.println("Disconnected from "+e.getChannel().getRemoteAddress());
    	PtuSimulator simulator = simulators.get(e.getChannel());
    	if (simulator != null) {
    		System.err.println("Interrupting Thread...");
    		simulator.interrupt();
    		simulators.remove(e.getChannel());
    	}
    	
    	super.channelDisconnected(ctx, e);
    }
    
    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {

    	String request = (String) e.getMessage();
    	System.err.println(request);
    	String response = request;
    	e.getChannel().write(response);
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        // Close the connection when an exception is raised.
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }
}