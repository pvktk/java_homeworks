package torrent.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.core.JsonGenerationException;
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
	private final Path mapPath = Paths.get("torrentData");
	private final Path filePathsPath = mapPath.resolve("filepaths");
	private final Path fileStatusPath = mapPath.resolve("filesStatus");
	private final Path comletePiecesPath = mapPath.resolve("completePieces");

	public int numParts(int fileId) {
		return (files.get(fileId).length + pieceSize - 1) / pieceSize;
	}
	
	public int pieceOffset(int fileId, int numPart) {
		return pieceSize * numPart;
	}
	
	public int pieceLenght(int fileId, int numPart) {
		int file_length = files.get(fileId).length;
		return (numPart + 1) * pieceSize >= file_length
				? pieceSize
						: (numPart + 1) * pieceSize - file_length;
	}

	public FilesHolder() throws JsonGenerationException, JsonMappingException, IOException {
		load();
	}

	public void save() throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(filePathsPath.toFile(), filePaths);
		mapper.writeValue(fileStatusPath.toFile(), fileStatus);
		mapper.writeValue(comletePiecesPath.toFile(), completePieces);

		for (Entry<Integer, String> ent : filePaths.entrySet()) {
			try (FileOutputStream fout = new FileOutputStream(new File(ent.getValue()))) {
				fout.write(files.get(ent.getKey()));
			}
		}
	}
	
	public void save(int fileId) throws FileNotFoundException, IOException {
		mapper.writeValue(filePathsPath.toFile(), filePaths);
		mapper.writeValue(fileStatusPath.toFile(), fileStatus);
		mapper.writeValue(comletePiecesPath.toFile(), completePieces);
		
		if (files.containsKey(fileId)) {
			try (FileOutputStream fout = new FileOutputStream(new File(filePaths.get(fileId)))) {
				fout.write(files.get(fileId));
			}
		}
	}

	public void load() throws JsonGenerationException, JsonMappingException, IOException {
		filePaths = mapper.readValue(filePathsPath.toFile(), filePaths.getClass());
		fileStatus = mapper.readValue(fileStatusPath.toFile(), fileStatus.getClass());
		completePieces = mapper.readValue(comletePiecesPath.toFile(), completePieces.getClass());

		for (Entry<Integer, String> ent : filePaths.entrySet()) {
			try (FileInputStream finp = new FileInputStream(new File(ent.getValue()))) {
				files.put(ent.getKey(), finp.readAllBytes());
			}
		}
	}

	public void deleteFile(int id) {		
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
		save(-1);
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
		save(-1);
	}

}
