package simpleThreadPool;

import java.util.function.Supplier;

public interface ThreadPool {
	public <R> LightFuture<R> submit(Supplier<R> supp);
	
	public void shutdown();
}
