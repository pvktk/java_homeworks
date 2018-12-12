package torrent.client;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import torrent.common.StorageManager;

public class FilesDownloader implements Runnable {
	
	StorageManager<FilesHolder> stm;
	Socket toServer;
	
	Map<Integer, List<InetSocketAddress>> fileSources = new HashMap<>();
	Map<Integer, List<Integer>> avaliablePieces = new HashMap<>();
	Map<Integer, Map<Integer, PieceDownloader>> downloadingPieces = new HashMap<>();
	
	public FilesDownloader(StorageManager<FilesHolder> stm, Socket toServer) {
		this.stm = stm;
		this.toServer = toServer;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(10000);
			
			
		} catch (InterruptedException e) {
			return;
		}
	}

}
