package hw_git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.BiFunction;

import org.apache.commons.io.FileUtils;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
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
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 2"});
    	Files.delete(Paths.get("testdir/file.txt"));
    	GitCli.main(new String[] {"rm", "testdir/file.txt"});
    	GitCli.main(new String[] {"checkout", "2"});
    	assertFalse(Files.exists(Paths.get("testdir/file.txt")));
    	
    	GitCli.main(new String[] {"checkout", "1"});
    	assertTrue(Files.exists(Paths.get("testdir/file.txt")));
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
    
    @Test
    public void testAddBranch() throws UnversionedException, JsonParseException, IOException {
    	GitCore core = new GitCore();
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	assertEquals("master", core.getCurrentBranchName());
    	
    	GitCli.main(new String[] {"branch", "b1"});
    	
    	assertEquals("master", core.getCurrentBranchName());
    	
    	GitCli.main(new String[] {"checkout", "b1"});
    	
    	assertEquals("b1", core.getCurrentBranchName());
    }
    
    @Test
    public void testFilesInBranches() throws JsonGenerationException, JsonMappingException, FileNotFoundException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCli.main(new String[] {"branch", "b1"});
    	GitCli.main(new String[] {"checkout", "b1"});
    	
    	try(PrintWriter out = new PrintWriter(new File("testdir/dir1/file_d1.txt"))) {
    		out.println("b1 content");
    	}
    	GitCli.main(new String[] {"add", "testdir/dir1/file_d1.txt"});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	assertTrue(Files.exists(Paths.get("testdir/file.txt")));
    	assertTrue(Files.exists(Paths.get("testdir/dir1/file_d1.txt")));
    	
    	GitCli.main(new String[] {"checkout", "master"});
    	
    	assertFalse(Files.exists(Paths.get("testdir/dir1/file_d1.txt")));
    	
    	GitCli.main(new String[] {"branch", "b2"});
    	GitCli.main(new String[] {"checkout", "b2"});
    	
    	try(PrintWriter out = new PrintWriter(new File("testdir/dir1/file_d1.txt"))) {
    		out.println("b2 content");
    	}
    	GitCli.main(new String[] {"add", "testdir/dir1/file_d1.txt"});
    	GitCli.main(new String[] {"commit", "message 3"});
    	
    	GitCli.main(new String[] {"checkout", "b1"});
    	try (Scanner in = new Scanner(new File("testdir/dir1/file_d1.txt"))) {
    		assertEquals("b1 content", in.nextLine());
    	}
    	GitCli.main(new String[] {"checkout", "b2"});
    	try (Scanner in = new Scanner(new File("testdir/dir1/file_d1.txt"))) {
    		assertEquals("b2 content", in.nextLine());
    	}
    }
    
    @Test(expected = BranchProblemException.class)
    public void testIncorrectBranchRm() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCli.main(new String[] {"branch", "b1"});
    	GitCli.main(new String[] {"checkout", "b1"});
    	
    	GitCli.main(new String[] {"add", "testdir/dir1/file_d1.txt"});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	GitCore core = new GitCore();
    	core.makeDeleteBranch("master");
    }
    
    @Test
    public void testBranchDeleteAndRestore() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCli.main(new String[] {"branch", "b1"});
    	GitCli.main(new String[] {"checkout", "b1"});
    	
    	GitCli.main(new String[] {"add", "testdir/dir1/file_d1.txt"});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	GitCli.main(new String[] {"branch", "-d", "b1"});
    	
    	GitCore core = new GitCore();
    	assertNull(core.getCurrentBranchName());
    	
    	assertFalse(Files.exists(Paths.get("testdir/dir1/file_d1.txt")));
    	assertTrue(Files.exists(Paths.get("testdir/file.txt")));
    	
    	core.makeBranch("b1");
    	core.makeCheckout("b1");
    	assertFalse(Files.exists(Paths.get("testdir/dir1/file_d1.txt")));
    	assertTrue(Files.exists(Paths.get("testdir/file.txt")));
    }
    
    @Test
    public void testMerge() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCli.main(new String[] {"branch", "b1"});
    	GitCli.main(new String[] {"checkout", "b1"});
    	
    	GitCli.main(new String[] {"add", "testdir/dir1/file_d1.txt"});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	GitCli.main(new String[] {"checkout", "master"});
    	
    	GitCore core = new GitCore();
    	core.makeMerge("b1", new BiFunction<Path, Path, Path>() {
			
			@Override
			public Path apply(Path t, Path u) {
				// TODO Auto-generated method stub
				return null;
			}
		});
    	
    	assertEquals("master", core.getCurrentBranchName());
    	assertTrue(Files.exists(Paths.get("testdir/dir1/file_d1.txt")));
    	assertTrue(Files.exists(Paths.get("testdir/file.txt")));
    }
}
