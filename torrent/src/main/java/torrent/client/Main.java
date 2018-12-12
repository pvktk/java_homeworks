package torrent.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import torrent.common.ConcreteTaskHandler;
import torrent.common.RequestCompletionHandler;
import torrent.common.ServerRequestHandler;
import torrent.common.StorageManager;
import torrent.server.ListHandler;
import torrent.server.OldClientsCleaner;
import torrent.server.ServerData;
import torrent.server.SourcesHandler;
import torrent.server.UpdateHandler;
import torrent.server.UploadHandler;

public class Main {
	
	final Socket toServer = new Socket("localhost", 8081);
	
	final static int myPort = 8082;
	final static String helpMessage = 
			"This is torrent client";
	
	public Main() throws IOException {
		
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException, InterruptedException, ExecutionException {
		
		StorageManager<FilesHolder> storageManager = new StorageManager<>(FilesHolder.class, "downloads/state");

		Options options = new Options();
		options.addOption("list", "list files, known to server");
		options.addOption("upload", true, "upload file to server");
		
		AsynchronousServerSocketChannel srvChannel = AsynchronousServerSocketChannel.open();
		srvChannel.bind(new InetSocketAddress(myPort));
		
		ConcreteTaskHandler[] concreteHandlers = new ConcreteTaskHandler[] {};
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Saving client state...");
				try {
					storageManager.lock.writeLock().lock();
					storageManager.save();
					storageManager.lock.writeLock().unlock();
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
