package torrent.common;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileInformation {
	public int id;
	public String name;
	public long size;
	//public Set<InetSocketAddress> clients;
	
	public FileInformation() {};
	public FileInformation(int id2, String name2, long size2) {
		id = id2;
		name = name2;
		size = size2;
		//clients = new HashSet<>();
		//clients.addAll(clients2);
	}
}
