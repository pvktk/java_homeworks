package hw_git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
	
	void addAtIdx(ArrayList<Integer> list, int idx, int inc) {
		list.set(idx, list.get(idx) + inc);
	}
	
	private Path getKeyPath(String filename) {
		return informPath.relativize(Paths.get(filename));
	}
	
	private Path getStoragePath(Path keyPath, int revision) {
		return informPath.resolve(storageFolder)
		.resolve(keyPath.getParent())
		.resolve(keyPath.getFileName().toString() + "r" + revision);
	}
	
	private void addFile(Path filepath) throws IOException {
		int revision = inform.revision;
		Path keyPath = filepath.subpath(1, filepath.getNameCount());
		Path storage = getStoragePath(keyPath, revision);
		storage.getParent().toFile().mkdirs();
		Files.copy(filepath, storage);
		String keyName = keyPath.toString();
		if (!inform.fileNumber.containsKey(keyName)) {
			System.out.println(keyName);
			inform.fileNumber.put(keyName, inform.nFiles++);
		}
		
		if (inform.nCommits <= revision) {
			inform.nCommits++;
			inform.commitedFiles.add(new TreeSet<>());
			inform.prevCommit.add(inform.branchEnds.get(inform.currentBranchNumber));
			inform.branchEnds.put(inform.currentBranchNumber, revision);
			inform.numberOfStartedBranchesAtRevision.add(0);
		}

		Set<Integer> currentRevisionFiles = inform.commitedFiles.get(revision);
		assert inform.fileNumber.get(keyName) != null;
		currentRevisionFiles.add(inform.fileNumber.get(keyName));
		
	}
	
	void makeAdd(String[] filenames) throws IOException, UnversionedException {
		findRepInformation();
		for (String fname : filenames) {
			Path orig = getKeyPath(fname);
			Path dest = informPath.resolve(stageFolder).resolve(orig);
			FileUtils.copyFile(orig.toFile(), dest.toFile());
		}
	}
	
	void makeCommit(String message) throws IOException, UnversionedException, BranchProblemException {
		findRepInformation();
		if (inform.revision != inform.currentBranchLastRevision()) {
			throw new BranchProblemException("Staying not at end of some branch");
		}
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
			
			if (inform.fileNumber.containsKey(key)) {
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
	
	private void restoreVersionedFiles(int revision) throws IOException {
		Set<Integer> restoredNumbers = new TreeSet<>();
		while (revision >= 0) {
			Set<Integer> revFiles = inform.commitedFiles.get(revision);
			for (Integer fileNumber : revFiles) {
				if (restoredNumbers.contains(fileNumber)) {
					continue;
				}
				
				restoredNumbers.add(fileNumber);
				
				String keyName = inform.getStringByNumber(fileNumber, inform.fileNumber);
				FileUtils.copyFile(
						informPath.resolve(getStoragePath(Paths.get(keyName), revision)).toFile(),
						informPath.resolve(keyName).toFile());
				
			}
			revision = inform.prevCommit.get(revision);
		}
	}
	
	void makeCheckout(int revision) throws IOException, UnversionedException {
		revision--;
		findRepInformation();
		deleteVersionedFiles(informPath.toAbsolutePath().toFile());
		restoreVersionedFiles(revision);
		inform.revision = revision;
		updateRepInformation();
	}
	
	void makeCheckout(String branchName) throws JsonParseException, JsonMappingException, IOException, UnversionedException, BranchProblemException {
		findRepInformation();
		Integer branchNumber = inform.branchNumbers.get(branchName);
		if (branchNumber == null) {
			throw new BranchProblemException("Branch not exists: " + branchName);
		}

		int branchRevision = inform.branchEnds.get(branchNumber);
		makeCheckout(branchRevision);
	}
	
	private int getLastRevisionOfFile(String keyName) {
		int revision = inform.currentBranchLastRevision();
		while (revision > 0 && !inform.commitedFiles.get(revision).contains(inform.fileNumber.get(keyName))) {
			revision = inform.prevCommit.get(revision);
		}
		return revision;
	}

	private void checkoutFile(Path keyPath) throws IOException {
		int revision = getLastRevisionOfFile(keyPath.toString());
		if (revision == -1) {
			throw new FileNotFoundException();
		}
		FileUtils.copyFile(getStoragePath(keyPath, revision).toFile(),
				informPath.resolve(keyPath).toFile());
	}

	void makeCheckout(String[] files) throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		findRepInformation();
		for (String fname : files) {
			System.out.println("checkout key: " + fname);
			checkoutFile(getKeyPath(fname));
		}
	}
	
	void makeReset(int revision) throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		makeCheckout(revision);
		FileUtils.cleanDirectory(informPath.resolve(stageFolder).toFile());
	}
	
	String getLog(int revision) throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		revision--;
		findRepInformation();
		if (revision == -1) {
			revision = inform.revision;
		}
		if(revision == 0) {
			return "Empty log";
		}
		return 
				"branch " + inform.getCurrentBranchName()
				+"revision: " + revision + "\n"
				+ inform.commitMessages.get(revision - 1) + "\n"
				+ inform.timestamps.get(revision - 1);
	}
	
	List<String> getDeletedFiles() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		ArrayList<String> result = new ArrayList<>();
		findRepInformation();
		
		for (String keypath : inform.fileNumber.keySet()) {
			if (!Files.exists(informPath.resolve(keypath))) {
				result.add(Paths.get("").relativize(Paths.get(keypath)).toString());
			}
		}

		return result;
	}

	
	List<String> getChangedFiles() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		ArrayList<String> result = new ArrayList<>();
		findRepInformation();
		
		for (String keyName : inform.fileNumber.keySet()) {
			if (Files.exists(informPath.resolve(keyName))
					&& !FileUtils.contentEquals(
							informPath.resolve(keyName).toFile(),
							getStoragePath(Paths.get(keyName), getLastRevisionOfFile(keyName)
									).toFile()
							)) {
				result.add(Paths.get("").relativize(Paths.get(keyName)).toString());
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
				if (!inform.fileNumber.containsKey(informPath.relativize(file).toString())) {
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
		Path keypath = getKeyPath(filename);
		System.out.println("keypath: " + keypath);
		for (int revision = inform.currentBranchLastRevision();
				revision >= 0;
				revision = inform.prevCommit.get(revision)) {
			if (inform.commitedFiles.get(revision).remove(inform.fileNumber.get(keypath.toString()))) {
				System.out.println("keypath: " + keypath.toString());
				Files.delete(getStoragePath(keypath, revision));
				break;
			}
		}
		Files.deleteIfExists(informPath.resolve(stageFolder).resolve(keypath));
		//inform.fileNumber.remove(keypath.toString());
	}
	
	void makeRM(String[] files) throws IOException, UnversionedException {
		findRepInformation();
		for (String filename : files) {
			removeFromRep(filename);
		}
		updateRepInformation();
	}
	
	void makeBranch(String branchName) throws JsonParseException, JsonMappingException, IOException, UnversionedException, BranchProblemException {
		findRepInformation();
		if (inform.branchNumbers.containsKey(branchName)) {
			throw new BranchProblemException("Branch already exists: " + branchName);
		}
		
		Integer newBranchNumber = inform.lastBranchNumber++;
		inform.branchNumbers.put(branchName, newBranchNumber);
		inform.branchEnds.put(newBranchNumber, inform.revision);
		addAtIdx(inform.numberOfStartedBranchesAtRevision, inform.revision, 1);
		updateRepInformation();
	}
	
	void makeDeleteBranch(String branchName) throws JsonParseException, JsonMappingException, IOException, UnversionedException, BranchProblemException {
		findRepInformation();
		Integer branchNumber = inform.branchNumbers.get(branchName);
		if (branchNumber == null) {
			throw new BranchProblemException("No such branch: " + branchName);
		}
		
		int revision = inform.branchEnds.get(branchNumber);
		inform.branchEnds.remove(branchNumber);
		
		while (revision >= 0 && inform.numberOfStartedBranchesAtRevision.get(revision) == 0) {
			for (Integer fileNumber : inform.commitedFiles.get(revision)) {
				FileUtils.forceDelete(
						getStoragePath(
								Paths.get(inform.getStringByNumber(fileNumber, inform.fileNumber)), revision)
						.toFile());
			}
			inform.commitedFiles.get(revision).clear();
			revision = inform.prevCommit.get(revision);
		}
		
		if (revision >= 0) {
			addAtIdx(inform.numberOfStartedBranchesAtRevision, revision, -1);
		}
		
		updateRepInformation();
	}
}
