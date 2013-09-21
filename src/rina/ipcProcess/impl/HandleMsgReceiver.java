/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * 
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 */

package rina.ipcProcess.impl;

import rina.ipcProcess.util.MessageQueue;

public class HandleMsgReceiver extends Thread {
	
	MessageQueue dtpMessageQueue = null;
	MessageQueue flowQueue = null;
	boolean stop = false;
	
	public HandleMsgReceiver(MessageQueue dtpMessageQueue, MessageQueue flowQueue)
	{
		this.dtpMessageQueue = dtpMessageQueue;
		this.flowQueue = flowQueue;
		this.start();
	}
	
	public void run()
	{
		while(!stop)
		{
			byte[] msg = this.flowQueue.getReceive();
			this.dtpMessageQueue.addReceive(msg);
		}
	}

}
