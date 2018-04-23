package hw_04;

import junit.framework.TestCase;
import java.util.*;

public class HomeworkTest extends TestCase {
	Function1<Double, Double> fPlus3_5;
	Function1<Double, Integer> fDiv2_0;
	Function1<Integer, String> length;
	Function2<String, Character, String> addChar;
	
	Predicate<Integer> isEven;

	public void setUp() {
		
		fPlus3_5 = new Function1<Double, Double>() {
			
			@Override
			public Double apply(Double arg) {
				return arg + 3.5;
			}
		};
		
		fDiv2_0 = new Function1<Double, Integer>() {
			
			@Override
			public Double apply(Integer arg) {
				return arg / 2.0;
			}
		};
		
		length = new Function1<Integer, String>() {
			
			@Override
			public Integer apply(String arg) {
				// TODO Auto-generated method stub
				return arg.length();
			}
		};
		
		addChar = new Function2<String, Character, String>() {
			
			@Override
			public String apply(Character arg1, String arg2) {
				return arg1 + arg2;
			}
		};
		
		isEven = new Predicate<Integer>() {
			
			@Override
			public boolean test(Integer arg) {
				return arg % 2 == 0;
			}
		};
	}
	
	public void testFunction1() {
		assertEquals(6.0, fDiv2_0.compose(fPlus3_5).apply(5));
	}
	
	public void testFunction2Apply() {
		assertEquals("abc", addChar.apply('a', "bc"));
	}
	
	public void testFunction2Compose() {
		assertEquals((Integer) 5, addChar.compose(length).apply('k', "abcd"));
	}
	
	public void testFunction2Bind1() {
		assertEquals("abc", addChar.bind1('a').apply("bc"));
	}
	
	public void testFunction2Bind2() {
		assertEquals("abc", addChar.bind2("bc").apply('a'));
	}
	
	public void testFunction2Curry() {
		assertEquals("abcd", addChar.curry().apply('a').apply("bcd"));
	}
	
	public void testPredicateBasic() {
		assertEquals(true, Predicate.ALWAYS_TRUE.test(new Object()));
		assertEquals(false, Predicate.ALWAYS_FALSE.test(null));

		assertEquals(true, isEven.test(2));
		assertEquals(false, isEven.test(3));
	}
	
	public void testPredicateOrAndNot() {
		assertEquals(true, Predicate.ALWAYS_TRUE.or(isEven).test(3));
		assertEquals(false, isEven.and(Predicate.ALWAYS_FALSE).test(2));
		assertEquals(true, Predicate.ALWAYS_FALSE.not().test(null));
	}
	
	public void testCollectionsMap() {
		assertEquals(Arrays.asList(0.5, 1.0, 1.5), Collections.map(fDiv2_0, Arrays.asList(1,2,3)));
	}
	
	public void testCollectionsFilter() {
		assertEquals(Arrays.asList(2,4), Collections.filter(isEven, Arrays.asList(1,2,3,4,5)));
		assertEquals(new ArrayList(), Collections.filter(Predicate.ALWAYS_FALSE, Arrays.asList(1,2,3,4,5)));

	}
	
	public void testTakeWhile() {
		assertEquals(Arrays.asList(1,3), Collections.takeWhile(isEven.not(), Arrays.asList(1,3,4,5)));
		assertEquals(Arrays.asList(1,3,4,5), Collections.takeWhile(Predicate.ALWAYS_TRUE, Arrays.asList(1,3,4,5)));
	}
	
	public void testTakeUnless() {
		assertEquals(Arrays.asList(1,3), Collections.takeUnless(isEven, Arrays.asList(1,3,4,5)));
	}
	
	public void testFoldr() {
		assertEquals("abcd", Collections.foldr(addChar, "", Arrays.asList('a', 'b', 'c', 'd')));
	}
	
	public void testFoldl() {
		assertEquals("abcd", Collections.foldl(
				new Function2<String, String, Character>() {
					@Override
					public String apply(String arg1, Character arg2) {
						return arg1 + arg2;
					}
					
		}
				, "", Arrays.asList('a', 'b', 'c', 'd')));
	}
}
