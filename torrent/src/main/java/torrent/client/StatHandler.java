package torrent.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

import torrent.common.ConcreteTaskHandler;
import torrent.common.ServerRequestHandler.MessageProcessStatus;

public class StatHandler implements ConcreteTaskHandler {
	
	private final FilesHolder fHolder;
	
	StatHandler(FilesHolder fHolder) {
		this.fHolder = fHolder;
	}

	@Override
	public MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf) {
		try {
			int id = in.readInt();
			
			Set<Integer> pieces = fHolder.completePieces.get(id);
			
			if (pieces == null) {
				return MessageProcessStatus.ERROR;
			}
			
			out.writeInt(pieces.size());
			
			for (int i : pieces) {
				out.write(i);
			}
			
			return MessageProcessStatus.SUCCESS;
		} catch (EOFException e) {
			return MessageProcessStatus.INCOMPLETE;
		} catch (IOException e) {
			return MessageProcessStatus.ERROR;
		}
		
	}

}
