package torrent.server;

import java.io.IOException;

import torrent.common.ConcreteTaskHandler;
import torrent.common.ServerProcess;

public class MainInner implements Runnable {

	@Override
	public void run() {
		StorageManager storageManager = null;
		try {
			storageManager = new StorageManager("serverFile");

			Thread cleanThread = new Thread(new OldClientsCleaner(storageManager));
			cleanThread.setDaemon(true);
			cleanThread.start();

			Thread srvThread = new Thread(new ServerProcess(
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
			System.out.println("Saving server state...");
			try {
				storageManager.save();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
