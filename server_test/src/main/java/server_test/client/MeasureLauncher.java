package server_test.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.swing.JProgressBar;
import server_test.Messages.MeasureRequest;
import server_test.Messages.MeasureResponse;
import server_test.Messages.ServerType;
import server_test.server.ServersManager;

public class MeasureLauncher {

	public enum ChangingVariables {ArraySize, NumberClients, TimeDelta};

	private ChangingVariables currentSelected = null;

	private Map<Integer, MeasureResponse> results;

	private JProgressBar progressBar;

	private int timeDeltaMillis, arraySize, numberClients, numberArrays;
	private String serverAddress;
	private ServerType serverType;

	private int vMin, dv, vMax;

	private final Path resultsPath = Paths.get("results");

	private volatile Socket srv;

	public void setChangingVariable(ChangingVariables var) {
		currentSelected = var;
	}

	public ChangingVariables getChangingVariable() {
		return currentSelected;
	}

	public void setRange(int vMin, int dv, int vMax) {
		this.vMin = vMin;
		this.dv = dv;
		this.vMax = vMax;
	}

	public void closeSocket() {
		if (srv != null) {
			try {
				srv.close();
			} catch (IOException e) {}
		}
	}

	private MeasureResponse makeSingleMeasure() throws IOException, InterruptedException {
		List<Client> clients = new ArrayList<>();
		List<Thread> clientThread = new ArrayList<>();

		try {
			srv = new Socket(serverAddress, ServersManager.controlPort);

			if (srv == null || Thread.interrupted()) {
				throw new InterruptedException();
			}

			MeasureRequest.newBuilder()
			.setNumberArrays(numberArrays)
			.setNumberClients(numberClients)
			.setServerType(serverType)
			.build().writeDelimitedTo(srv.getOutputStream());
			
			srv.getInputStream().read();
			
			for (int i = 0; i < numberClients; i++) {
				Client cl = new Client(timeDeltaMillis, numberArrays, arraySize, serverAddress);
				clients.add(cl);
				clientThread.add(new Thread(cl));
			}

			for (Thread t : clientThread) {
				t.start();
			}

			for (Thread t : clientThread) {
				t.join();
			}
			
			int avgOnLientTime = 0;
			for (Client c : clients) {
				avgOnLientTime += c.getAvgTime();
			}
			avgOnLientTime /= clients.size();
			
			MeasureResponse serverRes = MeasureResponse.parseDelimitedFrom(srv.getInputStream());
			
			if (serverRes == null) {
				throw new IOException("Connection failed");
			}
			
			MeasureResponse res = MeasureResponse.newBuilder().mergeFrom(serverRes).setAvgOnClientTime(avgOnLientTime).build();
			
			return res;
		} finally {
			closeSocket();

			for (Thread t : clientThread) {
				t.interrupt();
			}
			
			for (Client cl : clients) {
				cl.closeSocket();
			}
			
			for (Client cl : clients) {
				if (cl.getError() != null) {
					throw new IllegalStateException("Some clients detected a problem: " + cl.getError());
				}
			}
		}
	}

	private Map<Integer, MeasureResponse> measureChange(
			Consumer<Integer> consumer) throws IOException, InterruptedException {
		Map<Integer, MeasureResponse> res = new TreeMap<>();

		if (vMin < 0 || dv <= 0 || vMax <= 0) {
			throw new IllegalArgumentException();
		}
		progressBar.setMinimum(vMin);
		progressBar.setMaximum(vMax);

		for (int v = vMin; v <= vMax; v += dv) {

			consumer.accept(v);

			MeasureResponse r;

			r = makeSingleMeasure();

			if (!r.getMeasureSuccessful()) {
				throw new IllegalStateException("Measure failed");
			}

			res.put(v, r);

			progressBar.setValue(v);
		}
		return res;
	}

	private synchronized Map<Integer, MeasureResponse> measureChange() throws IOException, InterruptedException
	{
		switch (currentSelected) {
		case ArraySize:
			return measureChange(i -> arraySize = i);
		case NumberClients:
			return measureChange(i -> numberClients = i);
		case TimeDelta:
			return measureChange(i -> timeDeltaMillis = i);
		default:
			return null;
		}
	}

	public void makeMeasure(JProgressBar bar) throws IOException, InterruptedException {
		this.progressBar = bar;
		results = measureChange();
	}

	public void setTimeDeltaMillis(int timeDeltaMillis) {
		if (timeDeltaMillis < 0)
			throw new IllegalArgumentException();
		this.timeDeltaMillis = timeDeltaMillis;
	}

	public void setArraySize(int arraySize) {
		if (arraySize <= 0)
			throw new IllegalArgumentException();
		this.arraySize = arraySize;
	}

	public void setNumberClients(int numberClients) {
		if (numberClients <= 0)
			throw new IllegalArgumentException();
		this.numberClients = numberClients;
	}

	public void setNumberArrays(int numberArrays) {
		if (numberArrays <= 0)
			throw new IllegalArgumentException();
		this.numberArrays = numberArrays;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}

	public Map<Integer, MeasureResponse> getResults() {
		return results;
	}

	private String data;
	private PrintWriter getFile(String desc) throws FileNotFoundException {
		return new PrintWriter(resultsPath
				.resolve(serverType.toString())
				.resolve(desc + "_" + data + ".txt").toFile());
	}

	public void saveResultsToFile() throws IOException {
		data = (new Date()).toString();
		Files.createDirectories(Paths.get("results").resolve(serverType.toString()));

		try (
				PrintWriter onClientTime = new PrintWriter(getFile("avgOnClientTime"));
				PrintWriter processTime = new PrintWriter(getFile("processTime"));
				PrintWriter sortTime = new PrintWriter(getFile("sortingTime"));
				) {
			results.forEach((i, r) -> {
				onClientTime.println(i + " " + r.getAvgOnClientTime());
				processTime.println(i + " " + r.getAvgProcessTime());
				sortTime.println(i + " " + r.getAvgSortTime());
			});
		}

		try (PrintWriter metadata = new PrintWriter(getFile("metadata"))) {

			metadata.format("server type: %s\n"
					+ "changing variable: + %s\n"
					+ "range: min = %d, step = %d, max = %d\n"
					+ "timeDeltaMillis = %d\n"
					+ "array size = %d\n"
					+ "number clients = %d\n"
					+ "number arrays = %d\n",
					serverType,
					currentSelected, vMin, dv, vMax,
					timeDeltaMillis, arraySize, numberClients, numberArrays);

		}
	}

}
