/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 * 
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */
package rina.flowAllocator.impl.util;

import rina.flow.*;
import rina.flow0.TCPFlow;
import rina.ipcProcess.util.MessageQueue;

/**
 * OutgoingComHandler: this one is used for a 0 dif each time when this ipc creates a flow out
 * @author Yuefeng Wang and Flavio Esposito  . Computer Science Department, Boston University
 * @version 1.0
 */
public class OutgoingComHandler extends Thread {
	/**
	 * flow0
	 */
	private TCPFlow tcpFlow = null;
	/**
	 * message queue
	 */
	private MessageQueue msgQueue = null;

	public boolean listening = true;

	/**
	 * Constructor
	 * @param buffer
	 * @param flow
	 */
	public OutgoingComHandler(MessageQueue msgQueue, TCPFlow tcpFlow)
	{
		this.msgQueue = msgQueue;
		this.tcpFlow = tcpFlow;	
	}
	/**
	 * this object is attached each time a new flow is created
	 */
	public void run()
	{

		while(listening)
		{
			try {
				byte[] msg = this.tcpFlow.receive();
				this.msgQueue.addReceive(msg);

			} catch (Exception e) {
				//e.printStackTrace();
				if(this.tcpFlow!=null)
				{
					listening = false;
					this.tcpFlow.close();
					System.out.println("Connection closed");
				}

			}

		}
	}

}