package torrent.server;

public class OldClientsCleaner implements Runnable {

	public final long updateTime;// = 5 * 60 * 1000;

	private StorageManager storage;

	public OldClientsCleaner(StorageManager storage, long updateTimeMillis) {
		this.storage = storage;
		updateTime = updateTimeMillis;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(updateTime);
			} catch (InterruptedException e) {
				return;
			}
			
			System.out.println("Cleaner loop");
			
			long currTime = System.currentTimeMillis();

			storage.lastClientUpdate
			.entrySet()
			.removeIf(ent -> currTime - ent.getValue() > updateTime);

			storage.clients.values().forEach(set ->
			set.removeIf(s -> {
				boolean res = !storage.lastClientUpdate.containsKey(s);
				if (res) {
					System.out.println("ServerCleaner: removing " + s);
				}
				return res;
				}));

			try {
				storage.save();
			} catch (Exception e) {
				System.out.println("Cleaner: error while saving. " + e.getMessage());
			}

		}
	}

}
