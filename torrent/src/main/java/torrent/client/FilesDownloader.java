package torrent.client;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import torrent.client.FilesHolder.FileStatus;

public class FilesDownloader {

	FilesHolder filesHolder;
	SocketAddress toServer;

	ExecutorService pool = Executors.newCachedThreadPool();

	Map<Integer, Future<?>> fileDownloadsFutures = new HashMap<>();

	public FilesDownloader(FilesHolder stm, SocketAddress toServer) {
		this.filesHolder = stm;
		this.toServer = toServer;
	}

	public boolean startFileDownload(int fileId) throws IOException {
		if (filesHolder.fileStatus.get(fileId) != FileStatus.Paused) {
			return false;
		}

		filesHolder.fileStatus.put(fileId, FileStatus.Downloading);

		SingleFileDownloader downloader = new SingleFileDownloader(toServer, filesHolder, fileId);
		fileDownloadsFutures.put(fileId, pool.submit(downloader));
		return true;
	}

	public void stopFileDownload(int fileId) {
		if (!filesHolder.fileStatus.containsKey(fileId)) {
			throw new NullPointerException("This file wasn't been downloading");
		}

		if (filesHolder.fileStatus.get(fileId) != FileStatus.Downloading) {
			return;
		}

		fileDownloadsFutures.get(fileId).cancel(true);
		fileDownloadsFutures.remove(fileId);

		filesHolder.fileStatus.put(fileId, FileStatus.Paused);
	}

}
