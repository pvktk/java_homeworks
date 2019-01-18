package server_test.server.type12;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import server_test.Messages.ClientMessage;
import server_test.server.QuadraticSorter;
import server_test.server.StatisticsHolder;

public class Worker_type1 implements Runnable {

	private final Socket clientSocket;

	private final StatisticsHolder statHolder;

	public Worker_type1(Socket clientSocket, StatisticsHolder statHolder,
			ExecutorService pool, List<ExecutorService> transmitters) {
		this.clientSocket = clientSocket;
		this.statHolder = statHolder;
	}

	public void run() {
		try {
			while (true) {
				
				int mesSize = (new DataInputStream(clientSocket.getInputStream())).readInt();
				long startRecieveTime = System.nanoTime();
				
				boolean measureStartCorrect = statHolder.isAllClientsConnected();

				byte[] mbytes = new byte[mesSize];
				clientSocket.getInputStream().readNBytes(mbytes, 0, mesSize);
				
				ClientMessage clientMessage = ClientMessage.parseFrom(mbytes);
				
				int[] arrayToSort = clientMessage.getArrayList()
						.stream()
						.mapToInt(I -> I)
						.toArray();

				long startSortTime = System.nanoTime();

				QuadraticSorter.sort(arrayToSort);

				long sortTime = System.nanoTime() - startSortTime;

				ClientMessage.newBuilder()
				.addAllArray(Arrays.stream(arrayToSort)
						.boxed().collect(Collectors.toList()))
				.build().writeDelimitedTo(clientSocket.getOutputStream());

				long clientTime = System.nanoTime() - startRecieveTime;

				if (measureStartCorrect && statHolder.isAllClientsConnected()) {
					statHolder.numberOfArrays.incrementAndGet();
					statHolder.sortTimesSum.addAndGet(sortTime);
					statHolder.clientTimesSum.addAndGet(clientTime);
				}
			}
			
		} catch (IOException e) {
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {}
			
			statHolder.currentNumberClients.decrementAndGet();
		}
	}

}
