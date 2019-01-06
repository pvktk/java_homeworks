package server_test.server;

import java.io.IOException;

import server_test.Messages.ServerType;
import server_test.server.type1.Server;

public class TestServerFactory {
	public static Runnable getServer(ServerType type, StatisticsHolder statHolder) throws IOException {
		switch (type) {
		case simpleBlocking:
			return new Server(statHolder);

		default:
			return null;
		}
	}
}
