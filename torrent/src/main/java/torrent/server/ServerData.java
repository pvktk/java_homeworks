package torrent.server;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import torrent.common.FileInformation;

public class ServerData {
	public int filesCount = 0;
	public Map<Integer, FileInformation> map = new ConcurrentHashMap<>();
	public Map<InetSocketAddress, Long> lastClientUpdate = new ConcurrentHashMap<>();
}
