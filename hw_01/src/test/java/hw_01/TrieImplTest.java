package hw_01;

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
		assertEquals(5, b.size());
		assertEquals(1, b.howManyStartsWithPrefix("Hell"));
	}

	public void testAddContainsPrefix() {
		b.add("Hello");
		assertFalse(b.contains("Hell"));
		assertTrue(b.add("Hell"));
		assertTrue(b.contains("Hell"));
		assertEquals(5, b.size());
		assertEquals(2, b.howManyStartsWithPrefix("Hell"));
	}

	public void testAddOutstandingWord() {
		b.add("Hello");
		assertTrue(b.add("Head"));
		assertEquals(7, b.size());
	}

	public void testRemovePrefix() {
		b.add("Hello");
		b.add("Hell");
		assertFalse(b.remove("He"));
		assertTrue(b.remove("Hell"));
		assertEquals(5, b.size());
		assertEquals(1, b.howManyStartsWithPrefix("Hell"));
	}

	public void testTemoveSuffix() {
		b.add("Hello");
		b.add("Hell");
		assertTrue(b.remove("Hello"));
		assertEquals(4, b.size());
	}
/*
	public void testBorBigTest() {
		//b = new Bor();
		//Добавление
		assertFalse(b.contains("Hello"));
		assertTrue(b.add("Hello"));
		assertTrue(b.contains("Hello"));
		assertEquals(5, b.size());
		assertFalse(b.add("Hello"));
		assertEquals(5, b.size());
		assertFalse(b.contains("Hell"));
		assertTrue(b.add("Hell"));
		assertTrue(b.contains("Hell"));
		assertEquals(5, b.size());
		assertFalse(b.add("Hell"));
		assertEquals(5, b.size());
		assertFalse(b.contains("Head"));
		assertTrue(b.add("Head"));
		assertTrue(b.contains("Head"));
		assertEquals(7, b.size());
		assertFalse(b.contains(""));
		assertTrue(b.add(""));
		assertTrue(b.contains(""));
		assertEquals(7, b.size());
		
		//Проверка префиксов
		assertEquals(3, b.howManyStartsWithPrefix("H"));
		assertEquals(4, b.howManyStartsWithPrefix(""));
		assertEquals(1, b.howManyStartsWithPrefix("Hea"));
		assertEquals(2, b.howManyStartsWithPrefix("Hell"));
		assertEquals(0, b.howManyStartsWithPrefix("h"));
		
		//Удаление
		assertFalse(b.remove("h"));
		assertFalse(b.remove("H"));
		assertTrue(b.remove("Hell"));
		assertFalse(b.remove("Hell"));
		assertFalse(b.contains("Hell"));
		assertEquals(7, b.size());
		assertEquals(1, b.howManyStartsWithPrefix("Hell"));
		
		assertTrue(b.remove(""));
		assertFalse(b.remove(""));
		assertEquals(7, b.size());
		assertEquals(2, b.howManyStartsWithPrefix(""));
		
		assertTrue(b.remove("Hello"));
		assertFalse(b.remove("Hello"));
		assertEquals(4, b.size());
		
		assertTrue(b.contains("Head"));
		assertTrue(b.add("HeadZzZ"));
		assertEquals(7, b.size());
		assertEquals(2, b.howManyStartsWithPrefix("Head"));
		assertEquals(1, b.howManyStartsWithPrefix("HeadZzZ"));
		assertFalse(b.remove("HeadZ"));
		assertTrue(b.remove("HeadZzZ"));
		assertEquals(4, b.size());
		assertEquals(1, b.howManyStartsWithPrefix("Head"));
		assertEquals(1, b.howManyStartsWithPrefix(""));
		assertEquals(0, b.howManyStartsWithPrefix("HeadZ"));
		
		assertTrue(b.remove("Head"));
		assertEquals(0, b.size());
	}
*/
}
