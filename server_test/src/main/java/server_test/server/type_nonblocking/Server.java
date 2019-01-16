package server_test.server.type_nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server_test.server.ServersManager;
import server_test.server.StatisticsHolder;
import server_test.server.TestServer;

public class Server implements TestServer {

	private final StatisticsHolder statHolder;

	private final ServerSocketChannel srv;

	private final ExecutorService pool = Executors.newFixedThreadPool(numPoolThreads);

	private final Transmitter transmitter ;
	private final Reciever reciever;

	private List<SocketChannel> channels = new ArrayList<>();

	private Thread transmitThread;

	private Thread recieverThread;

	public Server(StatisticsHolder statHolder) throws IOException {
		this.statHolder = statHolder;
		transmitter = new Transmitter(statHolder);
		reciever = new Reciever(pool, statHolder, transmitter);

		srv = ServerSocketChannel.open();
		srv.bind(new InetSocketAddress(ServersManager.sortingPort));
	}

	@Override
	public void run() {
		try {
			transmitThread = new Thread(transmitter);
			recieverThread = new Thread(reciever);
			transmitThread.start();
			recieverThread.start();

			for (int i = 0; i < statHolder.expectedNumberClients; i++) {
				SocketChannel s = srv.accept();
				channels.add(s);
				reciever.addClient(s, new Attachment(maxMessageSize, i));

				statHolder.currentNumberClients.incrementAndGet();
			}

		} catch (IOException e) {
			e.printStackTrace();
			statHolder.setMeasureFailed();
		} finally {
			awaitClose();
		}
	}

	private void awaitClose() {
		try {
			recieverThread.join();
			System.out.println("Joined recieverThread");
			transmitThread.interrupt();
			transmitThread.join();
			System.out.println("Joined transmitThread");
			pool.shutdown();
		} catch (InterruptedException e) {}
		try {
			srv.close();
		} catch (IOException e1) {}
	}

	@Override
	public void closeForcibly() {
		try {
			System.out.println("closeForcibly called");
			srv.close();
		} catch (IOException e1) {}
		reciever.close();
		transmitter.close();
		pool.shutdownNow();
		channels.forEach(t -> {
			try {
				t.close();
			} catch (IOException e) {}
		});
	}
}
