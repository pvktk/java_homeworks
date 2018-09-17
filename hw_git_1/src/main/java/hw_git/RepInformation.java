package hw_git;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class RepInformation {
	int revision = 0;
	ArrayList<String> commitMessages = new ArrayList<>();
	ArrayList<Timestamp> timestamps = new ArrayList<>();
	Map<String, Integer> allFiles = new TreeMap<>();
	
	public int getRevision() {
		return revision;
	}
	public void setRevision(int revision) {
		this.revision = revision;
	}
	public List<String> getCommitMessages() {
		return commitMessages;
	}
	public void setCommitMessages(ArrayList<String> commitMessages) {
		this.commitMessages = commitMessages;
	}
	public List<Timestamp> getTimestamps() {
		return timestamps;
	}
	public void setTimestamps(ArrayList<Timestamp> timestamps) {
		this.timestamps = timestamps;
	}
	public Map<String, Integer> getAllFiles() {
		return allFiles;
	}
	public void setAllFiles(Map<String, Integer> allFiles) {
		this.allFiles = allFiles;
	}
}
