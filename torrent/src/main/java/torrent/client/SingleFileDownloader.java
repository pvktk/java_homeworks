package torrent.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import torrent.client.FilesHolder.FileStatus;

public class SingleFileDownloader implements Runnable {

	private SocketAddress srvAddr;
	final FilesHolder filesHolder;
	private final int fileId;
	private final int numPieces;
	private final FilesDownloader filesDownloader;

	private Random rand = new Random(3);

	private List<SocketAddress> fileSources = new ArrayList<>();
	private Map<Integer, List<SocketAddress>> pieceSources = new HashMap<>();
	private Map<Integer, AsynchronousSocketChannel> pieceChannels = new HashMap<>();
	
	private final int numOfActivePieces = 5;
	private final ExecutorService pieceQueue = Executors.newFixedThreadPool(numOfActivePieces);
	private final Semaphore pieceSemaphore = new Semaphore(numOfActivePieces);
	
	public SingleFileDownloader(SocketAddress srvAddr, FilesHolder stm, int fileId, FilesDownloader fdl) {
		this.srvAddr = srvAddr;
		this.filesHolder = stm;
		this.fileId = fileId;

		numPieces = stm.numParts(fileId);
		
		this.filesDownloader = fdl;
	}

	private void updateFileSources() {
		DataOutputStream out;
		DataInputStream in;
		try (Socket toServer = new Socket()) {
			toServer.connect(srvAddr);
			out = new DataOutputStream(toServer.getOutputStream());
			in = new DataInputStream(toServer.getInputStream());
			out.writeByte(3);
			out.writeInt(fileId);

			int clientsCount = in.readInt();
			fileSources.clear();
			byte[] ipBuf = new byte[4];
			for (int i = 0; i < clientsCount; i++) {
				in.readFully(ipBuf);

				fileSources.add(new InetSocketAddress(InetAddress.getByAddress(ipBuf),
						in.readShort()));
			}
		} catch (IOException e) {
			System.out.println("SourceUpdater: creation of output stream failed while update file sources");
		}
	}

	private void updatePieceSources() {
		DataOutputStream out;
		DataInputStream in;

		pieceSources.clear();
		for (int i = 0; i < numPieces; i++) {
			pieceSources.put(i, new ArrayList<>());
		}

		for (SocketAddress addr : fileSources) {
			try (Socket othClient = new Socket()) {
				othClient.connect(addr, 10000);
				out = new DataOutputStream(othClient.getOutputStream());
				in = new DataInputStream(othClient.getInputStream());

				out.writeByte(1);
				out.writeInt(fileId);

				int partCount = in.readInt();

				for (int i = 0; i < partCount; i++) {
					int partNum = in.readInt();
					pieceSources.get(partNum).add(addr);
				}

			} catch (IOException e) {
				System.out.println("SourceUpdater: failed in updatePieceSources");
			}
		}
	}


	private boolean getRequest(AsynchronousSocketChannel chan, int fileId, int partNum) throws IOException, InterruptedException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(baos);

		dout.writeByte(2);
		dout.writeInt(fileId);
		dout.writeInt(partNum);
		
		dout.close();
		ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
		
		do {
			try {
				chan.write(bb).get();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} while (bb.hasRemaining());

		return true;
	}

	private void dispatchPieceDownloaders() throws InterruptedException {
		for (int i = 0; i < numPieces; i++) {
			if (filesHolder.completePieces.get(fileId).contains(i)) {
				pieceChannels.remove(i);
				continue;
			}

			AsynchronousSocketChannel chan = pieceChannels.get(i);
			if (chan != null) {
				if (!chan.isOpen()) {
					pieceChannels.remove(i);
				} else {
					continue;
				}
			}

			List<SocketAddress> sources = pieceSources.get(i);
			if (!sources.isEmpty()) {
				int rIdx = rand.nextInt(sources.size());
				
				int nPiece = i;
				pieceQueue.execute(() -> {
					try {
						pieceSemaphore.acquire();
					} catch (InterruptedException e) {
						return;
					}
					AsynchronousSocketChannel pieceChan;
					try {
						pieceChan = AsynchronousSocketChannel.open();
						pieceChan.connect(sources.get(rIdx)).get();
						if (!getRequest(pieceChan, fileId, nPiece)) {
							System.out.println("SFD: get request failed");
							return;
						}
					} catch (IOException e) {
						e.printStackTrace();
						return;
					} catch (InterruptedException e) {
						return;
					} catch (ExecutionException e) {
						return;
					} finally {
						pieceSemaphore.release();
					}
					
					PieceDownloader pdl = new PieceDownloader(this, fileId, nPiece, pieceSemaphore);
					pieceChan.read(pdl.getBuffer(), pieceChan, pdl);
					pieceChannels.put(nPiece, pieceChan);
				});
				
			}
		}
	}

	boolean checkIfComplete() {
		if (filesHolder.completePieces.get(fileId).size() < filesHolder.numParts(fileId)) {
			return false;
		}
		try {
			filesDownloader.stopFileDownload(fileId);
			filesHolder.fileStatus.put(fileId, FileStatus.Complete);
			filesHolder.save();
		} catch (IOException e){
			e.printStackTrace();
		}
		return true;
	}

	private void closeAllChannels() {
		pieceQueue.shutdownNow();
		pieceChannels.forEach((i, ch) -> {
			try {
				ch.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});
	}

	@Override
	public void  run() {
		try {
			while (true) {
				if (checkIfComplete()) {
					return;
				}

				updateFileSources();
				updatePieceSources();
				
				try {
					dispatchPieceDownloaders();
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					return;
				}
			}
		} finally {
			closeAllChannels();
		}
	}
}
