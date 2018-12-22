package torrent.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import torrent.common.ConcreteTaskHandler;
import torrent.common.ServerProcess;

public class MainInner implements Runnable {

	private final String host;
	private final PrintStream out;
	private final InputStream sinp;
	private final long updateTime;
	private final FilesHolder filesHolder;
	private final int myport;

	public MainInner(String host, int port, PrintStream out, InputStream sinp, long updateTime, FilesHolder fh) {
		this.host = host;
		this.out = out;
		this.sinp = sinp;
		this.updateTime = updateTime;
		this.filesHolder = fh;
		this.myport = port;
	}

	@Override
	public void run() {
		Thread server = null, updater = null, replTh = null;
		try {
			final SocketAddress toServer = new InetSocketAddress(host, 8081);

			server = new Thread(new ServerProcess(
					myport,
					new ConcreteTaskHandler[] {
							new StatHandler(filesHolder),
							new GetHandler(filesHolder)
					}));

			updater = new Thread(new UpdatePerformer(filesHolder, toServer, myport, updateTime));

			REPL repl = new REPL(
					filesHolder,
					new FilesDownloader(filesHolder, toServer),
					toServer,
					out,
					sinp);

			replTh = new Thread(() -> repl.startRepl());

			server.start();
			updater.start();
			replTh.start();

			while (true) {
				Thread.sleep(10000);
			}
		} catch (IOException e) {
			out.println(e.getMessage());
		} catch (InterruptedException e) {
			server.interrupt();
			updater.interrupt();
			replTh.interrupt();

			try {
				server.join();
				updater.join();
				replTh.join();
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			out.println("Saving client state...");
			try {
				filesHolder.save();
				filesHolder.close();
			} catch (IOException e1) {
				out.println("Saving client state failed");
				out.print(e.getMessage());
			}
		}
	}
}
