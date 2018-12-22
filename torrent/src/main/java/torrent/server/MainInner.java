package torrent.server;

import java.io.IOException;

import torrent.common.ConcreteTaskHandler;
import torrent.common.ServerProcess;

public class MainInner implements Runnable {

	private final long updateMillis;

	public MainInner (long updateMillis) {
		this.updateMillis = updateMillis;
	}

	@Override
	public void run() {
		StorageManager storageManager = null;
		Thread cleanThread = null;
		Thread srvThread = null;
		try {
			storageManager = new StorageManager("serverFile");

			cleanThread = new Thread(new OldClientsCleaner(storageManager, updateMillis));
			cleanThread.setDaemon(true);
			cleanThread.start();

			srvThread = new Thread(new ServerProcess(
					8081,
					new ConcreteTaskHandler[] {
							new ListHandler(storageManager),
							new UploadHandler(storageManager),
							new SourcesHandler(storageManager),
							new UpdateHandler(storageManager)}));

			srvThread.setDaemon(true);
			srvThread.start();

			while (true) {
				Thread.sleep(10000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			try {
				cleanThread.interrupt();
				srvThread.interrupt();
				cleanThread.join();
				srvThread.join();
				storageManager.save();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

}
