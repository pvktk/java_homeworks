package server_test.server.type_nonblocking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
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

		this.pool = pool;
		this.transmitter = transmitter;
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

			int mark = bb.position();
			bb.position(0);
			int messageSize = bb.getInt();
			bb.position(mark);

			if (messageSize > 10000) {
				System.out.println("bad message size " + messageSize);
			}

			if (bb.position() - Integer.BYTES >= messageSize) {
				bb.flip();

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
