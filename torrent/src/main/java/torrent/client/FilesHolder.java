package torrent.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FilesHolder {
	
	public final int pieceSize = 0xA00000;

	public Map<Integer, byte[]> files = new HashMap<>();
	public Set<Integer> completedFiles = new HashSet<>();
	public Map<Integer, Set<Integer>> completePieces = new HashMap<>();
}
