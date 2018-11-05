package hw_git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

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

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException {
		System.out.println(
				processArgs(args)
				.stream()
				.collect(
						Collectors.joining("\n")
						)
				);
	}
	
	public static ArrayList<String> processArgs(String[] args) throws JsonGenerationException, JsonMappingException{
		
		ArrayList<String> res = new ArrayList<>();
		
		GitCore core = new GitCore();
		int revision;

		try {
		switch (args[0]) {
			case "init":
				core.makeInit();
				res.add("Repository initiated.");
				break;
			case "add":
				res.add("Addition...");
				core.makeAdd(Arrays.copyOfRange(args, 1, args.length));
				break;
			case "commit":
				res.add("Commiting...");
				core.makeCommit(args[1]);
				res.add("Commit made at revision " + (core.getCurrentRevision() + 1));
				break;
			case "checkout":
				if (args[1].equals("--")) {
					res.add("Checking out files...");
					core.makeCheckout(Arrays.copyOfRange(args, 2, args.length));
				} else {
					try {
						revision = Integer.parseInt(args[1]);
						res.add("Checkout to revision " + revision);
						core.makeCheckout(revision - 1);
						res.add("HEAD detached on revison " + revision);
					} catch (NumberFormatException e) {
						res.add("Checking out branch...");
						core.makeCheckout(args[1]);
					}
				}
				break;
			case "reset":
				revision = Integer.parseInt(args[1]);
				res.add("Performing reset to revision " + revision);
				core.makeReset(revision - 1);
				break;
			case "log":
				revision = args.length == 2 ? Integer.parseInt(args[1]) : 0;
				res.add("Log:");
				res.addAll(core.getLog(revision - 1));
				break;
			case "rm":
				res.add("Removing...");
				core.makeRM(Arrays.copyOfRange(args, 1, args.length));
				break;
			case "status":
				res.addAll(core.getStatus());
				break;
			case "branch":
				if (args[1].equals("-d")) {
					res.add("Deleting branch " + args[2]);
					core.makeDeleteBranch(args[2]);
				} else {
					res.add("Making branch " + args[1]);
					core.makeBranch(args[1]);
				}
				break;
			case "merge":
				res.add("Merging branch " + args[1] + " to " + core.getCurrentBranchName());
				core.makeMerge(args[1], GitCli::conflictFileChooser);
				break;
			default:
				res.add("Unknown argument: " + args[0]);
		}
		} catch (UnversionedException e) {
			res.add("This directory is not versioned");
		} catch (BranchProblemException e) {
			res.add(e.message);
		} catch (FileNotFoundException e) {
			res.add(e.getMessage());
		} catch (IOException e) {
			res.add("IOException: " + e.getMessage());
		} catch (ArrayIndexOutOfBoundsException e) {
			res.add("Lack of arguments");
		}
		
		return res;
	}
}
