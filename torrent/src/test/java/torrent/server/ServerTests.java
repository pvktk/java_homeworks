package torrent.server;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import torrent.server.MainInner;


public class ServerTests {

	Thread server;

	Socket socket = new Socket();

	DataOutputStream out;
	DataInputStream in;

	void establishConnection() throws IOException {
		socket = new Socket("localhost", 8081);
		out = new DataOutputStream(socket.getOutputStream());
		in = new DataInputStream(socket.getInputStream());
	}

	void closeConnection() throws IOException {
		in.close();
		out.close();
		socket.close();
	}

	public void startServer() throws InterruptedException {
		//Thread.sleep(1000);
		server = new Thread(new MainInner(3000));
		server.start();
		Thread.sleep(1000);
	}

	@Before
	public void setUp() throws InterruptedException {
		startServer();
	}


	public void stopServer() throws IOException, InterruptedException {
		System.out.println("Stopping server");
		server.interrupt();
		server.join();
	}

	@After
	public void cleanUp() throws IOException, InterruptedException {
		stopServer();
		Files.delete(Paths.get("serverFile"));
	}

	@Test
	public void testFantasy() throws UnknownHostException, IOException, InterruptedException {

		establishConnection();
		out.writeByte(1); //list
		assertEquals(0, in.readInt());

		closeConnection();
		establishConnection();

		out.writeByte(2); //upload
		out.writeUTF("file1.txt");

		out.writeLong(11);

		assertEquals(0, in.readInt());

		closeConnection();
		establishConnection();

		out.writeByte(1);
		assertEquals(1, in.readInt());
		assertEquals(0, in.readInt());
		assertEquals("file1.txt", in.readUTF());
		assertEquals(11, in.readLong());

		closeConnection();
		establishConnection();
		System.out.println("Sources for non existing id");
		out.writeByte(3); //sources
		out.writeInt(1);

		assertEquals(0, in.readInt());

		closeConnection();
		establishConnection();

		out.writeByte(3);
		out.writeInt(0);

		assertEquals(0, in.readInt());

		closeConnection();
		establishConnection();

		out.writeByte(4); //update
		out.writeShort(1234);
		out.writeInt(1);
		out.writeInt(0);
		assertTrue(in.readBoolean());

		closeConnection();
		establishConnection();

		out.writeByte(3);
		out.writeInt(0);

		assertEquals(1, in.readInt());

		byte[] ip = new byte[4];
		in.readFully(ip);
		assertArrayEquals(new byte[] {127,0,0,1}, ip);

		assertEquals(1234, in.readShort());

		Thread.sleep(1000);

		closeConnection();
		establishConnection();

		out.writeByte(2); //upload
		out.writeUTF("file2.txt");

		out.writeLong(12);

		assertEquals(1, in.readInt());

		closeConnection();
		establishConnection();

		out.writeByte(1);
		assertEquals(2, in.readInt());
		assertEquals(0, in.readInt());
		assertEquals("file1.txt", in.readUTF());
		assertEquals(11, in.readLong());
		assertEquals(1, in.readInt());
		assertEquals("file2.txt", in.readUTF());
		assertEquals(12, in.readLong());

		Thread.sleep(2000);

		closeConnection();
		establishConnection();

		out.writeByte(1);
		assertEquals(2, in.readInt());
		assertEquals(0, in.readInt());
		assertEquals("file1.txt", in.readUTF());
		assertEquals(11, in.readLong());
		assertEquals(1, in.readInt());
		assertEquals("file2.txt", in.readUTF());
		assertEquals(12, in.readLong());

		closeConnection();
		establishConnection();

		out.writeByte(4); //update
		out.writeShort(1235);
		out.writeInt(1);
		out.writeInt(1);
		assertTrue(in.readBoolean());

		closeConnection();
		establishConnection();
		//Thread.sleep(1000);
		out.writeByte(3); //sources
		out.writeInt(1);
		System.out.println("before 1 sources");
		assertEquals(1, in.readInt());

		do {
			closeConnection();
			establishConnection();

			out.writeByte(3); //sources
			out.writeInt(0);

			Thread.sleep(10);
		} while (0 != in.readInt());
	}

	@Test
	public void testSourcesCleaner() throws IOException, InterruptedException {
		establishConnection();
		out.writeByte(1); //list
		assertEquals(0, in.readInt());

		closeConnection();
		establishConnection();

		out.writeByte(2); //upload
		out.writeUTF("file1.txt");

		out.writeLong(11);

		assertEquals(0, in.readInt());

		closeConnection();
		establishConnection();

		out.writeByte(4); //update
		out.writeShort(1235);
		out.writeInt(1);
		out.writeInt(0);
		assertTrue(in.readBoolean());

		do {
			closeConnection();
			establishConnection();

			out.writeByte(3); //sources
			out.writeInt(0);

			Thread.sleep(10);
			//assertEquals(0, in.readInt());
		} while (0 != in.readInt());

	}

	@Test
	public void testSavingState() throws IOException, InterruptedException {
		establishConnection();

		out.writeByte(2); //upload
		out.writeUTF("file1.txt");

		out.writeLong(11);
		closeConnection();
		establishConnection();

		out.writeByte(2); //upload
		out.writeUTF("file2.txt");

		out.writeLong(12);

		assertEquals(1, in.readInt());

		closeConnection();
		stopServer();
		Thread.sleep(2000);
		startServer();

		establishConnection();

		out.writeByte(1);

		System.out.println("check count");
		assertEquals(2, in.readInt());

		assertEquals(0, in.readInt());
		assertEquals("file1.txt", in.readUTF());
		assertEquals(11, in.readLong());
		assertEquals(1, in.readInt());
		assertEquals("file2.txt", in.readUTF());
		assertEquals(12, in.readLong());
	}

}
