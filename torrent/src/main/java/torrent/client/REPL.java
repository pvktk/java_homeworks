package torrent.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
	private final PrintStream out;
	private final InputStream inStream;

	public REPL(
			FilesHolder filesHolder,
			FilesDownloader downloader,
			SocketAddress toServer,
			PrintStream out,
			InputStream inStream) {
		this.filesHolder = filesHolder;
		this.downloader = downloader;
		this.toServer = toServer;
		this.out = out;
		this.inStream = inStream;
	}

	void printStatus() {

		if (filesHolder.files.isEmpty()) {
			out.println("No files yet");
		}

		for (Integer id : filesHolder.files.keySet()) {
			out.print(id + " " + filesHolder.filePaths.get(id) + " ");
			switch (filesHolder.fileStatus.get(id)) {
			case Complete:
			{
				out.println("complete");
				continue;
			}
			case Downloading:
			{
				out.print("->");
				break;
			}
			case Paused:
			{
				out.print("||");
				break;
			}
			}
			out.println(filesHolder.completePieces.get(id).size() + " / "
					+ filesHolder.numParts(id));
		}
	}

	void startDownload(int id, String filename) throws FileNotFoundException, FileProblemException, IOException {
		Long size = listedFilesSize.get(id);
		if (size == null) {
			out.println("Unknown file id. Make list first.");
			return;
		}
		filesHolder.addFileToDownload(id, size, filename);
		downloader.startFileDownload(id);
	}

	void deleteFile(int id) {
		try {
			downloader.stopFileDownload(id);
		} catch (NullPointerException e) {}

		filesHolder.deleteFile(id);
	}

	void publishFile(String pathTo) throws FileProblemException {
		try {
			int id = Uploader.uploadAndGetId(toServer, pathTo);
			if (id == 1) {
				out.println("file not exist");
				return;
			}

			filesHolder.addExistingFile(id, Paths.get(pathTo));
		}	catch (FileNotFoundException e) {
			out.println("Failed to publish file.\n" +
					pathTo + " not found.");
		} catch (IOException e) {
			out.println("Failed to publish file.\n" + e.getMessage());
		}
	}

	void listAvaliableFiles() {
		try {
			ServerFilesLister.list(toServer, listedFilesSize, listedFilesName);

			if (listedFilesName.isEmpty()) {
				out.println("No files avaliable on server");
			}
			for (int id : listedFilesName.keySet()) {
				out.print(id + " " + listedFilesName.get(id));
				switch (filesHolder.fileStatus.get(id)) {
				case Complete:
				{
					out.println("complete");
					continue;
				}
				case Downloading:
				{
					out.print("->");
					break;
				}
				case Paused:
				{
					out.print("||");
					break;
				}
				}
			}

		} catch (IOException e) {
			out.println("Failed to list avaliable files.\n" + e.getMessage());
		}
	}

	final String helpMessage = 
			"This is torrent client.\n"
					+ "commands:\n"
					+ "list\n"
					+ "publish <filepath>\n"
					+ "delete <id>\n"
					+ "get <id> <savePath>\n"
					+ "pause <id>\n"
					+ "status\n"
					+ "help";

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

		try (Scanner in = new Scanner(inStream)) {
			while (true) {
				try {
					out.print(">");
					String command = in.next();
					switch (command) {
					case "help":
					{
						out.println(helpMessage);
					}
					case "list":
					{
						listAvaliableFiles();
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

					default:
					{
						out.println("unknown command " + command);
					}
					}
				} catch (IOException | FileProblemException | NullPointerException e) {
					out.println(e.getMessage());
				}
			}
		}
	}
}
