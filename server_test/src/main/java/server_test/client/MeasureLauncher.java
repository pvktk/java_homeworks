package server_test.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

	public final int maxArraySize = 30000;
	public enum ChangingVariables {ArraySize, NumberClients, TimeDelta};

	private ChangingVariables currentSelectedChangingVariable = null;

	private Map<Integer, MeasureResponse> results;

	private JProgressBar progressBar;

	private int timeDeltaMillis, arraySize, numberClients, numberArrays;
	private String serverAddress;
	private ServerType serverType;

	private int vMin, dv, vMax;

	private final Path resultsPath = Paths.get("results");

	private volatile Socket srv;

	public void setChangingVariable(ChangingVariables var) {
		currentSelectedChangingVariable = var;
	}

	public ChangingVariables getChangingVariable() {
		return currentSelectedChangingVariable;
	}

	public void setRange(int vMin, int dv, int vMax) {
		this.vMin = vMin;
		this.dv = dv;
		this.vMax = vMax;
		if (vMin < 0 || dv <= 0 || vMax < vMin) {
			throw new IllegalArgumentException("Range incorrect");
		}
		if (currentSelectedChangingVariable != ChangingVariables.TimeDelta && vMin == 0) {
			throw new IllegalArgumentException("Range incorrect");
		}
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

			if (currentSelectedChangingVariable == ChangingVariables.ArraySize && v > maxArraySize) {
				throw new IllegalStateException("Array too large");
			}

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
		switch (currentSelectedChangingVariable) {
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
			throw new IllegalArgumentException("time delta < 0");
		this.timeDeltaMillis = timeDeltaMillis;
	}

	public void setArraySize(int arraySize) {
		if (arraySize <= 0)
			throw new IllegalArgumentException("too small array size");
		if (arraySize > maxArraySize)
			throw new IllegalArgumentException("too big array size");
		this.arraySize = arraySize;
	}

	public void setNumberClients(int numberClients) {
		if (numberClients <= 0)
			throw new IllegalArgumentException("number clients <= 0");
		this.numberClients = numberClients;
	}

	public void setNumberArrays(int numberArrays) {
		if (numberArrays <= 0)
			throw new IllegalArgumentException("number of requests <= 0");
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

	private PrintWriter getFile(String desc) throws FileNotFoundException {
		return new PrintWriter(resultsPath
				.resolve(serverType.getNumber() +
						"" + currentSelectedChangingVariable.ordinal()
						+ desc + ".txt").toFile());
	}

	public void saveResultsToFile() throws IOException {
		Files.createDirectories(Paths.get("results"));

		try (
				PrintWriter onClientTime = new PrintWriter(getFile("0"));
				PrintWriter processTime = new PrintWriter(getFile("1"));
				PrintWriter sortTime = new PrintWriter(getFile("2"));
				) {
			results.forEach((i, r) -> {
				onClientTime.println(i + " " + r.getAvgOnClientTime());
				processTime.println(i + " " + r.getAvgProcessTime());
				sortTime.println(i + " " + r.getAvgSortTime());
			});
		}

		try (PrintWriter metadata = new PrintWriter(getFile("_metadata"))) {

			metadata.format("server type: %s\n"
					+ "changing variable: + %s\n"
					+ "range: min = %d, step = %d, max = %d\n"
					+ "timeDeltaMillis = %d\n"
					+ "array size = %d\n"
					+ "number clients = %d\n"
					+ "number arrays = %d\n",
					serverType,
					currentSelectedChangingVariable, vMin, dv, vMax,
					timeDeltaMillis, arraySize, numberClients, numberArrays);

		}
	}

}
