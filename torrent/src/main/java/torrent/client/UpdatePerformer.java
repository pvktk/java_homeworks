package torrent.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import torrent.common.StorageManager;

public class UpdatePerformer implements Runnable {

	private Socket toServer;
	private StorageManager<FilesHolder> stm;
	private short myPort;

	public UpdatePerformer(StorageManager stm, Socket toServer, short myPort) {
		this.stm = stm;
		this.toServer = toServer;
		this.myPort = myPort;
	}

	@Override
	public void run() {

		while(true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				return;
			}

			DataOutputStream out;
			try {
				out = new DataOutputStream(toServer.getOutputStream());
				out.writeByte(4);
				out.writeShort(myPort);
			} catch (IOException e) {
				System.out.println("UpdatePerformer: creation of DataOutputStream failed");
				continue;
			}

			stm.lock.readLock().lock();
			try {
				out.writeInt(stm.data.completePieces.size());
				for (Integer fileId : stm.data.completePieces.keySet()) {
					out.writeInt(fileId);
				}
			} catch (IOException e) {
				System.out.println("UpdatePerformer failed");
				continue;
			} finally {
				stm.lock.readLock().unlock();
			}
		}
	}

}
