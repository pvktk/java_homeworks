package torrent.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;

public class ServerFilesLister {
	public static void list(SocketAddress addr, Map<Integer, Long> fileSizes, Map<Integer, String> filesNames) throws IOException {
		Socket s = null;
		DataOutputStream dout = null;
		DataInputStream dinp = null;
		try {
			s = new Socket();
			s.connect(addr);
			dout = new DataOutputStream(s.getOutputStream());
			dinp = new DataInputStream(s.getInputStream());

			dout.writeByte(1);
			dout.flush();

			fileSizes.clear();
			filesNames.clear();

			int count = dinp.readInt();

			for (int i = 0; i < count; i++) {
				int id = dinp.readInt();
				String name = dinp.readUTF();
				long size = dinp.readLong();

				fileSizes.put(id, size);
				filesNames.put(id, name);
			}
		} catch (IOException e) {
			if (s == null || dinp == null || dout == null) {
				throw new IOException("Failed to connect to server");
			}
			throw e;
		} finally {
			if (dinp != null)
				dinp.close();
			if (dout != null)
				dout.close();
			if (s != null)
				s.close();
		}
	}
}
