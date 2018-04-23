package hw_04;

public interface Function1<R, A1> {
	R apply(A1 arg);

	default <Rg> Function1<Rg, A1> compose(Function1<Rg, ? super R> g) {
		return new Function1<Rg, A1>() {

			@Override
			public Rg apply(A1 arg) {
				return g.apply(Function1.this.apply(arg));
			}
		};
	}
}
