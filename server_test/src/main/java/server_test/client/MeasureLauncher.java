package server_test.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import server_test.Messages.MeasureParams;
import server_test.Messages.MeasureParams.Builder;
import server_test.Messages.MeasureRequest;
import server_test.Messages.MeasureResponse;
import server_test.server.ServersManager;

public class MeasureLauncher {

	public enum ChangingVariables {ArraySize, NumberClients, TimeDelta};

	private ChangingVariables currentSelected = null;

	private Map<Integer, MeasureResponse> results;

	private MeasureParams initialParams;
	private int vMin, dv, vMax;

	private final Path resultsPath = Paths.get("results");

	public void setChangingVariable(ChangingVariables var) {
		currentSelected = var;
	}

	public void setMeasureParams(MeasureParams params) {
		initialParams = params;
	}

	public void setRange(int vMin, int dv, int vMax) {
		this.vMin = vMin;
		this.dv = dv;
		this.vMax = vMax;
	}

	private static MeasureResponse makeMeasure(MeasureParams params) throws IOException, InterruptedException {
		Thread[] clients = new Thread[params.getNumberClients()];

		try (Socket srv = new Socket(params.getServerAddress(), ServersManager.controlPort)) {

			MeasureRequest.newBuilder()
			.setNumberArrays(params.getNumberArrays())
			.setNumberClients(params.getNumberClients())
			.setServerType(params.getServerType())
			.build().writeTo(srv.getOutputStream());

			for (int i = 0; i < clients.length; i++) {
				clients[i] = new Thread(new Client(params));
			}

			for (Thread t : clients) {
				t.start();
			}

			for (Thread t : clients) {
				t.join();
			}

			return MeasureResponse.parseFrom(srv.getInputStream());

		}
	}

	private static Map<Integer, MeasureResponse> measureChange(MeasureParams params,
			BiFunction<Builder, Integer, Builder> bf,
			int vMin, int dv, int vMax) throws IOException, InterruptedException {
		Map<Integer, MeasureResponse> res = new HashMap<>();

		if (vMin < 0 || dv <= 0 || vMax <= 0) {
			throw new IllegalArgumentException();
		}

		for (int v = vMin; v <= vMax; v += dv) {
			MeasureParams p = bf.apply(MeasureParams.newBuilder().mergeFrom(params), v).build();
			MeasureResponse r;

			r = makeMeasure(p);
			
			if (!r.getMeasureSuccessful()) {
				throw new IllegalStateException("Measure failed");
			}
			
			res.put(v, r);
		}
		return res;
	}

	private static Map<Integer, MeasureResponse> measureChange(
			MeasureParams params,
			ChangingVariables varType,
			int vMin, int dv, int vMax) throws IOException, InterruptedException
	{
		switch (varType) {
		case ArraySize:
			return measureChange(params, Builder::setArraySize, vMin, dv, vMax);
		case NumberClients:
			return measureChange(params, Builder::setNumberClients, vMin, dv, vMax);
		case TimeDelta:
			return measureChange(params, Builder::setTimeDeltaMillis, vMin, dv, vMax);
		default:
			return null;
		}
	}

	public void makeMeasure() throws IOException, InterruptedException {
		results = measureChange(initialParams, currentSelected, vMin, dv, vMax);
	}

	private String data;
	private PrintWriter getFile(String desc) throws FileNotFoundException {
		return new PrintWriter(resultsPath.resolve(desc + "_" + data + ".txt").toFile());
	}

	public void saveResultsToFile() throws IOException {
		data = (new Date()).toString();
		Files.createDirectories(Paths.get("results"));

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
	}

}
