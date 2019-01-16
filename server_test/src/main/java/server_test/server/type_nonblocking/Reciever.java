package server_test.server.type_nonblocking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.google.protobuf.InvalidProtocolBufferException;

import server_test.Messages.ClientMessage;
import server_test.server.QuadraticSorter;
import server_test.server.StatisticsHolder;

public class Reciever extends AbstractRecieverTransmitter{

	private final ExecutorService pool;
	private final Transmitter transmitter;

	public Reciever(
			ExecutorService pool,
			StatisticsHolder statHolder,
			Transmitter transmitter) throws IOException {
		super(statHolder);
		Thread wakeuper = new Thread(() ->  {
			while (true) {
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {}
				if (!selector.isOpen())
					return;
				selector.wakeup();
				System.out.println("wakeuper wakeuped");
			}
		});
		wakeuper.start();
		this.pool = pool;
		this.transmitter = transmitter;
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

				System.out.println("before select in reciever. size " + selector.keys().size()
						+ " closedClients " + statHolder.numberClosedClients.get()
						+ " expClients " + statHolder.expectedNumberClients);
				selector.keys().forEach(k -> {
					System.out.print("rec " + ((Attachment) k.attachment()).inputBuffer.position());
					ByteBuffer outputBuffer = ((Attachment) k.attachment()).outputBuffer;
					if (outputBuffer != null) {
						System.out.println(
								"rec " + ((Attachment) k.attachment()).outputBuffer.remaining()
							+	" " + ((Attachment) k.attachment()).outputBuffer.position()
								+ " " + k.isValid() + " " +(k.isValid() && k.interestOps() == SelectionKey.OP_READ)
								);
					} else {
						System.out.println("rec outputBuffer null");
					}
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
	public void workProcedure(SelectionKey key) {
		SocketChannel chan = (SocketChannel) key.channel();
		Attachment attach = (Attachment) key.attachment();
		ByteBuffer bb = attach.inputBuffer;

		if (!attach.arrayReceiveStarted) {
			attach.arrayReceiveStarted = true;
			attach.arrayProcessStart = System.currentTimeMillis();
			attach.isAllClientsWorkedAtMeasure = statHolder.isAllClientsConnected();
		}

		try {
			if (chan.read(bb) < 0) {
				closeClient(key);
				return;
			}
		} catch (IOException e2) {
			closeClient(key);
			return;
		}

		if (bb.position() >= Integer.BYTES) {
			int messageSize;
			try {
				messageSize = (new DataInputStream(new ByteArrayInputStream(bb.array()))).readInt();
				System.out.println("mes size " + messageSize);
			} catch (IOException e1) {
				closeClient(key);
				return;
			}
			System.out.println("underfull buffer " + bb.position());
			if (bb.position() - Integer.BYTES >= messageSize - 2) {
				System.out.println("good buffer pos " + bb.position());
				bb.flip();
				if (bb.limit() == 0) {
					System.out.println("messageSize " + messageSize +
							" channelNum " + attach.channelNum +
							" arrayRecStarted " + attach.arrayReceiveStarted);
				}
				bb.position(Integer.BYTES);
				ClientMessage clMessage;
				try {
					clMessage = ClientMessage.parseFrom(bb);
				} catch (InvalidProtocolBufferException e1) {
					closeClient(key);
					return;
				}
				bb.clear();
				pool.execute(() -> {
					
					try {
						

						int[] arrayToSort = clMessage.getArrayList()
								.stream()
								.mapToInt(I -> I)
								.toArray();

						long sortStart = System.currentTimeMillis();

						QuadraticSorter.sort(arrayToSort);

						attach.sortTime = System.currentTimeMillis() - sortStart;

						ByteArrayOutputStream baos = new ByteArrayOutputStream();

						ClientMessage.newBuilder()
						.addAllArray(Arrays.stream(arrayToSort)
								.boxed().collect(Collectors.toList()))
						.build().writeDelimitedTo(baos);

						attach.outputBuffer = ByteBuffer.wrap(baos.toByteArray());

						transmitter.addClient(
								chan,
								attach);

					} catch (IOException e) {
						statHolder.setMeasureFailed();
					}
				});
			}
		}
	}

	@Override
	int interestOps() {
		return SelectionKey.OP_READ;
	}
}
