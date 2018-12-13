package torrent.client;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import torrent.common.StorageManager;

public class FilesDownloader {
	
	StorageManager<FilesHolder> stm;
	SocketAddress toServer;
	
	ExecutorService pool = Executors.newFixedThreadPool(4);
	
	Map<Integer, SingleFileDownloader> fileDownloads = new HashMap<>();
	
	public FilesDownloader(StorageManager<FilesHolder> stm, SocketAddress toServer) {
		this.stm = stm;
		this.toServer = toServer;
	}
	
	public boolean startFileDownload(int fileId) {
		if (fileDownloads.containsKey(fileId)) {
			return false;
		}
		SingleFileDownloader downloader = new SingleFileDownloader(toServer, stm, fileId);
		fileDownloads.put(fileId, downloader);
		pool.execute(downloader);
		return true;
	}

}
