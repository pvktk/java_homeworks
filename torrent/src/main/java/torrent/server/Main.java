package torrent.server;

public class Main {
	public static void main(String[] args) {

		Thread t = new Thread(new MainInner(5 * 60 * 1000));
		t.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Saving server state...");
				t.interrupt();
			}
		});
	}
}
