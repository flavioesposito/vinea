package test;

import java.net.*;
import java.io.*;

/**
  * SimpleClient connects to TCP port 2000, writes a line
  * of text, and then reads the echoed text back from the server.
  * This client will detect network timeouts, and exit gracefully,
  * rather than stalling.
  * Start server, and the run by typing
  *
  */
public class SimpleClient
{
	/** Connects to a server on port 2000,
	    and handles network timeouts gracefully 
	  */
	public static void main (String args[]) throws Exception
	{
		System.out.println ("Starting timer.");
		// Start timer
		
		Timer timer = new Timer(3000);
		try {
			timer.start();
		}
		catch(Exception ex) {
			System.out.println("di tacco!!!: "+ ex);
		}
		// Connect to remote host
		Socket socket = new Socket ("localhost", 2000);
		System.out.println ("Connected to localhost:2000");

		// Reset timer - timeout can occur on connect
		timer.reset();

		// Create a print stream for writing
		PrintStream pout = new PrintStream ( 
			socket.getOutputStream() );

		// Create a data input stream for reading
		DataInputStream din = new DataInputStream( 
			socket.getInputStream() );

		// Print hello msg
		pout.println ("Hello world!");

		// Reset timer - timeout is likely to occur during the read
		timer.reset();

		// Print msg from server
		System.out.println (din.readLine());

		// Shutdown timer
		timer.stop();

		// Close connection
		socket.close();
	}
}