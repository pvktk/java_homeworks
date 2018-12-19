package torrent.client;

import java.io.IOException;

public class Main {
	
	public static void main(String[] args) throws IOException {
		
		String host = args.length == 0 ? "localhost" : args[0];
		
		Thread t = new Thread(new MainInner(host, 8082, System.out, System.in, 100000, new FilesHolder("torrentData")));
		t.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Saving client state...");
				t.interrupt();
			}
		});
		
	}

}
