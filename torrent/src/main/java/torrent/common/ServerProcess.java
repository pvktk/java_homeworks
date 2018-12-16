package torrent.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class ServerProcess implements Runnable {
	
	final int myPort;
	
	private final AsynchronousServerSocketChannel srvChannel = AsynchronousServerSocketChannel.open();
	
	private final ConcreteTaskHandler[] concreteHandlers;
	
	public ServerProcess(int port, ConcreteTaskHandler[] concreteHandlers) throws IOException {
		myPort = port;
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
