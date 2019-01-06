package server_test.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import server_test.Messages.ClientMessage;
import server_test.Messages.MeasureParams;
import server_test.server.ServersManager;

public class Client implements Runnable {
	
	private final int timeDelta;
	private final int nArrays, arraySize;
	private final String srvAddr;
	
	private final int[] arrayToSort, sortedArray;
	
	public Client(MeasureParams params) {
		timeDelta = params.getTimeDeltaMillis();
		nArrays = params.getNumberArrays();
		arraySize = params.getArraySize();
		srvAddr = params.getServerAddress();
		
		Random rnd = new Random(3);
		arrayToSort = rnd.ints().limit(arraySize).toArray();
		sortedArray = arrayToSort.clone();
		Arrays.sort(sortedArray);
	}
	
	@Override
	public void run() {
		try (Socket s = new Socket(srvAddr, ServersManager.sortingPort)) {
			for (int i = 0; i < nArrays; i++) {
				ClientMessage.newBuilder().addAllArray(
						Arrays
						.stream(arrayToSort)
						.boxed()
						.collect(Collectors.toList()))
				.build().writeTo(s.getOutputStream());
				
				int[] response = ClientMessage.parseFrom(s.getInputStream())
						.getArrayList().stream().mapToInt(I -> I).toArray();
				
				if (!Arrays.equals(sortedArray, response)) {
					System.err.println("Client: recieved bad array");
					return;
				}
				Thread.sleep(timeDelta);
			}
		} catch (IOException | InterruptedException e) {
			return;
		}
	}
	
}
