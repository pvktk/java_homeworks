package torrent.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class ServerRequestHandler{

	private ByteBuffer inputBuffer = ByteBuffer.allocate(1000);
	private ByteBuffer outputBuffer;

	public enum MessageProcessStatus {INCOMPLETE, ERROR, SUCCESS};

	private ConcreteTaskHandler[] handlers;

	public ServerRequestHandler(ConcreteTaskHandler[] handlers) {
		this.handlers = handlers;
	}

	public ByteBuffer getReceivingBuffer() {
		inputBuffer.clear();
		return inputBuffer;
	}

	public MessageProcessStatus messageProcessAttemp(InetSocketAddress clientInf) throws IOException {
		try (
				DataInputStream dInp = new DataInputStream(
						new ByteArrayInputStream(inputBuffer.array(),
								inputBuffer.arrayOffset(), 
								inputBuffer.limit() - inputBuffer.position()));

				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream dOut = new DataOutputStream(bout);
				) {
			byte typeIndex;
			try {
				typeIndex = dInp.readByte();
			} catch (IOException e) {
				return MessageProcessStatus.INCOMPLETE;
			}

			if (typeIndex > handlers.length) {
				return MessageProcessStatus.ERROR;
			}

			MessageProcessStatus status = handlers[typeIndex - 1].computeResult(dInp, dOut, clientInf);
			if (status != MessageProcessStatus.SUCCESS) {
				return status;
			}

			outputBuffer = ByteBuffer.wrap(bout.toByteArray());
			return status;
		}
	}

	public ByteBuffer getTransmittingBuffer() {
		return outputBuffer;
	}

}
