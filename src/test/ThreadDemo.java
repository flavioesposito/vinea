package test;

/**
 * 
 * @author flavioesposito
 *
 */
public class ThreadDemo implements Runnable {

   public void run() {
   
      Thread t = Thread.currentThread();
      System.out.print(t.getName());
      //checks if this thread is alive
      System.out.println(", culo status = " + t.isAlive());
      //t.interrupt(); to stop the thread
      

      System.out.println(", culo status = " + t.isAlive());
   }

   public static void main(String args[]) throws Exception {
   
      Thread t = new Thread(new ThreadDemo());
      // this will call run() function
      t.start();
      // waits for this thread to die
  
      
      
      t.join(1000);
      System.out.print(t.getName());
      //checks if this thread is alive
      System.out.println(", status = " + t.isAlive());
   }
} 