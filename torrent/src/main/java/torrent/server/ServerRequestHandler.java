package torrent.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import torrent.common.ConcreteRequestHandler;
import torrent.common.RequestHandler;

public class ServerRequestHandler implements RequestHandler {
	
	private ByteBuffer inputBuffer = ByteBuffer.allocate(10);
	private ByteBuffer outputBuffer = ByteBuffer.allocate(10);
	
	private ConcreteRequestHandler[] handlers;
	
	public ServerRequestHandler(ConcreteRequestHandler... handlers) {
		this.handlers = handlers;
	}
	
	@Override
	public ByteBuffer getReceivingBuffer() {
		inputBuffer.clear();
		return inputBuffer;
	}

	@Override
	public boolean inputMessageComplete() {
		DataInputStream dInp = new DataInputStream(
				new ByteArrayInputStream(inputBuffer.array(),
						inputBuffer.arrayOffset(), 
						inputBuffer.limit() - inputBuffer.position()));
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dOut = new DataOutputStream(bout);
		
		byte typeIndex;
		try {
			typeIndex = dInp.readByte();
		} catch (IOException e) {
			return false;
		}
		
		if (typeIndex > handlers.length) {
			return false;
		}
		
		if (!handlers[typeIndex - 1].computeResult(dInp, dOut)) {
			return false;
		}
		
		outputBuffer = ByteBuffer.wrap(bout.toByteArray());
		return true;
	}

	@Override
	public ByteBuffer getTransmittingBuffer() {
		return outputBuffer;
	}

	@Override
	public boolean allOutputSent() {
		// TODO Auto-generated method stub
		return false;
	}

}
