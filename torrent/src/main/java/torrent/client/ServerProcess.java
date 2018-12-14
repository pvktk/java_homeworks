package torrent.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

import torrent.common.ConcreteTaskHandler;
import torrent.common.RequestCompletionHandler;
import torrent.common.ServerRequestHandler;

public class ServerProcess implements Runnable {
	
	final static int myPort = 8082;
	
	private final AsynchronousServerSocketChannel srvChannel = AsynchronousServerSocketChannel.open();
	
	private final ConcreteTaskHandler[] concreteHandlers;
	
	ServerProcess(ConcreteTaskHandler[] concreteHandlers) throws IOException {
		this.concreteHandlers = concreteHandlers;
		srvChannel.bind(new InetSocketAddress(myPort));
	}
	
	@Override
	public void run() {
		while (true) {
			AsynchronousSocketChannel clientChannel = null;
			try {
				clientChannel = srvChannel.accept().get();
			} catch (InterruptedException e) {
				return;
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ServerRequestHandler handler = new ServerRequestHandler(concreteHandlers);
			
			clientChannel.read(handler.getReceivingBuffer(), handler,
					new RequestCompletionHandler(clientChannel));
		}

	}

}
