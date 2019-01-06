package server_test.server.type1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import server_test.server.ServersManager;
import server_test.server.StatisticsHolder;

public class Server implements Runnable {

	private ServerSocket srv = new ServerSocket(ServersManager.sortingPort);

	private List<Thread> threads = new ArrayList<>();

	private final StatisticsHolder statHolder;

	public Server(StatisticsHolder statHolder) throws IOException {
		this.statHolder = statHolder;
	}

	public void run() {
		for (int i = 0; i < statHolder.expectedNumberClients; i++) {
			if (statHolder.measureFailed) {
				return;
			}

			try {
				Socket s = srv.accept();
				Thread t = new Thread(new Worker(s, statHolder));
				t.start();
				threads.add(t);
			} catch (IOException e) {
				statHolder.measureFailed = true;
			}
		}
		close();
	}

	public void close() {
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
	}

}
