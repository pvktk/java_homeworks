package server_test.server.type_nonblocking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import server_test.server.StatisticsHolder;

public class Transmitter extends AbstractRecieverTransmitter {

	public Transmitter(StatisticsHolder statHolder) throws IOException {
		super(statHolder);
	}
	
	@Override
	void workProcedure(SelectionKey key) {
		SocketChannel chan = (SocketChannel) key.channel();
		Attachment attach = (Attachment) key.attachment();
		
		ByteBuffer bb = attach.outputBuffer;

		try {
			chan.write(bb);
		} catch (IOException e) {
			closeClient(key);
			return;
		}

		if (!bb.hasRemaining()) {
			long processTime = System.nanoTime() - attach.arrayProcessStart;
			key.cancel();
			if (attach.isAllClientsWorkedAtMeasure && statHolder.isAllClientsConnected()) {
				statHolder.fullNumberOfCorrectArrays.incrementAndGet();
				statHolder.sortTimesSum.addAndGet(attach.sortTime);
				statHolder.clientTimesSum.addAndGet(processTime);
			}
			attach.reset();
		}
	}

	@Override
	int interestOps() {
		return SelectionKey.OP_WRITE;
	}
}
