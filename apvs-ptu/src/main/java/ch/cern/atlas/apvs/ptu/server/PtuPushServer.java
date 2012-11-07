package ch.cern.atlas.apvs.ptu.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

public class PtuPushServer {

	private final String host;
	private final int port;
	private final int refresh;
	private final String[] ids;

	private boolean CONNECT_FOR_EVERY_MESSAGE = false;

	public PtuPushServer(String host, int port, int refresh, String[] ids) {
		this.host = host;
		this.port = port;
		this.refresh = refresh;
		this.ids = ids;
	}

	public void run() throws IOException {
		// Configure the client.
		ClientBootstrap bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		if (CONNECT_FOR_EVERY_MESSAGE) {
			PtuConnectPushHandler handler = new PtuConnectPushHandler();
			
			handler.run(host, port, refresh);
		} else {
			PtuPushHandler handler = new PtuPushHandler(bootstrap, ids, refresh);

			Timer timer = new HashedWheelTimer();

			// Configure the pipeline factory.
			bootstrap.setPipelineFactory(new PtuPipelineFactory(timer, handler));

			// Start the connection attempt.
			handler.connect(new InetSocketAddress(host, port));
		}
	}

	public static void main(String[] args) throws Exception {
		// Print usage if no argument is specified.
		if (args.length != 2) {
			System.err.println("Usage: " + PtuPushServer.class.getSimpleName()
					+ " <host> <port> <refresh>");
			return;
		}

		// Parse options.
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		int refresh = Integer.parseInt(args[2]);

		new PtuPushServer(host, port, refresh, null).run();
	}
}
