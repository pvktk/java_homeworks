package torrent.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class Uploader {
	public static int uploadAndGetId(SocketAddress addr, Path filePath) throws IOException {
		Socket s = null;
		DataOutputStream dout = null;
		DataInputStream dinp = null;
		try {
			s = new Socket();
			
			s.connect(addr);
			
			dout = new DataOutputStream(s.getOutputStream());
			dinp = new DataInputStream(s.getInputStream());

			if (!Files.exists(filePath)) {
				throw new FileNotFoundException();
			}

			long len = filePath.toFile().length();
			dout.writeByte(2);
			dout.writeUTF(filePath.getFileName().toString());
			dout.writeLong(len);

			return dinp.readInt();
		} finally {
			try {
				dinp.close();
				dout.close();
				s.close();
			} catch (NullPointerException npe) {
				throw new IOException("Failed to connect to server");
			}
		}
	}
}
