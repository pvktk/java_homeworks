package torrent.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import torrent.server.ServerData;

public class StorageManager {

	private final Path savePath;
	private final ObjectMapper mapper = new ObjectMapper();

	//public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public volatile ServerData data;

	public StorageManager(String savePath) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		this.savePath = Paths.get(savePath);
		try {
			load();
		} catch (Exception e) {
			data = new ServerData();
		}
	}

	public void save() throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(savePath.toFile(), data);
	}

	public void load() throws JsonParseException, JsonMappingException, IOException {
		data = mapper.readValue(savePath.toFile(), ServerData.class);
	}
}
