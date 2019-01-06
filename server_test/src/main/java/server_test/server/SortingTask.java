package server_test.server;

public class SortingTask implements Runnable {
	
	private long sortStart, sortFinished;
	
	private long arrayRecieved, arrayTransmitted;
	
	private int[] array;
	
	public void setRecieveTime(long timeMillis) {
		arrayRecieved = timeMillis;
	}
	
	public void setTransmitTime(long timeMillis) {
		arrayTransmitted = timeMillis;
	}
	
	public int[] getArray() {
		return array;
	}

	public void setArray(int[] array) {
		this.array = array;
	}

	public void run() {
		sortStart = System.currentTimeMillis();
		QuadraticSorter.sort(array);
		sortFinished = System.currentTimeMillis();
	}

}
