package hw_04;

import java.util.*;

public class Collections {
	public static <R, A> List<R> map(Function1<R, A> f, Iterable<? extends A> a) {
		ArrayList<R> res = new ArrayList<R>();
		for (A i : a) {
			res.add(f.apply(i));
		}
		return res;
	}
	
	public static <A> List<A> filter(Predicate<A> p, Iterable<? extends A> a) {
		ArrayList<A> res = new ArrayList<A>();
		for (A i : a) {
			if (p.test(i)) {
				res.add(i);
			}
		}
		return res;
	}
	
	public static <A> List<A> takeWhile(Predicate<A> p, Iterable<? extends A> a) {
		ArrayList<A> res = new ArrayList<A>();
		for (A i : a) {
			if (!p.test(i)) {
				return res;
			}
			res.add(i);
		}
		return res;
	}
	
	public static <A> List<A> takeUnless(Predicate<A> p, Iterable<? extends A> a) {
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
