package torrent;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import torrent.client.FilesHolder;
import torrent.server.MainInner;

public class InteractionTests {

	Thread server;
	
	final int ncl = 3;
	Thread[] cl = new Thread[ncl];

	PrintWriter[] inp = new PrintWriter[ncl];
	ByteArrayOutputStream[] outp = new ByteArrayOutputStream[ncl];
	FilesHolder[] fh = new FilesHolder[ncl];
	Path root[] = new Path[ncl];
	
	String getOutput(int i) throws InterruptedException {
		Thread.sleep(1000);
		String res = outp[i].toString();
		outp[i].reset();
		return res;
	}
	
	void commandTo(int i, String s) {
		inp[i].println(s);
		inp[i].flush();
	}

	public void startServer() throws InterruptedException {
		server = new Thread(new MainInner(3000));
		server.start();
		Thread.sleep(1000);
	}
	
	public void initClient(int i) throws IOException {
		outp[i] = new ByteArrayOutputStream();
		PipedInputStream pin = new PipedInputStream();
		inp[i] = new PrintWriter(new PipedOutputStream(pin));
		root[i] = Paths.get("torrentData/client" + i);
		fh[i] = new FilesHolder(root[i].toString());
		cl[i] = new Thread(new torrent.client.MainInner("localhost", 8090 + i,
				new PrintStream(outp[i]),
				pin,
				2000,
				fh[i]));
	}
	
	public void createClientFiles() throws IOException {
		byte[] arr = new byte[15 * (1 << 20)];
		Random r = new Random(5);
		r.nextBytes(arr);
		for (int i = 0; i < 2; i++) {
			Files.createDirectories(root[i]);			
			OutputStream out = new FileOutputStream(root[i].resolve("file1").toString());
			out.write(arr);
			out.close();
		}
	}

	@Before
	public void setUp() throws InterruptedException, IOException {
		//startServer();
		for (int i = 0; i < 3; i++) {
			initClient(i);
		}
		createClientFiles();
	}


	public void stopServer() throws IOException, InterruptedException {
		if (server == null)
			return;
		System.out.println("Stopping server");
		server.interrupt();
		server.join();
	}
	
	public void stopClients() throws InterruptedException {
		for (int i = 0; i < 3; i++) {
			cl[i].interrupt();
			cl[i].join();
		}
	}

	@After
	public void cleanUp() throws IOException, InterruptedException {
		stopServer();
		stopClients();
		Files.deleteIfExists(Paths.get("serverFile"));
		FileUtils.deleteDirectory(Paths.get("torrentData").toFile());
	}

	@Test
	public void testBaseCommands() throws InterruptedException {
		cl[0].start();
		
		assertEquals(">", getOutput(0));
		
		commandTo(0, "list");
		assertEquals(
				"Failed to list avaliable files.\n" + 
				"Failed to connect to server\n" +
				">", getOutput(0));
		
		commandTo(0, "status");
		assertEquals("No files yet\n>", getOutput(0));
		
		commandTo(0, "publish nonExstFile");
		assertEquals("Failed to publish file.\n" + 
				"Failed to connect to server\n>", getOutput(0));
		
		commandTo(0, "pause 1");
		assertEquals("This file wasn't been downloading\n>", getOutput(0));
		
		commandTo(0, "delete 1");
		assertEquals("File isn't known.\n>", getOutput(0));
		
		commandTo(0, "get 1 torrentData/client0/file1");
		assertEquals("Unknown file id. Make list first.\n>", getOutput(0));

		//################################
		startServer();
		//################################
		
		commandTo(0, "list");
		assertEquals("No files avaliable on server\n>", getOutput(0));
		
		commandTo(0, "status");
		assertEquals("No files yet\n>", getOutput(0));
		
		commandTo(0, "publish nonExstFile");
		assertEquals("Failed to publish file.\n" + 
				"nonExstFile not exists.\n>", getOutput(0));
	}
	
	@Test
	public void testDownloadFromSinglePeer() throws InterruptedException, IOException {
		startServer();
		
		cl[0].start();
		
		commandTo(0, "publish " + root[0].resolve("file1"));
		assertEquals(">The file has an id 0\n" + 
				">", getOutput(0));
		
		commandTo(0, "status");
		assertEquals("0 torrentData/client0/file1 complete"
				+ "\n>", getOutput(0));
		
		
		commandTo(0, "status");
		assertEquals("0 torrentData/client0/file1 complete"
				+ "\n>", getOutput(0));
		
		commandTo(0, "list");
		assertEquals("0 file1 complete\n" + 
				">", getOutput(0));
		
		cl[2].start();
		assertEquals(">", getOutput(2));
		
		commandTo(2, "list");
		assertEquals("0 file1 \n" + 
				">", getOutput(2));
		
		cl[0].interrupt();
		cl[0].join();
		
		commandTo(2, "list");
		assertEquals("0 file1 \n" + 
				">", getOutput(2));
		
		commandTo(2, "get 0 " + root[2].resolve("file1_copy"));
		assertEquals(">", getOutput(2));
		
		commandTo(2, "status");
		assertEquals("0 torrentData/client2/file1_copy -> 0/2\n" + 
				">", getOutput(2));
		
		initClient(0);
		cl[0].start();
		assertEquals(">", getOutput(0));
		
		commandTo(0, "status");
		assertEquals("0 torrentData/client0/file1 complete"
				+ "\n>", getOutput(0));
		
		String outp;
		do {
			commandTo(2, "status");
			outp = getOutput(2);
			//System.out.println(outp);
		} while (!"0 torrentData/client2/file1_copy complete\n>".equals(outp));
	}
	
}
