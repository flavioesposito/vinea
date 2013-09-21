/**
 * 
 */
package test;

import java.util.LinkedHashMap;

/**
 * @author Flavio Esposito
 *
 */
public class Tout {

	public static LinkedHashMap<Integer,Thread> threadMap = new LinkedHashMap();
	public Tout() {}
	/**
	 * @param args
	 */
	public static void main(String[] args) {


		// waits for this thread to die

		Tout tout = new Tout();
		tout.setTimeOut();
		Thread t = threadMap.get(1);
		System.out.println("11Thread: "+t.getName()+" is alive: "+t.isAlive());
		t.interrupt();
		threadMap.remove(1);
		threadMap.put(1,t);
		//     t.join(1000);
		//    System.out.print(t.getName());
		//checks if this thread is alive
		//   System.out.println(", status = " + t.isAlive());
		tout.check();



	}

	public void setTimeOut() {

		Thread t = new Thread(new ThreadDemo());
		t.start();
		threadMap.put(1, t);
		// this will call run() function
		//t.join(2000);
	}

	
	public void check() {
		Thread t = threadMap.get(1);
		System.out.println("11Thread: "+t.getName()+" is alive: "+t.isAlive());
		System.out.println("11Thread: "+t.getName()+" is Interrupted: "+t.isInterrupted());
		
		
		
	}



}

