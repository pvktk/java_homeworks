package server_test.server;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		Thread t = null;
		try {
			t = new Thread(new ServersManager());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}
		t.start();
		t.join();
	}

}
