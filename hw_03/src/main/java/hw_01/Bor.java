package hw_01;

import java.io.*;

public class Bor implements Trie, StreamSerializable {
	final private Node bor;

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
			int currIdx = arrayIndex(element.charAt(i));
			
			if (p.lifes[currIdx] == null) {
				p.lifes[currIdx] = new Node();
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
			int currIdx = arrayIndex(element.charAt(i));

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
			int currIdx = arrayIndex(element.charAt(i));
			
			if (p.lifes[currIdx].nwords == 1) {
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
		return bor.nwords;
	}

	public int howManyStartsWithPrefix(String prefix) {
		Node p = bor;
		for (int i = 0; i < prefix.length(); i++) {
			int currIdx = arrayIndex(prefix.charAt(i));
			
			if (p.lifes[currIdx] == null) {
				return 0;
			}
			p = p.lifes[currIdx];
		}
		
		return p.nwords;
	}

	private int arrayIndex(char a) {
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
	
	private void serializeNode(Node r, DataOutputStream dos) throws IOException {
		dos.writeBoolean(r.isTerminal);
		dos.writeInt(r.nwords);
		
		for(int i = 0; i < r.lifes.length; i++) {
			if (r.lifes[i] == null) {
				dos.writeBoolean(false);
			} else {
				dos.writeBoolean(true);
				serializeNode(r.lifes[i], dos);
			}
		}
		
	}
	
	private void deserializeNode(Node r, DataInputStream dis) throws IOException {
		r.isTerminal = dis.readBoolean();
		r.nwords = dis.readInt();
		
		for(int i = 0; i < r.lifes.length; i++) {
			boolean b = dis.readBoolean();
			if (b) {
				r.lifes[i] = new Node();
				deserializeNode(r.lifes[i], dis);
			}
		}
	}
	
	@Override
	public void serialize(OutputStream out) throws IOException {
		serializeNode(bor, new DataOutputStream(out));
		out.flush();
	}

	@Override
	public void deserialize(InputStream in) throws IOException {
		deserializeNode(bor, new DataInputStream(in));;
	}
}
