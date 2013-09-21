/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package test;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sample Java class to illustrate How to join two threads in Java.
 * join() method allows you to serialize processing of two threads.
 */

public class Join {
  
    public static void main(String args[]) throws InterruptedException{
      
        System.out.println(Thread.currentThread().getName() + " is Started");
      
        Thread exampleThread = new Thread(){
            public void run(){
                try {
                    System.out.println(Thread.currentThread().getName() + " is Started");
                    Thread.sleep(2000);
                    System.out.println(Thread.currentThread().getName() + " is Completed");
                } catch (InterruptedException ex) {
                    Logger.getLogger(Join.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
      
        exampleThread.start();
        exampleThread.join();
      
        System.out.println(Thread.currentThread().getName() + " is Completed");
    }
  
}


