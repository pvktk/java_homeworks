package hw_git;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AppTest extends Assert {

	@Before
	public void setUp() throws IOException {
		//System.out.println("setUp");
		Files.createDirectories(Paths.get("testdir/dir1"));
    	Files.createFile(Paths.get("testdir/dir1/file_d1.txt"));
    	Files.createFile(Paths.get("testdir/file.txt"));
	}
	
	@After
	public void tearDown() throws IOException {
		//System.out.println("tearDown");
		FileUtils.deleteDirectory(Paths.get("testdir").toFile());
		FileUtils.deleteDirectory(Paths.get(".mygitdata").toFile());
		if (Files.exists(Paths.get(".myGitData"))) {
			Files.delete(Paths.get(".myGitData"));
		}
	}
	
	@Test
    public void testInformationLoad() throws JsonGenerationException, JsonMappingException, IOException, UnversionedException {
    	GitCli.main(new String[] {"init"});
    	GitCore core = new GitCore();
    	core.findRepInformation();
    	assertEquals(core.getCurrentRevision(), -1);
    	//Files.delete(Paths.get(".myGitData"));
    }
    
    @Test(expected = UnversionedException.class)
    public void testUnversioned() throws IOException, UnversionedException {
    	GitCore core = new GitCore();
    	core.findRepInformation();
    	//core.makeCheckout(0);
    }
    
    @Test
    public void testCheckout() throws JsonGenerationException, JsonMappingException, IOException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	GitCli.main(new String[] {"add", "testdir/dir1/file_d1.txt"});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	GitCli.main(new String[] {"checkout", "1"});
    	assertTrue(Files.exists(Paths.get("testdir/file.txt")));
    	assertFalse(Files.exists(Paths.get("testdir/dir1/file_d1.txt")));
    	
    	GitCli.main(new String[] {"checkout", "2"});
    	assertTrue(Files.exists(Paths.get("testdir/file.txt")));
    	assertTrue(Files.exists(Paths.get("testdir/dir1/file_d1.txt")));
    }
    
    @Test
    public void testCheckoutFiles() throws JsonGenerationException, JsonMappingException, IOException {
    	GitCli.main(new String[] {"init"});
    	try (PrintWriter out = new PrintWriter(new File("testdir/file.txt"))) {
    		out.println("text 1");
    	}
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	try (PrintWriter out = new PrintWriter(new File("testdir/file.txt"))) {
    		out.println("text 2");
    	}
    	
    	GitCli.main(new String[] {"checkout", "--", "testdir/file.txt"});
    	try (Scanner in = new Scanner(new File("testdir/file.txt"))) {
    		assertTrue(in.nextLine().equals("text 1"));
    	}
    }
    
    @Test
    public void testFileChangeBetweenCommits() throws JsonGenerationException, JsonMappingException, IOException {
    	GitCli.main(new String[] {"init"});
    	try (PrintWriter out = new PrintWriter(new File("testdir/file.txt"))) {
    		out.print("commit 1 content");
    	}
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	GitCli.main(new String[] {"add", "testdir/dir1/file_d1.txt"});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	try (PrintWriter out = new PrintWriter(new File("testdir/file.txt"))) {
    		out.print("commit 3 content");
    	}
    	
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 3"});
    	
    	GitCli.main(new String[] {"checkout", "2"});
    	try (Scanner in = new Scanner(new File("testdir/file.txt"))) {
    		assertEquals(in.nextLine(), "commit 1 content");
    	}
    	
    	GitCli.main(new String[] {"checkout", "3"});
    	try (Scanner in = new Scanner(new File("testdir/file.txt"))) {
    		assertEquals(in.nextLine(), "commit 3 content");
    	}
    }
    
    @Ignore
    @Test
    public void testReset() throws JsonGenerationException, JsonMappingException, IOException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	String s1;
    	try (Scanner in = new Scanner(new File(".myGitData"))) {
    		s1 = in.nextLine();
    	}
    	GitCli.main(new String[] {"add", "testdir/dir1/file_d1.txt"});
    	GitCli.main(new String[] {"commit", "message 2"});
    	GitCli.main(new String[] {"reset", "1"});
    	String s2;
    	try (Scanner in = new Scanner(new File(".myGitData"))) {
    		s2 = in.nextLine();
    	}
    	assertEquals(s1, s2);
    }
    
    @Test
    public void testRM() throws JsonGenerationException, JsonMappingException, IOException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	Files.delete(Paths.get("testdir/file.txt"));
    	GitCli.main(new String[] {"rm", "testdir/file.txt"});
    	GitCli.main(new String[] {"checkout", "1"});
    	assertFalse(Files.exists(Paths.get("testdir/file.txt")));
    }
    
    @Test
    public void testStatus() throws JsonGenerationException, JsonMappingException, IOException, UnversionedException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCore core = new GitCore();
    	core.findRepInformation();
    	
    	assertEquals(core.getChangedFiles(), Arrays.asList());
    	//assertEquals(core.getUntrackedFiles(), Arrays.asList("testdir/dir1/file_d1.txt"));
    	assertEquals(core.getDeletedFiles(), Arrays.asList());
    	
    	try (PrintWriter out = new PrintWriter(new File("testdir/file.txt"))) {
    		out.print("commit 3 content");
    	}
    	
    	assertEquals(core.getChangedFiles(), Arrays.asList("testdir/file.txt"));
    	
    	Files.delete(Paths.get("testdir/file.txt"));
    	
    	assertEquals(core.getDeletedFiles(), Arrays.asList("testdir/file.txt"));
    }
}
