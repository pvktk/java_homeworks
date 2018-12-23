package torrent.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.NoSuchElementException;
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

	void printLineForExistingFile(int id, String printName) {
		out.print(id + " " + printName + " ");
		switch (filesHolder.fileStatus.get(id)) {
		case Complete:
		{
			out.print("complete");
			break;
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
		out.println(" " + filesHolder.completePieces.get(id).size() + "/"
				+ filesHolder.numParts(id));
	}

	void printStatus() throws IOException {

		if (filesHolder.fileStatus.isEmpty()) {
			out.println("No files yet");
		}

		for (Integer id : filesHolder.fileStatus.keySet()) {
			printLineForExistingFile(id, filesHolder.filePaths.get(id));
		}
	}

	void listAvaliableFiles() {
		try {
			ServerFilesLister.list(toServer, listedFilesSize, listedFilesName);

			if (listedFilesName.isEmpty()) {
				out.println("No files avaliable on server");
			}
			for (int id : listedFilesName.keySet()) {
				if (!filesHolder.fileStatus.containsKey(id)) {
					out.print(id + " " + listedFilesName.get(id) + " ");
					out.println(0 + "/" + getNPieces(listedFilesSize.get(id)));
				} else {
					printLineForExistingFile(id, listedFilesName.get(id));
				}
			}

		} catch (IOException e) {
			out.println("Failed to list avaliable files.\n" + e.getMessage());
		}
	}

	void startDownload(int id, String filename) throws FileNotFoundException, FileProblemException, IOException {
		filename = filename.trim();
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

	void publishFile(String path) throws FileProblemException {
		Path pathTo = Paths.get(path.trim());
		try {
			if (filesHolder.filePaths.containsValue(pathTo.toString())) {
				throw new FileProblemException("file with specified path used");
			}

			int id = Uploader.uploadAndGetId(toServer, pathTo);

			filesHolder.addExistingFile(id, pathTo);
			out.println("The file has an id " + id);
		}	catch (FileNotFoundException e) {
			//e.printStackTrace();
			out.println("Failed to publish file.\n" +
					pathTo + " not exists.");
		} catch (IOException e) {
			//e.printStackTrace();
			out.println("Failed to publish file.\n" + e.getMessage());
		}
	}

	int getNPieces(long size) {
		return Math.toIntExact((size - 1 + filesHolder.pieceSize) / filesHolder.pieceSize);
	}

	public final static String helpMessage = 
			"This is torrent client.\n"
					+ "commands:\n"
					+ "list\n"
					+ "publish <filepath>\n"
					+ "delete <id>\n"
					+ "get <id> <savePath>\n"
					+ "pause <id>\n"
					+ "resume <id>\n"
					+ "status\n"
					+ "help";

	public void startRepl() {

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
					break;
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
					case "resume":
					{
						downloader.startFileDownload(in.nextInt());
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
				} catch (InputMismatchException imme) {
					in.next();
					out.println("Input mismatch");
				} catch (NoSuchElementException e) {
					return;
				}
			}
		}
	}
}
