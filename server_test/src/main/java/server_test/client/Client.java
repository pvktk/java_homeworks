package server_test.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import server_test.Messages.ClientMessage;
import server_test.server.ServersManager;

public class Client implements Runnable {

	private final int timeDelta;
	private final int nArrays;
	private final String srvAddr;

	private final int[] arrayToSort, sortedArray;

	private volatile Socket s;

	private String errorMessage = null;
	
	private int avgOnClientTime;
	
	public Client(int timeDelta, int nArrays, int arraySize, String srvAddr) {
		this.timeDelta = timeDelta;
		this.nArrays = nArrays;
		this.srvAddr = srvAddr;

		Random rnd = new Random(3);
		arrayToSort = rnd.ints().limit(arraySize).toArray();
		sortedArray = arrayToSort.clone();
		Arrays.sort(sortedArray);
	}

	@Override
	public void run() {
		try {
			
			long startTime = System.currentTimeMillis();
			
			s = new Socket(srvAddr, ServersManager.sortingPort);

			if (s == null || Thread.interrupted()) {
				return;
			}

			for (int i = 0; i < nArrays; i++) {
				ClientMessage request = ClientMessage.newBuilder().addAllArray(
						Arrays
						.stream(arrayToSort)
						.boxed()
						.collect(Collectors.toList()))
						.build();

				DataOutputStream dout = new DataOutputStream(s.getOutputStream());
				dout.writeInt(request.getSerializedSize());
				dout.flush();
				request.writeTo(s.getOutputStream());

				ClientMessage resp = ClientMessage.parseDelimitedFrom(s.getInputStream());
				if (resp == null) {
					errorMessage = "No response from server";
					return;
				}
				int[] response = resp
						.getArrayList().stream().mapToInt(I -> I).toArray();

				if (!Arrays.equals(sortedArray, response)) {
					System.err.println("Client: recieved bad array");
					errorMessage = "Recieved array is bad";
					return;
				}
				Thread.sleep(timeDelta);
			}
			
			avgOnClientTime = Math.toIntExact((System.currentTimeMillis() - startTime) / nArrays);
			
		} catch (IOException e) {
			errorMessage = e.getMessage();
		} catch (InterruptedException e){
			errorMessage = "InteeruptedException caught";
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (IOException e) {
					errorMessage = e.getMessage();
				}
		}
	}

	public void closeSocket() {
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {}
		}
	}

	public String getError() {
		return errorMessage;
	}
	
	public int getAvgTime() {
		return avgOnClientTime;
	}

}
