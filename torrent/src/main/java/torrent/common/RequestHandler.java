package torrent.common;

import java.nio.ByteBuffer;

public interface RequestHandler {
	ByteBuffer getReceivingBuffer();
	boolean inputMessageComplete();
	ByteBuffer getTransmittingBuffer();
	boolean allOutputSent();
}
