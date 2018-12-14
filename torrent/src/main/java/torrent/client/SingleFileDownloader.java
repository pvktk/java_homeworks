package torrent.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SingleFileDownloader implements Runnable {

	private SocketAddress srvAddr;
	final FilesHolder filesHolder;
	private final int fileId;
	private final int numPieces;

	private Random rand = new Random(3);

	private List<SocketAddress> fileSources = new ArrayList<>();
	private Map<Integer, List<SocketAddress>> pieceSources = new HashMap<>();
	private Map<Integer, AsynchronousSocketChannel> pieceChannels = new HashMap<>();

	public SingleFileDownloader(SocketAddress srvAddr, FilesHolder stm, int fileId) {
		this.srvAddr = srvAddr;
		this.filesHolder = stm;
		this.fileId = fileId;

		numPieces = stm.numParts(fileId);
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

	private void dispatchPieceDownloaders() {
		for (int i = 0; i < numPieces; i++) {
			if (filesHolder.completePieces.get(fileId).contains(i)) {
				pieceChannels.remove(i);
				continue;
			}

			AsynchronousSocketChannel chan = pieceChannels.get(i);
			if (chan != null) {
				if (!chan.isOpen()) {
					pieceChannels.remove(i);
				}
			}

			List<SocketAddress> sources = pieceSources.get(i);
			if (!sources.isEmpty()) {
				int rIdx = rand.nextInt(sources.size());

				try {
					chan = AsynchronousSocketChannel.open();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				chan.connect(sources.get(rIdx));
				PieceDownloader pdl = new PieceDownloader(this, fileId, i);
				chan.read(pdl.getBuffer(), chan, pdl);
				pieceChannels.put(i, chan);
			}
		}
	}

	private boolean checkIfComplete() {
		if (filesHolder.completePieces.get(fileId).size() < filesHolder.numParts(fileId)) {
			return false;
		}
		try {
			filesHolder.completedFiles.add(fileId);
			filesHolder.save();
		} catch (IOException e){
			e.printStackTrace();
		}
		return true;
	}

	private void closeAllChannels() {
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
		while (true) {
			if (checkIfComplete()) {
				closeAllChannels();
				return;
			}
			updateFileSources();
			updatePieceSources();
			dispatchPieceDownloaders();

			if (filesHolder.completedFiles.contains(fileId)) {
				break;
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				closeAllChannels();
				return;
			}
		}
	}
}
