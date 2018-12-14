package torrent.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Set;

import torrent.common.StorageManager;

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
		int piece_size = filesDownloader.filesHolder.pieceSize;

		int file_length = filesDownloader.filesHolder.files.get(fileId).length;

		this.pieceOffset = pieceIdx * piece_size;
		this.pieceLength = (pieceIdx + 1) * piece_size >= file_length
				? piece_size
						: (pieceIdx + 1) * piece_size - file_length;

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
