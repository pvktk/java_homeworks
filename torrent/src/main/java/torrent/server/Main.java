package torrent.server;

public class Main {
	public static void main(String[] args) {

		Thread t = new Thread(new MainInner(5 * 60 * 1000));
		t.start();
		
		System.out.println("Server started");
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("\nSaving server state...");
				t.interrupt();
			}
		});
	}
}
