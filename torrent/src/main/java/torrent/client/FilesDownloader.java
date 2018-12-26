package torrent.client;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import torrent.client.FilesHolder.FileStatus;

public class FilesDownloader {

	private FilesHolder filesHolder;
	private SocketAddress toServer;

	private ExecutorService pool = Executors.newCachedThreadPool();

	private Map<Integer, Future<?>> fileDownloadsFutures = new HashMap<>();

	public FilesDownloader(FilesHolder stm, SocketAddress toServer) {
		this.filesHolder = stm;
		this.toServer = toServer;
		stm.fileStatus.forEach((id, status) -> {
			if (status == FileStatus.Downloading ) {
				try {
					startFileDownload(id);
				} catch (IOException e) {
					System.err.println("Failed. to start download at startup");
					e.printStackTrace();
				}
			}
		});
	}

	public boolean startFileDownload(int fileId) throws IOException {
		if (fileDownloadsFutures.containsKey(fileId)) {
			return false;
		}

		filesHolder.fileStatus.put(fileId, FileStatus.Downloading);

		SingleFileDownloader downloader = new SingleFileDownloader(toServer, filesHolder, fileId, this);
		fileDownloadsFutures.put(fileId, pool.submit(downloader));
		return true;
	}

	public void stopFileDownload(int fileId) {
		if (!filesHolder.fileStatus.containsKey(fileId)) {
			throw new IllegalStateException("This file wasn't been downloading");
		}

		if (filesHolder.fileStatus.get(fileId) != FileStatus.Downloading) {
			return;
		}

		fileDownloadsFutures.get(fileId).cancel(true);
		fileDownloadsFutures.remove(fileId);

		filesHolder.fileStatus.put(fileId, FileStatus.Paused);
	}

}
