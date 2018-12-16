package torrent.client;

public class Main {
	
	public static void main(String[] args) {
		
		String host = args.length == 0 ? "localhost" : args[0];
		
		Thread t = new Thread(new MainInner(host, System.out, System.in));
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
