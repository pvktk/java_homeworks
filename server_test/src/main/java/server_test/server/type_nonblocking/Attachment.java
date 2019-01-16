package server_test.server.type_nonblocking;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Attachment {
	public volatile ByteBuffer inputBuffer, outputBuffer;
	public final AtomicBoolean isClientClosed = new AtomicBoolean();
	public volatile long arrayProcessStart, sortTime;
	public volatile boolean arrayReceiveStarted = false;
	public volatile boolean isAllClientsWorkedAtMeasure;
	
	public final int channelNum;
	
	public Attachment(int recommendedBufferSize, int channelNum) {
		inputBuffer = ByteBuffer.allocate(recommendedBufferSize);
		this.channelNum = channelNum;
	}
	
	public void reset() {
		arrayReceiveStarted = false;
		isAllClientsWorkedAtMeasure = false;
		inputBuffer.clear();
	}
	
}
