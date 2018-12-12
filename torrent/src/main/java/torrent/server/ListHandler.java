package torrent.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import torrent.common.AbstractConcreteTaskHandler;
import torrent.common.FileInformation;
import torrent.common.ServerRequestHandler.MessageProcessStatus;
import torrent.common.StorageManager;

public class ListHandler extends AbstractConcreteTaskHandler<ServerData> {

	public ListHandler(StorageManager<ServerData> stm) {
		super(stm);
	}

	@Override
	public MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf){
		storage.lock.readLock().lock();
		try {
		out.writeInt(storage.data.map.size());
		
		for(FileInformation d : storage.data.map.values()) {
			out.writeInt(d.id);
			out.writeUTF(d.name);
			out.writeLong(d.size);
		}
		} catch (IOException e) {
			return MessageProcessStatus.ERROR;
		} finally {
			storage.lock.readLock().unlock();
		}

		return MessageProcessStatus.SUCCESS;
	}

}
