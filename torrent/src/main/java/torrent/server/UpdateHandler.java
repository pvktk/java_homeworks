package torrent.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;

import torrent.common.AbstractConcreteTaskHandler;
import torrent.common.FileInformation;
import torrent.common.ServerRequestHandler.MessageProcessStatus;
import torrent.common.StorageManager;

public class UpdateHandler extends AbstractConcreteTaskHandler<ServerData> {

	public UpdateHandler(StorageManager<ServerData> stm) {
		super(stm);
	}

	@Override
	public MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf) {
		storage.lock.writeLock().lock();
		try {
			short clientPort = in.readShort();
			int count = in.readInt();
			
			for (int i = 0; i < count; i++) {
				int id = in.readInt();
				
				InetSocketAddress clientAddr = new InetSocketAddress(clientInf.getAddress(), clientPort);
				FileInformation fInf = storage.data.map.get(id);

				if (fInf == null) {
					return MessageProcessStatus.ERROR;
				}

				fInf.clients.add(clientAddr);
				storage.data.lastClientUpdate.put(clientAddr, System.currentTimeMillis());
			}
			storage.save();
			out.writeBoolean(true);
		} catch (EOFException e){
			return MessageProcessStatus.INCOMPLETE;
		} catch (IOException e) {
			try {
				out.writeBoolean(false);
			} catch (IOException e1) {}
			return MessageProcessStatus.ERROR;
		} finally {
			storage.lock.writeLock().unlock();
		}
		
		return MessageProcessStatus.SUCCESS;
	}

}
