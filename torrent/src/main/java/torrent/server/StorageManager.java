package torrent.server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.databind.ObjectMapper;

import torrent.server.ServerData;

public class StorageManager {

	private final Path savePath;
	private final ObjectMapper mapper = new ObjectMapper();

	//public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public volatile ServerData data = new ServerData();

	public StorageManager(String savePath) {
		this.savePath = Paths.get(savePath);
		try {
			load();
		} catch (Exception e) {
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
