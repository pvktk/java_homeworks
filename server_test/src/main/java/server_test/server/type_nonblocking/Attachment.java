package server_test.server.type_nonblocking;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Attachment {
	public volatile ByteBuffer inputBuffer, sizeBuffer, outputBuffer;
	public final AtomicBoolean isClientClosed = new AtomicBoolean();
	public volatile long arrayProcessStart, sortTime;
	public volatile boolean arrayReceiveStarted = false;
	public volatile boolean isAllClientsWorkedAtMeasure;

	public Attachment(int recommendedBufferSize) {
		inputBuffer = ByteBuffer.allocate(recommendedBufferSize);
		sizeBuffer = ByteBuffer.allocate(Integer.BYTES);
	}

	public void reset() {
		arrayReceiveStarted = false;
		isAllClientsWorkedAtMeasure = false;
	}

}
