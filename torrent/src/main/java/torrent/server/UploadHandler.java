package torrent.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

import torrent.common.FileInformation;
import torrent.common.ServerRequestHandler.MessageProcessStatus;

public class UploadHandler extends AbstractServerTaskHandler {

	public UploadHandler(StorageManager stm) {
		super(stm);
	}

	@Override
	public MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf) {
		try {
			String name = in.readUTF();
			long size = in.readLong();
			
			int id = storage.data.filesCount++;
			storage.data.map.put(id, new FileInformation(id, name, size, Arrays.asList(clientInf)));
			storage.save();
			out.writeInt(id);
		} catch (EOFException e) {
			return MessageProcessStatus.INCOMPLETE;
		} catch (IOException e){
			return MessageProcessStatus.ERROR;
		}
		
		return MessageProcessStatus.SUCCESS;
	}

}
