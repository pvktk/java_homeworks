package torrent.common;

public class FileInformation {
	public int id;
	public String name;
	public long size;
	
	public FileInformation() {};
	public FileInformation(int id2, String name2, long size2) {
		id = id2;
		name = name2;
		size = size2;
	}
}
