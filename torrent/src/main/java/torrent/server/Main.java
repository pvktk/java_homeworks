package torrent.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import torrent.common.ConcreteTaskHandler;
import torrent.common.RequestCompletionHandler;
import torrent.common.ServerRequestHandler;

public class Main {
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		StorageManager storageManager = new StorageManager("serverFile");
		
		AsynchronousServerSocketChannel srvChannel = AsynchronousServerSocketChannel.open();
		srvChannel.bind(new InetSocketAddress(8081));
		
		ConcreteTaskHandler[] concreteHandlers = new ConcreteTaskHandler[] {
						new ListHandler(storageManager),
						new UploadHandler(storageManager),
						new SourcesHandler(storageManager),
						new UpdateHandler(storageManager)};
		
		Thread cleanThread = new Thread(new OldClientsCleaner(storageManager));
		cleanThread.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cleanThread.interrupt();
				System.out.println("Saving server state...");
				try {
					storageManager.save();
				} catch (JsonGenerationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		while (true) {
			AsynchronousSocketChannel clientChannel = srvChannel.accept().get();
			
			ServerRequestHandler handler = new ServerRequestHandler(concreteHandlers);
			
			clientChannel.read(handler.getReceivingBuffer(), handler,
					new RequestCompletionHandler(clientChannel));
		}
	}
}
