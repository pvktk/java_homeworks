package torrent.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import torrent.common.ConcreteTaskHandler;
import torrent.common.ServerProcess;

public class MainInner implements Runnable {

	private final String host;
	private final PrintStream out;
	private final InputStream sinp;

	public MainInner(String host, PrintStream out, InputStream sinp) {
		this.host = host;
		this.out = out;
		this.sinp = sinp;
	}

	@Override
	public void run() {
		FilesHolder filesHolder = null;
		try {
			final int myport = 8082;
			final SocketAddress toServer = new InetSocketAddress(host, 8081);

			filesHolder = new FilesHolder();

			ExecutorService exec = Executors.newFixedThreadPool(3);
			exec.execute(new ServerProcess(
					myport,
					new ConcreteTaskHandler[] {
							new StatHandler(filesHolder),
							new GetHandler(filesHolder)
					}));

			exec.execute(new UpdatePerformer(filesHolder, toServer, myport));

			REPL repl = new REPL(
					filesHolder,
					new FilesDownloader(filesHolder, toServer),
					toServer,
					out,
					sinp);

			exec.execute(() -> repl.startRepl());

			while (true) {
				Thread.sleep(10000);
			}
		} catch (IOException e) {
			out.println(e.getMessage());
		} catch (InterruptedException e) {
			out.println("Saving client state...");
			try {
				filesHolder.save();
			} catch (IOException e1) {
				out.print(e.getMessage());
			}
		}
	}
}
