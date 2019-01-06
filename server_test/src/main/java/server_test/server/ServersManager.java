package server_test.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server_test.Messages.MeasureRequest;
import server_test.Messages.MeasureResponse;

public class ServersManager implements Runnable {
	public static final int sortingPort = 8081, controlPort = 8082;

	private final ServerSocket srv = new ServerSocket(controlPort);

	public ServersManager() throws IOException {}

	@Override
	public void run() {
		while (true) {
			try (Socket s = srv.accept()) {
				MeasureRequest request = MeasureRequest.parseFrom(s.getInputStream());

				StatisticsHolder statHolder = new StatisticsHolder(
						request.getNumberClients(),
						request.getNumberArrays());

				Runnable ts = TestServerFactory.getServer(request.getServerType(), statHolder);
				MeasureResponse response;
				if (ts == null) {
					response = MeasureResponse.newBuilder().setMeasureSuccessful(false)
							.build();
				} else {

					Thread t = new Thread(ts);
					t.start();
					t.join();

					response = MeasureResponse.newBuilder()
							.setMeasureSuccessful(true)
							.setAvgSortTime(statHolder.getAveragetSortTime())
							.setAvgProcessTime(statHolder.getAverageProcessTime())
							.setAvgOnClientTime(statHolder.getAverageOnClientTime())
							.build();
				}

				response.writeTo(s.getOutputStream());
			} catch (IOException | InterruptedException e) {
				continue;
			}
		}
	}
}
