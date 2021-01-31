package ch.cern.atlas.apvs.ptu.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PtuServerHandler extends ChannelInboundHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private final Map<Channel, List<PtuSimulator>> simulators = new HashMap<Channel, List<PtuSimulator>>();
	private String[] ptuIds = { "PTU_78347", "PTU_82098", "PTU_37309",
			"PTU_27372", "PTU_39400", "PTU_88982" };

	private final int refresh;

	public PtuServerHandler(int refresh, String[] ids) {
		this.refresh = refresh;

		if (ids != null) {
			ptuIds = ids;
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("Connected from " + ctx.channel().remoteAddress());

		List<PtuSimulator> listOfSimulators = new ArrayList<PtuSimulator>(
				ptuIds.length);
		for (int i = 0; i < ptuIds.length; i++) {
			String ptuId = ptuIds[i];

			PtuSimulator simulator = new PtuSimulator(ptuId, refresh,
					ctx.channel());
			listOfSimulators.add(simulator);
			simulator.start();
		}

		simulators.put(ctx.channel(), listOfSimulators);

		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("Disconnected from " + ctx.channel().remoteAddress());
		List<PtuSimulator> listOfSimulators = simulators.get(ctx.channel());
		if (listOfSimulators != null) {
			log.info("Interrupting Threads...");
			for (Iterator<PtuSimulator> i = listOfSimulators.iterator(); i
					.hasNext();) {
				i.next().interrupt();
			}
			simulators.remove(ctx.channel());
		}

		super.channelInactive(ctx);
	}
  
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		log.info(msg.toString());

		// FIXME #634
		ctx.channel().write(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Close the connection when an exception is raised.
		log.warn("Unexpected exception from downstream.", cause);
		ctx.channel().close();
	}
}
