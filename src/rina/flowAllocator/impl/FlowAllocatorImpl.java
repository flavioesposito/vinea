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
package rina.flowAllocator.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import message.CDAPMessage;
import message.DTPMessage;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import rina.flow.*;
import rina.cdap.impl.googleprotobuf.CDAP;
import rina.config.RINAConfig;
import rina.dns.DNS;
import rina.dns.DNSMessage;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow0.TCPFlow;
import rina.flowAllocator.api.FlowAPI;
import rina.flowAllocator.api.FlowAllocatorAPI;
import rina.flowAllocator.impl.util.*;
import rina.ipcProcess.impl.IPCProcessImpl;
import rina.ipcProcess.impl.TCPFlowManager;
import rina.ipcProcess.util.MessageQueue;
import rina.irm.IRM;
import rina.rib.impl.RIBImpl;


/**
 * This is a component of the IPC Process that responds to allocation Requests from Application Processes(or from upper DIF IPC)
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public class FlowAllocatorImpl implements FlowAllocatorAPI{

	private String IPCName = null;

	private String DIFName = null;

	private String NMSName = null;

	private String IDDName = null;

	/**
	 * configuration file
	 */
	private RINAConfig config = null;
	/**
	 * local RIB
	 */
	private RIBImpl rib = null;

	private MessageQueue msgQueue = null;

	private FlowAllocated flowAllocated = null ;

	private TCPFlowManager tcpManager = null;
	private IRM irm = null;



	//this contains the upper dif ipc to lower dif ipc mapping
	private LinkedHashMap<String, String> appToIPCMapping = null;

	private LinkedHashMap<String, Integer> appWellKnownPort = null;

	private LinkedHashMap<Integer, MessageQueue> flowMsgQueue = null;



	////////////////////// this two is from the old upperIPC class 
	private LinkedHashMap<String, LinkedHashMap>  neighboursUpperIPCs = null;

	private LinkedList<String> neighboursUpperIPCsList = null;
	////////////////////////


	/**
	 * LinkedHashMap<destination Application Process Name, Address of the next place to look>
	 */		
	private LinkedHashMap<String, String> forwardingTable = null;




	// msgQueue is passed from the IPC, also the rib
	public FlowAllocatorImpl(RIBImpl rib, MessageQueue msgQueue, TCPFlowManager tcpManager)
	{
		this.msgQueue = msgQueue;

		this.rib = rib;

		this.tcpManager = tcpManager;

		this.config = (RINAConfig)this.rib.getAttribute("config");

		this.IDDName = this.rib.getAttribute("iddName").toString();

		this.IPCName =  this.rib.getAttribute("ipcName").toString();

		if( this.rib.getAttribute("difName") != null)
		{
			this.DIFName = this.rib.getAttribute("difName").toString();
		}

		this.flowAllocated = new FlowAllocated();

		this.appToIPCMapping = new LinkedHashMap<String, String>();

		this.appWellKnownPort = new LinkedHashMap<String, Integer>();

		this.flowMsgQueue = new LinkedHashMap<Integer, MessageQueue>();

		this.forwardingTable = this.rib.getForwardingTable();



	}

	// msgQueue is passed from the IPC, also the rib
	public FlowAllocatorImpl(RIBImpl rib, MessageQueue msgQueue, IRM irm)
	{
		this.msgQueue = msgQueue;

		this.rib = rib;

		this.irm = irm;

		this.IPCName =  this.rib.getAttribute("ipcName").toString();
		this.IDDName = this.rib.getAttribute("iddName").toString();

		if( this.rib.getAttribute("difName") != null)
		{
			this.DIFName = this.rib.getAttribute("difName").toString();
		}

		this.flowAllocated = new FlowAllocated();

		this.appToIPCMapping = new LinkedHashMap<String, String>();

		this.appWellKnownPort = new LinkedHashMap<String, Integer>();

		this.flowMsgQueue = new LinkedHashMap<Integer, MessageQueue>();

		this.forwardingTable = this.rib.getForwardingTable();

	}


	/**
	 * 
	 * @param srcIPCName: upperDIF IPC
	 * @param dstIPCName: upperDIF IPC
	 * @return flow if any
	 */
	public Flow allocate(String srcIPCName, String dstIPCName) {

		Flow flow = null;

		System.out.println("FlowAllocator: this.IPCName is " +  this.IPCName);
		System.out.println("FlowAllocator: srcIPCName is " +  srcIPCName);
		System.out.println("FlowAllocator: dstIPCName is " +  dstIPCName);
		System.out.println("FlowAllocator IDD name is " + this.IDDName);

		if(dstIPCName.equals(this.IDDName))
		{
			this.rib.RIBlog.infoLog("Flow Allocator: allocate flow to IDD for " +  srcIPCName ); 

			if(this.tcpManager!= null)
			{
				TCPFlow tcpFlow = this.tcpManager.getTCPFlow(this.IDDName);
				flow = new Flow(srcIPCName, dstIPCName, 11111, tcpFlow);
				this.flowAllocated.addFlow(flow);
				this.flowMsgQueue.put(flow.getSrcPort(), flow.getMsgQueue());

				this.rib.RIBlog.infoLog("Flow Allocator:  portID is "+ flow.getSrcPort() + " for IDD, and app Name is " + srcIPCName);

				//	this.forwardingTable.put(dstIPCName, dstIPCName);

			}else
			{

				String underlyingIPCName = this.irm.getHandleMap().get(this.irm.getIDDHandle()).ipcName;
				int underlyingPort =  this.irm.getHandleMap().get(this.irm.getIDDHandle()).portID;

				IPCProcessImpl underlyingIPC = this.irm.getUnderlyingIPCs().get(underlyingIPCName);

				Flow underlyingflow = underlyingIPC.getFlowAllocator().getFlowAllocated().getFlow(underlyingPort);

				flow = new Flow(srcIPCName, dstIPCName, this.IPCName, this.IDDName, underlyingflow);

				this.flowAllocated.addFlow(flow);
				this.flowMsgQueue.put(flow.getSrcPort(), flow.getMsgQueue());
				this.rib.RIBlog.infoLog("Flow Allocator:  portID is "+ flow.getSrcPort() + " for IDD, and app Name is " + srcIPCName);

				//	this.forwardingTable.put(dstIPCName, dstIPCName);

			}
		}else
		{

			String underlyingDstIPCName;
			int wellKnownPort;

			if(this.appToIPCMapping.containsKey(dstIPCName) && this.appToIPCMapping.get(dstIPCName) != null 
					&& this.appWellKnownPort.containsKey(dstIPCName) )
			{
				underlyingDstIPCName = this.appToIPCMapping.get(dstIPCName);
				wellKnownPort = this.appWellKnownPort.get(dstIPCName);

			}else
			{

				underlyingDstIPCName= this.resolveApplicationByNMS(dstIPCName);


				if(underlyingDstIPCName == null)
				{
					this.rib.RIBlog.infoLog("Flow Allocator: flow allocated failed between " 
							+ srcIPCName + " and " +  dstIPCName + ". as the dstIPCName cannot be resolved to low DIF IPC in the same DIF");

					return null;

				}

				wellKnownPort = this.appWellKnownPort.get(dstIPCName);

			}
			this.rib.RIBlog.infoLog("Flow Allocator(" + this.IPCName +"): for app " + dstIPCName+ "underlyingDstIPCName is " +  underlyingDstIPCName + " wellknownPort is " + wellKnownPort);



			//		this.forwardingTable.put(dstIPCName, dstIPCName);

			if(this.tcpManager!= null)
			{

				TCPFlow tcpFlow = this.tcpManager.getTCPFlow(underlyingDstIPCName);

				flow = new Flow(srcIPCName, dstIPCName, tcpFlow);

				this.flowAllocated.addFlow(flow);

				this.flowMsgQueue.put(flow.getSrcPort(), flow.getMsgQueue());

				this.rib.RIBlog.infoLog("Flow Allocator: portID is "+ flow.getSrcPort() );

			}else
			{

				//underlyingDstIPCName
				//wellKnownPort


				//check if underlyingDstIPC is neighbour
				//if not send it to next hop

				System.out.println("FlowAllocator(" + this.IPCName + ") : underlyingDstIPCName is " + underlyingDstIPCName);


				/////////////////////////////////////////////////////////////////////////////////////
				String nextHopToUnderlyingDstIPC =this.rib.getForwardingTable().get(underlyingDstIPCName);
				
				this.rib.RIBlog.infoLog("@@@@@@@@@ nextHopToUnderlyingDstIPC is " +nextHopToUnderlyingDstIPC + ", underlyingDstIPCName is " +underlyingDstIPCName );
				
				
				while( this.rib.getForwardingTable().get(underlyingDstIPCName) == null){}
				
				nextHopToUnderlyingDstIPC =this.rib.getForwardingTable().get(underlyingDstIPCName);
				
				this.rib.RIBlog.infoLog("@@@@@@@@@ nextHopToUnderlyingDstIPC is " +nextHopToUnderlyingDstIPC + ", underlyingDstIPCName is " +underlyingDstIPCName );
				
				
				// comment for debugging purposing
				//String nextHopToUnderlyingDstIPC = "appB";
				////////////////////////////////////////////////////////////////////////////////////

				int handleOfNextHopToUnderlyingDstIPC = this.irm.getHandle(nextHopToUnderlyingDstIPC);

				String underlyingIPCName = this.irm.getHandleMap().get(handleOfNextHopToUnderlyingDstIPC).ipcName;

				int underlyingPort =  this.irm.getHandleMap().get(handleOfNextHopToUnderlyingDstIPC).portID;

				IPCProcessImpl underlyingIPC = this.irm.getUnderlyingIPCs().get(underlyingIPCName);


				Flow underlyingflow = underlyingIPC.getFlowAllocator().getFlowAllocated().getFlow(underlyingPort);

				flow = new Flow(srcIPCName, dstIPCName, this.IPCName, underlyingDstIPCName, underlyingflow);

				this.flowAllocated.addFlow(flow);
				this.flowMsgQueue.put(flow.getSrcPort(), flow.getMsgQueue());
				this.rib.RIBlog.infoLog("Flow Allocator:  portID is "+ flow.getSrcPort() + ", and app Name is " + srcIPCName);


			}

			//generate a CDAP message to create a flow object on the other end and get back the port id from the other side

			CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();
			obj.setIntval(flow.getSrcPort());
			obj.setStrval(this.IPCName);


			CDAP.CDAPMessage cdapMessage = CDAPMessage.generateM_CREATE
			("flow", srcIPCName, obj.buildPartial(),dstIPCName,dstIPCName,srcIPCName, srcIPCName );

			DTP.DTPMessage payload = message.DTPMessage.generatePayloadM_CDAP(cdapMessage.toByteArray());

			DTP.DTPMessage dTPMessage = message.DTPMessage.generateM_CDAP(underlyingDstIPCName, wellKnownPort, this.IPCName, flow.getSrcPort(), payload.toByteArray());



			try {
				if(flow.getTcpFlow() != null)
				{
					flow.getTcpFlow().sendDTPMsg(dTPMessage.toByteArray());
					this.rib.RIBlog.debugLog("DTP message sent on first level flow ----");

				}else
				{
					flow.getUnderlyingFlow().sendDTPMsg(dTPMessage.toByteArray());
					this.rib.RIBlog.debugLog("DTP message sent on second level flow ----");
				}


				this.rib.RIBlog.infoLog("Flow Allocator: M_CREATE(flow) msg sent to  " + dstIPCName);

				byte[] dtpReply = flow.receive();

				DTP.DTPMessage payloadWithHeader = null;

				try {
					payloadWithHeader = DTP.DTPMessage.parseFrom(dtpReply);

				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

				CDAP.CDAPMessage cdapMsg_M_CREATE_R = CDAP.CDAPMessage.parseFrom(payloadWithHeader.getPayload().toByteArray());
				//	CDAP.CDAPMessage cdapMsg_M_CREATE_R = CDAP.CDAPMessage.parseFrom(dtpReply);

				if(cdapMsg_M_CREATE_R.getResult() != -1) //-1 mean the other side don't want accept this flow request
				{
					int dstPort =cdapMsg_M_CREATE_R.getObjValue().getIntval();
					flow.setDstPort(dstPort);

					this.rib.RIBlog.infoLog("Flow Allocator: flow allocated successful  between srcIPC " + srcIPCName + "(portID:" + flow.getSrcPort()+ ") and dstIPC "
							+  dstIPCName + "(portID:" +  dstPort + ")" );


				}else
				{
					this.rib.RIBlog.infoLog("Flow Allocator: flow allocated failed between " + srcIPCName + " and " +  dstIPCName );
					this.flowAllocated.removeFlow(flow.getSrcPort());

					return null;
				}


			} catch (Exception e) {
				// TODO Auto-generated catch block

				//remove this flow from flowAllocated
				this.flowAllocated.removeFlow(flow.getSrcPort());

				e.printStackTrace();
				return null;
			}
		}


		return flow;
	}

	public Flow allocateAccept(String srcIPCName, String dstIPCName, String dstUnderlyingIPC, int dstUnderlyingIPCPort) {

		Flow flow = null;

		if(this.tcpManager != null)
		{
			TCPFlow tcpFlow = this.tcpManager.getTCPFlow(dstUnderlyingIPC);

			flow = new Flow(srcIPCName, dstIPCName, dstUnderlyingIPCPort, tcpFlow );

			this.flowAllocated.addFlow(flow);

			this.flowMsgQueue.put(flow.getSrcPort(), flow.getMsgQueue());


		}else
		{
			
			
			/////////////////////////////////////////////////////////////////////////////////////
			String nextHopToUnderlyingDstIPC = this.rib.getForwardingTable().get(dstUnderlyingIPC);
			
			this.rib.RIBlog.infoLog("@@@@@@@@@ nextHopToUnderlyingDstIPC is " +nextHopToUnderlyingDstIPC + ", dstUnderlyingIPC is " +dstUnderlyingIPC );
			
			
			while( this.rib.getForwardingTable().get(dstUnderlyingIPC) == null){}
			
			nextHopToUnderlyingDstIPC =this.rib.getForwardingTable().get(dstUnderlyingIPC);
			
			this.rib.RIBlog.infoLog("@@@@@@@@@ nextHopToUnderlyingDstIPC is " +nextHopToUnderlyingDstIPC + ", underlyingDstIPCName is " +dstUnderlyingIPC );
			
			
			// comment for debugging purposing

			//String nextHopToUnderlyingDstIPC = "appB";
			////////////////////////////////////////////////////////////////////////////////////


			int handleOfNextHopToUnderlyingDstIPC = this.irm.getHandle(nextHopToUnderlyingDstIPC);


			String underlyingIPCName = this.irm.getHandleMap().get(handleOfNextHopToUnderlyingDstIPC).ipcName;

			int underlyingPort =  this.irm.getHandleMap().get(handleOfNextHopToUnderlyingDstIPC).portID;

			IPCProcessImpl underlyingIPC = this.irm.getUnderlyingIPCs().get(underlyingIPCName);

			Flow underlyingflow = underlyingIPC.getFlowAllocator().getFlowAllocated().getFlow(underlyingPort);

			flow = new Flow(srcIPCName, dstIPCName, this.IPCName, dstUnderlyingIPC, dstUnderlyingIPCPort, underlyingflow);

			this.flowAllocated.addFlow(flow);
			this.flowMsgQueue.put(flow.getSrcPort(), flow.getMsgQueue());

		}


		CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();
		obj.setIntval(flow.getSrcPort());
		obj.setStrval(this.IPCName);


		CDAP.CDAPMessage cdapMessage = CDAPMessage.generateM_CREATE_R
		(0,"flow", srcIPCName, obj.buildPartial(), dstIPCName,dstIPCName,srcIPCName, srcIPCName );

		DTP.DTPMessage payload = message.DTPMessage.generatePayloadM_CDAP(cdapMessage.toByteArray());

		DTP.DTPMessage dTPMessage = message.DTPMessage.generateM_CDAP(dstUnderlyingIPC, dstUnderlyingIPCPort, this.IPCName, flow.getSrcPort(), payload.toByteArray());

		try {
			if(this.tcpManager!= null)
			{
				flow.getTcpFlow().sendDTPMsg(dTPMessage.toByteArray());
			}else
			{
				flow.getUnderlyingFlow().sendDTPMsg(dTPMessage.toByteArray());
			}

			this.rib.RIBlog.infoLog("Flow Allocator:M_CREATE_R(flow accepted) sent back,  " +
					"flow accepted successful between srcIPC " + srcIPCName + "(portID:" + flow.getSrcPort()+ ") and dstIPC "
					+  dstIPCName + "(portID:" +  dstUnderlyingIPCPort + ")" );

			//	this.forwardingTable.put(dstIPCName, dstIPCName);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return flow;
	}


	public void allocateReject(String srcIPCName, String dstIPCName, String dstUnderlyingIPC, int dstUnderlyingIPCPort) {


		CDAP.CDAPMessage cdapMessage = CDAPMessage.generateM_CREATE_R
		(-1,"flow", srcIPCName, dstIPCName,dstIPCName,srcIPCName, srcIPCName );

		DTP.DTPMessage payload = message.DTPMessage.generatePayloadM_CDAP(cdapMessage.toByteArray());

		DTP.DTPMessage dTPMessage = message.DTPMessage.generateM_CDAP(dstUnderlyingIPC, dstUnderlyingIPCPort, this.IPCName, this.appWellKnownPort.get(srcIPCName), payload.toByteArray());

		try {
			if(this.tcpManager!=null)
			{
				this.tcpManager.getTCPFlow(dstUnderlyingIPC).sendDTPMsg(dTPMessage.toByteArray());
			}else
			{

				String nextHopToUnderlyingDstIPC = this.forwardingTable.get(dstUnderlyingIPC);

				int handleOfNextHopToUnderlyingDstIPC = this.irm.getHandle(nextHopToUnderlyingDstIPC);

				String underlyingIPCName = this.irm.getHandleMap().get(handleOfNextHopToUnderlyingDstIPC).ipcName;

				int underlyingPort =  this.irm.getHandleMap().get(handleOfNextHopToUnderlyingDstIPC).portID;

				IPCProcessImpl underlyingIPC = this.irm.getUnderlyingIPCs().get(underlyingIPCName);

				Flow underlyingflow = underlyingIPC.getFlowAllocator().getFlowAllocated().getFlow(underlyingPort);

				underlyingflow.sendDTPMsg(dTPMessage.toByteArray());

			}
			this.rib.RIBlog.infoLog("Flow Allocator:M_CREATE_R(flow reject) sent back,  flow rejected between " + srcIPCName + " and " +  dstIPCName );

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	//	/**
	//	 * this method is used when a multi-hop flow needs to be created
	//	 * first ask IDD who can provide relay service to the dst IPC, if no , return null
	//	 * if yes, create this flow
	//	 * @param srcIPCName
	//	 * @param dstIPCName
	//	 * @return
	//	 */
	//	private Flow allocateMultiHopFlow(String srcIPCName, String dstIPCName) {
	//		// TODO Auto-generated method stub
	//
	//		String nextHop = this.forwardingTable.get(dstIPCName);
	//
	//		if(nextHop == null)
	//		{
	//			nextHop= this.resolveUpperDIFIPCbyIDD(dstIPCName);
	//		}
	//
	//		if(nextHop == null)
	//		{
	//			return null;
	//		}
	//
	//		int nextHopFlowHandle;
	//		try {
	//			nextHopFlowHandle = this.flowAllocated.getHandleID(srcIPCName, nextHop);
	//		} catch (Exception e) {
	//			// if there exception means there is no such flow
	//			Flow nextHopFlow = this.allocate(srcIPCName, nextHop);
	//			if(nextHopFlow == null)
	//			{
	//				this.rib.RIBlog.errorLog("Flow Allocator: flow allocated failed between " + srcIPCName + " and " +  dstIPCName );
	//				return null;
	//			}
	//			nextHopFlowHandle = nextHopFlow.getSrcPort();
	//			//e.printStackTrace();
	//		}
	//
	//		Flow flow = new Flow(srcIPCName, dstIPCName);
	//
	//		this.flowAllocated.addFlow(flow);
	//
	//		this.flowMsgQueue.put(flow.getSrcPort(), flow.getMsgQueue());
	//
	//
	//		CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();
	//		obj.setIntval(flow.getSrcPort());
	//		obj.setStrval(this.IPCName);
	//
	//
	//		CDAP.CDAPMessage cdapMessage = CDAPMessage.generateM_CREATE
	//		("flow", srcIPCName, obj.buildPartial(), dstIPCName, dstIPCName, srcIPCName, srcIPCName );
	//
	//		DTP.DTPMessage payload = message.DTPMessage.generatePayloadM_CDAP(cdapMessage.toByteArray());
	//
	//		try {
	//			this.send(nextHopFlowHandle, payload.toByteArray());
	//
	//
	//			byte[] reply = flow.receive();
	//
	//
	//			//			DTP.DTPMessage dtpMessage = null;
	//			//
	//			//			try {
	//			//				dtpMessage = DTP.DTPMessage.parseFrom(reply);
	//			//
	//			//			} catch (InvalidProtocolBufferException e) {
	//			//				e.printStackTrace();
	//			//			}
	//			//
	//			//			CDAP.CDAPMessage cdapMsg_M_CREATE_R = CDAP.CDAPMessage.parseFrom(dtpMessage.getPayload().toByteArray());
	//
	//			CDAP.CDAPMessage cdapMsg_M_CREATE_R = CDAP.CDAPMessage.parseFrom(reply);
	//
	//			if(cdapMsg_M_CREATE_R.getResult() != -1) //-1 mean the other side don't want accept this flow request
	//			{
	//				int dstPort =cdapMsg_M_CREATE_R.getResult();
	//				flow.setDstPort(dstPort);
	//
	//				this.rib.RIBlog.infoLog("Flow Allocator: flow allocated successful between " + srcIPCName + " and " +  dstIPCName );
	//			}
	//		} catch (Exception e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//
	//		return flow;
	//	}




	//	private String resolveUpperDIFIPCbyIDD(String appName) {
	//
	//		if(this.IDDName == null)
	//		{
	//			this.rib.getAttribute("iddName").toString();
	//		}
	//
	//		String nextHop = null;
	//
	//		CDAP.CDAPMessage cdapMsg_M_READ = message.CDAPMessage.generateM_READ
	//		("resloveRelay", appName, this.IDDName, this.IDDName, this.IPCName,this.IPCName);
	//
	//
	//		try {
	//			this.tcpManager.getTCPFlow(this.IDDName).sendCDAPMsg(cdapMsg_M_READ.toByteArray());
	//		} catch (Exception e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//
	//		System.out.println("I am in the resolveUpperDIFIPC. CDAP M_READ sent ");
	//
	//
	//		//loop until got reply, here M_READ_R is processed by the IPC handle
	//		while(!this.forwardingTable.containsKey(appName)){}
	//
	//		nextHop = this.forwardingTable.get(appName);
	//
	//		return nextHop;
	//	}




	/**
	 * @return the flowMsgQueue
	 */
	public  MessageQueue getFlowMsgQueue(int portID) {
		return this.flowMsgQueue.get(portID);
	}


	/**
	 * this is used to revolve an application to the low DIF IPC
	 * just like reslove a URL to ip address
	 * @param dstIPCName
	 * @return
	 * @throws  
	 */
	public String resolveApplicationByNMS(String appName) {
		String underlyingIPCName = null;

		if(this.NMSName == null)
		{
			this.NMSName = this.rib.getAttribute("nmsName").toString();
		}
		//send M_READ_R msg to NMS and wait 


		CDAP.CDAPMessage cdapMsg_M_READ = message.CDAPMessage.generateM_READ
		("resloveApp", appName, this.NMSName, this.NMSName, this.IPCName,this.IPCName);


		try {

			if(this.tcpManager != null)
			{
				this.tcpManager.getTCPFlow(this.NMSName).sendCDAPMsg(cdapMsg_M_READ.toByteArray());

			}else
			{

				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(cdapMsg_M_READ.toByteArray());
				this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	this.rib.RIBlog.debugLog("FlowAllocatorImpl::resolveUpperDIFIPC: CDAP M_READ sent, appName is " + appName + ", NMS name is " + this.NMSName);


		//loop until got reply, here M_READ_R is processed by the IPC handle
		while(!this.appToIPCMapping.containsKey(appName)){}

		underlyingIPCName = this.appToIPCMapping.get(appName);

		


		return underlyingIPCName;
	}


	/**
	 * @return the appWellKnownPort
	 */
	public synchronized LinkedHashMap<String, Integer> getAppWellKnownPort() {
		return appWellKnownPort;
	}


	/**
	 * @param appWellKnownPort the appWellKnownPort to set
	 */
	public synchronized void setAppWellKnownPort(
			LinkedHashMap<String, Integer> appWellKnownPort) {
		this.appWellKnownPort = appWellKnownPort;
	}


	/**
	 * return a handle to IPC
	 */
	public int addIncomingFlow(String srcIPCName, String dstIPCName, int dstPortID, String dstUnderlyingIPCName)
	{
		int handleID = -1;

		Flow flow = new Flow(srcIPCName, dstIPCName,dstPortID);

		this.flowAllocated.addFlow(flow);

		this.flowMsgQueue.put(flow.getSrcPort(), flow.getMsgQueue());

		handleID =flow.getSrcPort();


		this.rib.RIBlog.infoLog("Flow Allocator: incoming flow allocated successful between " 
				+ srcIPCName + " and " +  dstIPCName + " with handleID is " + handleID );

		return handleID;
	}

	public void deallocate(int portID) {

		this.flowAllocated.removeFlow(portID);
	}


	//Note: msg here is a payload msg with a payload header (M_CDAP or M_DTP)
	public void send(int portID, byte[] payloadMsg) throws Exception {

		Flow flow = this.flowAllocated.getFlow(portID);


		//String nextHopIPC = this.forwardingTable.get(flow.getDstIPCName());

		//System.out.println("let us see what is the nextHopIPC " + nextHopIPC);
		//System.out.println("let us see what is flow.getSrcIPCName() " + flow.getSrcIPCName());

		//  flow.printFlow();
		//	Flow nextHopFlow = this.flowAllocated.getFlow(flow.getSrcIPCName(), nextHopIPC);
		//	nextHopFlow.printFlow();

		DTP.DTPMessage payload = null;

		payload = DTP.DTPMessage.parseFrom(payloadMsg);


		switch(payload.getOpCode()){


		case M_CDAP:

			flow.sendCDAPMsg(payload.getPayload().toByteArray());

			break;

		case M_DTP:

			flow.sendDTPMsg(payload.getPayload().toByteArray());

			break;

		default:

			break;
		}


	}


	public byte[] receive(int handleID) {

		return this.flowAllocated.getFlow(handleID).receive();
	}



	/**
	 * return a flow by giving a dst IPC Name
	 * Since there may be multiple flows to the same IPC,so just pick the first one
	 * this will be used when do the relaying
	 * @param IPCName
	 */
	public void getOneFlow(String IPCName)
	{


	}


	/**
	 * @return the flowAllocated
	 */
	public synchronized FlowAllocated getFlowAllocated() {
		return flowAllocated;
	}




	/**
	 * @param flowAllocated the flowAllocated to set
	 */
	public synchronized void setFlowAllocated(FlowAllocated flowAllocated) {
		this.flowAllocated = flowAllocated;
	}



	/**
	 * @return the forwardingTable
	 */
	public synchronized LinkedHashMap<String, String> getForwardingTable() {
		return forwardingTable;
	}




	/**
	 * @param forwardingTable the forwardingTable to set
	 */
	public synchronized void setForwardingTable(
			LinkedHashMap<String, String> forwardingTable) {
		this.forwardingTable = forwardingTable;
	}


	/**
	 * @return the appToIPCMapping
	 */
	public synchronized LinkedHashMap<String, String> getAppToIPCMapping() {
		return appToIPCMapping;
	}




	/**
	 * @param appToIPCMapping the appToIPCMapping to set
	 */
	public synchronized void setAppToIPCMapping(
			LinkedHashMap<String, String> appToIPCMapping) {
		this.appToIPCMapping = appToIPCMapping;
	}


	public int addNewApp(String upperIPCName) {
		int wellKnownPort = this.flowAllocated.addNewApp(upperIPCName);

		this.appWellKnownPort.put(upperIPCName, wellKnownPort);

		return wellKnownPort;
	}








	//	/**
	//	 * allocate flow
	//	 */
	//	public synchronized Flow0 allocate(String IPCName, String controlOrDataFlag) {
	//
	//		Flow0 flow = null;
	//
	//		//FIXME:///IDD flow
	//		if(IPCName.equals(this.config.getIDD_NAME()))
	//		{
	//			if ( flowAllocated.containsFlow(IPCName, controlOrDataFlag))//existed already
	//			{
	//				flow = flowAllocated.getFlow(IPCName, controlOrDataFlag);
	//			}
	//			else{
	//
	//				if(controlOrDataFlag.equals("control"))
	//				{
	//					flow = new Flow0(IPCName, this.config.getIDDPort());
	//				}
	//				else
	//				{
	//					flow = new Flow0(IPCName, this.config.getIDDDataPort());
	//
	//				}
	//
	//				if( flow.getTcpFlow().getSocket() == null)
	//				{
	//					return null;
	//				}
	//
	//				flowAllocated.addFlow(IPCName,controlOrDataFlag, flow);
	//
	//
	//				try{
	//
	//					if(controlOrDataFlag.equals("control"))
	//					{
	//						new OutgoingComHandler(this.controlMsgQueue, flow).start();			
	//					}
	//					else  if(controlOrDataFlag.equals("data"))
	//					{
	//						new OutgoingComHandler(this.dataMsgQueue, flow).start();
	//					}
	//					else 
	//					{
	//						this.rib.RIBlog.errorLog("Flow Allocator: Flow type not correct");
	//					}
	//
	//				}catch(Exception e)
	//				{
	//
	//					this.rib.RIBlog.errorLog(e.getMessage());
	//				}
	//
	//			}
	//			return flow;
	//		}
	//		///////////////////////////////////////////////////
	//
	//		///not IDD flow
	//
	//		if ( flowAllocated.containsFlow(IPCName, controlOrDataFlag))//existed already
	//		{
	//			flow = flowAllocated.getFlow(IPCName, controlOrDataFlag);
	//		}
	//		else{
	//
	//			///first check whether the name is resolved by DNS
	//
	//			if(!this.dataBase.containsKey(IPCName))
	//			{
	//				this.queryDNS(IPCName);
	//			}
	//
	//
	//			DNS.DNSRecord dr = this.dataBase.get(IPCName);
	//
	//			String IP = dr.getIp();
	//			int controlPort = dr.getControlPort();
	//			int dataPort = dr.getDataPort();
	//
	//
	//			if(controlOrDataFlag.equals("control"))
	//			{
	//				//flow = new Flow0(IPCName, this.controlPortRINA);
	//
	//				flow = new Flow0(IP, controlPort);
	//
	//			}
	//			else
	//			{
	//				//flow = new Flow0(IPCName, this.dataPortRINA);
	//				flow = new Flow0(IP, dataPort);
	//
	//			}
	//
	//			if( flow.getTcpFlow().getSocket() == null)
	//			{
	//				return null;
	//			}
	//
	//
	//			flowAllocated.addFlow(IPCName,controlOrDataFlag, flow);
	//
	//			//attach a handler thread for this flow to receive msg and put it in the msgQueue
	//			try{
	//
	//				if(controlOrDataFlag.equals("control"))
	//				{
	//					new OutgoingComHandler(this.controlMsgQueue, flow).start();			
	//				}
	//				else  if(controlOrDataFlag.equals("data"))
	//				{
	//					new OutgoingComHandler(this.dataMsgQueue, flow).start();
	//				}
	//				else 
	//				{
	//					this.rib.RIBlog.errorLog("Flow Allocator: Flow type not correct");
	//				}
	//
	//			}catch(Exception e)
	//			{
	//
	//				this.rib.RIBlog.errorLog(e.getMessage());
	//			}
	//
	//		}
	//
	//		return flow;
	//	}

	//	/**
	//	 * deallocate flow
	//	 */
	//	public synchronized void deallocate(String IPCName) {
	//		if (flowAllocated.containsFlow(IPCName)){
	//
	//			if(flowAllocated.containsFlow(IPCName, "control"))
	//			{
	//				Flow0 flowControl = flowAllocated.getFlow(IPCName, "control");
	//				flowControl.close();
	//			}
	//
	//			if(flowAllocated.containsFlow(IPCName, "data"))
	//			{
	//				Flow0 flowData = flowAllocated.getFlow(IPCName, "data");
	//				flowData.close();
	//			}
	//
	//			flowAllocated.removeFlow(IPCName);		
	//		}
	//	}
	//	/**
	//	 * 
	//	 * @param IPCName
	//	 * @return flow
	//	 */
	//	public synchronized Flow0 getFlow(String IPCName, String controlOrDataFlag){
	//		return flowAllocated.getFlow(IPCName, controlOrDataFlag);
	//
	//	}
	//
	//
	//
	//	/**
	//	 * @return the allocatedFlows
	//	 */
	//	public synchronized FlowAllocated0 getAllFlowAllocated() {
	//		return flowAllocated;
	//	}






	//	/**
	//	 * allocation  request
	//	 * The source Flow Allocator determines if the request is well formed.  
	//	 * If not well-formed, an response is invoked with the appropriate error code.  
	//	 * If the request is well-formed, a new FlowAllocator-Intsance is created and passed the 
	//	 * parameters of this request to handle the allocation. 
	//	 * It is a matter of DIF policy (AllocateNotificationPolicy) whether an 
	//	 * Allocate_Request is invoked with a status of pending, or whether a response is withheld 
	//	 * until an Allocate_Response can be delivered with a status of success or failure.
	//	 */
	//	public void allocationRequest(String IPCName) {
	//		// TODO Auto-generated method stub
	//		//return allocate(IPCName);
	//
	//	}
	//	/**
	//	 * allocation response
	//	 */
	//	public Flow0 allocationResponse() {
	//		// TODO Auto-generated method stub
	//		return null;
	//
	//	}
	//	/**
	//	 * create request
	//	 *  the requestor is looking for an IPC Process in this DIF that can create a local binding 
	//	 *  with the requested Destination_Naming_Info requested.
	//	 *  
	//	 *  If the address returned by the DirectoryForwardingTable is this IPC Process, it creates a FAI and passes the Create_request to it.
	//	 *  If the address returned b	y the DirectoryForwardingTable is not this IPC Process, it decrements the HopCount in the Request,
	//	 *  if the HopCount is zero, then it logs the error and sends a Create Flow Response to the Requesting IPC Process indicating the application was not found.
	//	 *  If the HopCount is greater than zero, then it forwards the request to the default entry in the table.
	//	 */
	//	public void createRequest(String IPCName) {
	//		// TODO Auto-generated method stub
	//
	//	}
	//	/**
	//	 * create response
	//	 */
	//	public Flow0 createResponse() {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}

	/**
	 * @return the tcpManager
	 */
	public synchronized TCPFlowManager getTcpManager() {
		return tcpManager;
	}

	/**
	 * @param tcpManager the tcpManager to set
	 */
	public synchronized void setTcpManager(TCPFlowManager tcpManager) {
		this.tcpManager = tcpManager;
	}

	/**
	 * @return the irm
	 */
	public synchronized IRM getIrm() {
		return irm;
	}

	/**
	 * @param irm the irm to set
	 */
	public synchronized void setIrm(IRM irm) {
		this.irm = irm;
	}






}//end of class
