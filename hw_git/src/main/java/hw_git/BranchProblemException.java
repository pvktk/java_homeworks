package hw_git;

public class BranchProblemException extends Exception {
	String message;
	public BranchProblemException(String s) {
		message = s;
	}
}
