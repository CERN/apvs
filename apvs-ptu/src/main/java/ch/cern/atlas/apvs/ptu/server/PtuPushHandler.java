package ch.cern.atlas.apvs.ptu.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.cern.atlas.apvs.domain.Packet;

public class PtuPushHandler extends PtuReconnectHandler {

	private final Logger log = LoggerFactory.getLogger(getClass().getName());

	private final Map<Channel, List<PtuSimulator>> simulators = new HashMap<Channel, List<PtuSimulator>>();
	private String[] ptuIds = { "PTU_78347", "PTU_82098", "PTU_37309",
			"PTU_27372", "PTU_39400", "PTU_88982" };
	private final int refresh;

	public PtuPushHandler(Bootstrap bootstrap, String[] ids, int refresh) {
		super(bootstrap, "PTU_PUSH");

		if (ids != null) {
			ptuIds = ids;
		}

		this.refresh = refresh;
	}
	    
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);

		log.info("Connected");

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
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		log.info("Disconnected");

		List<PtuSimulator> listOfSimulators = simulators.get(ctx.channel());
		if (listOfSimulators != null) {
			log.info("Interrupting Threads...");
			for (Iterator<PtuSimulator> i = listOfSimulators.iterator(); i
					.hasNext();) {
				i.next().interrupt();
			}
			simulators.remove(ctx.channel());
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet msg)
			throws Exception {
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// ignored
	}
}
