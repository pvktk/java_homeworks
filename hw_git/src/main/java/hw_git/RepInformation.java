package hw_git;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class RepInformation {
	
	int revision = -1;
	int currentBranchNumber = 0;
	int nCommits = 0;
	int nFiles = 0;
	int lastBranchNumber = 0;

	ArrayList<String> commitMessages = new ArrayList<>();

	ArrayList<Timestamp> timestamps = new ArrayList<>();

	ArrayList<Integer> prevCommit = new ArrayList<>();

	ArrayList<Set<Integer>> commitedFiles = new ArrayList<>();
	
	ArrayList<Set<Integer>> removedFiles = new ArrayList<>();

	TreeMap<String, Integer> fileNumber = new TreeMap<>();

	TreeMap<String, Integer> branchNumbers = new TreeMap<>();
	
	TreeMap<Integer, Integer> branchEnds = new TreeMap<>();
	
	ArrayList<Integer> numberOfStartedBranchesAtRevision = new ArrayList<>();

	public RepInformation() {
		branchNumbers.put("master", 0);
		branchEnds.put(0, -1);
	}

	static <K, V> K getKeyByValue(V i, Map<K, V> map) {
		for (Entry<K, V> ent : map.entrySet()) {
			if (ent.getValue().equals(i)) {
				return ent.getKey();
			}
		}

		return null;
	}
	
	String getCurrentBranchName() {
		return getKeyByValue(currentBranchNumber, branchNumbers);
	}
	
	int currentBranchLastRevision() {
		return branchEnds.get(currentBranchNumber);
	}

// Automatic getters and setters

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public int getCurrentBranchNumber() {
		return currentBranchNumber;
	}

	public void setCurrentBranchNumber(int currentBranchNumber) {
		this.currentBranchNumber = currentBranchNumber;
	}

	public int getnCommits() {
		return nCommits;
	}

	public void setnCommits(int nCommits) {
		this.nCommits = nCommits;
	}

	public ArrayList<String> getCommitMessages() {
		return commitMessages;
	}

	public void setCommitMessages(ArrayList<String> commitMessages) {
		this.commitMessages = commitMessages;
	}

	public ArrayList<Timestamp> getTimestamps() {
		return timestamps;
	}

	public void setTimestamps(ArrayList<Timestamp> timestamps) {
		this.timestamps = timestamps;
	}

	public ArrayList<Integer> getPrevCommit() {
		return prevCommit;
	}

	public void setPrevCommit(ArrayList<Integer> prevCommit) {
		this.prevCommit = prevCommit;
	}

	public ArrayList<Set<Integer>> getCommitedFiles() {
		return commitedFiles;
	}

	public void setCommitedFiles(ArrayList<Set<Integer>> commitedFiles) {
		this.commitedFiles = commitedFiles;
	}
	
	public ArrayList<Set<Integer>> getRemovedFiles() {
		return removedFiles;
	}

	public void setRemovedFiles(ArrayList<Set<Integer>> removedFiles) {
		this.removedFiles = removedFiles;
	}

	public TreeMap<String, Integer> getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(TreeMap<String, Integer> fileNumber) {
		this.fileNumber = fileNumber;
	}

	public TreeMap<String, Integer> getBranchNumbers() {
		return branchNumbers;
	}

	public void setBranchNumbers(TreeMap<String, Integer> branchNumbers) {
		this.branchNumbers = branchNumbers;
	}

	public TreeMap<Integer, Integer> getBranchEnds() {
		return branchEnds;
	}

	public void setBranchEnds(TreeMap<Integer, Integer> branchEnds) {
		this.branchEnds = branchEnds;
	}
	
	public int getnFiles() {
		return nFiles;
	}

	public void setnFiles(int nFiles) {
		this.nFiles = nFiles;
	}
	
	public int getLastBranchNumber() {
		return lastBranchNumber;
	}

	public void setLastBranchNumber(int lastBranchNumber) {
		this.lastBranchNumber = lastBranchNumber;
	}
	
	public ArrayList<Integer> getNumberOfStartedBranchesAtRevision() {
		return numberOfStartedBranchesAtRevision;
	}

	public void setNumberOfStartedBranchesAtRevision(ArrayList<Integer> numberOfStartedBrancesAtRevision) {
		this.numberOfStartedBranchesAtRevision = numberOfStartedBrancesAtRevision;
	}
}
