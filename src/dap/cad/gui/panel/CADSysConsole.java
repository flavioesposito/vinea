/**
 * @copyright 2013 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.gui.panel;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author Flavio Esposito and Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0 
 *
 */
public class CADSysConsole extends WindowAdapter implements WindowListener, ActionListener, Runnable
{
	private JFrame frame;
	private JTextArea textArea;
	private Thread reader;
	private Thread reader2;
	private boolean quit;

	private final PipedInputStream pin=new PipedInputStream(); 
	private final PipedInputStream pin2=new PipedInputStream(); 

	Thread errorThrower; // just for testing (Throws an Exception at this Console

	public CADSysConsole()
	{
		// create all components and add them
		frame=new JFrame("RINA Console");
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize=new Dimension((int)(screenSize.width/4),(int)(screenSize.height/4));
		int x=(int)(frameSize.width/4);
		int y=(int)(frameSize.height/4);
		frame.setBounds(x,y,frameSize.width,frameSize.height);

		textArea=new JTextArea();
		textArea.setEditable(false);
		JButton button=new JButton("clear");

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(textArea),BorderLayout.CENTER);
		frame.getContentPane().add(button,BorderLayout.SOUTH);
		frame.setVisible(true);		

		frame.addWindowListener(this);		
		button.addActionListener(this);

		try
		{
			PipedOutputStream pout=new PipedOutputStream(this.pin);
			System.setOut(new PrintStream(pout,true)); 
		} 
		catch (java.io.IOException io)
		{
			textArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
		}
		catch (SecurityException se)
		{
			textArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
		} 

		try 
		{
			PipedOutputStream pout2=new PipedOutputStream(this.pin2);
			System.setErr(new PrintStream(pout2,true));
		} 
		catch (java.io.IOException io)
		{
			textArea.append("Couldn't redirect STDERR to this console\n"+io.getMessage());
		}
		catch (SecurityException se)
		{
			textArea.append("Couldn't redirect STDERR to this console\n"+se.getMessage());
		} 		

		quit=false; // signals the Threads that they should exit

		// Starting two seperate threads to read from the PipedInputStreams				
		//
		reader=new Thread(this);
		reader.setDaemon(true);	
		reader.start();	
		//
		reader2=new Thread(this);	
		reader2.setDaemon(true);	
		reader2.start();

		// testing
		// 
//		System.out.println("Hello World 2");
//		System.out.println("All fonts available to Graphic2D:\n");
//		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		String[] fontNames=ge.getAvailableFontFamilyNames();
//		for(int n=0;n<fontNames.length;n++)  System.out.println(fontNames[n]);		

		// Testing part: simple an error thrown anywhere in this JVM will be printed on the Console
		// We do it with a seperate Thread becasue we don't wan't to break a Thread used by the Console.
//		System.out.println("\nLets throw an error on this console");	
//		errorThrower=new Thread(this);
//		errorThrower.setDaemon(true);
//		errorThrower.start();					
	}

	public synchronized void windowClosed(WindowEvent evt)
	{
		quit=true;
		this.notifyAll(); // stop all threads
		try { reader.join(1000);pin.close();   } catch (Exception e){}		
		try { reader2.join(1000);pin2.close(); } catch (Exception e){}
		System.exit(0);
	}		

	public synchronized void windowClosing(WindowEvent evt)
	{
		frame.setVisible(false); // default behaviour of JFrame	
		frame.dispose();
	}

	public synchronized void actionPerformed(ActionEvent evt)
	{
		textArea.setText("");
	}

	public synchronized void run()
	{
		try
		{			
			while (Thread.currentThread()==reader)
			{
				try { this.wait(100);}catch(InterruptedException ie) {}
				if (pin.available()!=0)
				{
					String input=this.readLine(pin);
					textArea.append(input);
				}
				if (quit) return;
			}

			while (Thread.currentThread()==reader2)
			{
				try { this.wait(100);}catch(InterruptedException ie) {}
				if (pin2.available()!=0)
				{
					String input=this.readLine(pin2);
					textArea.append(input);
				}
				if (quit) return;
			}			
		} catch (Exception e)
		{
			textArea.append("\nConsole reports an Internal error.");
			textArea.append("The error is: "+e);			
		}

		// just for testing (Throw a Nullpointer after 1 second)
		if (Thread.currentThread()==errorThrower)
		{
			try { this.wait(1000); }catch(InterruptedException ie){}
			throw new NullPointerException("Application test: throwing an NullPointerException It should arrive at the console");
		}

	}

	public synchronized String readLine(PipedInputStream in) throws IOException
	{
		String input="";
		do
		{
			int available=in.available();
			if (available==0) break;
			byte b[]=new byte[available];
			in.read(b);
			input=input+new String(b,0,b.length);														
		}while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
		return input;
	}	

	


//	public static void main(String[] arg)
//	{
//		new RINAConsole(); // create console with not reference	
//		for(int i=0;i<20;i++)
//		{
//			System.out.println("sdadsadsadsasadddddddd:  "+i);
//		}
//	}			
}