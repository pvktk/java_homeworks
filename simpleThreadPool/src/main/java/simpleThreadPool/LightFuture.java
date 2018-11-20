package simpleThreadPool;

import java.util.function.Function;

public interface LightFuture<R> {
	
	public boolean isReady();
	
	public R get() throws LightExecutionException, InterruptedException;
	
	public <R1> LightFuture<R1> thenApply(Function<R, R1> nextFunc);
}
