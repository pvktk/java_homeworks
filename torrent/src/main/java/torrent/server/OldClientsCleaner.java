package torrent.server;

import torrent.common.StorageManager;

public class OldClientsCleaner implements Runnable {
	
	public final long updateTime = 5 * 60 * 1000;
	
	private StorageManager<ServerData> storage;
	
	public OldClientsCleaner(StorageManager<ServerData> storage) {
		this.storage = storage;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(updateTime);
			} catch (InterruptedException e) {
				return;
			}
			
			storage.lock.writeLock().lock();
			long currTime = System.currentTimeMillis();
			
			storage.data.lastClientUpdate
			.entrySet()
			.removeIf(ent -> currTime - ent.getValue() > updateTime);
			
			storage.data.map.values().stream().forEach(fInf ->
				fInf.clients.removeIf(s -> !storage.data.lastClientUpdate.containsKey(s)));
			
			try {
				storage.save();
			} catch (Exception e) {
				System.out.println("Cleaner: error while saving. " + e.getMessage());
			}
			
			storage.lock.writeLock().unlock();
		}
	}

}
