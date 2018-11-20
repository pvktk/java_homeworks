package simpleThreadPool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;

import simpleThreadPool.Worker.LightFutureImpl;

public class ThreadPoolImpl implements ThreadPool{
	
	private Queue<SupplierFuturePair> tasks = new LinkedList<>();
	
	private List<Thread> pool = new ArrayList<>();
	
	public ThreadPoolImpl(int nthreads) {
		for (int i = 0; i < nthreads; i++) {
			Thread t = new Thread(new Worker(tasks));
			t.setDaemon(true);
			pool.add(t);
			t.start();
		}
	}
	
	@Override
	public <R> LightFuture<R> submit(Supplier<R> supp) {
		SupplierFuturePair pair = new SupplierFuturePair(supp,
				new LightFutureImpl(this));
		
		synchronized (tasks) {
			tasks.add(pair);
			tasks.notify();
		}
		
		return pair.future;
	}

	@Override
	public void shutdown() {
		for (Thread t : pool) {
			t.interrupt();
		}
	}
}