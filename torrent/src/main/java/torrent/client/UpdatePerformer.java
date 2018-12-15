package torrent.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UpdatePerformer implements Runnable {

	private Socket toServer;
	private FilesHolder filesHolder;
	private short myPort;

	public UpdatePerformer(FilesHolder filesHolder, Socket toServer, short myPort) {
		this.filesHolder = filesHolder;
		this.toServer = toServer;
		this.myPort = myPort;
	}

	@Override
	public void run() {

		while(true) {
			
			DataOutputStream out;
			try {
				out = new DataOutputStream(toServer.getOutputStream());
				out.writeByte(4);
				out.writeShort(myPort);
			} catch (IOException e) {
				System.out.println("UpdatePerformer: creation of DataOutputStream failed");
				continue;
			}

			try {
				out.writeInt(filesHolder.completePieces.size());
				for (Integer fileId : filesHolder.completePieces.keySet()) {
					out.writeInt(fileId);
				}
			} catch (IOException e) {
				System.out.println("UpdatePerformer failed");
				continue;
			}
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

}
