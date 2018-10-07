package hw_git;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class GitCli {
	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {
		
		GitCore core = new GitCore();
		int revision;

		try {
		switch (args[0]) {
			case "init":
				core.makeInit();
				break;
			case "add":
				System.out.println("Addition...");
				core.makeAdd(Arrays.copyOfRange(args, 1, args.length));
				break;
			case "commit":
				System.out.println("Commiting...");
				core.makeCommit(args[1]);
				System.out.println("Commit made at revision " + core.getCurrentRevision());
				break;
			case "checkout":
				try {
					revision = Integer.parseInt(args[1]);
					System.out.println("Check out to revision " + revision);
					core.makeCheckout(revision);
				} catch (NumberFormatException e) {
					core.makeCheckout(Arrays.copyOfRange(args, 2, args.length));
				}
				break;
			case "reset":
				revision = Integer.parseInt(args[1]);
				System.out.println("Performing reset to revision " + revision);
				core.makeReset(revision);
				break;
			case "log":
				revision = args.length == 2 ? Integer.parseInt(args[1]) : -1;
				System.out.println("Log: " + core.getLog(revision));
				break;
			case "rm":
				System.out.println("Removing...");
				core.makeRM(Arrays.copyOfRange(args, 1, args.length));
				break;
			case "status":
				core.findRepInformation();
				System.out.println("Status:");
				System.out.println("Deleted files:\n________________");
				for (String fname : core.getDeletedFiles()) {
					System.out.println(fname);
				}
				System.out.println("Changed files:\n________________");
				for (String fname : core.getChangedFiles()) {
					System.out.println(fname);
				}
				System.out.println("Untracked files:\n________________");
				for (String fname : core.getUntrackedFiles()) {
					System.out.println(fname);
				}
				break;
			default:
				System.out.println("Unknown argument: " + args[0]);
		}
		} catch (UnversionedException e) {
			System.out.println("This directory is not versioned");
		} catch (BranchProblemException e) {
			System.out.println(e.message);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}
}
