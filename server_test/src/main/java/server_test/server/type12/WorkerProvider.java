package server_test.server.type12;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;

import server_test.server.StatisticsHolder;

public interface WorkerProvider {
	public Runnable getWorker(
			Socket s,
			StatisticsHolder sh,
			ExecutorService commonPool,
			List<ExecutorService> transmitters);
}
