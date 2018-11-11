package hw_git;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GitCore {
	private RepInformation inform = null;
	private Path informPath = null;
	private final String infoFileName = ".myGitDataFile";
	private final String storageFolder = ".myGitDataStorage";
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
		}
	}
	
	void addAtIdx(ArrayList<Integer> list, int idx, int inc) {
		list.set(idx, list.get(idx) + inc);
	}
	
	private Path getKeyPath(String filename) {
		return informPath.relativize(Paths.get(filename));
	}
	
	private Path getStoragePath(Path keyPath, int revision) {
		return informPath.resolve(storageFolder)
		.resolve(keyPath.toString() + "r" + revision);
	}
	
	private Path cutRoot(Path p) {
		return p.subpath(1, p.getNameCount());
	}

	private void addFileDuringCommit(Path filepath) throws IOException {
		int revision = inform.revision;
		Path keyPath = cutRoot(filepath);
		Path storage = getStoragePath(keyPath, revision);
		storage.getParent().toFile().mkdirs();
		Files.copy(filepath, storage);
		String keyName = keyPath.toString();
		if (!inform.fileNumber.containsKey(keyName)) {
			System.out.println(keyName);
			inform.fileNumber.put(keyName, inform.nFiles++);
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

		inform.revision = inform.nCommits;
		inform.commitMessages.add(message);
		inform.timestamps.add(new Timestamp(System.currentTimeMillis()));
		
		{
			inform.nCommits++;
			inform.commitedFiles.add(new TreeSet<>());
			inform.removedFiles.add(new TreeSet<>());
			inform.prevCommit.add(inform.branchEnds.get(inform.currentBranchNumber));
			inform.branchEnds.put(inform.currentBranchNumber, inform.revision);
			inform.numberOfStartedBranchesAtRevision.add(0);
		}
		
		inform.removedFiles.get(inform.revision).addAll(inform.stageRemovedFiles);
		inform.stageRemovedFiles.clear();
		
		Files.walkFileTree(
				informPath.resolve(stageFolder),
				new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				addFileDuringCommit(file);
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		
		updateRepInformation();
		
		
		if (inform.currentBranchNumber == -1) {
			throw new BranchProblemException("Staying not at end of some branch.");
		}
		
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
	
	private void collectVersionedFiles(int revision, Map<Integer, Integer> filesToRestore) throws IOException {
		if (revision < 0) {
			return;
		}
		
		collectVersionedFiles(inform.prevCommit.get(revision), filesToRestore);
		
		for (Integer fileNumber : inform.commitedFiles.get(revision)) {
			filesToRestore.put(fileNumber, revision);
		}
		
		for (Integer fileNumber : inform.removedFiles.get(revision)) {
			filesToRestore.remove(fileNumber);
		}
		
	}
	
	private void restoreFile(Integer fileNumber, Integer revision) throws IOException {
		String keyName = RepInformation.getKeyByValue(fileNumber, inform.fileNumber);
		FileUtils.copyFile(
				informPath.resolve(getStoragePath(Paths.get(keyName), revision)).toFile(),
				informPath.resolve(keyName).toFile()
		);
	}

	void makeCheckout(int revision) throws IOException, UnversionedException, BranchProblemException {
		findRepInformation();
		if (revision >= inform.nCommits || revision < 0) {
			throw new BranchProblemException("revision number" + (revision + 1) + " is incorrect");
		}

		deleteVersionedFiles(informPath.toAbsolutePath().toFile());

		Map<Integer, Integer> filesToRestore = new TreeMap<>();
		collectVersionedFiles(revision, filesToRestore);
		
		for (Entry<Integer, Integer> fileEntry : filesToRestore.entrySet()) {
			restoreFile(fileEntry.getKey(), fileEntry.getValue());
		}
		
		inform.revision = revision;
		inform.detachedHeadRevision = revision;
		inform.currentBranchNumber = -1;
		
		updateRepInformation();
	}
	
	void makeCheckout(String branchName) throws JsonParseException, JsonMappingException, IOException, UnversionedException, BranchProblemException {
		findRepInformation();
		Integer branchNumber = inform.branchNumbers.get(branchName);
		if (branchNumber == null) {
			throw new BranchProblemException("Branch not exists: " + branchName);
		}

		int branchRevision = inform.branchEnds.get(branchNumber);
		
		try {
			makeCheckout(branchRevision);
		} catch (BranchProblemException e) {}
		
		inform.currentBranchNumber = branchNumber;
		updateRepInformation();
	}
	
	private int getLastRevisionOfFile(String keyName) {
		int revision = inform.revision;
		while (revision >= 0 && !inform.commitedFiles.get(revision)
				.contains(inform.fileNumber.get(keyName))) {
			revision = inform.prevCommit.get(revision);
		}
		return revision;
	}

	private void checkoutFile(Path keyPath) throws IOException {
		int revision = getLastRevisionOfFile(keyPath.toString());
		if (revision == -1) {
			throw new FileNotFoundException("No file " + keyPath.toString() + " found");
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
	
	void makeReset(int revision) throws JsonParseException, JsonMappingException, IOException, UnversionedException, BranchProblemException {
		findRepInformation();

		int currBranchT = inform.currentBranchNumber;
		int currDetachedPosT = inform.detachedHeadRevision;
		makeCheckout(revision);
		
		FileUtils.cleanDirectory(Paths.get(stageFolder).toFile());
		inform.stageRemovedFiles.clear();
		
		if (currBranchT != -1) {
			inform.currentBranchNumber = currBranchT;
			inform.branchEnds.put(currBranchT, revision);
		}
		inform.detachedHeadRevision = currDetachedPosT;
		
		updateRepInformation();
	}
	
	ArrayList<String> getLog(int revision) throws JsonParseException, JsonMappingException, IOException, UnversionedException, BranchProblemException {
		findRepInformation();
		
		ArrayList<String> result = new ArrayList<>();
		
		if (revision < -1 || revision >= inform.nCommits) {
			throw new BranchProblemException("revision number " + (revision + 1) + " is incorrect");
		}
		
		if (revision == -1) {
			revision = inform.revision;
		}

		if(revision == -1) {
			result.add("Empty log");
			return result;
		}

		while (revision >= 0) {
			result.add(
				"\nrevision: " + (revision + 1) + "\n"
				+ inform.commitMessages.get(revision)
				);
			result.add(inform.timestamps.get(revision) + "\n");

			revision = inform.prevCommit.get(revision);
		}

		return result;
	}
	
	List<String> getDeletedFiles() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		findRepInformation();

		return inform.stageRemovedFiles.stream()
				.map(i -> RepInformation.getKeyByValue(i, inform.fileNumber))
				.collect(Collectors.toList());
	}

	
	List<String> getChangedFiles() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		ArrayList<String> result = new ArrayList<>();
		findRepInformation();
		
		int revision = inform.revision;
		TreeMap<Integer, Integer> filesToRestore = new TreeMap<>();
		collectVersionedFiles(revision, filesToRestore);
		for (Entry<Integer, Integer> fileEnt : filesToRestore.entrySet()) {
			String keyName = RepInformation.getKeyByValue(
					fileEnt.getKey(), inform.fileNumber);
			if (!FileUtils.contentEquals(
					informPath.resolve(keyName).toFile(),
					getStoragePath(Paths.get(keyName), fileEnt.getValue()).toFile()))
			{
				result.add(keyName);
			}
		}
		return result;
	}
	
	List<String> getStagedFiles() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		ArrayList<String> result = new ArrayList<>();
		Files.walkFileTree(informPath.resolve(stageFolder), new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				result.add(cutRoot(file).toString());
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});

		return result;
	}
	
	List<String> getUntrackedFiles() throws IOException {
		ArrayList<String> result = new ArrayList<>();
		Files.walkFileTree(Paths.get(""), new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return dir.equals(Paths.get(stageFolder)) || dir.equals(Paths.get(storageFolder))
						? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (!inform.fileNumber.containsKey(informPath.relativize(file).toString())) {
					result.add(file.toString());
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
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
		
		Integer fileNumber = inform.fileNumber.get(keypath.toString());
		
		if (fileNumber == null) {
			throw new FileNotFoundException("File not versioned: " + filename);
		}

		inform.stageRemovedFiles.add(fileNumber);
		
		Files.deleteIfExists(informPath.resolve(stageFolder).resolve(keypath));
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
		
		Integer newBranchNumber = inform.nBranches++;
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

		if (branchNumber.equals(inform.currentBranchNumber)) {
			throw new BranchProblemException("You can't delete this branch while staying on it.");
		}

		inform.branchEnds.remove(branchNumber);
		
		inform.branchNumbers.remove(branchName);
		
		updateRepInformation();
	}
	
	private int findLCA(int u, int v) {
		int ulen = 0, vlen = 0;
		for (int u1 = u; u1 >= 0; u1 = inform.prevCommit.get(u1), ulen++);
		for (int v1 = v; v1 >= 0; v1 = inform.prevCommit.get(v1), vlen++);
		
		for (;ulen > vlen; ulen--, u = inform.prevCommit.get(u));
		for (;vlen > ulen; vlen--, v = inform.prevCommit.get(v));
		
		for(; u != v;
				u = inform.prevCommit.get(u),
				v = inform.prevCommit.get(v));
		return u;
	}
	
	ArrayList<String> makeMerge(String otherBranchName) throws JsonParseException, JsonMappingException, IOException, UnversionedException, BranchProblemException {
		findRepInformation();
		
		Integer otherBranchNumber = inform.branchNumbers.get(otherBranchName);
		if (otherBranchNumber == null) {
			throw new BranchProblemException("No branch with name " + otherBranchName + " found.");
		}
		
		ArrayList<String> res = new ArrayList<>();
		res.add("Please, resolve conflicts in these files, and \"add\" them to commit:");
		
		int currRev = inform.revision;
		int otherRev = inform.branchEnds.get(otherBranchNumber);
		
		//fileNumber, revision
		Map<Integer, Integer>
			filesToRestoreCurrent = new TreeMap<>(),
			filesToRestoreOther = new TreeMap<>();
		
		collectVersionedFiles(currRev, filesToRestoreCurrent);
		collectVersionedFiles(otherRev, filesToRestoreOther);
		
		Integer lca = findLCA(currRev, otherRev);
		
		for (Entry<Integer, Integer> fileEntry : filesToRestoreCurrent.entrySet()) {
			Integer thisEntRevision = fileEntry.getValue();
			Integer otherEntRevision = filesToRestoreOther.get(fileEntry.getKey());
			Integer entFileNum = fileEntry.getKey();
			String keyName = RepInformation.getKeyByValue(entFileNum, inform.fileNumber);
			
			if (otherEntRevision == null
					|| otherEntRevision <= lca) {
			} else if (thisEntRevision <= lca && otherEntRevision > lca) {
				restoreFile(entFileNum, otherEntRevision);
				makeAdd(new String[] {keyName});
			} else {
				
				File fCurr = getStoragePath(Paths.get(keyName), thisEntRevision).toFile();
				File fOth = getStoragePath(Paths.get(keyName), otherEntRevision).toFile();
				
				try (PrintWriter out = new PrintWriter(new File(keyName))) {
					out.println("===============Content from revision " + (thisEntRevision + 1) + " ========");
					try (BufferedReader in = new BufferedReader(new FileReader(fCurr))) {
						in.lines().forEach(l -> out.println(l));
					}
					out.println("===============Content from revision " + (otherEntRevision + 1) + " ========");
					try (BufferedReader in = new BufferedReader(new FileReader(fOth))) {
						in.lines().forEach(l -> out.println(l));
					}
				}
				
				res.add(keyName);
				
			} 
		}
		
		for (Entry<Integer, Integer> fileEntry : filesToRestoreOther.entrySet()) {
			if (!filesToRestoreCurrent.containsKey(fileEntry.getKey())) {
				restoreFile(fileEntry.getKey(), fileEntry.getValue());
				String keyName = RepInformation.getKeyByValue(
						fileEntry.getKey(), inform.fileNumber);
				makeAdd(new String[] {keyName});
			}
		}
		
		updateRepInformation();
		
		if (res.size() == 1)
			res.clear();
		return res;
	}
	
	String getCurrentBranchName() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		findRepInformation();
		return inform.getCurrentBranchName();
	}

	public ArrayList<String> getStatus() throws JsonParseException, JsonMappingException, IOException, UnversionedException {
		ArrayList<String> result = new ArrayList<>();
		
		findRepInformation();
		result.add("Status:");
		if (inform.currentBranchNumber != -1) {
			result.add("branch " + inform.getCurrentBranchName());
		} else {
			result.add("detached head at revision " + (inform.detachedHeadRevision + 1));
		}
		
		result.add("Staged files:\n________________");
		result.addAll(getStagedFiles());
		
		result.add("Deleted files:\n________________");
		result.addAll(getDeletedFiles());
		
		result.add("Changed files:\n________________");
		result.addAll(getChangedFiles());
		
		result.add("Untracked files:\n________________");
		result.addAll(getUntrackedFiles());
		
		return result;
	}
}
