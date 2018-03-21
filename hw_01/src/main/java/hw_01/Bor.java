package hw_01;

public class Bor implements Trie {
	final private Node bor;
	private int size = 0;

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
			int currIdx = arrNum(element.charAt(i));
			
			if (p.lifes[currIdx] == null) {
				p.lifes[currIdx] = new Node();
				size++;
			}
			p = p.lifes[currIdx];
			p.nwords++;
		}
		
		p.isTerminal = true;
		return true;
	}

	public boolean contains(String element) {
		Node p = bor;
		for (int i = 0; i < element.length(); i++) {
			int currIdx = arrNum(element.charAt(i));

			if (p.lifes[currIdx] == null) {
				return false;
			}
			p = p.lifes[currIdx];
		}
	
		return p.isTerminal;
	}

	public boolean remove(String element) {
		if (!contains(element)) {
			return false;
		}
		
		Node p = bor;
		p.nwords--;
		for (int i = 0; i < element.length(); i++) {
			int currIdx = arrNum(element.charAt(i));
			
			if (p.lifes[currIdx].nwords == 1) {
				size -= element.length() - i;
				p.lifes[currIdx] = null;
				return true;
			}
			p = p.lifes[currIdx];
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
			int currIdx = arrNum(prefix.charAt(i));
			
			if (p.lifes[currIdx] == null) {
				return 0;
			}
			p = p.lifes[currIdx];
		}
		
		return p.nwords;
	}

	private int arrNum(char a) {
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
