package server_test.server.type_nonblocking;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import server_test.server.StatisticsHolder;

public abstract class AbstractRecieverTransmitter implements Runnable {

	private final Selector selector = Selector.open();
	
	private final ReadWriteLock selectorMonitor = new ReentrantReadWriteLock();
	
	protected final StatisticsHolder statHolder;
	
	public AbstractRecieverTransmitter(StatisticsHolder statHolder) throws IOException {
		this.statHolder = statHolder;
	}

	abstract int interestOps();

	public void addClient(SocketChannel sch, Object attach) throws IOException {
		sch.configureBlocking(false);

		try {
			selectorMonitor.readLock().lock();
			selector.wakeup();
			sch.register(selector, interestOps(), attach);
		} finally {
			selectorMonitor.readLock().unlock();
		}

	}

	@Override
	public void run() {
		try {
			while (true) {
				
				selectorMonitor.writeLock().lock();
				selectorMonitor.writeLock().unlock();
				
				if (statHolder.numberClosedClients.compareAndSet(
						statHolder.expectedNumberClients,
						statHolder.expectedNumberClients)) {
					return;
				}
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

				while (iter.hasNext()) {
					SelectionKey key = iter.next();

					workProcedure(key);

					iter.remove();
				}
			}
		} catch (IOException e1) {
		} finally {
			close();
		}
	}

	abstract void workProcedure(SelectionKey key);

	public void close() {
		try {
			selector.close();
		} catch (IOException e) {}
	}

	protected void closeClient(SelectionKey key) {
		key.cancel();
		try {
			key.channel().close();
		} catch (IOException e) {}
		AtomicBoolean isClientClosed = ((Attachment) key.attachment()).isClientClosed;
		if (isClientClosed.compareAndSet(false, true)) {
			statHolder.currentNumberClients.decrementAndGet();
			statHolder.numberClosedClients.incrementAndGet();
		}
	}
}
