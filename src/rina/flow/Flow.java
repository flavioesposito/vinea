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
package rina.flow;


import message.DTPMessage;
import rina.cdap.impl.googleprotobuf.CDAP;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow0.TCPFlow;
import rina.flowAllocator.api.*;
import rina.ipcProcess.util.MessageQueue;


/**
 * This class is used to create a flow for Zero DIF
 * It uses TCP flow.
 * @author Yuefeng Wang and Flavio Esposito  . Computer Science Department, Boston University
 * @version 1.0
 * 
 */
public class Flow implements FlowAPI{


	private int srcPort = -1;

	private int dstPort = -1;

	private String srcIPCName;

	private String dstIPCName;

	private String srcUnderlyingIPCName;

	private String dstUnderlyingIPCName;

	private MessageQueue msgQueue;



	private TCPFlow tcpFlow;

	private Flow underlyingFlow;






	//NOTE: 
	//Here we ausume that in dif zero, all member in the dif are fully connected
	//but rember to do the following 
	//FIXME:
	//change TCPflow sent

	//	DTP.DTPMessage DTPMsg = DTPMessage.generateM_DTP
	//	(this.tcpFlow.getDstName(),
	//			this.dstPort, 
	//			this.tcpFlow.getSrcName(),
	//			this.srcPort,
	//			dtpMsg
	//	); 

	//and 

	//	DTP.DTPMessage DTPMsg = DTPMessage.generateM_CDAP
	//	(       this.tcpFlow.getDstName(),
	//			this.dstPort, 
	//			this.tcpFlow.getSrcName(),
	//			this.srcPort,
	//			cdapMsg
	//	);


	// to 
	
	//		DTP.DTPMessage DTPMsg = DTPMessage.generateM_DTP
	//		(this.dstUnderlyingIPCName,
	//				this.dstPort, 
	//				this.srcUnderlyingIPCName,
	//				this.srcPort,
	//				dtpMsg
	//		); 

	//  and
	
	//		DTP.DTPMessage DTPMsg = DTPMessage.generateM_CDAP
	//		(this.dstUnderlyingIPCName,
	//				this.dstPort, 
	//				this.srcUnderlyingIPCName,
	//				this.srcPort,
	//				dtpMsg
	//		); 

	//later. 
	// but in order to do this, all place using constructor with tcpflow has to be modified
	
	//for underlying flow, change is already done.
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	// this is for dif 0 flow allocator
	public Flow(String srcIPCName, String dstIPCName){
		this.srcIPCName = srcIPCName;
		this.dstIPCName = dstIPCName;
		this.msgQueue = new MessageQueue();
	}

	public Flow(String srcIPCName, String dstIPCName, TCPFlow tcpFlow){
		this.srcIPCName = srcIPCName;
		this.dstIPCName = dstIPCName;
		this.tcpFlow = tcpFlow;
		this.msgQueue = new MessageQueue();
	}

	public Flow(String srcIPCName, String dstIPCName, int dstPort, TCPFlow tcpFlow){
		this.srcIPCName = srcIPCName;
		this.dstIPCName = dstIPCName;
		this.dstPort = dstPort;
		this.tcpFlow = tcpFlow;
		this.msgQueue = new MessageQueue();
	}


	public Flow(String srcIPCName, String dstIPCName, int dstPort){
		this.srcIPCName = srcIPCName;
		this.dstIPCName = dstIPCName;
		this.dstPort = dstPort;
		this.msgQueue = new MessageQueue();
	}

	public Flow(String srcIPCName, String dstIPCName, String srcUnderlyingIPCName, String dstUnderlyingIPCName, Flow underlyingFlow){
		this.srcIPCName = srcIPCName;
		this.dstIPCName = dstIPCName;
		this.srcUnderlyingIPCName = srcUnderlyingIPCName;
		this.dstUnderlyingIPCName = dstUnderlyingIPCName;
		this.underlyingFlow = underlyingFlow;
		this.msgQueue = new MessageQueue();
	}

	public Flow(String srcIPCName, String dstIPCName, String srcUnderlyingIPCName, String dstUnderlyingIPCName,int dstPort, Flow underlyingFlow){
		this.srcIPCName = srcIPCName;
		this.dstIPCName = dstIPCName;
		this.srcUnderlyingIPCName = srcUnderlyingIPCName;
		this.dstUnderlyingIPCName = dstUnderlyingIPCName;
		this.dstPort = dstPort;
		this.underlyingFlow = underlyingFlow;
		this.msgQueue = new MessageQueue();
	}







	public void send(byte[] message) throws Exception {
		this.msgQueue.addSend(message);

	}

	public byte[] receive(){

		return this.msgQueue.getReceive();
	}


	public void sendDTPMsg(byte[] dtpMsg) throws Exception {


		if(this.tcpFlow != null)
		{
			DTP.DTPMessage payload = DTPMessage.generatePayloadM_DTP(dtpMsg);
			
			DTP.DTPMessage DTPMsg = DTPMessage.generateM_DTP
			(this.tcpFlow.getDstName(),
					this.dstPort, 
					this.tcpFlow.getSrcName(),
					this.srcPort,
					payload.toByteArray()
			);
			this.tcpFlow.sendDTPMsg(DTPMsg.toByteArray());
		}else
		{

			DTP.DTPMessage payload = DTPMessage.generatePayloadM_DTP(dtpMsg);

			DTP.DTPMessage DTPMsg = DTPMessage.generateM_DTP
			(this.dstUnderlyingIPCName,
					this.dstPort, 
					this.srcUnderlyingIPCName,
					this.srcPort,
					payload.toByteArray()
			);
			this.underlyingFlow.sendDTPMsg(DTPMsg.toByteArray());
		}
	}

	public void sendCDAPMsg(byte[] cdapMsg) throws Exception {


		if(this.tcpFlow != null)
		{
			DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(cdapMsg);

			DTP.DTPMessage DTPMsg = DTPMessage.generateM_CDAP
			(       this.tcpFlow.getDstName(),
					this.dstPort, 
					this.tcpFlow.getSrcName(),
					this.srcPort,
					payload.toByteArray()
			);

			this.tcpFlow.sendDTPMsg(DTPMsg.toByteArray());
		}else
		{
			DTP.DTPMessage payload = DTPMessage.generatePayloadM_DTP(cdapMsg);

			DTP.DTPMessage DTPMsg = DTPMessage.generateM_CDAP
			(this.dstUnderlyingIPCName,
					this.dstPort, 
					this.srcUnderlyingIPCName,
					this.srcPort,
					payload.toByteArray()
			);
			this.underlyingFlow.sendDTPMsg(DTPMsg.toByteArray());
		}

	}

	public void printFlow()
	{
		System.out.println("print flow");

		System.out.println("this.dstIPCName " +  this.dstIPCName);
		System.out.println("this.dstPort " +  this.dstPort);
		System.out.println("this.srcIPCName " +  this.srcIPCName);
		System.out.println("this.srcPort " +  this.srcPort);

		if(this.tcpFlow ==  null)
		{
			System.out.println("tcpFlow is null");
		}else
		{
			System.out.println("underlyingFlow is null");
		}

	}

	/**
	 * @return the srcPort
	 */
	public synchronized int getSrcPort() {
		return srcPort;
	}

	/**
	 * @param srcPort the srcPort to set
	 */
	public synchronized void setSrcPort(int srcPort) {
		this.srcPort = srcPort;
	}

	/**
	 * @return the dstPort
	 */
	public synchronized int getDstPort() {
		return dstPort;
	}

	/**
	 * @param dstPort the dstPort to set
	 */
	public synchronized void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}

	/**
	 * @return the dstIPCName
	 */
	public synchronized String getDstIPCName() {
		return dstIPCName;
	}

	/**
	 * @param dstIPCName the dstIPCName to set
	 */
	public synchronized void setDstIPCName(String dstIPCName) {
		this.dstIPCName = dstIPCName;
	}

	/**
	 * @return the srcIPCName
	 */
	public synchronized String getSrcIPCName() {
		return srcIPCName;
	}

	/**
	 * @param srcIPCName the srcIPCName to set
	 */
	public synchronized void setSrcIPCName(String srcIPCName) {
		this.srcIPCName = srcIPCName;
	}


	/**
	 * @return the msgQueue
	 */
	public synchronized MessageQueue getMsgQueue() {
		return msgQueue;
	}



	/**
	 * @param msgQueue the msgQueue to set
	 */
	public synchronized void setMsgQueue(MessageQueue msgQueue) {
		this.msgQueue = msgQueue;
	}

	/**
	 * @return the tcpFlow
	 */
	public synchronized TCPFlow getTcpFlow() {
		return tcpFlow;
	}

	/**
	 * @param tcpFlow the tcpFlow to set
	 */
	public synchronized void setTcpFlow(TCPFlow tcpFlow) {
		this.tcpFlow = tcpFlow;
	}

	/**
	 * @return the underlyingFlow
	 */
	public synchronized Flow getUnderlyingFlow() {
		return underlyingFlow;
	}

	/**
	 * @param underlyingFlow the underlyingFlow to set
	 */
	public synchronized void setUnderlyingFlow(Flow underlyingFlow) {
		this.underlyingFlow = underlyingFlow;
	}





}
