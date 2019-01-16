package server_test.server;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsHolder {
	public final AtomicInteger numberOfArrays = new AtomicInteger();
	public final AtomicLong sortTimesSum = new AtomicLong();
	public final AtomicLong clientTimesSum = new AtomicLong();
	
	public final AtomicInteger currentNumberClients = new AtomicInteger(),
			numberClosedClients = new AtomicInteger();
	
	public final int expectedNumberClients;
	public final int expectedNumberArrays;
	
	private volatile boolean measureFailed;
	
	public StatisticsHolder(int expectedNumberClients, int expectedNumberArrays) {
		this.expectedNumberClients = expectedNumberClients;
		this.expectedNumberArrays = expectedNumberArrays;
	}
	
	public boolean isAllClientsConnected() {
		return currentNumberClients.compareAndSet(expectedNumberClients, expectedNumberClients);
	}
	
	public int getAveragetSortTime() {
		return Math.toIntExact(sortTimesSum.get() / numberOfArrays.get());
	}
	
	public int getAverageProcessTime() {
		return Math.toIntExact(clientTimesSum.get() / numberOfArrays.get());
	}
	
	public void setMeasureFailed() {
		measureFailed = true;
	}
	
	public boolean isMeasureSuccessful() {
		return !measureFailed;
	}
}
