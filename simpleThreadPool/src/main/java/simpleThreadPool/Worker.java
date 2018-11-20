package simpleThreadPool;

import java.util.Queue;
import java.util.function.Function;

public class Worker implements Runnable {
	
	private Queue<SupplierFuturePair> queue;
	
	public Worker(Queue<SupplierFuturePair> q) {
		queue = q;
	}
	
	static class LightFutureImpl<R> implements LightFuture<R> {
		private volatile boolean isready;
		private ThreadPool masterPool;
		
		private R result;
		private LightExecutionException execExcept;
		
		public LightFutureImpl(ThreadPool tp) {
			masterPool = tp;
		}

		@Override
		public boolean isReady() {
			return isready;
		}

		@Override
		public R get() throws LightExecutionException, InterruptedException {
			if (!isready) {
				synchronized (this) {
					while (!isready) {
						this.wait();
					}
				}
			}

			if (execExcept != null) {
				throw execExcept;
			}
			
			return result;
		}

		@Override
		public <R1> LightFuture<R1> thenApply(Function<R, R1> nextFunc) {
			return masterPool.submit(() -> {
				R1 res = null;
				try {
					res =  nextFunc.apply(get());
				} catch (Exception e) {
					throw new RuntimeException();
				}
				return res;
			});
		}

	}
	
	@Override
	public void run() {
a:		while (true) {
			SupplierFuturePair workPair;

			synchronized (queue) {
				while (queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
						break a;
					}

				}
				workPair = queue.poll();
			}

			LightFutureImpl future = workPair.future;
			synchronized (future) {

				try {
					future.result = workPair.supplier.get();
				} catch (Exception e) {
					future.execExcept = new LightExecutionException();
				}

				future.isready = true;
				future.notifyAll();
			}
		}
	}

}
