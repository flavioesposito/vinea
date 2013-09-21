/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.ipcProcess.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.dtp.impl.googleprotobuf.DTP.DTPMessage;
import rina.flow.*;
import rina.flowAllocator.impl.FlowAllocatorImpl;
import rina.ipcProcess.util.MessageQueue;
import rina.irm.IRM;
import rina.rib.impl.RIBImpl;

/**
 * This Relay and Mutiplexing part is used by IPC Process in  Zero DIF
 * Note: RMT in 0 DIF and non-0 DIF are different, since in the dif 0 , the underlying is internet.
 * Each TCP flow has a DTP header in order to do demultiplexing in dif 0.
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 */
public class IPCProcessRMT extends Thread{
	/**
	 * IPC Name 
	 */
	private String IPCName = null;
	/**
	 * local Resource Information Base 
	 */
	private RIBImpl rib = null;

	/**
	 * IPC message queue
	 */
	private MessageQueue dtpMsgQueue = null;

	/**
	 * IPC CDAP message queue
	 */
	private MessageQueue cdapMsgQueue = null;


	private FlowAllocatorImpl flowAllocator = null;


	private LinkedHashMap<Integer, String> portToIPC = null;

	private LinkedHashMap<String, Integer> appWellKnownPort = null;

	private LinkedHashMap<String, MessageQueue> upperIPCsDTPMsgQueue = null ;

	private LinkedHashMap<String, MessageQueue> upperIPCsCDAPMsgQueue = null ;


	private IRM  irm = null;





	public IPCProcessRMT(FlowAllocatorImpl flowAllocatorImpl, RIBImpl rib, MessageQueue dtpMsgQueue, MessageQueue  cdapMsgQueue,
			LinkedHashMap<String, MessageQueue> upperIPCsDTPMsgQueue,LinkedHashMap<String, MessageQueue> upperIPCsCDAPMsgQueue )
	{

		this.flowAllocator = flowAllocatorImpl;
		this.portToIPC = this.flowAllocator.getFlowAllocated().getPortToIPC();
		this.appWellKnownPort = this.flowAllocator.getAppWellKnownPort();
		this.rib = rib;
		this.IPCName = this.rib.getAttribute("ipcName").toString();
		this.dtpMsgQueue = dtpMsgQueue;
		this.cdapMsgQueue = cdapMsgQueue;

		this.upperIPCsDTPMsgQueue = upperIPCsDTPMsgQueue;
		this.upperIPCsCDAPMsgQueue = upperIPCsCDAPMsgQueue;
		this.irm = this.flowAllocator.getIrm();

		this.start();
	}


	/**
	 * start thread for receiving msg
	 */
	public void run()
	{
		int count = 1;
		while(true)
		{

			byte[] msg = this.dtpMsgQueue.getReceive();
			//			System.out.println("IPC Process(" + this.IPCName +  ") RMT: this is the " +  count++ + "msg received");

			handleReceiveMessage(msg);

		}
	}

	/**
	 * @param msg
	 */
	private void handleReceiveMessage(byte[] msg) {

		DTP.DTPMessage dtpMessage = null;

		try {
			dtpMessage = DTP.DTPMessage.parseFrom(msg);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		byte[] realPayload = null;

		if(this.flowAllocator.getTcpManager()!=null)//DIF 0 IPC
		{


//			System.out.println("thread id is " + Thread.currentThread().getId());
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: opcode is  " + dtpMessage.getOpCode());
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ")RMT: srcName is   " + dtpMessage.getSrcIPCName());
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ")RMT: srcPort is   " + dtpMessage.getSrcPortID());
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ")RMT: dstName is   " + dtpMessage.getDestIPCName());
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ")RMT: dstPort is   " + dtpMessage.getDestPortID());


			DTP.DTPMessage dtpMessagePayload = null;

			try {
				dtpMessagePayload = DTP.DTPMessage.parseFrom(dtpMessage.getPayload().toByteArray());

			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}

			realPayload = dtpMessagePayload.getPayload().toByteArray();

		}else//Non DIF Zero IPC
		{
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: opcode is  " + dtpMessage.getOpCode());
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ")RMT: srcName is   " + dtpMessage.getSrcIPCName());
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ")RMT: srcPort is   " + dtpMessage.getSrcPortID());
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ")RMT: dstName is   " + dtpMessage.getDestIPCName());
//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ")RMT: dstPort is   " + dtpMessage.getDestPortID());
			realPayload = dtpMessage.getPayload().toByteArray();
		}



		switch(dtpMessage.getOpCode()){


		case M_CDAP:

			CDAP.CDAPMessage cdapMessage = null;

			try {
				cdapMessage = CDAP.CDAPMessage.parseFrom(realPayload);

			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}

			//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ")RMT: M_CDAP destApName is " + cdapMessage.getDestApName());



			if(cdapMessage.getDestApName().equals(this.IPCName))
			{

				this.cdapMsgQueue.addReceive(realPayload);

				//				this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: put the msg into cdapMessage Queue");
			}else
			{


				relayCDAPMsg(realPayload);
			}

			break;

		case M_DTP:

			DTP.DTPMessage CommonDtpMessage = null;

			try {
				CommonDtpMessage = DTP.DTPMessage.parseFrom(realPayload);

			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}


			if(CommonDtpMessage.getDestIPCName().equals(this.IPCName))// demultiplexing
			{
				demultiplexDTPMsg(realPayload);

			}
			else // relaying 
			{
				relayDTPMsg(realPayload);
			}

			break;

		default:

			break;
		}




	}



	private void demultiplexDTPMsg(byte[] msg) {

		DTP.DTPMessage dtpMessage = null;

		try {
			dtpMessage = DTP.DTPMessage.parseFrom(msg);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		int dstPortID = dtpMessage.getDestPortID();

		byte[] payload = dtpMessage.getPayload().toByteArray();

	//	this.rib.RIBlog.infoLog("IPC Process (" + this.IPCName +  ") RMT: dstPortID is " +  dstPortID);

		DTP.DTPMessage payloadWithHeader = null;

		try {
			payloadWithHeader = DTP.DTPMessage.parseFrom(payload);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}


		if(this.portToIPC.containsKey(dstPortID))
		{
			//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: doing the demultiplexing ");
			//
			//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: opcode is  " + dtpMessage.getOpCode());
			//			this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: dstPortID is  " + dtpMessage.getDestPortID());

			if(this.appWellKnownPort.containsValue(dstPortID))
			{
				//			this.rib.RIBlog.infoLog("IPC Process RMT: this is a msg goes to the well known port"  );

				String dstIPCName = this.flowAllocator.getFlowAllocated().getIPCName(dstPortID);

				//			this.rib.RIBlog.infoLog("IPC Process RMT: this well known port is for IPC " + dstIPCName   );

				//this directly goes to cdapmessage queue of upper IPC, so we getPayLoad
				this.upperIPCsCDAPMsgQueue.get(dstIPCName).addReceive(payloadWithHeader.getPayload().toByteArray());

			}else
			{
				//	this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: put the msg to the upper app with dstPortID " + dstPortID  );

				this.flowAllocator.getFlowMsgQueue(dstPortID).addReceive(payloadWithHeader.toByteArray());

				//this is two kind of demulplexing, DIF 0 and Non-Zero are different FIXME
			}
		}
		else
		{

			this.rib.RIBlog.errorLog("IPC Process (" + this.IPCName +  ") RMT: No such port allocated on this IPC");
		}

	}

	/**
	 * @param dtpMessage
	 */
	private void relayDTPMsg(byte[] msg) {


	

		DTP.DTPMessage dtpMessage = null;

		try {
			dtpMessage = DTP.DTPMessage.parseFrom(msg);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		String dstName = dtpMessage.getDestIPCName();

		this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: relay DTP message for dst " + dstName );
		
		String nextHop = this.rib.getForwardingTable().get(dstName);
	

		if(dstName.equals("appC.ipc.0"))
		{
			nextHop = "appC.ipc.0";
		}else if(dstName.equals("appA.ipc.0"))
		{
			nextHop = "appA.ipc.0";
		}


		int nextHopHandle = this.irm.getHandle(nextHop);

		try {

			DTP.DTPMessage payload = message.DTPMessage.generatePayloadM_DTP(msg);

			this.irm.send(nextHopHandle, payload.toByteArray());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}




	private void relayCDAPMsg(byte[] msg) {

	

		CDAP.CDAPMessage cdapMessage = null;

		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}


		String dstName = cdapMessage.getDestApName();
		
		this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: relay CDAP message for dst " + dstName);

		String nextHop = this.rib.getForwardingTable().get(dstName);
	
		this.rib.RIBlog.infoLog("IPC Process(" + this.IPCName +  ") RMT: relay CDAP message for dst " + dstName + "and nexthop is " + nextHop);
		/////////////////////////////////////////////////////////////////////////

		int nextHopHandle = this.irm.getHandle(nextHop);

		try {

			DTP.DTPMessage payload = message.DTPMessage.generatePayloadM_CDAP(msg);

			this.irm.send(nextHopHandle, payload.toByteArray());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

