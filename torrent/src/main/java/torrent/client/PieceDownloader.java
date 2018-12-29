package torrent.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Semaphore;

public class PieceDownloader implements CompletionHandler<Integer, AsynchronousSocketChannel> {
	private int fileId;
	private int pieceLength;
	private int pieceIdx;
	private SingleFileDownloader singleFileDownloader;

	private ByteBuffer buffer;
	private final Semaphore pieceSemaphore;

	public PieceDownloader(SingleFileDownloader filesDownloader,
			int fileId, int pieceIdx,
			Semaphore pieceSemaphore) {
		this.singleFileDownloader = filesDownloader;
		this.fileId = fileId;

		this.pieceIdx = pieceIdx;

		this.pieceLength = filesDownloader.filesHolder.pieceLenght(fileId, pieceIdx);

		buffer = ByteBuffer.wrap(new byte[pieceLength]);
		
		this.pieceSemaphore = pieceSemaphore;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	@Override
	public void completed(Integer result, AsynchronousSocketChannel attachment) {
		if (buffer.hasRemaining()) {
			attachment.read(buffer, attachment, this);
		} else {
			try {
				singleFileDownloader.filesHolder.putPiece(fileId, pieceIdx, buffer.array());
				singleFileDownloader.filesHolder.completePieces.get(fileId).add(pieceIdx);
				singleFileDownloader.checkIfComplete();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					attachment.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				pieceSemaphore.release();
			}
		}
	}

	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		pieceSemaphore.release();
		System.err.println("PieceDownloader failed");
	}


}
