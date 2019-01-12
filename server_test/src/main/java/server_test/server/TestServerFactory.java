package server_test.server;

import java.io.IOException;

import server_test.Messages.ServerType;
import server_test.server.type12.Worker_type2;
import server_test.server.type12.Server;
import server_test.server.type12.Worker_type1;

public class TestServerFactory {
	public static TestServer getServer(ServerType type, StatisticsHolder statHolder) throws IOException {
		switch (type) {
		case simpleBlocking:
			return new Server(statHolder, Worker_type1::new);
		case middleBlocking:
			return new Server(statHolder, Worker_type2::new);
		case nonBlocking:
			return new server_test.server.type_nonblocking.Server(statHolder);
		default:
			return null;
		}
	}
}
