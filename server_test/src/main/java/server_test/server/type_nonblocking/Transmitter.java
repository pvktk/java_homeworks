package server_test.server.type_nonblocking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import server_test.server.StatisticsHolder;

public class Transmitter extends AbstractRecieverTransmitter {

	public Transmitter(StatisticsHolder statHolder) throws IOException {
		super(statHolder);
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				
				selectorMonitor.writeLock().lock();
				//selectorMonitor.writeLock().unlock();
				
				if (statHolder.numberClosedClients.get() == 
						statHolder.expectedNumberClients) {
					return;
				}

				System.out.println("before select in transmitter. size " + selector.keys().size()
						+ " closedClients " + statHolder.numberClosedClients.get()
						+ " expClients " + statHolder.expectedNumberClients);
				selector.keys().forEach(k -> {
					System.out.println(((Attachment) k.attachment()).inputBuffer.remaining()
			+ " " + ((Attachment) k.attachment()).outputBuffer.remaining()
			+ " " + ((Attachment) k.attachment()).outputBuffer.position()
			+ " " + k.isValid()
							);
				});
				selectorMonitor.writeLock().unlock();

				selector.select();
				System.out.println("selected");
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					workProcedure(key);

					iter.remove();
				}
			}
		} catch (IOException e1) {
		} finally {
			System.out.println("Closing transmitter");
			close();
		}
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
			long processTime = System.currentTimeMillis() - attach.arrayProcessStart;
			key.cancel();
			if (attach.isAllClientsWorkedAtMeasure && statHolder.isAllClientsConnected()) {
				statHolder.numberOfArrays.incrementAndGet();
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
