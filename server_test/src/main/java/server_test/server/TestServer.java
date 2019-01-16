package server_test.server;

public interface TestServer extends Runnable{
	void closeForcibly();
	int numPoolThreads = 4;
	int maxMessageSize = 1000000;
}
