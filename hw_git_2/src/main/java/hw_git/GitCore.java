package hw_git;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitCore {
	private RepInformation inform = null;
	private Path informPath = null;
	private final String infoFileName = ".myGitData";
	private final String storageFolder = ".mygitdata";
	private final String stageFolder = ".stageData";
	
	private int getLastItem(ArrayList<Integer> list) {
		return list.get(list.size() - 1);
	}
	void findRepInformation() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		RepInformation result = null;
		Path p = Paths.get("");
		
		while (p != null && !Files.exists(p.resolve(infoFileName))) {
			p = p.getParent();
		}
		
		if (p != null) {
			ObjectMapper omapper = new ObjectMapper();
			result = omapper.readValue(p.resolve(infoFileName).toFile(), RepInformation.class);
		}
		
		if (result == null) {
			throw new UnversionedException();
		}
		inform =  result;
		informPath = p;
	}
	
	private void updateRepInformation() throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper omapper = new ObjectMapper();
		omapper.writeValue(informPath.resolve(infoFileName).toFile(), inform);
	}
	
	void makeInit() throws JsonGenerationException, JsonMappingException, IOException, UnversionedException {
		try {
		findRepInformation();
		} catch (UnversionedException e) {
			informPath = Paths.get("");
			inform = new RepInformation();
			updateRepInformation();
			System.out.println("Ok.");
		}
	}
	
	void increaseRevisionNumber() {
		inform.revision++;
	}
	
	private Path getPathRealRelative(String filename) {
		return informPath.relativize(Paths.get(filename));
	}
	
	private Path getStoragePath(Path keyPath, int revision) {
		return informPath.resolve(storageFolder)
		.resolve(keyPath.getParent())
		.resolve(keyPath.getFileName().toString() + revision);
	}
	
	private void addFile(Path filepath) throws IOException {
		int revision = inform.revision;
		Path keyPath = filepath.subpath(1, filepath.getNameCount());
		Path storage = getStoragePath(keyPath, revision);
		storage.getParent().toFile().mkdirs();
		Files.copy(filepath, storage);
		String keyName = keyPath.toString();
		ArrayList<Integer> revisions = inform.allFiles.get(keyName);
		if (revisions == null) {
			revisions = new ArrayList<>();
			inform.allFiles.put(keyName, revisions);
		}
		revisions.add(revision);
	}
	
	void makeAdd(String[] filenames) throws IOException, UnversionedException {
		findRepInformation();
		for (String fname : filenames) {
			Path orig = getPathRealRelative(fname);
			Path dest = informPath.resolve(stageFolder).resolve(orig);
			//Files.copy(orig, dest);
			FileUtils.copyFile(orig.toFile(), dest.toFile());
		}
	}
	
	void makeCommit(String message) throws IOException, UnversionedException {
		findRepInformation();
		increaseRevisionNumber();
		inform.commitMessages.add(message);
		inform.timestamps.add(new Timestamp(System.currentTimeMillis()));
		Files.walkFileTree(
				informPath.resolve(stageFolder),
				new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				// TODO Auto-generated method stub
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				addFile(file);
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				// TODO Auto-generated method stub
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				// TODO Auto-generated method stub
				return FileVisitResult.CONTINUE;
			}
		});
		updateRepInformation();
	}
	
	private void deleteVersionedFiles(File root) {
		if (root.isFile()) {
			String key = informPath.toAbsolutePath()
					.relativize(Paths.get(root.getAbsolutePath()))
					.toString();
			
			if (inform.allFiles.containsKey(key)) {
				root.delete();
			}
			return;
		}
		if (root.isDirectory()) {
			for (File f : root.listFiles()) {
				if (f.getName().equals(infoFileName) || f.getName().equals(storageFolder)) {
					continue;
				}
				deleteVersionedFiles(f);
			}
		}
	}
	
	private int getIndexOfLessEq(ArrayList<Integer> list, int val) {
		int curr = 0;
		int prev = -1;
		while (curr < list.size() && list.get(curr) <= val) {
			prev = curr;
			curr++;
		}
		
		return prev;
	}
	
	private void restoreVersionedFiles(int revision) throws IOException {
		for (Map.Entry<String, ArrayList<Integer>> ent : inform.allFiles.entrySet()) {
			int revisionIdx = getIndexOfLessEq(ent.getValue(), revision);
			if (revisionIdx >= 0) {
				int revNumber = ent.getValue().get(revisionIdx);
				FileUtils.copyFile(informPath.resolve(storageFolder).resolve(ent.getKey() + revNumber).toFile(),
						informPath.resolve(ent.getKey()).toFile());
			}
		}
	}
	
	void makeCheckout(int revision) throws IOException, UnversionedException {
		findRepInformation();
		deleteVersionedFiles(informPath.toAbsolutePath().toFile());
		restoreVersionedFiles(revision);
	}
	
	void makeCheckout(String[] files) throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		findRepInformation();
		for (String fname : files) {
			String key = getPathRealRelative(fname).toString();
			System.out.println("checkout key: " + key);
			FileUtils.copyFile(getStoragePath(getPathRealRelative(fname),
						getLastItem(inform.allFiles.get(key))).toFile(),
					informPath.resolve(key).toFile());
		}
	}
	
	void makeReset(int revision) throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		findRepInformation();
		Iterator<Entry<String, ArrayList<Integer>>> it = inform.allFiles.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, ArrayList<Integer>> ent = it.next();
			int revisionIndex = getIndexOfLessEq(ent.getValue(), revision);
			if (revisionIndex == -1) {
				it.remove();
			} else {
				ent.getValue().subList(revisionIndex + 1, ent.getValue().size()).clear();
			}
			
		}
		inform.revision = revision;
		inform.commitMessages.subList(revision , inform.commitMessages.size()).clear();
		inform.timestamps.subList(revision, inform.timestamps.size()).clear();
		updateRepInformation();
	}
	
	String getLog(int revision) throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		findRepInformation();
		if (revision == -1) {
			revision = inform.revision;
		}
		if(revision == 0) {
			return "Empty log";
		}
		return "revision: " + revision + "\n"
			+ inform.commitMessages.get(revision - 1) + "\n"
				+ inform.timestamps.get(revision - 1);
	}
	
	List<String> getDeletedFiles() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		ArrayList<String> result = new ArrayList<>();
		findRepInformation();
		
		for (String keypath : inform.allFiles.keySet()) {
			if (!Files.exists(informPath.resolve(keypath))) {
				result.add(Paths.get("").relativize(Paths.get(keypath)).toString());
			}
		}

		return result;
	}
	
	List<String> getChangedFiles() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		ArrayList<String> result = new ArrayList<>();
		findRepInformation();
		
		for (String keypath : inform.allFiles.keySet()) {
			if (Files.exists(informPath.resolve(keypath))
					&& !FileUtils.contentEquals(
							informPath.resolve(keypath).toFile(),
							getStoragePath(Paths.get(keypath), getLastItem(inform.allFiles.get(keypath))
									).toFile()
							)) {
				result.add(Paths.get("").relativize(Paths.get(keypath)).toString());
			}
		}

		return result;
	}
	
	List<String> getUntrackedFiles() throws IOException {
		ArrayList<String> result = new ArrayList<>();
		Files.walkFileTree(Paths.get(""), new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				// TODO Auto-generated method stub
				return dir.equals(Paths.get(stageFolder)) || dir.equals(Paths.get(storageFolder))
						? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				// TODO Auto-generated method stub
				if (!inform.allFiles.containsKey(informPath.relativize(file).toString())) {
					result.add(file.toString());
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				// TODO Auto-generated method stub
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				// TODO Auto-generated method stub
				return FileVisitResult.CONTINUE;
			}
		});
		
		return result;
	}
	
	int getCurrentRevision() {
		return inform.revision;
	}
	
	
	private void removeFromRep(String filename) throws IOException {
		Path keypath = getPathRealRelative(filename);
		System.out.println("keypath: " + keypath);
		for (int revision : inform.allFiles.get(keypath.toString())) {
			Files.delete(getStoragePath(keypath, revision));
		}
		Files.deleteIfExists(informPath.resolve(stageFolder).resolve(keypath));
		inform.allFiles.remove(keypath.toString());
	}
	
	void makeRM(String[] files) throws IOException, UnversionedException {
		findRepInformation();
		for (String filename : files) {
			removeFromRep(filename);
		}
		updateRepInformation();
	}
}
