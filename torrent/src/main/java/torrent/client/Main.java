package torrent.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
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

	final SocketAddress toServer = new InetSocketAddress("localhost", 8081);

	final static String helpMessage = 
			"This is torrent client";

	public Main() throws IOException {

	}

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException, InterruptedException, ExecutionException {

		FilesHolder storageManager = new FilesHolder();


		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Saving client state...");
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

		Thread srvThread = new Thread(new ServerProcess(new ConcreteTaskHandler[] {

		}));
		srvThread.setDaemon(true);
		srvThread.start();

		REPL.startRepl();
	}

}
