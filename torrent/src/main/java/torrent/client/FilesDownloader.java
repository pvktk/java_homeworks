package torrent.client;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FilesDownloader {
	
	FilesHolder filesHolder;
	SocketAddress toServer;
	
	ExecutorService pool = Executors.newCachedThreadPool();
	
	Map<Integer, Future<?>> fileDownloadsFutures = new HashMap<>();
	
	public FilesDownloader(FilesHolder stm, SocketAddress toServer) {
		this.filesHolder = stm;
		this.toServer = toServer;
	}
	
	public boolean startFileDownload(int fileId) {
		if (fileDownloadsFutures.containsKey(fileId)) {
			return false;
		}
		SingleFileDownloader downloader = new SingleFileDownloader(toServer, filesHolder, fileId);
		//fileDownloads.put(fileId, downloader);
		fileDownloadsFutures.put(fileId, pool.submit(downloader));
		return true;
	}
	
	public void stopFileDownload(int fileId) {
		if (!fileDownloadsFutures.containsKey(fileId)) {
			return;
		}
		fileDownloadsFutures.get(fileId).cancel(true);
		fileDownloadsFutures.remove(fileId);
	}

}
