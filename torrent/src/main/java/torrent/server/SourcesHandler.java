package torrent.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Set;

import torrent.common.AbstractConcreteTaskHandler;
import torrent.common.ServerRequestHandler.MessageProcessStatus;
import torrent.common.StorageManager;

public class SourcesHandler extends AbstractConcreteTaskHandler<ServerData> {

	public SourcesHandler(StorageManager<ServerData> stm) {
		super(stm);
	}

	@Override
	public MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf) {
		storage.lock.readLock().lock();
		try {
			int id = in.readInt();

			if (!storage.data.map.containsKey(id)) {
				return MessageProcessStatus.ERROR;
			}
			
			Set<InetSocketAddress> clients = storage.data.map.get(id).clients;

			out.writeInt(clients.size());
			
			for (InetSocketAddress addr : storage.data.map.get(id).clients) {
				out.write(addr.getAddress().getAddress());
				out.writeShort(addr.getPort());
			}

		} catch (EOFException e){
			return MessageProcessStatus.INCOMPLETE;
		} catch (IOException e){
			return MessageProcessStatus.ERROR;
		} finally {
			storage.lock.readLock().unlock();
		}
		
		return MessageProcessStatus.SUCCESS;
	}

}
