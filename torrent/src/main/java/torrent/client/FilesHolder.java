package torrent.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FilesHolder {

	public final int pieceSize = 0xA00000;
	//рабочие данные

	public enum FileStatus {Complete, Downloading, Paused};

	public Map<Integer, byte[]> files = new ConcurrentHashMap<>();
	public Map<Integer, String> filePaths = new ConcurrentHashMap<>();

	public Map<Integer, FileStatus> fileStatus = new ConcurrentHashMap<>();
	public Map<Integer, Set<Integer>> completePieces = new ConcurrentHashMap<>();
	//

	ObjectMapper mapper = new ObjectMapper();
	private final Path mapPath;
	private final Path filePathsPath;
	private final Path fileStatusPath;
	private final Path comletePiecesPath;

	public int numParts(int fileId) {
		return (files.get(fileId).length + pieceSize - 1) / pieceSize;
	}

	public int pieceOffset(int fileId, int numPart) {
		return pieceSize * numPart;
	}

	public int pieceLenght(int fileId, int numPart) {
		int file_length = files.get(fileId).length;
		return (numPart + 1) * pieceSize <= file_length
				? pieceSize
						: file_length - numPart * pieceSize;
	}

	public FilesHolder(String path) throws IOException {
		mapPath = Paths.get(path);
		filePathsPath = mapPath.resolve("filepaths");
		fileStatusPath = mapPath.resolve("filesStatus");
		comletePiecesPath = mapPath.resolve("completePieces");
		load();
	}

	private void writeMaps() throws IOException {
		if (!Files.exists(mapPath))
			Files.createDirectories(mapPath);

		filePathsPath.toFile().createNewFile();
		mapper.writeValue(filePathsPath.toFile(), filePaths);

		fileStatusPath.toFile().createNewFile();
		mapper.writeValue(fileStatusPath.toFile(), fileStatus);

		comletePiecesPath.toFile().createNewFile();
		mapper.writeValue(comletePiecesPath.toFile(), completePieces);
	}

	public void save() throws JsonGenerationException, JsonMappingException, IOException {

		writeMaps();

		for (Entry<Integer, String> ent : filePaths.entrySet()) {
			try (FileOutputStream fout = new FileOutputStream(new File(ent.getValue()))) {
				fout.write(files.get(ent.getKey()));
			}
		}
	}

	public void save(int fileId) throws FileNotFoundException, IOException {

		writeMaps();

		if (files.containsKey(fileId)) {
			try (FileOutputStream fout = new FileOutputStream(new File(filePaths.get(fileId)))) {
				fout.write(files.get(fileId));
			}
		}
	}

	public void load() throws JsonGenerationException, JsonMappingException, IOException {
		if (filePathsPath.toFile().exists())
			filePaths = mapper.readValue(filePathsPath.toFile(), new TypeReference<Map<Integer, String>>() {});
		if (fileStatusPath.toFile().exists())
			fileStatus = mapper.readValue(fileStatusPath.toFile(), new TypeReference<Map<Integer, FileStatus>>() {});
		if (comletePiecesPath.toFile().exists())
			completePieces = mapper.readValue(comletePiecesPath.toFile(), new TypeReference<Map<Integer, Set<Integer>>>() {});
		
		if (!filePaths.keySet().equals(fileStatus.keySet())
				|| !filePaths.keySet().equals(completePieces.keySet())) {
			throw new RuntimeException("maps key sets not equal");
		}

		for (Entry<Integer, String> ent : filePaths.entrySet()) {
			try (FileInputStream finp = new FileInputStream(new File(ent.getValue()))) {
				files.put(ent.getKey(), finp.readAllBytes());
			} catch (FileNotFoundException fnfe) {
				completePieces.get(ent.getKey()).clear();
				fileStatus.put(ent.getKey(), FileStatus.Downloading);
			}
		}
	}

	public void deleteFile(int id) {
		if (! files.containsKey(id)) {
			throw new NullPointerException("File isn't known.");
		}
		files.remove(id);
		filePaths.remove(id);
		fileStatus.remove(id);
		completePieces.remove(id);
	}

	public void addFileToDownload(int id, long size, String filePath) throws FileProblemException, FileNotFoundException, IOException {
		if (files.containsKey(id)) {
			throw new FileProblemException("id already used");
		}
		if (filePaths.containsValue(filePath)) {
			throw new FileProblemException("file with specified path used");
		}
		files.put(id, new byte[Math.toIntExact(size)]);
		filePaths.put(id, filePath);
		completePieces.put(id, ConcurrentHashMap.newKeySet());
		fileStatus.put(id, FileStatus.Paused);
		writeMaps();
	}

	public void addExistingFile(int id, Path path) throws FileProblemException, FileNotFoundException, IOException {
		if (files.containsKey(id)) {
			throw new FileProblemException("id already used");
		}
		if (filePaths.containsValue(path.toString())) {
			throw new FileProblemException("file with specified path used");
		}

		try (FileInputStream finp = new FileInputStream(path.toFile())) {
			files.put(id, finp.readAllBytes());
		}
		filePaths.put(id, path.toString());
		fileStatus.put(id, FileStatus.Complete);
		completePieces.put(id,
				Stream.iterate(0, i -> i + 1)
				.limit(numParts(id))
				.collect(Collectors.toSet()));
		save(-1);
	}

}
