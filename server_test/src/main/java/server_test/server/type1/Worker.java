package server_test.server.type1;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;

import server_test.Messages.ClientMessage;
import server_test.server.QuadraticSorter;
import server_test.server.StatisticsHolder;

public class Worker implements Runnable {

	private final Socket clientSocket;

	private final StatisticsHolder statHolder;

	private final long connectionOpenedTime = System.currentTimeMillis();

	public Worker(Socket clientSocket, StatisticsHolder statHolder) {
		this.clientSocket = clientSocket;
		this.statHolder = statHolder;
	}

	private void blockUntilBytesAvaliable() throws IOException {
		clientSocket.getInputStream().mark(1);
		clientSocket.getInputStream().read();
		clientSocket.getInputStream().reset();
	}

	public void run() {
		try {
			for (int i = 0; i < statHolder.expectedNumberArrays; i++) {

				blockUntilBytesAvaliable();
				long startRecieveTime = System.currentTimeMillis();

				ClientMessage clientMessage = ClientMessage.parseFrom(clientSocket.getInputStream());

				boolean measureStartCorrect = statHolder.isAllClientsConnected();

				int[] arrayToSort = clientMessage.getArrayList()
						.stream()
						.mapToInt(I -> I)
						.toArray();

				long startSortTime = System.currentTimeMillis();

				QuadraticSorter.sort(arrayToSort);

				long sortTime = System.currentTimeMillis() - startSortTime;

				ClientMessage.newBuilder()
				.addAllArray(Arrays.stream(arrayToSort)
						.boxed().collect(Collectors.toList()))
				.build().writeTo(clientSocket.getOutputStream());

				long clientTime = System.currentTimeMillis() - startRecieveTime;

				if (measureStartCorrect && statHolder.isAllClientsConnected()) {
					statHolder.numberOfArrays.incrementAndGet();
					statHolder.sortTimesSum.addAndGet(sortTime);
					statHolder.clientTimesSum.addAndGet(clientTime);
				}
			}
			
			long clientAverageTime = 
					(System.currentTimeMillis() - connectionOpenedTime) / 
					statHolder.expectedNumberArrays;
			statHolder.onClientAverageTimesSum.addAndGet(clientAverageTime);
		} catch (IOException e) {
			statHolder.measureFailed = true;
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {}

			statHolder.currentNumberClients.decrementAndGet();
		}
	}

}
