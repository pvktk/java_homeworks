package torrent.client;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import torrent.common.StorageManager;

public class PieceDownloader implements CompletionHandler<Integer, AsynchronousSocketChannel> {
	private int fileId;
	private int pieceOffset, pieceLength;
	private int pieceIdx;
	private FilesDownloader filesDownloader;
	
	private ByteBuffer buffer;
	
	public PieceDownloader(FilesDownloader filesDownloader, int fileId, int pieceIdx) {
		this.filesDownloader = filesDownloader;
		this.fileId = fileId;
		
		this.pieceIdx = pieceIdx;
		int piece_size = filesDownloader.stm.data.pieceSize;
		int file_length = filesDownloader.stm.data.files.get(fileId).length;
		
		this.pieceOffset = pieceIdx * piece_size;
		this.pieceLength = (pieceIdx + 1) * piece_size >= file_length
				? piece_size
				: (pieceIdx + 1) * piece_size - file_length;
		
		buffer = ByteBuffer.wrap(filesDownloader.stm.data.files.get(fileId), pieceOffset, pieceLength);
	}
	
	public ByteBuffer getBuffer() {
		return buffer;
	}

	@Override
	public void completed(Integer result, AsynchronousSocketChannel attachment) {
		if (buffer.hasRemaining()) {
			attachment.read(buffer, attachment, this);
		} else {
			filesDownloader.stm.data.completePieces.get(fileId).add(pieceIdx);
			filesDownloader.downloadingPieces.get(fileId).remove(pieceIdx);
		}
	}

	@Override
	public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
		// TODO Auto-generated method stub
		
	}

	
}
