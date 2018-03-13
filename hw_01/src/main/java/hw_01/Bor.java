package hw_01;

public class Bor implements Trie {
	public Bor() {
		bor = new Node();
	}

	public boolean add(String element) {
		if (contains(element)) {
			return false;
		}
		
		Node p = bor;
		p.nwords++;
		for (int i = 0; i < element.length(); i++) {
			if (p.lifes[arr_num(element.charAt(i))] == null) {
				p.lifes[arr_num(element.charAt(i))] = new Node();
				size++;
			}
			p = p.lifes[arr_num(element.charAt(i))];
			p.nwords++;
		}
		
		p.isTerminal = true;
		return true;
	}

	public boolean contains(String element) {
		Node p = bor;
		for (int i = 0; i < element.length(); i++) {
			if (p.lifes[arr_num(element.charAt(i))] == null) {
				return false;
			}
			p = p.lifes[arr_num(element.charAt(i))];
		}
		
		if (p.isTerminal) {
			return true;
		}

		return false;
	}

	public boolean remove(String element) {
		if (!contains(element)) {
			return false;
		}
		
		Node p = bor;
		p.nwords--;
		for (int i = 0; i < element.length(); i++) {
			if (p.lifes[arr_num(element.charAt(i))].nwords == 1) {
				size -= element.length() - i;
				p.lifes[arr_num(element.charAt(i))] = null;
				return true;
			}
			p = p.lifes[arr_num(element.charAt(i))];
			p.nwords--;
		}
		
		p.isTerminal = false;
		
		return true;
	}

	public int size() {
		return size;
	}

	public int howManyStartsWithPrefix(String prefix) {
		Node p = bor;
		for (int i = 0; i < prefix.length(); i++) {
			if (p.lifes[arr_num(prefix.charAt(i))] == null) {
				return 0;
			}
			p = p.lifes[arr_num(prefix.charAt(i))];
		}
		
		return p.nwords;
	}

	private Node bor;
	private int size = 0;
	private int arr_num(char a) {
		if ('a' <= a && a <= 'z') {
			return a - 'a';
		}
		
		if ('A' <= a && a <= 'Z') {
			return a - 'A' + 'z' - 'a' + 1;
		}
		
		return -1;
	}
	private class Node {
		public boolean isTerminal;
		public int nwords;
		Node[] lifes = new Node[2*('z' - 'a' + 1)];
	}
}
