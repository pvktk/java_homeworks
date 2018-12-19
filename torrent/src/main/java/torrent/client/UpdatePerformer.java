package torrent.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class UpdatePerformer implements Runnable {

	private SocketAddress toServer;
	private FilesHolder filesHolder;
	private final int myPort;
	
	private final long updateTime;
	public UpdatePerformer(FilesHolder filesHolder, SocketAddress toServer, int myPort, long updateTime) {
		this.filesHolder = filesHolder;
		this.toServer = toServer;
		this.myPort = myPort;
		this.updateTime = updateTime;
	}

	@Override
	public void run() {

		while(true) {
			Socket s = null;
			DataOutputStream out = null;
			try {
				s = new Socket();
				s.connect(toServer);
				out = new DataOutputStream(s.getOutputStream());

				out.writeByte(4);
				out.writeShort(myPort);

				out.writeInt(filesHolder.completePieces.size());
				for (Integer fileId : filesHolder.completePieces.keySet()) {
					out.writeInt(fileId);
				}

			} catch (IOException e1) {
				System.out.println("\nUpdate to server failed");
			} finally {
				try {
					out.close();
					s.close();
				} catch (Exception e) {
				}

			}
			
			try {
				Thread.sleep(updateTime);
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}

