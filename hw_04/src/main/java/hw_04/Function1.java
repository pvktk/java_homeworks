package hw_04;

public interface Function1<R, A1> {
	R apply(A1 arg);

	default <Rcomp, Rg extends Rcomp, Acomp extends A1> Function1<Rcomp, Acomp> compose(Function1<Rg, ? super R> g) {
		return new Function1<Rcomp, Acomp>() {

			@Override
			public Rg apply(Acomp arg) {
				return g.apply(Function1.this.apply(arg));
			}
		};
	}
}
