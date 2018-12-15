package torrent.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;

import torrent.common.ConcreteTaskHandler;
import torrent.common.ServerRequestHandler.MessageProcessStatus;

public class GetHandler implements ConcreteTaskHandler {
	
	private final FilesHolder fHolder;
	
	GetHandler(FilesHolder fHolder) {
		this.fHolder = fHolder;
	}

	@Override
	public MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf) {
		try {
			int id = in.readInt();
			int partNum = in.readInt();
			
			if (partNum >= fHolder.numParts(id)) {
				return MessageProcessStatus.ERROR;
			}

			out.write(
					fHolder.files.get(id),
					fHolder.pieceOffset(id, partNum),
					fHolder.pieceLenght(id, partNum));
			
			return MessageProcessStatus.SUCCESS;
		} catch (EOFException e) {
			return MessageProcessStatus.INCOMPLETE;
		} catch (IOException e) {
			return MessageProcessStatus.ERROR;
		}
	}

}
