package simpleThreadPool;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Assert;
import org.junit.Test;

public class PoolTest extends Assert{

	@Test
	public void testBasicWorkability() throws LightExecutionException, InterruptedException {
		ThreadPool pool = new ThreadPoolImpl(1);
		LightFuture<Integer> f1 = pool.submit(() -> 1);
		LightFuture<Integer> f2 = pool.submit(() -> 2);
		assertEquals(Integer.valueOf(1), f1.get());
		assertEquals(Integer.valueOf(2), f2.get());
	}
	
	@Test
	public void testFutureReady() throws LightExecutionException, InterruptedException {
		
		ThreadPool pool = new ThreadPoolImpl(1);
		
		Lock l1 = new ReentrantLock();
		l1.lock();
		
		LightFuture<Integer> f1 = pool.submit(() -> {
			l1.lock();
			return 1;
		});
		assertFalse(f1.isReady());
		
		l1.unlock();
		f1.get();
		assertTrue(f1.isReady());
	}
	
	@Test
	public void testMultipleThenApply() throws LightExecutionException, InterruptedException {
		ThreadPool pool = new ThreadPoolImpl(1);
		
		LightFuture<Integer> f = pool
				.submit(() -> 1)
				.thenApply((i) -> i + 1)
				.thenApply((i) -> i + 1);
		
		assertEquals(Integer.valueOf(3), f.get());
				
	}
	
	@Test(expected = LightExecutionException.class)
	public void testException() throws LightExecutionException, InterruptedException {
		ThreadPool pool = new ThreadPoolImpl(1);
		pool.submit(() -> 1/0).get();
	}
	
	@Test(expected = LightExecutionException.class)
	public void testExceptionThenApply() throws LightExecutionException, InterruptedException {
		ThreadPool pool = new ThreadPoolImpl(1);
		pool
		.submit(() -> 1/0)
		.thenApply((i) -> i + 1)
		.get();
	}
	
	@Test(expected = LightExecutionException.class)
	public void testInterruption() throws LightExecutionException, InterruptedException {
		ThreadPool pool = new ThreadPoolImpl(1);
		Object l = new Object();
		LightFuture<Integer> f = pool
		.submit(() -> {
			try {
				synchronized(l) {
					while(true)
						l.wait();
				}
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
			//return 1;
		});
		pool.shutdown();
		f.get();
	}
	
	@Test
	public void testContainsNThreads() throws LightExecutionException, InterruptedException {
		for (int nth = 1; nth < 50; nth++) {
			ThreadPool pool = new ThreadPoolImpl(nth);
			Lock l = new ReentrantLock();
			Collection<String> thNames = new LinkedBlockingQueue<>();
			l.lock();
			LightFuture<Integer> f = pool
					.submit(() -> {
					l.lock();
					thNames.add(Thread.currentThread().getName()); 
					return 1;});
			for (int j = 0; j < nth + 2; j++) {
				f = f.thenApply((i) -> {
					thNames.add(Thread.currentThread().getName());
					return 0;});

			}
			Thread.sleep(10);
			l.unlock();
			f.get();
			assertEquals(nth + 3, thNames.size());
			assertEquals(nth, thNames.stream().distinct().count());
		}
	}
	
	@Test
	public void testSameObjects() throws LightExecutionException, InterruptedException {
		ThreadPool pool = new ThreadPoolImpl(1);
		LightFuture<Integer> f1 = pool.submit(() -> 1);
		assertTrue(f1.get() == f1.get());
	}
}
