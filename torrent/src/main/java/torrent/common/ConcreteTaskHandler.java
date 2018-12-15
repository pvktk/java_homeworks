package torrent.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import torrent.common.ServerRequestHandler.MessageProcessStatus;

public interface ConcreteTaskHandler {
	MessageProcessStatus computeResult(DataInputStream in, DataOutputStream out, InetSocketAddress clientInf);
}
