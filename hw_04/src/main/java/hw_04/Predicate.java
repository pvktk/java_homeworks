package hw_04;

public abstract class Predicate<A1> {
	public static final Predicate<Object> ALWAYS_TRUE = new Predicate<Object>() {

		@Override
		public boolean test(Object arg) {
			return true;
		}
		
	};
	
	public static final Predicate<Object> ALWAYS_FALSE = new Predicate<Object>() {

		@Override
		public boolean test(Object arg) {
			return false;
		}
		
	};
	
	abstract public boolean test(A1 arg);
	
	public <T extends A1 >Predicate<T> or(Predicate<? super T> other) {
		return new Predicate<T>() {
			@Override
			public boolean test(T arg) {
				return Predicate.this.test(arg) || other.test(arg);
			}
		};
	}
	
	public <T extends A1> Predicate<T> and(Predicate<? super T> other) {
		return new Predicate<T>() {
			@Override
			public boolean test(T arg) {
				return Predicate.this.test(arg) && other.test(arg);
			}
		};
	}
	
	public Predicate<A1> not() {
		return new Predicate<A1>() {
			@Override
			public boolean test(A1 arg) {
				return !Predicate.this.test(arg);
			}
		};
	}
}
