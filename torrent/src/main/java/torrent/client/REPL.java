package torrent.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketAddress;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class REPL {

	private final FilesHolder filesHolder;
	private final FilesDownloader downloader;

	private Map<Integer, Long> listedFilesSize = new HashMap<>();
	private Map<Integer, String> listedFilesName = new HashMap<>();

	private SocketAddress toServer;
	PrintStream out = System.out;

	public REPL(FilesHolder filesHolder, FilesDownloader downloader, SocketAddress toServer) {
		this.filesHolder = filesHolder;
		this.downloader = downloader;
		this.toServer = toServer;
	}

	void printStatus() {

		for (Integer id : filesHolder.files.keySet()) {
			out.print(id + " " + filesHolder.filePaths.get(id) + " ");
			if (filesHolder.completedFiles.contains(id)) {
				out.println("complete");
				continue;
			}
			if (downloader.fileDownloadsFutures.containsKey(id)) {
				out.print("->");
			} else {
				out.print("||");
			}
			out.println(filesHolder.completePieces.get(id).size() + " / "
					+ filesHolder.numParts(id));
		}
	}

	void startDownload(int id, String filename) throws FileNotFoundException, FileProblemException, IOException {
		Long size = listedFilesSize.get(id);
		if (size == null) {
			out.println("Unknown file size. Make list first.");
			return;
		}
		filesHolder.addFileToDownload(id, size, filename);
		downloader.startFileDownload(id);
	}

	void deleteFile(int id) {
		downloader.stopFileDownload(id);
		filesHolder.deleteFile(id);
	}

	void publishFile(String pathTo) {
		try {
			int id = Uploader.uploadAndGetId(toServer, pathTo);
			if (id == 1) {
				out.println("file not exist");
				return;
			}

			filesHolder.addExistingFile(id, Paths.get(pathTo));
		} catch (IOException e) {
			out.println(e.getMessage());
		} catch (FileProblemException e) {
			out.println(e.getMessage());
		}
	}
	
	void listAvaliableFiles() {
		try {
			ServerFilesLister.list(toServer, listedFilesSize, listedFilesName);
			
			for (int id : listedFilesName.keySet()) {
				out.print(id + " " + listedFilesName.get(id));
				if (filesHolder.files.containsKey(id)) {
					if (filesHolder.completedFiles.contains(id)) {
						out.println("downloaded");
					} else if (downloader.fileDownloadsFutures.containsKey(id))
				}
			}
			
		} catch (IOException e) {
			out.println("Failed to list avaliable files");
			out.println(e.getMessage());
		}
	}
	
	public void startRepl() {
		/*
		Options options = new Options();
		options.addOption("list", "list files, known to server");
		options.addOption("publish", true, "make file known by torrent");
		options.addOption("delete", true, "make file unknown by your client");
		options.addOption("get", true, "download file by id");
		options.addOption("pause", true, "stop file download");
		options.addOption("resume", true, "resume file downloading");
		options.addOption("status", true, "list files known by your client and their status");
		 */

		try (Scanner in = new Scanner(System.in)) {
			while (true) {
				try {
					System.out.print(">");
					switch (in.next()) {
					case "list":
					{
						
					}
					break;
					case "publish":
					{
						publishFile(in.nextLine());
					}
					break;
					case "delete":
					{
						deleteFile(in.nextInt());
					}
					break;
					case "get":
					{
						startDownload(in.nextInt(), in.next());
					}
					break;
					case "pause":
					{
						downloader.stopFileDownload(in.nextInt());
					}
					break;
					case "status":
					{
						printStatus();
					}
					break;
					}
				} catch (Exception e) {
					out.println(e.getMessage());
				}
			}
		}
	}
}
