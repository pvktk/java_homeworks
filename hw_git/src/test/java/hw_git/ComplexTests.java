package hw_git;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

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
		Files.createDirectory(Paths.get(testdir));
		
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
	public void testBranchRM() throws IOException, UnversionedException, BranchProblemException {
		Files.createDirectory(Paths.get(testdir));
		
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
		
		ArrayList<String> beforeRMLog = GitCli.processArgs(new String[] {"log"});
		assertEquals(beforeRMLog, GitCli.processArgs(new String[] {"log", "3"}));
		
		assertEquals(
				Arrays.asList(
						"Deleting branch b1",
						"You can't delete this branch while staying on it."),
				GitCli.processArgs(new String[] {"branch", "-d", "b1"}));
		
		GitCli.main(new String[] {"checkout", "master"});
		try (Scanner in = new Scanner(new File("testdir/file1"))) {
			assertEquals("revision 2 content", in.nextLine());
		}
		
		assertEquals(
				Arrays.asList(
						"Deleting branch b1"),
				GitCli.processArgs(new String[] {"branch", "-d", "b1"}));
		
		assertEquals(beforeRMLog, GitCli.processArgs(new String[] {"log", "3"}));
		assertEquals(
				Arrays.asList(
						"Checking out branch...",
						"Branch not exists: b1"),
				GitCli.processArgs(new String[] {"checkout", "b1"}));
		GitCli.main(new String[] {"checkout", "3"});
		
		assertEquals(beforeRMLog, GitCli.processArgs(new String[] {"log"}));
		GitCore core = new GitCore();
		core.makeBranch("b1");
		core.makeCheckout("b1");
	}
	
	@Test
	public void testMergeNoConflict() throws IOException, UnversionedException, BranchProblemException {
		Files.createDirectory(Paths.get(testdir));
		try (PrintWriter out = new PrintWriter(new File("testdir/file1"))) {
			out.println("initial 1 content");
		}
		try (PrintWriter out = new PrintWriter(new File("testdir/file2"))) {
			out.println("initial 2 content");
		}

		GitCli.main(new String[] {"init"});
		GitCli.main(new String[] {"add", "testdir/file1", "testdir/file2"});
		GitCli.main(new String[] {"commit", "mes 1"});
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file2"))) {
			out.println("2 content");
		}
		
		GitCli.main(new String[] {"add", "testdir/file2"});
		GitCli.main(new String[] {"commit", "mes 2"});
		
		GitCli.main(new String[] {"checkout", "1"});
		GitCli.main(new String[] {"branch", "b1"});
		GitCli.main(new String[] {"checkout", "b1"});
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file3"))) {
			out.println("3 content");
		}
		
		GitCli.main(new String[] {"add", "testdir/file3"});
		GitCli.main(new String[] {"commit", "mes 3"});
		
		GitCli.main(new String[] {"checkout", "master"});
		
		assertEquals(Arrays.asList("Merging branch b1 to current state."
				+ "\nYou should make commit then."),
				GitCli.processArgs(new String[] {"merge", "b1"}));
				
		//new GitCore().makeMerge("b1");
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"testdir/file3",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 7));
	}
	
	@Test
	public void testMergeWithConflicts() throws IOException, UnversionedException, BranchProblemException {
		Files.createDirectory(Paths.get(testdir));
		try (PrintWriter out = new PrintWriter(new File("testdir/file1"))) {
			out.println("initial 1 content");
		}
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file2"))) {
			out.println("initial 2 content");
		}
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file3"))) {
			out.println("initial 3 content");
		}
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file4"))) {
			out.println("initial 4 content");
		}
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file5"))) {
			out.println("initial 5 content");
		}
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file6"))) {
			out.println("initial 6 content");
		}
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file7"))) {
			out.println("initial 7 content");
		}

		GitCli.main(new String[] {"init"});
		GitCli.main(new String[] {"add", "testdir/file1", "testdir/file2", "testdir/file5"});
		GitCli.main(new String[] {"add", "testdir/file3", "testdir/file4", "testdir/file7"});
		GitCli.main(new String[] {"commit", "mes 1"});
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file2"))) {
			out.println("2 content");
		}
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file4"))) {
			out.println("4 content");
		}
		
		GitCli.main(new String[] {"rm", "testdir/file7"});
		GitCli.main(new String[] {"add", "testdir/file2", "testdir/file4"});
		GitCli.main(new String[] {"commit", "mes 2"});
		
		GitCli.main(new String[] {"checkout", "1"});
		GitCli.main(new String[] {"branch", "b1"});
		GitCli.main(new String[] {"checkout", "b1"});
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file3"))) {
			out.println("3 from b1 content");
		}
		
		GitCli.main(new String[] {"rm", "testdir/file5"});
		
		try (PrintWriter out = new PrintWriter(new File("testdir/file4"))) {
			out.println("4 b1 content");
		}
		GitCli.main(new String[] {"add", "testdir/file3", "testdir/file4", "testdir/file6"});
		GitCli.main(new String[] {"commit", "mes 3"});
		
		
		GitCli.main(new String[] {"rm", "testdir/file6"});
		GitCli.main(new String[] {"commit", "mes 4"});
		
		try (Scanner in = new Scanner(new File("testdir/file6"))) {
			assertEquals("initial 6 content", in.nextLine());
		}
		
		GitCli.main(new String[] {"checkout", "master"});
		
		assertEquals(Arrays.asList("Merging branch b1 to current state."
				+ "\nYou should make commit then.",
				"Please, resolve conflicts in these files, and \"add\" them to commit:",
				"testdir/file4"),
				GitCli.processArgs(new String[] {"merge", "b1"}));
				
		//new GitCore().makeMerge("b1");
		
		assertEquals(
				Arrays.asList(
						"Status:",
						"branch master",
						"Staged files:\n________________",
						"testdir/file3",
						"testdir/file7",
						"Deleted files:\n________________",
						"Changed files:\n________________",
						"testdir/file3",
						"testdir/file4",
						"Untracked files:\n________________"),
				GitCli.processArgs(new String[] {"status"}).subList(0, 10));
		
		try (Scanner in = new Scanner(new File("testdir/file1"))) {
			assertEquals("initial 1 content", in.nextLine());
		}
		
		try (Scanner in = new Scanner(new File("testdir/file2"))) {
			assertEquals("2 content", in.nextLine());
		}
		
		try (Scanner in = new Scanner(new File("testdir/file3"))) {
			assertEquals("3 from b1 content", in.nextLine());
		}
		
		try (BufferedReader in = new BufferedReader(new FileReader(new File("testdir/file4")))) {
			
			assertEquals(Arrays.asList(
					"===============Content from revision 2 ========",
					"4 content",
					"===============Content from revision 3 ========",
					"4 b1 content")
			, in.lines().collect(Collectors.toList()));
		}
		
		try (Scanner in = new Scanner(new File("testdir/file5"))) {
			assertEquals("initial 5 content", in.nextLine());
		}
		
		assertFalse(Files.exists(Paths.get("testdir/file6")));
		
		try (Scanner in = new Scanner(new File("testdir/file7"))) {
			assertEquals("initial 7 content", in.nextLine());
		}
	}
}
