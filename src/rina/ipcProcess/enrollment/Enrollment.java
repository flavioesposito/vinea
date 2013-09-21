
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

package rina.ipcProcess.enrollment;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.config.RINAConfig;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow0.TCPFlow;

import rina.idd.IDD;
import rina.ipcProcess.impl.IPCProcessImpl;
import rina.ipcProcess.impl.TCPFlowManager;
import rina.ipcProcess.util.MessageQueue;
import rina.irm.IRM;

import rina.rib.impl.RIBDaemonImpl;
import rina.rib.impl.RIBImpl;
import rina.routing.RoutingDaemon;

import java.util.*;

import message.DTPMessage;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 
 * This is component of IPC Process which does the enrollment. It will enroll the new member into the DIF.
 * NOTE: Basically this is previous NMS which does enrollment and resolve application name to a IPC name in the DIF.
 *
 */
public class Enrollment extends Thread {


	private RIBDaemonImpl RIBdaemon = null;

	private RIBImpl rib = null;

	private TCPFlowManager tcpManager = null;

	private IRM irm = null;

	private String IPCName;

	private String DIFName;

	private String IDDName;
	
	private int defaultFrequency = 2;


	/**
	 * It indicates that this NMS has not  finished registeration at IDD 
	 */
	private boolean registered = false;


	// The following are used to check whether the menber enrolled is alive or not
	private LinkedHashMap<String, Boolean> ipcStatus = null;
	private LinkedHashMap<String, Boolean> ipcProbeStatus = null;

	private int period = 3;

	/**
	 * all messages for enrollment first go to this msg queue
	 */
	private MessageQueue msgQueue = null;



	/**
	 * queues of messages, this is used for each new member request
	 */
	private LinkedHashMap<String, MessageQueue> messageQueues = null;


	/**
	 * <application, <underlyingIPC, wellknownport>>
	 */
	private  LinkedHashMap<String, LinkedList<ApplicationEntry>> appToIPCMapping = null;

	private LinkedList<String> appsReachable = null;


	/**
	 * This one is used to store the status of the members that are actively enrolled by NMS
	 * mainly corresponding  with multiple hop flow creation
	 */
	private LinkedHashMap<String, String> askToEnrollMemberStatus = null;


	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////Routing related , start /////////////////////////////////////////


	private RoutingDaemon routingDaemon = null;

	private MessageQueue routingDaemonQueue = null;

	//////////////////////Routing related , end /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * DIF-0 NMS
	 */
	public Enrollment(RIBDaemonImpl RIBdaemon, TCPFlowManager tcpManager)
	{


		this.RIBdaemon = RIBdaemon;
		this.rib = this.RIBdaemon.getLocalRIB();
		this.DIFName = this.rib.getAttribute("difName").toString();
		this.IDDName = this.rib.getAttribute("iddName").toString();
		this.IPCName = this.rib.getAttribute("ipcName").toString(); 
		
		

		this.tcpManager = tcpManager;

		this.rib.getMemberList().add(this.IPCName);

		this.appToIPCMapping = new LinkedHashMap<String, LinkedList<ApplicationEntry>> ();
		this.rib.addAttribute("appToIPCMapping",this.appToIPCMapping);

		this.ipcProbeStatus = new LinkedHashMap<String, Boolean> ();
		this.rib.addAttribute("ipcProbeStatus",this.ipcProbeStatus);
		this.ipcStatus = new LinkedHashMap<String, Boolean> ();
		this.rib.addAttribute("ipcStatus",this.ipcStatus);

		this.appsReachable = new LinkedList<String>();
		this.rib.addAttribute("appsReachable", this.appsReachable);

		this.messageQueues = new  LinkedHashMap<String, MessageQueue>();
		this.msgQueue = new MessageQueue();

		this.askToEnrollMemberStatus = new LinkedHashMap<String, String>() ;
		this.rib.addAttribute("askToEnrollMemberStatus", askToEnrollMemberStatus);

		this.routingDaemonQueue = (MessageQueue)this.rib.getAttribute("routingDaemonQueue");
		this.routingDaemon = (RoutingDaemon)this.rib.getAttribute("routingDaemon");

		this.registerToIDD();
		this.start();
	}


	/**
	 * Non-0 DIF NMS
	 * @param NMSName
	 * @param DIFName
	 * @param IDDName
	 * @param underlyingIPCList
	 */
	public Enrollment( RIBDaemonImpl RIBdaemon, IRM irm)
	{

		this.RIBdaemon = RIBdaemon;
		this.rib = this.RIBdaemon.getLocalRIB();
		this.DIFName = (String)this.rib.getAttribute("difName");
		this.IDDName = (String)this.rib.getAttribute("iddName");
		this.IPCName = (String)this.rib.getAttribute("ipcName"); 

		this.irm = irm;

		this.rib.getMemberList().add(this.IPCName);


		this.appToIPCMapping = new LinkedHashMap<String, LinkedList<ApplicationEntry>> ();
		this.rib.addAttribute("appToIPCMapping",this.appToIPCMapping);

		this.ipcProbeStatus = new LinkedHashMap<String, Boolean> ();
		this.rib.addAttribute("ipcProbeStatus",this.ipcProbeStatus);
		this.ipcStatus = new LinkedHashMap<String, Boolean> ();
		this.rib.addAttribute("ipcStatus",this.ipcStatus);

		this.appsReachable = new LinkedList<String>();
		this.rib.addAttribute("appsReachable", this.appsReachable);

		this.messageQueues = new  LinkedHashMap<String, MessageQueue>();
		this.msgQueue = new MessageQueue();

		this.askToEnrollMemberStatus = new LinkedHashMap<String, String>() ;
		this.rib.addAttribute("askToEnrollMemberStatus", askToEnrollMemberStatus);

		this.routingDaemonQueue = (MessageQueue)this.rib.getAttribute("routingDaemonQueue");
		this.routingDaemon = (RoutingDaemon)this.rib.getAttribute("routingDaemon");


		this.registered = true; // for now we don't register NMS to IDD for non-o DIF
		this.start();
	}



	/**
	 * NMS listening thread
	 */
	public void run()
	{

		this.rib.RIBlog.infoLog("Enrollment Component: ("  +this.IPCName +  ") starts to work ");



		DIFStatus difStatus = new DIFStatus(period, this.RIBdaemon, this.ipcStatus, this.ipcProbeStatus);


		this.initPub();

//		if(this.tcpManager!=null)//for now only check status at DIF 0
//		{
//			//			difStatus.start();
//		}else
//		{
//			//this.routingDaemon.start();
//
//		}




		while(true)
		{

			byte[] msg = this.msgQueue.getReceive();

			if(msg != null)
			{
				handleReceiveMessage(msg);
			}
		}
	}


	

	private void initPub() {

		if(this.rib.getAttribute("Pubfrequency") != null) {
			this.RIBdaemon.createPub(Integer.parseInt((this.rib.getAttribute("Pubfrequency").toString())), "appsReachable");
		}else {
			this.RIBdaemon.createPub(this.defaultFrequency, "appsReachable");
		}

		//comment for now : load balancing
		//		int pubID = this.RIBdaemon.createPub(this.defaultFrequency, "appStatus"); // this will be subscribed by IDD
		//		// for now we automaticly add idd to subsciber
		//		this.rib.getPubIDToHandler().get(pubID).addSubscriber(this.rib.getAttribute("iddName").toString());


	}



	private boolean firstIDDMsgFlag = true;
	/**
	 * @param msg
	 */
	private void handleReceiveMessage(byte[] msg) {



		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		this.rib.RIBlog.infoLog("Enrollment Component: cdapMessage received, opcode is  " +  cdapMessage.getOpCode()  +
				", src is  " +  cdapMessage.getSrcApName() + ", objectClass is " + cdapMessage.getObjClass());




		if(cdapMessage.getObjClass().equals("PubSub") && !cdapMessage.getObjValue().getStrval().equals("appsReachable"))
		{
			this.rib.RIBlog.infoLog("Enrollment Component:: PubSub( not  appsReahable )  received, opcode is "+cdapMessage.getOpCode() +
					"  and it is "  +cdapMessage.getObjValue().getStrval() );

			( (MessageQueue)this.rib.getAttribute("ribDaemonQueue")).addReceive(msg);
			return;
		}


		String SrcName = cdapMessage.getSrcApName();

		// this is an registeration reply from IDD
		if(SrcName.equals(this.IDDName)&& this.registered == false )
		{
			handleIDDRegistrationReply(cdapMessage);
			return;
		}




		if(this.registered == true && this.tcpManager !=null)
		{

			if(SrcName.equals(this.IDDName) )
			{                   
				messageQueues.get(cdapMessage.getObjName()).addReceive(msg);
			}	
			else
			{


				if(messageQueues.containsKey(SrcName))
				{
					messageQueues.get(SrcName).addReceive(msg);
				}
				else
				{

					MessageQueue mq = new MessageQueue();
					mq.addReceive(msg);
					messageQueues.put(SrcName, mq);


					new EnrollmentHandler(this.RIBdaemon,this.tcpManager, mq, SrcName).start();

				}
			}
		}else //for non-o dif NMS, so the first message from IDD is a register relay service M_WRITE_R
		{

			if(SrcName.equals(this.IDDName)&& this.firstIDDMsgFlag == false)
			{                   
				messageQueues.get(cdapMessage.getObjName()).addReceive(msg);
			}else
			{
				///////////////////////////////
				if(SrcName.equals(this.IDDName))
				{
					this.firstIDDMsgFlag = false;
				}
				///////////////////////////////////


				if(messageQueues.containsKey(SrcName))
				{
					messageQueues.get(SrcName).addReceive(msg);
				}
				else
				{

					MessageQueue mq = new MessageQueue();
					mq.addReceive(msg);
					messageQueues.put(SrcName, mq);

					new EnrollmentHandler(this.RIBdaemon,this.irm, mq, SrcName).start();

				}
			}
		}

	}


	/**
	 *  register to IDD that it can enrollment new member for a DIF
	 */
	private synchronized void registerToIDD() {



		//		String underlyingDif = this.config.getProperty("rina.dif.supportingDIF");


		IDD.iddEntry.Builder IDDEntry = IDD.iddEntry.newBuilder()
		//.addSupportingDIF(underlyingDif )
		//.addServiceURL(this.NMSName) 
		.setDIFName(this.DIFName)
		.setNmsURL(this.IPCName);


		IDDEntry.build();


		//generate M_WRITE to IDD

		CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
		ByteString IDDentry = ByteString.copyFrom(IDDEntry.build().toByteArray());
		ObjValue.setByteval(IDDentry);
		CDAP.objVal_t objvalue = ObjValue.buildPartial();


		CDAP.CDAPMessage M_WRITE_msg = message.CDAPMessage.generateM_WRITE
		(      "newNMSRegistration",
				this.IPCName,		
				objvalue, 
				"IDD",//destAEInst
				"IDD",//destAEName
				this.IDDName,//destApInst
				this.IDDName,//destApName
				00001, //invokeID
				"NMS",//srcAEInst
				"NMS", //srcAEName
				this.IPCName,//srcApInst
				this.IPCName//srcApName
		);


		try {
			if(this.tcpManager != null)
			{
				TCPFlow IDDFlow  =  this.tcpManager.allocateTCPFlow(this.IDDName);
				IDDFlow.sendCDAPMsg(M_WRITE_msg.toByteArray());
			}else
			{
				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_WRITE_msg.toByteArray());


				System.out.println("IRM: IDDName is  " + this.IDDName);
				System.out.println("IRM: idd handle is " +  this.irm.getHandle(this.IDDName));


				this.irm.send(this.irm.getHandle(this.IDDName), payload.toByteArray());
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: ("  +this.IPCName +  "): register(M_WRITE) to IDD sent");


	}

	/**
	 * handle IDD Registration Reply
	 * @param cdapMessage
	 */
	private void handleIDDRegistrationReply(CDAPMessage cdapMessage) {

		if(cdapMessage.getResult() == 0)//successful
		{
			this.registered = true;
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: registration to IDD sucessful");

		}else if(cdapMessage.getResult() == -1)//DIFNAME already used by others 
		{
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: registration to IDD fails, DIF Name already used by others. " +
			"Please choose another DIF name.");

		}

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




}
