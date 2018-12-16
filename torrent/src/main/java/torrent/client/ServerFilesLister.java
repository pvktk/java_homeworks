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
			dout = new DataOutputStream(s.getOutputStream());
			dinp = new DataInputStream(s.getInputStream());
					
			s.connect(addr);
			dout.writeByte(1);
			
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
		} finally {
			dinp.close();
			dout.close();
			s.close();
		}
	}
}
