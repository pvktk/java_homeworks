package torrent.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import torrent.common.ServerRequestHandler.MessageProcessStatus;

public class SourcesHandler extends AbstractServerTaskHandler {

	public SourcesHandler(StorageManager stm) {
		super(stm);
	}

	@Override
	public MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf) {
		try {
			int id = in.readInt();

			if (!storage.data.map.containsKey(id)) {
				out.writeInt(0);
				return MessageProcessStatus.SUCCESS;
			}
			if (storage.clients.get(id) == null) {
				storage.clients.put(id, ConcurrentHashMap.newKeySet());
			}

			Set<InetSocketAddress> clients = new HashSet<>(storage.clients.get(id));

			out.writeInt(clients.size());

			for (InetSocketAddress addr : storage.clients.get(id)) {
				out.write(addr.getAddress().getAddress());
				out.writeShort(addr.getPort());
			}

		} catch (EOFException e){
			return MessageProcessStatus.INCOMPLETE;
		} catch (IOException e){
			return MessageProcessStatus.ERROR;
		}

		return MessageProcessStatus.SUCCESS;
	}

}
