package torrent.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import torrent.common.ConcreteTaskHandler;
import torrent.common.ServerRequestHandler.MessageProcessStatus;

public abstract class AbstractServerTaskHandler implements ConcreteTaskHandler{
	protected final StorageManager storage;

	public AbstractServerTaskHandler(StorageManager storage) {
		this.storage = storage;
	}

	@Override
	public abstract MessageProcessStatus computeResult(
			DataInputStream in,
			DataOutputStream out,
			InetSocketAddress clientInf);
}
