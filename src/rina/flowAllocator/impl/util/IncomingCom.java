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

import java.util.LinkedHashMap;

import rina.flow.*;
import rina.flow0.TCPFlow;
import rina.ipcProcess.util.MessageQueue;

/**
 * IncommingCom: this one is used for a 0 dif ipc to listen to a certain port
 * @author Yuefeng Wang and Flavio Esposito  . Computer Science Department, Boston University
 * @version 1.0
 */
public class IncomingCom extends Thread{


	private TCPFlow tcpListeningFlow = null ;

	private MessageQueue messageQueue = null;

	private LinkedHashMap<String, TCPFlow> tcpFlowAllocated = null ;


	public  IncomingCom( TCPFlow tcpListeningFlow, MessageQueue messageQueue, LinkedHashMap<String, TCPFlow> tcpFlowAllocated )
	{
		this.tcpListeningFlow = tcpListeningFlow;	
		this.messageQueue = messageQueue;
		this.tcpFlowAllocated = tcpFlowAllocated;
	}
	/**
	 * accepts incomingCom threads
	 */
	public void run()
	{
		while(true)
		{	
			try {

				TCPFlow clientTCPFlow = tcpListeningFlow.accept();

				new IncomingClientHandler(clientTCPFlow, this.messageQueue, this.tcpFlowAllocated).start();

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

}