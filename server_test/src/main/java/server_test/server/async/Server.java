package server_test.server.async;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
					final ByteBuffer readBuffer = ByteBuffer.allocate(maxMessageSize);

					chan.read(readBuffer, null, new CompletionHandler<Integer, Object>() {

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
						public void completed(Integer result, Object attachment) {
							if (result == -1) {
								closeClient(chan);
								return;
							}

							if (!arrayReceiveStarted) {
								arrayReceiveStarted = true;
								arrayProcessStart = System.currentTimeMillis();
								isAllClientsWorkedAtMeasure = statHolder.isAllClientsConnected();
							}


							if (readBuffer.position() < Integer.BYTES) {
								chan.read(readBuffer, null, this);
								return;
							}

							int messageSize;
							try {
								messageSize = (new DataInputStream(new ByteArrayInputStream(readBuffer.array()))).readInt();
							} catch (IOException e) {
								closeClient(chan);
								return;
							}

							if (readBuffer.position() - Integer.BYTES >= messageSize) {

								pool.execute(() -> {

									readBuffer.flip();

									readBuffer.position(Integer.BYTES);

									try {
										ClientMessage clMessage = ClientMessage.parseFrom(readBuffer);
										readBuffer.clear();

										int[] arrayToSort = clMessage.getArrayList()
												.stream()
												.mapToInt(I -> I)
												.toArray();

										long sortStart = System.currentTimeMillis();

										QuadraticSorter.sort(arrayToSort);

										sortTime = System.currentTimeMillis() - sortStart;

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
													long processTime = System.currentTimeMillis() - arrayProcessStart;
													if (isAllClientsWorkedAtMeasure && statHolder.isAllClientsConnected()) {
														statHolder.numberOfArrays.incrementAndGet();
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

							chan.read(readBuffer, null, this);

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
