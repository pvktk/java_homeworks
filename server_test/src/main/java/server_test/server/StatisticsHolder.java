package server_test.server;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsHolder {
	public final AtomicInteger fullNumberOfCorrectArrays = new AtomicInteger();
	public final AtomicLong sortTimesSum = new AtomicLong();
	public final AtomicLong clientTimesSum = new AtomicLong();

	public final AtomicInteger currentNumberClients = new AtomicInteger(),
			numberClosedClients = new AtomicInteger();

	public final int expectedNumberClients;
	public final int expectedNumberArraysPerClient;

	public StatisticsHolder(int expectedNumberClients, int expectedNumberArrays) {
		this.expectedNumberClients = expectedNumberClients;
		this.expectedNumberArraysPerClient = expectedNumberArrays;
	}

	public boolean isAllClientsConnected() {
		return currentNumberClients.compareAndSet(expectedNumberClients, expectedNumberClients);
	}

	public int getAveragetSortTimeMillis() {
		return Math.toIntExact(sortTimesSum.get() / fullNumberOfCorrectArrays.get() / 1000000);
	}

	public int getAverageProcessTimeMillis() {
		return Math.toIntExact(clientTimesSum.get() / fullNumberOfCorrectArrays.get() / 1000000);
	}
	
	public int getFullExpectedNumberArrays() {
		return expectedNumberArraysPerClient * expectedNumberClients;
	}
}
