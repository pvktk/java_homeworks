package hw_04;

import junit.framework.TestCase;
import java.util.*;

import hw_04.HomeworkTest.C0;
import hw_04.HomeworkTest.C1;
import hw_04.HomeworkTest.D0;
import hw_04.HomeworkTest.D1;
import hw_04.HomeworkTest.D2;

public class HomeworkTest extends TestCase {
	final static Function1<Double, Double> fPlus3_5 = new Function1<Double, Double>() {
		
		@Override
		public Double apply(Double arg) {
			return arg + 3.5;
		}
	};
	
	
	final static Function1<Double, Integer> fDiv2_0 = new Function1<Double, Integer>() {
		
		@Override
		public Double apply(Integer arg) {
			return arg / 2.0;
		}
	};
	
	final static Function1<Integer, String> length = new Function1<Integer, String>() {
		
		@Override
		public Integer apply(String arg) {
			// TODO Auto-generated method stub
			return arg.length();
		}
	};
	
	final static Function2<String, Character, String> addChar = 
			new Function2<String, Character, String>() {
		
		@Override
		public String apply(Character arg1, String arg2) {
			return arg1 + arg2;
		}
	};
	
	final static Predicate<Integer> isEven = new Predicate<Integer>() {
		
		@Override
		public boolean test(Integer arg) {
			return arg % 2 == 0;
		}
	};
	
	final static Predicate<Integer> failer = new Predicate<Integer>() {
		
		@Override
		public boolean test(Integer arg) {
			// TODO Auto-generated method stub
			int a = 3 / 0;
			return false;
		}
	};
	
	static class D0 {
		public boolean b = true;
	}
	
	static class D1 extends D0 {}
	static class D2 extends D1 {}
	static class D3 extends D2 {}
	
	class C0 {}
	class C1 extends C0 {}
	class C2 extends C1 {}
	
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
	
	public void testFunctionsInheritance() {
		Function1<C1, D1> g = null;

		Function1<D2, C0> f = new Function1<HomeworkTest.D2, HomeworkTest.C0>() {
			
			@Override
			public D2 apply(C0 arg) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		Function1<C0, C1> fg = f.compose(g);
		
		Function2<D2, C0, C1> f2 = new Function2<HomeworkTest.D2, HomeworkTest.C0, HomeworkTest.C1>() {
			
			@Override
			public D2 apply(C0 arg1, C1 arg2) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		Function2<C0, C1, C1> f2g = f2.compose(g);

	}
	
	public void testPredicateBasic() {
		assertTrue(Predicate.ALWAYS_TRUE.test(new Object()));
		assertFalse(Predicate.ALWAYS_FALSE.test(null));

		assertTrue(isEven.test(2));
		assertFalse(isEven.test(3));
	}
	
	public void testPredicateOrAndNot() {
		assertTrue(Predicate.ALWAYS_TRUE.or(isEven).test(3));
		assertFalse(isEven.and(Predicate.ALWAYS_FALSE).test(2));
		assertTrue(Predicate.ALWAYS_FALSE.not().test(null));
	}
	
	public void testPredicateLazyness() {
		assertTrue(Predicate.ALWAYS_TRUE.or(failer).test(null));
		assertFalse(Predicate.ALWAYS_FALSE.and(failer).test(null));
	}
	
	public void testPredicatesInheritance() {
		Predicate<D0> pd0 = new Predicate<HomeworkTest.D0>() {
			
			@Override
			public boolean test(D0 arg) {
				// TODO Auto-generated method stub
				return false;
			}
		};
		
		Predicate<D1> pd1 = new Predicate<HomeworkTest.D1>() {
			
			@Override
			public boolean test(D1 arg) {
				// TODO Auto-generated method stub
				return true;
			}
		};
		
		Predicate<D2> pd2 = pd0.and(pd1);
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
	
	public void testCollectionsInheritance() {
		List<D0> id0 = Collections.map(new Function1<D1, D2>(){

			@Override
			public D1 apply(D2 arg) {
				// TODO Auto-generated method stub
				return null;
			}}, Arrays.asList(new D2(), new D3()));
		
		id0 = Collections.filter(new Predicate<D2>() {

			@Override
			public boolean test(D2 arg) {
				// TODO Auto-generated method stub
				return false;
			}
		}, Arrays.asList(new D2(), new D3()));

		D0 foldResD0 = Collections.foldr(new Function2<D1, C1, D1>() {

			@Override
			public D1 apply(C1 arg1, D1 arg2) {
				// TODO Auto-generated method stub
				return null;
			}
			
		}, new D1(), Arrays.asList(new C1()));
	}
}
