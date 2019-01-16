package server_test.server.type12;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import server_test.server.ServersManager;
import server_test.server.StatisticsHolder;
import server_test.server.TestServer;

public class Server implements TestServer {

	private final ServerSocket srv = new ServerSocket(ServersManager.sortingPort);

	private final List<Thread> threads = new ArrayList<>();
	private final ExecutorService pool = Executors.newFixedThreadPool(numPoolThreads);
	private final List<ExecutorService> singleThreadExecs = new ArrayList<>();
	
	private final List<Socket> sockets = new ArrayList<>();
	
	private final StatisticsHolder statHolder;
	
	private final WorkerProvider wp;
	
	public Server(StatisticsHolder statHolder, WorkerProvider wp) throws IOException {
		this.statHolder = statHolder;
		this.wp = wp;
	}

	public void run() {
		try {
			for (int i = 0; i < statHolder.expectedNumberClients; i++) {
				if (!statHolder.isMeasureSuccessful()) {
					return;
				}

				try {
					Socket s = srv.accept();
					statHolder.currentNumberClients.incrementAndGet();
					sockets.add(s);
									
					Thread t = new Thread(wp.getWorker(s, statHolder, pool, singleThreadExecs));
					t.start();
					threads.add(t);
				} catch (IOException e) {
					statHolder.setMeasureFailed();
				}
			}
		} finally {
			awaitClosing();
		}
	}

	private void awaitClosing() {
		try {
			srv.close();
		} catch (IOException e) {}
		threads.forEach(Thread::interrupt);
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				return;
			}
		}

		try {
			pool.shutdown();
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			return;
		}

		singleThreadExecs.forEach(t -> {
			try {
				t.shutdown();
				t.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {}
		});
		
		sockets.forEach(t -> {
			try {
				t.close();
			} catch (IOException e) {}
		});
	}

	@Override
	public void closeForcibly() {
		pool.shutdownNow();
		sockets.forEach(t -> {
			try {
				t.close();
			} catch (IOException e) {}
		});
	}

}
