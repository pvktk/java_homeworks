package torrent.server;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import torrent.common.FileInformation;

public class ServerData {
	public int filesCount = 0;
	public Map<Integer, FileInformation> map = new HashMap<>();
	public Map<InetSocketAddress, Long> lastClientUpdate = new HashMap<>();
}
