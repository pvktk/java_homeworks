package torrent.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import torrent.common.FileInformation;
import torrent.common.ServerRequestHandler.MessageProcessStatus;

public class ListHandler extends AbstractServerTaskHandler {

	public ListHandler(StorageManager stm) {
		super(stm);
	}

	@Override
	public MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf){
		try {
			
			List<FileInformation> cl = new ArrayList<>(storage.data.map.values());
			
			out.writeInt(cl.size());
			
			for(FileInformation d : cl) {
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
