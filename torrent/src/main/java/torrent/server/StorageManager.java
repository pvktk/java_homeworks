package torrent.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import torrent.server.ServerData;

public class StorageManager {

	private final Path savePath;
	private final ObjectMapper mapper = new ObjectMapper();

	//public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public volatile ServerData data = new ServerData();
	public volatile Map<InetSocketAddress, Long> lastClientUpdate = new ConcurrentHashMap<>();
	public volatile Map<Integer, Set<InetSocketAddress>> clients = new ConcurrentHashMap<>();
	
	public StorageManager(String savePath) {
		this.savePath = Paths.get(savePath);
		try {
			load();
		} catch (Exception e) {
			e.printStackTrace();
			data = new ServerData();
		}
	}

	public void save() throws IOException {
		savePath.toFile().createNewFile();
		mapper.writeValue(savePath.toFile(), data);
	}

	public void load() throws IOException {
		if (savePath.toFile().exists())
			data = mapper.readValue(savePath.toFile(), ServerData.class);
	}
}
