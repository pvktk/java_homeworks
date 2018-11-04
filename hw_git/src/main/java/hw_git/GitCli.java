package hw_git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class GitCli {
	public static void printFile(Path p) {
		try (Scanner in = new Scanner(new File(p.toString()))) {
			System.out.println(p.toString());
			System.out.println("--------------------------------");
			while (in.hasNextLine()) {
				System.out.println(in.nextLine());
			}
			System.out.println("--------------------------------");
		} catch (FileNotFoundException e) {
			System.out.println("File " + p.toString() + " not found");
		}
	}

	public static Path conflictFileChooser(Path p1, Path p2) {
		System.out.println("merge conflict between " + p1.toString() + " and " + p2.toString());
		printFile(p1);
		printFile(p2);
		int choice = 0;
		while (!(choice == 1 || choice == 2 || choice == 3)) {
			System.out.println("Enter \"1\", if you want to use first file in merging\n"
					+ "Enter \"2\" to use second\n"
					+ "Enter \"3\" to specify file manually");
			try (Scanner in = new Scanner(System.in)) {
				choice = in.nextInt();
			} catch (Exception e) {}
		}
		
		switch (choice) {
		case 1:
			return p1;
		case 2:
			return p2;
		default:
			String choosenPath = null;
			System.out.println("Specify path to file:");
			try (Scanner in = new Scanner(System.in)) {
				choosenPath = in.nextLine();
			}
			return Paths.get(choosenPath);
		}
	}

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException{
		
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
				System.out.println("Commit made at revision " + (core.getCurrentRevision() + 1));
				break;
			case "checkout":
				if (args[1].equals("--")) {
					System.out.println("Checking out files...");
					core.makeCheckout(Arrays.copyOfRange(args, 2, args.length));
				} else {
					try {
						revision = Integer.parseInt(args[1]);
						System.out.println("Check out to revision " + revision);
						core.makeCheckout(revision - 1);
					} catch (NumberFormatException e) {
						System.out.println("Checking out branch...");
						core.makeCheckout(args[1]);
					}
				}
				break;
			case "reset":
				revision = Integer.parseInt(args[1]);
				System.out.println("Performing reset to revision " + revision);
				core.makeReset(revision - 1);
				break;
			case "log":
				revision = args.length == 2 ? Integer.parseInt(args[1]) : 0;
				System.out.println("Log: " + core.getLog(revision - 1));
				break;
			case "rm":
				System.out.println("Removing...");
				core.makeRM(Arrays.copyOfRange(args, 1, args.length));
				break;
			case "status":
				System.out.println(core.getStatus());
				break;
			case "branch":
				if (args[1].equals("-d")) {
					System.out.println("Deleting branch " + args[2]);
					core.makeDeleteBranch(args[2]);
				} else {
					System.out.println("Making branch " + args[1]);
					core.makeBranch(args[1]);
				}
				break;
			case "merge":
				System.out.println("Merging branch " + args[1] + " to " + core.getCurrentBranchName());
				core.makeMerge(args[1], GitCli::conflictFileChooser);
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
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Lack of arguments");
		}
	}
}
