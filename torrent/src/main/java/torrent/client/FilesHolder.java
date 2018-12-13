package torrent.client;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FilesHolder {
	
	public final int pieceSize = 0xA00000;
	
	//рабочие данные
	public Map<Integer, byte[]> files = new HashMap<>();
	public Map<Integer, String> filePaths = new HashMap<>();
	
	public Set<Integer> completedFiles = new HashSet<>();
	public Map<Integer, Set<Integer>> completePieces = new HashMap<>();
	//
	
	ObjectMapper mapper = new ObjectMapper();
	final Path mapPath = Paths.get("torrentData");
	final Path filesPaths = mapPath.resolve("filepaths");
	final Path compFiles = mapPath.resolve("completeFiles");
	final Path comPieces = mapPath.resolve("completePieces");
	
	public int numParts(int fileId) {
		return (files.get(fileId).length + pieceSize - 1) / pieceSize;
	}
	
	public void save() throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(filesPaths.toFile(), filePaths);
		mapper.writeValue(compFiles.toFile(), completedFiles);
		mapper.writeValue(comPieces.toFile(), completePieces);
	}
	
}
