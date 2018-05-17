package hw_04;

public abstract class Function2<R, A1, A2> {
	public abstract R apply(A1 arg1, A2 arg2);
	
	public <Rcomp, Rg extends Rcomp, A1comp extends A1, A2comp extends A2>
		Function2<Rcomp, A1comp, A2comp> compose(Function1<Rg, ? super R> g) {
		return new Function2<Rcomp, A1comp, A2comp>() {

			@Override
			public Rg apply(A1comp arg1, A2comp arg2) {
				return g.apply(Function2.this.apply(arg1, arg2));
			}
			
		};
	}
	
	public Function1<R, A2> bind1(A1 arg1) {
		return new Function1<R, A2>() {

			@Override
			public R apply(A2 arg) {
				return Function2.this.apply(arg1, arg);
			}
		};
	}
	
	public Function1<R, A1> bind2(A2 arg2) {
		return new Function1<R, A1>() {

			@Override
			public R apply(A1 arg) {
				return Function2.this.apply(arg, arg2);
			}
		};
	}
	
	public Function1<Function1<R, A2>, A1> curry() {
		return new Function1<Function1<R, A2>, A1>() {

			@Override
			public Function1<R, A2> apply(A1 arg) {
				return Function2.this.bind1(arg);
			}
			
		};
	}
}
