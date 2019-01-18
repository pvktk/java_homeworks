package server_test.server;

public interface TestServer extends Runnable{
	void closeForcibly();
	int numPoolThreads = 8;
	int maxMessageSize = 500000;
}
