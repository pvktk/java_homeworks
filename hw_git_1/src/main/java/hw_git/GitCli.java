package hw_git;

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
			case "commit":
				System.out.println("Commiting...");
				core.makeCommit(args[1], Arrays.copyOfRange(args, 2, args.length));
				System.out.println("Commit made at revision " + core.getCurrentRevision());
				break;
			case "checkout":
				revision = Integer.parseInt(args[1]);
				System.out.println("Check out to revision " + revision);
				core.makeCheckout(revision);
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
			default:
				System.out.println("Unknown argument: " + args[0]);
		}
		} catch (UnversionedException e) {
			System.out.println("This directory is not versioned");
		}
	}
}
