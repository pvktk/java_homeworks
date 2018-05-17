package hw_04;

import java.util.*;

public class Collections {
	public static <R1, R extends R1, A> List<R1> map(Function1<R, A> f, Iterable<? extends A> a) {
		ArrayList<R1> res = new ArrayList<R1>();
		for (A i : a) {
			res.add(f.apply(i));
		}
		return res;
	}
	
	public static <A1, A extends A1> List<A1> filter(Predicate<A> p, Iterable<? extends A> a) {
		ArrayList<A1> res = new ArrayList<A1>();
		for (A i : a) {
			if (p.test(i)) {
				res.add(i);
			}
		}
		return res;
	}
	
	public static <A1, A extends A1> List<A1> takeWhile(Predicate<A> p, Iterable<? extends A> a) {
		ArrayList<A1> res = new ArrayList<A1>();
		for (A i : a) {
			if (!p.test(i)) {
				return res;
			}
			res.add(i);
		}
		return res;
	}
	
	public static <A1, A extends A1> List<A1> takeUnless(Predicate<A> p, Iterable<? extends A> a) {
		return takeWhile(p.not(), a);
	}
	
	private static <R, A> R foldr(Function2<R, A, R> f, R ini, Iterator<? extends A> iter) {
		if (!iter.hasNext()) {
			return ini;
		}
		return f.apply(iter.next(), foldr(f, ini, iter));
	}
	
	public static <R, A> R foldr(Function2<R, A, R> f, R ini, Collection<? extends A> col) {
		return foldr(f, ini, col.iterator());
	}
	
	public static <R, A> R foldl(Function2<R, R, A> f, R ini, Collection<? extends A> col) {
		Iterator<? extends A> iter = col.iterator();
		while (iter.hasNext()) {
			ini = f.apply(ini, iter.next());
		}
		return ini;
	}
}
