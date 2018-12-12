package torrent.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import torrent.common.ServerRequestHandler.MessageProcessStatus;

public abstract class AbstractConcreteTaskHandler<T> implements ConcreteTaskHandler{
	protected final StorageManager<T> storage;
	
	public AbstractConcreteTaskHandler(StorageManager<T> stm) {
		storage = stm;
	}
	
	@Override
	public abstract MessageProcessStatus computeResult(
			DataInputStream in,
			DataOutputStream out,
			InetSocketAddress clientInf);
}
