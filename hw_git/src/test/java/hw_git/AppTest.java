package hw_git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import org.apache.commons.io.FileUtils;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AppTest extends Assert {
	
	public static final String storageFilename = ".myGitDataFile";
	public static final String storageFolder = ".myGitDataStorage";
	public static final String stageFolder = ".stageData";
	
	//files
	final String file1 = "testdir/file.txt";
	final String file2 = "testdir/dir1/file_d1.txt";
	
	@Before
	public void setUp() throws IOException {
		//System.out.println("setUp");
		Files.createDirectories(Paths.get("testdir/dir1"));
    	Files.createFile(Paths.get(file2));
    	Files.createFile(Paths.get(file1));
    	Files.createFile(Paths.get("one.txt"));
	}
	
	@After
	public void tearDown() throws IOException {
		//System.out.println("tearDown");
		FileUtils.deleteDirectory(Paths.get("testdir").toFile());
		FileUtils.deleteDirectory(Paths.get(storageFolder).toFile());
		FileUtils.deleteDirectory(Paths.get(stageFolder).toFile());
		if (Files.exists(Paths.get(storageFilename))) {
			Files.delete(Paths.get(storageFilename));
		}
		if (Files.exists(Paths.get("one.txt"))) {
			Files.delete(Paths.get("one.txt"));
		}
	}
	
	@Test
    public void testInformationLoad() throws JsonGenerationException, JsonMappingException, IOException, UnversionedException {
    	GitCli.main(new String[] {"init"});
    	GitCore core = new GitCore();
    	core.findRepInformation();
    	assertEquals(core.getCurrentRevision(), -1);
    }
    
    @Test(expected = UnversionedException.class)
    public void testUnversioned() throws IOException, UnversionedException {
    	GitCore core = new GitCore();
    	core.findRepInformation();
    }
    
    @Test
    public void testCheckout() throws JsonGenerationException, JsonMappingException, IOException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 1"});
    	GitCli.main(new String[] {"add", file2});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	GitCli.main(new String[] {"checkout", "1"});
    	assertTrue(Files.exists(Paths.get(file1)));
    	assertFalse(Files.exists(Paths.get(file2)));
    	
    	GitCli.main(new String[] {"checkout", "2"});
    	assertTrue(Files.exists(Paths.get(file1)));
    	assertTrue(Files.exists(Paths.get(file2)));
    }
    
    @Test
    public void testCheckoutFiles() throws JsonGenerationException, JsonMappingException, IOException {
    	GitCli.main(new String[] {"init"});
    	try (PrintWriter out = new PrintWriter(new File(file1))) {
    		out.println("text 1");
    	}
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 1"});
    	try (PrintWriter out = new PrintWriter(new File(file1))) {
    		out.println("text 2");
    	}
    	
    	GitCli.main(new String[] {"checkout", "--", file1});
    	try (Scanner in = new Scanner(new File(file1))) {
    		assertTrue(in.nextLine().equals("text 1"));
    	}
    }
    
    @Test
    public void testFileChangeBetweenCommits() throws JsonGenerationException, JsonMappingException, IOException {
    	GitCli.main(new String[] {"init"});
    	try (PrintWriter out = new PrintWriter(new File(file1))) {
    		out.print("commit 1 content");
    	}
    	GitCli.main(new String[] {"add", file1, file2});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	try (PrintWriter out = new PrintWriter(new File(file1))) {
    		out.print("commit 2 content");
    	}
    	
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	GitCli.main(new String[] {"checkout", "1"});
    	try (Scanner in = new Scanner(new File(file1))) {
    		assertEquals(in.nextLine(), "commit 1 content");
    	}
    	
    	GitCli.main(new String[] {"checkout", "2"});
    	try (Scanner in = new Scanner(new File(file1))) {
    		assertEquals(in.nextLine(), "commit 2 content");
    	}
    }
    
    @Test
    public void testRM() throws JsonGenerationException, JsonMappingException, IOException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 1"});
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 2"});
    	Files.delete(Paths.get(file1));
    	GitCli.main(new String[] {"rm", file1});
    	GitCli.main(new String[] {"commit", "remove file1"});
    	GitCli.main(new String[] {"checkout", "2"});
    	assertTrue(Files.exists(Paths.get(file1)));
    	
    	GitCli.main(new String[] {"checkout", "1"});
    	assertTrue(Files.exists(Paths.get(file1)));
    	GitCli.main(new String[] {"checkout", "master"});
    	assertFalse(Files.exists(Paths.get(file1)));
    }
    
    @Test
    public void testStatus() throws JsonGenerationException, JsonMappingException, IOException, UnversionedException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 1"});

    	GitCore core = new GitCore();
    	core.findRepInformation();
    	
    	assertEquals(core.getChangedFiles(), Arrays.asList());
    	//assertEquals(core.getUntrackedFiles(), Arrays.asList(file2));
    	assertEquals(core.getDeletedFiles(), Arrays.asList());
    	
    	try (PrintWriter out = new PrintWriter(new File(file1))) {
    		out.print("commit 3 content");
    	}
    	
    	assertEquals(core.getChangedFiles(), Arrays.asList(file1));
    	
    	Files.delete(Paths.get(file1));
    	
    	assertEquals(core.getChangedFiles(), Arrays.asList(file1));
    	
    }
    
    @Test
    public void testAddBranch() throws UnversionedException, JsonParseException, IOException {
    	GitCore core = new GitCore();
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", file1});
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
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCli.main(new String[] {"branch", "b1"});
    	GitCli.main(new String[] {"checkout", "b1"});
    	
    	try(PrintWriter out = new PrintWriter(new File(file2))) {
    		out.println("b1 content");
    	}
    	GitCli.main(new String[] {"add", file2});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	assertTrue(Files.exists(Paths.get(file1)));
    	assertTrue(Files.exists(Paths.get(file2)));
    	
    	GitCli.main(new String[] {"checkout", "master"});
    	
    	assertFalse(Files.exists(Paths.get(file2)));
    	
    	GitCli.main(new String[] {"branch", "b2"});
    	GitCli.main(new String[] {"checkout", "b2"});
    	
    	try(PrintWriter out = new PrintWriter(new File(file2))) {
    		out.println("b2 content");
    	}
    	GitCli.main(new String[] {"add", file2});
    	GitCli.main(new String[] {"commit", "message 3"});
    	
    	GitCli.main(new String[] {"checkout", "b1"});
    	try (Scanner in = new Scanner(new File(file2))) {
    		assertEquals("b1 content", in.nextLine());
    	}
    	GitCli.main(new String[] {"checkout", "b2"});
    	try (Scanner in = new Scanner(new File(file2))) {
    		assertEquals("b2 content", in.nextLine());
    	}
    }
    
    public void testIncorrectBranchRm() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	assertEquals(
    			"You can't delete this branch while staying on it.",
    			GitCli.processArgs(new String[] {"branch", "-d", "master"})
    			.get(0));
    }
    
    @Test
    public void testBranchDeleteAndRestore() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCli.main(new String[] {"branch", "b1"});
    	GitCli.main(new String[] {"checkout", "b1"});
    	
    	GitCli.main(new String[] {"add", file2});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	GitCli.main(new String[] {"checkout", "master"});
    	GitCli.main(new String[] {"branch", "-d", "b1"});

    	GitCli.main(new String[] {"checkout", "0"});
    	GitCli.main(new String[] {"branch", "b1"});
    	GitCli.main(new String[] {"checkout", "b1"});

    	assertFalse(Files.exists(Paths.get(file2)));
    	assertTrue(Files.exists(Paths.get(file1)));
    }
    
    @Test
    public void testLogBasic() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", file1});

    	assertEquals("Empty log", 
    			GitCli.processArgs(new String[] {"log"}).get(1));
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	assertEquals("\nrevision: 1\nmessage 1", 
    			GitCli.processArgs(new String[] {"log"}).get(1));
    	assertEquals(
    			GitCli.processArgs(new String[] {"log"}),
    			GitCli.processArgs(new String[] {"log", "1"}));
    }
    

    public void testIncorrectBigCheckoutRevision() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	assertEquals(
    			"revision number" + 2 + " is incorrect",
    			GitCli.processArgs(new String[] {"checkout", "2"}).get(0));
    	
    	assertEquals(
    			"revision number" + 0 + " is incorrect",
    			GitCli.processArgs(new String[] {"checkout", "0"}).get(0));
    	
    	assertEquals(
    			"revision number" + 2 + " is incorrect",
    			GitCli.processArgs(new String[] {"reset", "2"}).get(0));
    	
    	assertEquals(
    			"revision number" + 0 + " is incorrect",
    			GitCli.processArgs(new String[] {"reset", "0"}).get(0));
    }
    
    @Test
    public void testDetachedState() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	assertEquals("HEAD detached on revison 1",
    			GitCli.processArgs(new String[] {"checkout", "1"}).get(1));
    	
    	assertEquals("Staying not at end of some branch.",
    			GitCli.processArgs(new String[] {"commit", "abc"}).get(1));
    }
    
    @Test
    public void testEmptyCommitNotFails() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", file1});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCli.main(new String[] {"commit", "message 2"});

    	ArrayList<String> logRes = GitCli.processArgs(new String[] {"log"});
    	assertEquals("\nrevision: 2\nmessage 2", logRes.get(1));
    	assertEquals("\nrevision: 1\nmessage 1", logRes.get(3));
    }
    
    @Test
    public void testCommitFileInRoot() throws JsonParseException, IOException, UnversionedException, BranchProblemException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "one.txt"});
    	GitCli.main(new String[] {"commit", "message 2"});
    	
    	ArrayList<String> logRes = GitCli.processArgs(new String[] {"log"});
    	assertEquals("\nrevision: 1\nmessage 2", logRes.get(1));
    }
    
    @Test
    public void testResetClearsStage() throws JsonGenerationException, JsonMappingException {
    	GitCli.main(new String[] {"init"});
    	GitCli.main(new String[] {"add", "one.txt"});
    	GitCli.main(new String[] {"commit", "message 1"});
    	
    	GitCli.main(new String[] {"add", "testdir/file.txt"});
    	GitCli.main(new String[] {"rm", "one.txt"});
    	
    	assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"testdir/file.txt",
						"Deleted files:\n________________",
						"one.txt",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 8));
    	
    	GitCli.main(new String[] {"reset", "1"});
    	
    	assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 6));
    }
    
}
