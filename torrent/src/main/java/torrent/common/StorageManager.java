package torrent.common;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StorageManager<T> {
	
	private final Path savePath;
	private final ObjectMapper mapper = new ObjectMapper();
	
	public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	public volatile T data;
	private final Class<T> Tclass;
	
	public StorageManager(Class<T> Tclass, String savePath) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		this.savePath = Paths.get(savePath);
		this.Tclass = Tclass;
		try {
			load();
		} catch (Exception e) {
			data = Tclass.getConstructor().newInstance();
		}
	}
	
	public void save() throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(savePath.toFile(), data);
	}
	
	public void load() throws JsonParseException, JsonMappingException, IOException {
		data = mapper.readValue(savePath.toFile(), Tclass);
	}
}
