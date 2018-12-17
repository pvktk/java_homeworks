package torrent.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import torrent.common.ServerRequestHandler.MessageProcessStatus;

public class RequestCompletionHandler implements CompletionHandler<Integer, ServerRequestHandler> {
	private AsynchronousSocketChannel clientChannel;

	public RequestCompletionHandler(AsynchronousSocketChannel sch) {
		clientChannel = sch;
	}

	@Override
	public void completed(Integer result, ServerRequestHandler handler) {
		MessageProcessStatus status;

		try {
			status = handler.messageProcessAttemp((InetSocketAddress) clientChannel.getRemoteAddress());
		} catch (IOException e) {
			return;
		}

		if (status == MessageProcessStatus.SUCCESS) {
			ByteBuffer toTransmit = handler.getTransmittingBuffer();
			clientChannel.write(toTransmit, null, new CompletionHandler<Integer, Object>() {

				@Override
				public void completed(Integer result, Object attachment) {
					if (toTransmit.hasRemaining()) {
						clientChannel.write(toTransmit, null, this);
					} else {
						try {
							clientChannel.close();
						} catch (IOException e) {
							System.out.println("RequestCompletionHandler: Error while closing channel: " + e.getMessage());
						}
					}
				}

				@Override
				public void failed(Throwable exc, Object attachment) {
					System.out.println("Fail while transmitting data: " + exc.getMessage());
				}
			});
		}

		if (status == MessageProcessStatus.INCOMPLETE){
			clientChannel.read(handler.getReceivingBuffer(), handler, this);
		}
		
		if (status == MessageProcessStatus.ERROR) {
			try {
				clientChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void failed(Throwable exc, ServerRequestHandler attachment) {
		System.out.println("Fail while receiving data: " + exc.getMessage());
	}
}
