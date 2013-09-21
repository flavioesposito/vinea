package test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TimeOut{
	//maintains a thread for executing the doWork method
	private ExecutorService executor = Executors.newFixedThreadPool(1);

	public void doWork() {
		//perform some long running task here...
		while(1>0) {
			System.out.println("thread running");
		}
	}
	public void doWorkWithTimeout(int timeoutSecs) {
		//set the executor thread working
		final Future<?> future = executor.submit(new Runnable() {
			public void run() {
				try {
					doWork();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		//check the outcome of the executor thread and limit the time allowed for it to complete
		try {
			future.get(timeoutSecs, TimeUnit.SECONDS);
		} catch (Exception e) {
			//ExecutionException: deliverer threw exception
			//TimeoutException: didn't complete within downloadTimeoutSecs
			//InterruptedException: the executor thread was interrupted

			//interrupts the worker thread if necessary

			future.cancel(true);
			System.out.println("encountered problem while doing some work: "+e);

		}
	}
	
	public static void main(String args[]) throws InterruptedException{
		TimeOut t = new TimeOut();
		t.doWorkWithTimeout(1);
	}
}