package simpleThreadPool;

import java.util.function.Supplier;

import simpleThreadPool.Worker.LightFutureImpl;

public class SupplierFuturePair {
	Supplier supplier;
	LightFutureImpl future;
	
	public SupplierFuturePair(Supplier suppl, LightFutureImpl fut) {
		supplier = suppl;
		future = fut;
	}
}
