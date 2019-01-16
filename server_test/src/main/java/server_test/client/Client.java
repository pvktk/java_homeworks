package server_test.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import server_test.Messages.ClientMessage;
import server_test.server.ServersManager;
import server_test.server.StatisticsHolder;

public class Client implements Runnable {

	private final int timeDelta;
	private final int nArrays;
	private final String srvAddr;

	private final int[] arrayToSort, sortedArray;

	private volatile Socket s;

	private String errorMessage = null;
	
	private int avgOnClientTime;
	
	public final static AtomicInteger numFinished = new AtomicInteger();

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
		int i = 0;
		try {
			
			long startTime = System.currentTimeMillis();
			
			s = new Socket(srvAddr, ServersManager.sortingPort);

			if (s == null || Thread.interrupted()) {
				return;
			}
			
			
			for (i = 0; i < nArrays; i++) {
				ClientMessage request = ClientMessage.newBuilder().addAllArray(
						Arrays
						.stream(arrayToSort)
						.boxed()
						.collect(Collectors.toList()))
						.build();

				DataOutputStream dout = new DataOutputStream(s.getOutputStream());
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				request.writeTo(baos);
				int baosSize = baos.size();
				
				dout.writeInt(baosSize);
				dout.flush();
				
				baos.writeTo(s.getOutputStream());
				
				System.out.println("client wrote to socket");
				ClientMessage resp = ClientMessage.parseDelimitedFrom(s.getInputStream());
				System.out.println("client red from socket");
				if (resp == null) {
					errorMessage = "No response from server";
					System.out.println("no response on " + i + " Written " + baosSize);
					return;
				}
				int[] response = resp
						.getArrayList().stream().mapToInt(I -> I).toArray();

				if (!Arrays.equals(sortedArray, response)) {
					System.err.println("Client: recieved bad array\n"
							+ "orig size = " + sortedArray.length + ", recieved size = " + response.length);
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
			System.out.println("Client: numFinished " + numFinished.incrementAndGet());
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
