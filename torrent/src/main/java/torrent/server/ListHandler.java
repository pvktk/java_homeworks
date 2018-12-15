package torrent.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import torrent.common.FileInformation;
import torrent.common.ServerRequestHandler.MessageProcessStatus;

public class ListHandler extends AbstractServerTaskHandler {

	public ListHandler(StorageManager stm) {
		super(stm);
	}

	@Override
	public MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf){
		try {
			out.writeInt(storage.data.map.size());

			for(FileInformation d : storage.data.map.values()) {
				out.writeInt(d.id);
				out.writeUTF(d.name);
				out.writeLong(d.size);
			}
		} catch (IOException e) {
			return MessageProcessStatus.ERROR;
		}

		return MessageProcessStatus.SUCCESS;
	}

}
