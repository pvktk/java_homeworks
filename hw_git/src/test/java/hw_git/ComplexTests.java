package hw_git;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

public class ComplexTests extends Assert {
	
	final String testdir = "testdir/";
	static ArrayList<String> removeTimestampsFromLog(ArrayList<String> orig) {
		ArrayList<String> res = new ArrayList<>();
		res.add(orig.get(0));
		for (int i = 1; i < orig.size(); i += 2) {
			res.add(orig.get(i));
		}
		return res;
	}

	@After
	public void deleteTestFiles() throws IOException {
		FileUtils.deleteDirectory(new File(testdir));
		FileUtils.deleteDirectory(new File(AppTest.storageFolder));
		FileUtils.deleteDirectory(new File(AppTest.stageFolder));
		FileUtils.forceDelete(new File(AppTest.storageFilename));
	}
	
	@Test
	public void testCheckoutWithLogAndStatus() throws IOException {
		FileUtils.touch(Paths.get(testdir).resolve("file1").toFile());

		assertEquals("This directory is not versioned",
				GitCli.processArgs(new String[] {"add", "testdir/file1"}).get(1));
		
		assertEquals("This directory is not versioned",
				GitCli.processArgs(new String[] {"status"}).get(0));
		assertEquals("This directory is not versioned",
				GitCli.processArgs(new String[] {"log"}).get(1));
		
		assertEquals("Repository initiated.",
				GitCli.processArgs(new String[] {"init"}).get(0));
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 6));
		
		assertEquals("Empty log",
				GitCli.processArgs(new String[] {"log"}).get(1));
		
		GitCli.main(new String[] {"add", testdir + "file1"});
		
		assertEquals("Empty log",
				GitCli.processArgs(new String[] {"log"}).get(1));
		
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"testdir/file1",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 7));
		
		GitCli.main(new String[] {"commit", "mes 1"});
		
		assertEquals(
				Arrays.asList(
						"Log:",
						"\nrevision: 1\nmes 1"),
				GitCli.processArgs(new String[] {"log"}).subList(0, 2));
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 6));
		
		assertEquals(Arrays.asList(
				"Commiting...",
				"Commit made at revision 2"),
				GitCli.processArgs(new String[] {"commit", "empty commit"}));
		
		assertEquals(
				Arrays.asList(
						"Log:",
						"\nrevision: 2\nempty commit",
						"\nrevision: 1\nmes 1"),
				removeTimestampsFromLog(GitCli.processArgs(new String[] {"log"})));
		
		assertEquals(Arrays.asList(
				"Checkout to revision 1",
				"HEAD detached on revison 1"),
				GitCli.processArgs(new String[] {"checkout", "1"}));
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"detached head at revision 1",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 6));
		
		assertEquals(
				Arrays.asList(
						"Log:",
						"\nrevision: 1\nmes 1"),
				removeTimestampsFromLog(GitCli.processArgs(new String[] {"log"})));
		
		assertEquals(Arrays.asList(
				"Checkout to revision 2",
				"HEAD detached on revison 2"),
				GitCli.processArgs(new String[] {"checkout", "2"}));
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"detached head at revision 2",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 6));
		
		assertEquals(
				Arrays.asList(
						"Log:",
						"\nrevision: 2\nempty commit",
						"\nrevision: 1\nmes 1"),
				removeTimestampsFromLog(GitCli.processArgs(new String[] {"log"})));
		
		assertEquals(Arrays.asList(
				"Checking out branch..."),
				GitCli.processArgs(new String[] {"checkout", "master"}));
		
		assertEquals(
				Arrays.asList(
						"Log:",
						"\nrevision: 2\nempty commit",
						"\nrevision: 1\nmes 1"),
				removeTimestampsFromLog(GitCli.processArgs(new String[] {"log"})));
		
		GitCli.main(new String[] {"checkout", "1"});
		GitCli.main(new String[] {"branch", "b1"});
		GitCli.main(new String[] {"checkout", "b1"});
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"branch b1",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 6));

		assertEquals(
				Arrays.asList(
						"Log:",
						"\nrevision: 1\nmes 1"),
				removeTimestampsFromLog(GitCli.processArgs(new String[] {"log"})));
		
	}
	
	@Test
	public void testResetWithLogAndStatus() throws IOException {
		FileUtils.touch(Paths.get(testdir).resolve("file1").toFile());
		try (PrintWriter out = new PrintWriter(new File("testdir/file1"))) {
			out.println("initial content");
		}
		
		assertEquals("Repository initiated.",
				GitCli.processArgs(new String[] {"init"}).get(0));
		
		GitCli.main(new String[] {"add", "testdir/file1"});
		
		GitCli.main(new String[] {"commit", "mes 1"});
		
		try (PrintWriter out = new PrintWriter("testdir/file1")) {
			out.println("revision 2 content");
		}
		
		GitCli.main(new String[] {"add", "testdir/file1"});
		
		assertEquals(Arrays.asList(
				"Commiting...",
				"Commit made at revision 2"),
				GitCli.processArgs(new String[] {"commit", "second commit"}));
		
		try (Scanner in = new Scanner(new File("testdir/file1"))) {
			assertEquals("revision 2 content", in.nextLine());
		}

		assertEquals(
				Arrays.asList(
						"Performing reset to revision 1"),
				removeTimestampsFromLog(GitCli.processArgs(new String[] {"reset", "1"})));
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 6));

		assertEquals(
				Arrays.asList(
						"Log:",
						"\nrevision: 1\nmes 1"),
				removeTimestampsFromLog(GitCli.processArgs(new String[] {"log"})));
		
		try (Scanner in = new Scanner(new File("testdir/file1"))) {
			assertEquals("initial content", in.nextLine());
		}
		
		try (PrintWriter out = new PrintWriter("testdir/file1")) {
			out.println("reset state content");
		}
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"testdir/file1",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 7));
		
		GitCli.main(new String[] {"add", "testdir/file1"});
		assertEquals(Arrays.asList(
				"Commiting...",
				"Commit made at revision 3"),
				GitCli.processArgs(new String[] {"commit", "third commit"}));
		
		try (Scanner in = new Scanner(new File("testdir/file1"))) {
			assertEquals("reset state content", in.nextLine());
		}
		
		assertEquals(
				Arrays.asList(
						"Log:",
						"\nrevision: 3\nthird commit",
						"\nrevision: 1\nmes 1"),
				removeTimestampsFromLog(GitCli.processArgs(new String[] {"log"})));
		
		GitCli.main(new String[] {"reset", "2"});
		
		try (Scanner in = new Scanner(new File("testdir/file1"))) {
			assertEquals("revision 2 content", in.nextLine());
		}
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 6));
		
		GitCli.main(new String[] {"checkout", "1"});
		GitCli.main(new String[] {"reset", "3"});
		
		assertEquals(
				Arrays.asList(
						"Log:",
						"\nrevision: 3\nthird commit",
						"\nrevision: 1\nmes 1"),
				removeTimestampsFromLog(GitCli.processArgs(new String[] {"log"})));
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"detached head at revision 1",
						"Staged files:\n________________",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 6));
		
		try (Scanner in = new Scanner(new File("testdir/file1"))) {
			assertEquals("reset state content", in.nextLine());
		}
		
		GitCli.main(new String[] {"checkout", "master"});
		
		try (Scanner in = new Scanner(new File("testdir/file1"))) {
			assertEquals("revision 2 content", in.nextLine());
		}
	}
	
	@Test
	public void testBranchRM() throws IOException {
		FileUtils.touch(Paths.get(testdir).resolve("file1").toFile());
		try (PrintWriter out = new PrintWriter(new File("testdir/file1"))) {
			out.println("initial content");
		}
		
		assertEquals("Repository initiated.",
				GitCli.processArgs(new String[] {"init"}).get(0));
		
		GitCli.main(new String[] {"add", "testdir/file1"});
		
		GitCli.main(new String[] {"commit", "mes 1"});
		
		try (PrintWriter out = new PrintWriter("testdir/file1")) {
			out.println("revision 2 content");
		}
		
		GitCli.main(new String[] {"add", "testdir/file1"});
		
		GitCli.main(new String[] {"commit", "mes 2"});
		GitCli.main(new String[] {"checkout", "1"});
		
		GitCli.main(new String[] {"branch", "b1"});
		GitCli.main(new String[] {"checkout", "b1"});
		
		try (PrintWriter out = new PrintWriter("testdir/file1")) {
			out.println("revision 3 content");
		}
		GitCli.main(new String[] {"commit", "mes 3"});
		
		assertEquals(
				Arrays.asList(
						"Deleting branch b1",
						"You can't delete this branch while staying on it."),
				GitCli.processArgs(new String[] {"branch", "-d", "b1"}));
	}
}
