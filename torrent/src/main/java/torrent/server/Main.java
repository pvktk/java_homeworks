package torrent.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

import torrent.common.RequestCompletionHandler;
import torrent.common.RequestHandler;

public class Main {
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		AsynchronousServerSocketChannel srvChannel = AsynchronousServerSocketChannel.open();
		srvChannel.bind(new InetSocketAddress(8081));
		RequestHandler handler = new ServerRequestHandler();
		
		
		while (true) {
			AsynchronousSocketChannel clientChannel = srvChannel.accept().get();

			clientChannel.read(handler.getReceivingBuffer(), handler,
					new RequestCompletionHandler(clientChannel));
		}
	}
}
