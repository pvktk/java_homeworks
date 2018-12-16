package torrent.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class PieceDownloader implements CompletionHandler<Integer, AsynchronousSocketChannel> {
	private int fileId;
	private int pieceOffset, pieceLength;
	private int pieceIdx;
	private SingleFileDownloader filesDownloader;

	private ByteBuffer buffer;

	public PieceDownloader(SingleFileDownloader filesDownloader, int fileId, int pieceIdx) {
		this.filesDownloader = filesDownloader;
		this.fileId = fileId;

		this.pieceIdx = pieceIdx;
		filesDownloader.filesHolder.files.get(fileId);

		this.pieceOffset = filesDownloader.filesHolder.pieceOffset(fileId, pieceIdx);
		this.pieceLength = filesDownloader.filesHolder.pieceLenght(fileId, pieceIdx);

		buffer = ByteBuffer.wrap(filesDownloader.filesHolder.files.get(fileId), pieceOffset, pieceLength);
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

	@Override
	public void completed(Integer result, AsynchronousSocketChannel attachment) {
		if (buffer.hasRemaining()) {
			attachment.read(buffer, attachment, this);
		} else {
			filesDownloader.filesHolder.completePieces.get(fileId).add(pieceIdx);
			try {
				attachment.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		// TODO Auto-generated method stub

	}


}
