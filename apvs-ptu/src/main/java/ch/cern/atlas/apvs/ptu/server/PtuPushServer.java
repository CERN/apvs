package ch.cern.atlas.apvs.ptu.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;

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
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.channel(NioSocketChannel.class);

		if (CONNECT_FOR_EVERY_MESSAGE) {
			PtuConnectPushHandler handler = new PtuConnectPushHandler();
			
			handler.run(host, port, refresh);
		} else {
			PtuPushHandler handler = new PtuPushHandler(bootstrap, ids, refresh);

			bootstrap.handler(new PtuChannelInitializer(new PtuServerHandler(refresh, ids)));

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
