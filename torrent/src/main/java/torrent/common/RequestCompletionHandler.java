package torrent.common;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class RequestCompletionHandler implements CompletionHandler<Integer, RequestHandler> {
	private AsynchronousSocketChannel clientChannel;

	public RequestCompletionHandler(AsynchronousSocketChannel sch) {
		clientChannel = sch;
	}

	@Override
	public void completed(Integer result, RequestHandler handler) {
		if (handler.inputMessageComplete()) {
			ByteBuffer toTransmit = handler.getTransmittingBuffer();
			clientChannel.write(toTransmit, null, new CompletionHandler<Integer, Object>() {

				@Override
				public void completed(Integer result, Object attachment) {
					if (!toTransmit.hasRemaining()) {
						clientChannel.write(toTransmit, null, this);
					}
				}

				@Override
				public void failed(Throwable exc, Object attachment) {}
			});
		} else {
			clientChannel.read(handler.getReceivingBuffer(), handler, this);
		}
	}

	@Override
	public void failed(Throwable exc, RequestHandler attachment) {}
}
