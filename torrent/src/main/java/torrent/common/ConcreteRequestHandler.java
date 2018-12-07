package torrent.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface ConcreteRequestHandler {
	boolean computeResult(DataInputStream in, DataOutputStream out);
}
