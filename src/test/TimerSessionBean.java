package test;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Simple demo that uses java.util.Timer to schedule a task 
 * to execute once 5 seconds have passed.
 */

public class TimerSessionBean {
    Timer timer;

    public TimerSessionBean(int seconds) {
        timer = new Timer();
        timer.schedule(new RequestVNPartition(), seconds*1000);
        
	}

    class RequestVNPartition extends TimerTask {
        public void run() {
            System.out.format("Time's up!%n");
            timer.cancel(); //Terminate the timer thread
            
        }
    }

    public static void main(String args[]) {
        new TimerSessionBean(5);
        System.out.format("Task scheduled.%n");
        
    }
}
