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
		ByteBuffer readBuffer = attach.inputBuffer;
		ByteBuffer sizeBuffer = attach.sizeBuffer;

		if (!attach.arrayReceiveStarted) {
			attach.arrayReceiveStarted = true;
			attach.arrayProcessStart = System.nanoTime();
			attach.isAllClientsWorkedAtMeasure = statHolder.isAllClientsConnected();
		}

		try {
			if (chan.read(new ByteBuffer[]{sizeBuffer, readBuffer}) < 0) {
				closeClient(key);
				return;
			}
		} catch (IOException e2) {
			closeClient(key);
			return;
		}

		if (!sizeBuffer.hasRemaining()) {
			
			sizeBuffer.flip();
			int messageSize = sizeBuffer.getInt();

			if (messageSize > readBuffer.capacity()) {
				closeClient(key);
				return;
			}

			readBuffer.limit(messageSize);

			if (!readBuffer.hasRemaining()) {
				readBuffer.flip();

				ClientMessage clMessage;
				try {
					clMessage = ClientMessage.parseFrom(readBuffer);
				} catch (InvalidProtocolBufferException e1) {
					closeClient(key);
					return;
				}
				readBuffer.clear();
				sizeBuffer.clear();
				pool.execute(() -> {

					int[] arrayToSort = clMessage.getArrayList()
							.stream()
							.mapToInt(I -> I)
							.toArray();

					long sortStart = System.nanoTime();

					QuadraticSorter.sort(arrayToSort);

					attach.sortTime = System.nanoTime() - sortStart;

					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					try {
						ClientMessage.newBuilder()
						.addAllArray(Arrays.stream(arrayToSort)
								.boxed().collect(Collectors.toList()))
						.build().writeDelimitedTo(baos);

						attach.outputBuffer = ByteBuffer.wrap(baos.toByteArray());

						transmitter.addClient(
								chan,
								attach);
					} catch (IOException e) {
						closeClient(key);
						return;
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
