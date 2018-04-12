package hw_01;

import java.io.*;

import junit.framework.TestCase;

public class TrieImplTest extends TestCase {
	private Bor b;

	public void setUp() {
		b = new Bor();
	}

	public void testAddSizeContains() {
		assertFalse(b.contains("Hello"));
		assertTrue(b.add("Hello"));
		assertTrue(b.contains("Hello"));
		assertEquals(1, b.size());
		assertEquals(1, b.howManyStartsWithPrefix("Hell"));
	}

	public void testAddContainsPrefix() {
		b.add("Hello");
		assertFalse(b.contains("Hell"));
		assertTrue(b.add("Hell"));
		assertTrue(b.contains("Hell"));
		assertEquals(2, b.size());
		assertEquals(2, b.howManyStartsWithPrefix("Hell"));
	}

	public void testAddOutstandingWord() {
		b.add("Hello");
		assertTrue(b.add("Head"));
		assertEquals(2, b.size());
	}

	public void testRemovePrefix() {
		b.add("Hello");
		b.add("Hell");
		assertFalse(b.remove("He"));
		assertTrue(b.remove("Hell"));
		assertEquals(1, b.size());
		assertEquals(1, b.howManyStartsWithPrefix("Hell"));
	}

	public void testTemoveSuffix() {
		b.add("Hello");
		b.add("Hell");
		assertTrue(b.remove("Hello"));
		assertEquals(1, b.size());
	}
	
	public void testSerialDeserial() throws IOException {
		b.add("Hello");
		b.add("Head");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		b.serialize(baos);
		byte[] buf1 = baos.toByteArray();
		
		Bor b1 = new Bor();
		b1.deserialize(new ByteArrayInputStream(buf1));
		baos = new ByteArrayOutputStream();
		b1.serialize(baos);
		byte[] buf2 = baos.toByteArray();
		
		assertEquals(buf1.length, buf2.length);
		
		for(int i = 0; i < buf1.length; i++) {
			assertEquals(buf1[i], buf2[i]);
		}
		
		assertTrue(b1.contains("Hello"));
		assertTrue(b1.contains("Head"));
		assertEquals(2, b1.size());
	}

}
