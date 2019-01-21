package server_test.server.async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.protobuf.InvalidProtocolBufferException;

import server_test.Messages.ClientMessage;
import server_test.server.QuadraticSorter;
import server_test.server.ServersManager;
import server_test.server.StatisticsHolder;
import server_test.server.TestServer;

public class Server implements TestServer {

	private final AsynchronousChannelGroup group = 
			AsynchronousChannelGroup.withFixedThreadPool(4, Executors.defaultThreadFactory());
	private final AsynchronousServerSocketChannel srv = AsynchronousServerSocketChannel
			.open(group)
			.bind(new InetSocketAddress(ServersManager.sortingPort));

	private final StatisticsHolder statHolder;

	private final ExecutorService pool = Executors.newFixedThreadPool(numPoolThreads);

	private final Object closeLock = new Object();
	private boolean shouldStartClosing = false;

	private void startAwaitingClose() {
		synchronized (closeLock) {
			shouldStartClosing = true;
			closeLock.notify();
		}
	}

	public Server(StatisticsHolder statHolder) throws IOException {
		this.statHolder = statHolder;
	}

	@Override
	public void run() {
		try {
			srv.accept(null,
					new CompletionHandler<AsynchronousSocketChannel, Object>() {

				int numberAcceptedClients;
				@Override
				public void completed(AsynchronousSocketChannel chan, Object attachment) {

					numberAcceptedClients++;
					statHolder.currentNumberClients.incrementAndGet();
					if (numberAcceptedClients < statHolder.expectedNumberClients) {
						srv.accept(null, this);
					} else {
						startAwaitingClose();
					}

					final AtomicBoolean isClientClosed = new AtomicBoolean();
					final ByteBuffer sizeBuffer = ByteBuffer.allocate(Integer.BYTES);
					final ByteBuffer readBuffer = ByteBuffer.allocate(maxMessageSize);
					final ByteBuffer[] buffers = new ByteBuffer[] {sizeBuffer, readBuffer};

					chan.read(buffers, 0, 2, Long.MAX_VALUE, TimeUnit.SECONDS,
							null, new CompletionHandler<Long, Object>() {

						void closeClient(AsynchronousSocketChannel chan) {
							try {
								chan.close();
							} catch (IOException e) {}
							if (isClientClosed.compareAndSet(false, true)) {
								statHolder.currentNumberClients.decrementAndGet();
								statHolder.numberClosedClients.incrementAndGet();
							}
						}

						long sortTime, arrayProcessStart;
						volatile boolean arrayReceiveStarted, isAllClientsWorkedAtMeasure;
						@Override
						public void completed(Long result, Object attachment) {

							if (result == -1) {
								closeClient(chan);
								return;
							}

							if (!arrayReceiveStarted) {
								arrayReceiveStarted = true;
								arrayProcessStart = System.nanoTime();
								isAllClientsWorkedAtMeasure = statHolder.isAllClientsConnected();
							}

							int messageSize;
							if (!sizeBuffer.hasRemaining()) {
								sizeBuffer.flip();
								messageSize = sizeBuffer.getInt();
							} else {
								chan.read(buffers, 0, 2, Long.MAX_VALUE, TimeUnit.SECONDS,
										null, this);
								return;
							}

							if (messageSize > readBuffer.capacity()) {
								closeClient(chan);
								return;
							}

							readBuffer.limit(messageSize);

							if (!readBuffer.hasRemaining()) {

								readBuffer.flip();

								ClientMessage clMessage = null;
								try {
									clMessage = ClientMessage.parseFrom(readBuffer);
								} catch (InvalidProtocolBufferException e1) {
									closeClient(chan);
									return;
								}
								sizeBuffer.clear();
								readBuffer.clear();
								arrayReceiveStarted = false;
								int[] arrayToSort = clMessage.getArrayList()
										.stream()
										.mapToInt(I -> I)
										.toArray();

								pool.execute(() -> {

									try {

										long sortStart = System.nanoTime();

										QuadraticSorter.sort(arrayToSort);

										sortTime = System.nanoTime() - sortStart;

										ByteArrayOutputStream baos = new ByteArrayOutputStream();

										ClientMessage.newBuilder()
										.addAllArray(Arrays.stream(arrayToSort)
												.boxed().collect(Collectors.toList()))
										.build().writeDelimitedTo(baos);

										ByteBuffer outputBuffer = ByteBuffer.wrap(baos.toByteArray());

										chan.write(outputBuffer, null, new CompletionHandler<Integer, Object>() {

											@Override
											public void completed(Integer result, Object attachment) {
												if (!outputBuffer.hasRemaining()) {
													long processTime = System.nanoTime() - arrayProcessStart;
													if (isAllClientsWorkedAtMeasure && statHolder.isAllClientsConnected()) {
														statHolder.fullNumberOfCorrectArrays.incrementAndGet();
														statHolder.sortTimesSum.addAndGet(sortTime);
														statHolder.clientTimesSum.addAndGet(processTime);
													}
												} else {
													chan.write(outputBuffer, null, this);
												}
											}

											@Override
											public void failed(Throwable exc, Object attachment) {
												closeClient(chan);
											}
										});

									} catch (IOException e) {
										closeClient(chan);
									} catch (ShutdownChannelGroupException e) {}
								});
							}

							chan.read(buffers, 0, 2, Long.MAX_VALUE, TimeUnit.SECONDS,
									null, this);
						}

						@Override
						public void failed(Throwable exc, Object attachment) {
							closeClient(chan);
						}
					});
				}

				@Override
				public void failed(Throwable exc, Object attachment) {
					startAwaitingClose();
				}
			});
		} catch (ShutdownChannelGroupException e) {}

		synchronized (closeLock) {
			while (!shouldStartClosing)
				try {
					closeLock.wait();
				} catch (InterruptedException e) {}
		}
		awaitClose();
	}

	private void awaitClose() {
		try {
			srv.close();
		} catch (IOException e1) {}
		try {
			group.shutdown();
			group.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			pool.shutdown();
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {}
	}

	@Override
	public void closeForcibly() {
		try {
			pool.shutdownNow();
			group.shutdownNow();
		} catch (IOException e) {}
	}

}
