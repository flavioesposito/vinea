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

/**
 *  IncomingClientHandler: It is used to handle each incoming client 
 * @author Yuefeng Wang and Flavio Esposito . Computer Science Department, Boston University
 * @version 1.0
 */
import java.util.LinkedHashMap;

import message.DTPMessage;

import com.google.protobuf.InvalidProtocolBufferException;
import rina.cdap.impl.googleprotobuf.CDAP;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow.*;
import rina.flow0.TCPFlow;
import rina.ipcProcess.util.MessageQueue;



public class IncomingClientHandler extends Thread {


	private TCPFlow  tcpflow = null;

	private MessageQueue msgQueue = null;

	private boolean listening = true;

	private  LinkedHashMap<String, TCPFlow> tcpFlowAllocated = null ;

	private String dstIPCName = null;



	public IncomingClientHandler(TCPFlow tcpflow, MessageQueue msgQueue, LinkedHashMap<String, TCPFlow> tcpFlowAllocated   )
	{

		this.tcpflow = tcpflow;	
		this.msgQueue = msgQueue;
		this.tcpFlowAllocated = tcpFlowAllocated;
	}

	/**
	 * listen for messages and add them to flow
	 */
	public void run()
	{

		//first time receive a message from the client, and this msg contains the src IPC Name
		byte[] msg = null;
		try {
			msg = this.tcpflow.receive();
		} catch (Exception e1) {
			e1.printStackTrace();
		} 

		DTP.DTPMessage dtpMessage = null;

		try {
			dtpMessage = DTP.DTPMessage.parseFrom(msg);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
		this.dstIPCName = dtpMessage.getSrcIPCName();
		
		
		
		this.tcpflow.setSrcName(dtpMessage.getDestIPCName());
		this.tcpflow.setDstName(dtpMessage.getSrcIPCName());
		
		

		this.tcpFlowAllocated.put(this.dstIPCName, this.tcpflow);
		
		

		
		System.out.println("Incoming client hanlder: New TCP Flow is added: " +  this.dstIPCName);

		////////the flow is set up 
		while(listening)
		{

			try {
				msg = this.tcpflow.receive();

				msgQueue.addReceive(msg);

			} catch (Exception e) {
				if(this.tcpflow!=null){
					this.tcpflow.close();
					this.tcpFlowAllocated.remove(this.dstIPCName);
					listening = false;
					System.out.println("connection close");
				}
			} 


		}

	}
}