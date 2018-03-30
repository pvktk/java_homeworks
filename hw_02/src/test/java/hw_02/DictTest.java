package hw_02;

import junit.framework.TestCase;

public class DictTest extends TestCase {
	
	private DictImpl d;

	public void setUp() {
		d = new DictImpl();
	}

	public void testPutContains() {
		assertEquals(0, d.size());
		assertFalse(d.contains("a"));
		assertEquals(null, d.put("a", "a1"));
		assertEquals("a1", d.put("a", "a2"));
		assertEquals(1, d.size());
	}
	
	public void testGet() {
		assertEquals(null, d.get("a"));
		d.put("a", "a1");
		d.put("b", "b1");
		assertEquals("a1", d.get("a"));
		assertEquals("b1", d.get("b"));
		assertEquals(2, d.size());
	}
	
	public void testRemove() {
		assertEquals(null, d.remove("a"));
		assertEquals(0, d.size());
		d.put("a", "a1");
		d.put("b", "b1");
		assertEquals("a1", d.remove("a"));
		assertEquals(1, d.size());
		assertEquals("b1", d.remove("b"));
		assertEquals(0, d.size());
	}
	
	public void testRehash() {
		d.put("a", "a1");
		d.put("b", "b1");
		d.put("c", "c1");
		d.put("d", "d1");
		//после 4 элементов должен произойти rehash
		assertEquals(4, d.size());
		assertEquals(8, d.arrSize);
		assertEquals("a1", d.get("a"));
		assertEquals("b1", d.get("b"));
		assertEquals("c1", d.get("c"));
		assertEquals("d1", d.get("d"));
	}
	
	public void testList() {
		d.arrSize = 1;
		d.maxLoadFactor = 100;
		
		d.put("a", "a1");
		d.put("b", "b1");
		d.put("c", "c1");

		assertEquals("a1", d.remove("a"));
		assertEquals("b1", d.get("b"));
		assertEquals("c1", d.remove("c"));
		
		assertEquals(1, d.size());
	}
	
	public void testClear() {
		d.put("a", "a1");
		
		d.clear();
		
		assertEquals(0, d.size());
		assertEquals(null, d.get("a"));
	}
}
