package simpleThreadPool;

import java.util.Random;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;
import org.openjdk.jcstress.infra.results.I_Result;

@JCStressTest
@Outcome(id = "0, 0", expect = Expect.ACCEPTABLE, desc = "Good outcome.")
@State
public class SubmitGetTest {
	
	static ThreadPool pool = new ThreadPoolImpl(2);
	Random rand = new Random(10);
	
	void filler(II_Result r) {
		Integer rnd = rand.nextInt();
        LightFuture<Integer> f1 = pool.submit(() -> rnd);
        LightFuture<Integer> f2 = f1.thenApply((i) -> i + 1);
        try {
			r.r1 = rnd - f1.get();
			r.r2 = rnd + 1 - f2.get();
		} catch (LightExecutionException | InterruptedException e) {
			int i = 1/0;
		}
	}
	
    @Actor
    public void actor1(II_Result r) {
    		filler(r);
    }
    
    @Actor
    public void actor2(II_Result r) {
    		filler(r);
    }
    
    @Actor
    public void actor3(II_Result r) {
    		filler(r);
    }
    
    @Actor
    public void actor4(II_Result r) {
    		filler(r);
    }
}
