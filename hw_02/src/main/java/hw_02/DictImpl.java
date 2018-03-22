package hw_02;

public class DictImpl implements Dictionary {
	private Node[] arr;
	int arrSize;
	double maxLoadFactor = 0.75;
	private double minLoadFactor = 0.2;
	private final int minSize = 4;
	private int size = 0;

	public void clear() {
		size = 0;
		arrSize = minSize;
		resetArray();
	}
	
	private void resetArray() {
		arr = new Node[arrSize];
		for (int i = 0; i < arrSize; i++) {
			arr[i] = new Node();
		}
	}

	public DictImpl() {
		clear();
	}
	
	public DictImpl(double maxLoadFactor) {
		this();
		this.minLoadFactor = maxLoadFactor / 3;
		this.maxLoadFactor = maxLoadFactor;
	}

	public int size() {
		return size;
	}

	public boolean contains(String key) {
		return lookup(key).next != null;
	}

	public String get(String key) {
		Node p = lookup(key).next;
		if (p == null) {
			return null;
		}
		return p.value;
	}

	public String put(String key, String value) {
		Node p = lookup(key);
		String oldValue = null;
		if (p.next != null) {
			oldValue = p.next.value;
			p.next.value = value;
		} else {
			p.next = new Node(key, value);
			size++;
		}
		rehashIfNeeded();
		return oldValue;
	}

	public String remove(String key) {
		Node p = lookup(key);
		String oldValue = null;
		if (p.next != null) {
			oldValue = p.next.value;
			p.next = p.next.next;
			size--;
		}
		rehashIfNeeded();
		return oldValue;
	}

	private Node lookup(String s) {
		Node p = arr[arrIdx(s)];
		
		while (p.next != null && !p.next.key.equals(s)) {
			p = p.next;
		}
		
		return p;
	}
	
	private int arrIdx(String s) {
		return s.hashCode() % arrSize;
	}
	
	private void rehashIfNeeded() {
		if (size > arrSize * maxLoadFactor) {
			arrSize *= 2;
		} else if (size < arrSize * minLoadFactor) {
			if (arrSize >= 2 * minSize) {
				arrSize /= 2;
			}
		} else {
			return;
		}
		
		Node[] oldArr = arr;
		
		resetArray();
		
		for (int i = 0; i < oldArr.length; i++) {
			Node p = oldArr[i].next;
			while (p != null) {
				Node q = p;
				p = p.next;
				
				int currIdx = arrIdx(q.key);
				q.next = arr[currIdx].next;
				arr[currIdx].next = q;
			}
		}
	}
	
	private class Node {
		public Node(String key2, String value2) {
			key = key2;
			value = value2;
		}
		public Node() {}
		String key, value;
		Node next;
	}
}
