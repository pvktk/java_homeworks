package hw_git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitCore {
	RepInformation inform = null;
	Path informPath = null;
	final String infoFileName = ".myGitData";
	final String storageFolder = ".mygitdata";

	private void findRepInformation() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
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
		System.out.println("writing to path: " + informPath.toString());
		omapper.writeValue(informPath.resolve(infoFileName).toFile(), inform);
	}
	
	void makeInit() throws JsonGenerationException, JsonMappingException, IOException, UnversionedException {
		try {
		findRepInformation();
		} catch (UnversionedException e) {
			//ObjectMapper omapper = new ObjectMapper();
			//omapper.writeValue(Paths.get("").resolve(filename).toFile(), new RepInformation());
			informPath = Paths.get("");
			inform = new RepInformation();
			//inform.allFiles.put(Paths.get(""), 0);
			updateRepInformation();
			System.out.println("Ok.");
		}
	}
	
	void increaseRevisionNumber() {
		inform.revision++;
	}
	
	private Path getPathRealRelative(String filename) {
		return informPath.relativize(Paths.get("")).resolve(filename);
	}
	
	private Path getStoragePath(String filename, int revision) {
		Path rel = getPathRealRelative(filename);
		String fname = rel.getFileName().toString();
		
		rel = rel.getParent();
		
		Path storage = informPath.resolve(rel).resolve(fname + revision);
		
		return storage;
	}
	
	private void addFile(String filename) throws IOException {
		int revision = inform.revision;
		Files.copy(Paths.get("").resolve(filename), getStoragePath(filename, revision));
		inform.allFiles.put(getPathRealRelative(filename).toString(), revision);
	}
	
	void makeCommit(String message, String[] filenames) throws IOException, UnversionedException {
		findRepInformation();
		increaseRevisionNumber();
		inform.commitMessages.add(message);
		inform.timestamps.add(new Timestamp(0));
		for (String fname : filenames) {
			addFile(fname);
		}
	}
	
	private void deleteVersionedFiles(File root) {
		if (root.isFile()) {
			if (inform.allFiles.containsKey(root.toPath().relativize(informPath))) {
				System.out.println("deleting" + root.getName());
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
	
	private void restoreVersionedFiles(int revision) throws IOException {
		for (Map.Entry<String, Integer> ent : inform.allFiles.entrySet()) {
			if (ent.getValue() <= revision) {
				Files.copy(informPath.resolve(storageFolder).resolve(ent.getKey()),
						informPath.resolve(ent.getKey()));
			}
		}
	}
	
	void makeCheckout(int revision) throws IOException, UnversionedException {
		findRepInformation();
		deleteVersionedFiles(informPath.toFile());
		restoreVersionedFiles(revision);
	}
	
	void makeReset(int revision) throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		findRepInformation();
		Iterator<Entry<String, Integer>> it = inform.allFiles.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<String, Integer> ent = it.next();
			if (ent.getValue() > revision) {
				it.remove();
			}
		}
		updateRepInformation();
	}
	
	String getLog(int revision) throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		findRepInformation();
		if (revision == -1) {
			revision = inform.revision;
		}
		return inform.commitMessages.get(revision) + "\n"
				+ inform.timestamps.get(revision);
	}
}
