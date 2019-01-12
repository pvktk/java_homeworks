package server_test.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server_test.Messages.MeasureRequest;
import server_test.Messages.MeasureResponse;

public class ServersManager implements Runnable {
	public static final int sortingPort = 36782, controlPort = 36783;

	private final ServerSocket srv = new ServerSocket(controlPort);

	private volatile TestServer ts;

	private void shutDownOnDisconnect(Socket s) {
		Thread t = new Thread( () -> {
			try {
				s.getInputStream().read();
			} catch (IOException e) {
				ts.closeForcibly();
				//System.out.println("Attemp to shutdown test server by disconnect");
			}});
		t.start();
	}

	public ServersManager() throws IOException {}

	@Override
	public void run() {
		while (true) {
			try (Socket s = srv.accept()) {
				MeasureRequest request = MeasureRequest.parseDelimitedFrom(s.getInputStream());

				StatisticsHolder statHolder = new StatisticsHolder(
						request.getNumberClients(),
						request.getNumberArrays());

				ts = TestServerFactory.getServer(request.getServerType(), statHolder);
				s.getOutputStream().write(1);

				MeasureResponse response;
				if (ts == null) {
					response = MeasureResponse.newBuilder().setMeasureSuccessful(false)
							.build();
				} else {

					shutDownOnDisconnect(s);

					Thread t = new Thread(ts);
					t.start();
					t.join();
					//System.out.println("TSThread joined");
					if (!statHolder.isMeasureSuccessful() || statHolder.numberOfArrays.compareAndSet(0, 0)) {
						response = MeasureResponse.newBuilder()
								.setMeasureSuccessful(false).build();
					} else {
						response = MeasureResponse.newBuilder()
								.setMeasureSuccessful(true)
								.setAvgSortTime(statHolder.getAveragetSortTime())
								.setAvgProcessTime(statHolder.getAverageProcessTime())
								.build();
					}
				}

				response.writeDelimitedTo(s.getOutputStream());
			} catch (IOException | InterruptedException e) {
				continue;
			}
		}
	}
}
