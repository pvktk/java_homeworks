package torrent.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SourcesUpdater implements Runnable{
	
	private Socket toServer;
	private FilesDownloader filesDownloader;
	
	public SourcesUpdater(Socket toServer, FilesDownloader filesDownloader) {
		this.toServer = toServer;
		this.filesDownloader = filesDownloader;
	}
	
	public void updateSources() {
		DataOutputStream out;
		DataInputStream in;
		try {
			out = new DataOutputStream(toServer.getOutputStream());
			in = new DataInputStream(toServer.getInputStream());
			for (Integer fileId : filesDownloader.avaliablePieces.keySet()) {
				out.writeByte(3);
				out.writeInt(fileId);
				
				int clientsCount = in.readInt();
				List<InetSocketAddress> sources = new ArrayList<>();
				filesDownloader.fileSources.put(fileId, sources);
				for (int i = 0; i < clientsCount; i++) {
					sources.add(new InetSocketAddress(new InetAddress, in.readShort()));
				}
			}
		} catch (IOException e) {
			System.out.println("SourceUpdater: creation of output stream failed");
		}
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
