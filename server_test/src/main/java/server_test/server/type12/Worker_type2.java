package server_test.server.type12;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import server_test.Messages.ClientMessage;
import server_test.server.QuadraticSorter;
import server_test.server.StatisticsHolder;

public class Worker_type2 implements Runnable {

	private final Socket clientSocket;

	private final StatisticsHolder statHolder;

	private final long connectionOpenedTime = System.currentTimeMillis();

	private final ExecutorService pool;

	private final ExecutorService transmitter = Executors.newSingleThreadExecutor();

	public Worker_type2(Socket clientSocket, StatisticsHolder statHolder,
			ExecutorService pool,
			List<ExecutorService> transmitters) {
		this.clientSocket = clientSocket;
		this.statHolder = statHolder;
		this.pool = pool;
		transmitters.add(transmitter);
	}

	public void run() {
		try {
			for (int i = 0; i < statHolder.expectedNumberArrays; i++) {

				int mesSize = (new DataInputStream(clientSocket.getInputStream())).readInt();
				long startRecieveTime = System.currentTimeMillis();
				
				boolean measureStartCorrect = statHolder.isAllClientsConnected();

				byte[] mbytes = new byte[mesSize];
				clientSocket.getInputStream().readNBytes(mbytes, 0, mesSize);


				ClientMessage clientMessage = ClientMessage.parseFrom(mbytes);


				int[] arrayToSort = clientMessage.getArrayList()
						.stream()
						.mapToInt(I -> I)
						.toArray();

				int mesNum = i;
				pool.execute(() -> {

					long startSortTime = System.currentTimeMillis();

					QuadraticSorter.sort(arrayToSort);

					long sortTime = System.currentTimeMillis() - startSortTime;

					transmitter.execute(() -> {
						try {
							ClientMessage.newBuilder()
							.addAllArray(Arrays.stream(arrayToSort)
									.boxed().collect(Collectors.toList()))
							.build().writeDelimitedTo(clientSocket.getOutputStream());

							long clientTime = System.currentTimeMillis() - startRecieveTime;

							if (measureStartCorrect && statHolder.isAllClientsConnected()) {
								statHolder.numberOfArrays.incrementAndGet();
								statHolder.sortTimesSum.addAndGet(sortTime);
								statHolder.clientTimesSum.addAndGet(clientTime);
							}
						} catch (IOException e) {
							statHolder.measureFailed = true;
						} finally {
							if (mesNum + 1 == statHolder.expectedNumberArrays) {
								long clientAverageTime = 
										(System.currentTimeMillis() - connectionOpenedTime) / 
										statHolder.expectedNumberArrays;
								statHolder.onClientAverageTimesSum.addAndGet(clientAverageTime);

								try {
									clientSocket.close();
								} catch (IOException e) {}

								statHolder.currentNumberClients.decrementAndGet();
							}
						}
					});
				});

			}

		} catch (IOException e) {
			statHolder.measureFailed = true;
			System.out.println(e.getMessage());
		} 
	}

}
