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

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import message.DTPMessage;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.config.RINAConfig;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow.Flow;
import rina.flow0.TCPFlow;
import rina.flowAllocator.impl.FlowAllocatorImpl;
import rina.idd.IDD;
import rina.ipcProcess.api.IPCProcessAPI;
import rina.ipcProcess.api.IPCProcessRIBAPI;
import rina.ipcProcess.enrollment.Enrollment;
import rina.ipcProcess.util.MessageQueue;
import rina.irm.IRM;
import rina.rib.impl.RIBDaemonImpl;
import rina.rib.impl.RIBImpl;
import rina.routing.RoutingDaemon;
import rina.messages.*;
import rina.messages.EnrollmentInformationT.enrollmentInformation_t;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * IPC process Implementation: this task creates and monitors a flow and provides any 
 * management over its life time

 *
 */
public class IPCProcessImpl  extends Thread implements IPCProcessAPI, IPCProcessRIBAPI{

	private RINAConfig config = null;

	private String IPCName = null;

	private RIBDaemonImpl RIBdaemon = null;

	private RIBImpl rib = null;

	private MessageQueue ribDaemonQueue = null;

	private FlowAllocatorImpl flowAllocator = null;

	private Enrollment enrollmentCompoment = null;


	/**
	 * this is the flow allocator for DIF 0
	 * since the DIF 0 flow is TCP flow so we call it TCPFlow Manager in DIF 0
	 * IRM is the counterpart for non-zero DIF
	 */
	private TCPFlowManager tcpManager = null;
	private IRM irm = null;


	private String IDDName = null; 

	private String NMSName = null;

	private String DIFName;

	private String DNSName;

	private String authen_name;

	private String authen_pw;


	private state IPCState = null;

	private enum state {
		LISTENING, 
		DIF_JOINED,
		JOINING_DIF,
		WAIT_FOR_START,
		WAIT_TO_JOIN_DIF,
		WAIT_FOR_IDD_LOOKUP,
		WAIT_FOR_RELEASE
	};

	/**
	 * this is the queue which stores all messages received 
	 * RMT will process this queue to decide whether msg is for this IPC or need to be RMTed
	 */
	private MessageQueue dtpMsgQueue = null;

	/**
	 * this is the queue which stores cdap message received 
	 * and the msg is obtained from msgQueue above
	 */
	private MessageQueue cdapMsgQueue = null;


	///////////////////////////////////////////////////////////////////////////////////////////////////////
	//The following  are for RMT

	/**
	 * map String name , MessageQueue
	 */
	private LinkedHashMap<String, MessageQueue> upperIPCsDTPMsgQueue = null ;

	private LinkedHashMap<String, MessageQueue> upperIPCsCDAPMsgQueue = null;

	/**
	 * for RMT: This is a thread that handles the RMT task, basically it handles the data message Queue
	 * Relay or Multiplexing 
	 */
	private IPCProcessRMT ipcProcessRMT = null;
	//////////////////////////////////////////////////////////////////////////////////////////////////////



	private LinkedHashMap<String, IPCProcessImpl> underlyingIPCs = null;



	//this one is used to dynamic creating a DIF 
	private boolean DIFFormation = false;

	private String appCreateThisIPC = null;


	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////Routing related , start /////////////////////////////////////////

	/**
	 * how long the time period is the IPC probes NMS
	 */
	private int period = 3; //extract routing HELLO message frequency into config file as a policy 

	private RoutingDaemon routingDaemon = null;

	private MessageQueue routingDaemonQueue = null;

	//////////////////////Routing related , end /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////



	/**
	 * DIF0
	 * @param config
	 */
	public IPCProcessImpl(RINAConfig config)
	{
		this.rib = new RIBImpl();
		this.ribDaemonQueue = new MessageQueue();
		this.rib.addAttribute("ribDaemonQueue", this.ribDaemonQueue);

		this.routingDaemonQueue = new MessageQueue();
		this.rib.addAttribute("routingDaemonQueue", this.routingDaemonQueue);

		this.config = config;
		this.IPCName = this.config.getIPCName();
		this.DIFName = this.config.getDIFName();

		//put the information into the RIB
		this.rib.addAttribute("config", this.config);
		this.rib.addAttribute("difName", this.DIFName);
		this.rib.addAttribute("ipcName", this.IPCName);

		this.rib.setDIF0Flag(true);

		this.authen_name =  this.config.getUserName();
		this.authen_pw =  this.config.getPassWord();

		this.IDDName = this.config.getIDDName();

		this.rib.addAttribute("iddName", this.IDDName);

		this.IPCState = state.LISTENING;

		this.dtpMsgQueue = new MessageQueue();

		this.cdapMsgQueue = new  MessageQueue();

		this.upperIPCsDTPMsgQueue = new LinkedHashMap<String, MessageQueue> ();
		this.upperIPCsCDAPMsgQueue = new LinkedHashMap<String, MessageQueue> ();


		this.tcpManager = new TCPFlowManager(this.rib, this.dtpMsgQueue);
		this.RIBdaemon = new RIBDaemonImpl(this.rib, this.tcpManager);
		this.rib.setRibDaemon(this.RIBdaemon);

		this.initConnections();


		this.flowAllocator = new FlowAllocatorImpl(this.rib, this.dtpMsgQueue, this.tcpManager);


		this.ipcProcessRMT = new IPCProcessRMT(this.flowAllocator, this.rib, this.dtpMsgQueue, this.cdapMsgQueue
				, this.upperIPCsDTPMsgQueue, this.upperIPCsCDAPMsgQueue);

		this.routingDaemon = new RoutingDaemon(this.RIBdaemon, this.tcpManager);
		this.rib.addAttribute("routingDaemon", this.routingDaemon);


		// if this IPC is able to enroll new IPC members.
		if(this.config.enrollmentFlag() == true)
		{ // to config Pubfrequency from config file
			if(this.rib.getAttribute("Pubfrequency")!= null) {
					this.rib.addAttribute("Pubfrequency", this.config.getProperty("rina.publish.frequency"));
		}
			this.enrollmentCompoment = new Enrollment(this.RIBdaemon,this.tcpManager);
			this.rib.setEnrollmentFlag(true);
			this.rib.addAttribute("authenPolicy", this.config.getProperty("rina.enrollment.authenPolicy"));
			this.rib.addAttribute("userName", this.config.getProperty("rina.ipc.userName"));
			this.rib.addAttribute("passWord", this.config.getProperty("rina.ipc.passWord"));
			this.IPCState = state.DIF_JOINED;
		}

		//if it is , and it is not an dif 0 ipc that does not belong to any DIF, then do the enrollment
		if( this.config.enrollmentFlag()!= true && this.DIFName != null)
		{
			this.enrollment(this.DIFName);
		}

		this.start();

	}


	/**
	 * Non-0 DIF
	 * @param IPCName
	 * @param IDDName
	 */
	public IPCProcessImpl(String IPCName, String IDDName)
	{


		this.rib = new RIBImpl();
		this.ribDaemonQueue = new MessageQueue();
		this.rib.addAttribute("ribDaemonQueue", this.ribDaemonQueue);

		this.routingDaemonQueue = new MessageQueue();
		this.rib.addAttribute("routingDaemonQueue", this.routingDaemonQueue);

		this.IPCName = IPCName;
		this.IDDName = IDDName;	

		this.rib.addAttribute("iddName", this.IDDName);
		this.rib.addAttribute("ipcName", this.IPCName);
		this.rib.addAttribute("Pubfrequency", this.config.getProperty("rina.publish.frequency"));


		this.dtpMsgQueue = new MessageQueue();

		this.cdapMsgQueue = new  MessageQueue();

		this.upperIPCsDTPMsgQueue = new LinkedHashMap<String, MessageQueue> ();

		this.upperIPCsCDAPMsgQueue = new LinkedHashMap<String, MessageQueue> ();

		this.IPCState = state.LISTENING;

		this.underlyingIPCs = new LinkedHashMap<String, IPCProcessImpl>();

		this.irm = new IRM(this.IPCName,this.rib, this.underlyingIPCs, this.dtpMsgQueue, this.cdapMsgQueue);
		this.RIBdaemon = new RIBDaemonImpl(this.rib, this.irm);
		this.rib.setRibDaemon(this.RIBdaemon);

		this.flowAllocator = new FlowAllocatorImpl(this.rib, this.dtpMsgQueue, this.irm);

		this.ipcProcessRMT = new IPCProcessRMT(this.flowAllocator, this.rib, this.dtpMsgQueue, this.cdapMsgQueue
				, this.upperIPCsDTPMsgQueue, this.upperIPCsCDAPMsgQueue);

		this.routingDaemon = new RoutingDaemon(this.RIBdaemon, this.irm);
		this.rib.addAttribute("routingDaemon", this.routingDaemon);


	}

	/**
	 * APP
	 * @param IPCName
	 */
	public IPCProcessImpl(String IPCName, String IDDName, boolean appFlag)
	{
		this.IPCName = IPCName;
		this.IDDName = IDDName;
		this.rib = new RIBImpl();

		this.rib.addAttribute("iddName", this.IDDName);
		this.rib.addAttribute("ipcName", this.IPCName);
		this.ribDaemonQueue = new MessageQueue();
		this.rib.addAttribute("ribDaemonQueue", this.ribDaemonQueue);


		this.routingDaemonQueue = new MessageQueue();
		this.rib.addAttribute("routingDaemonQueue", this.routingDaemonQueue);


		this.dtpMsgQueue = new MessageQueue();

		this.cdapMsgQueue = new  MessageQueue();

		this.IPCState = state.LISTENING;

		this.underlyingIPCs = new LinkedHashMap<String, IPCProcessImpl>();

		this.irm = new IRM(this.IPCName,this.rib, this.underlyingIPCs, this.dtpMsgQueue, this.cdapMsgQueue);

		this.RIBdaemon = new RIBDaemonImpl(this.rib, this.irm);
		this.rib.setRibDaemon(this.RIBdaemon);

		//this.routingDaemon = new RoutingDaemon(this.RIBdaemon, this.irm);

	}





	/**
	 * this method creates all TCP connection, basically it is to set up the wire.
	 */
	private void initConnections() {


		// before init connection sleep some time, such that everyone is up then TCP connection can be created
		// otherwise there will be no socket error
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		boolean stop = true;
		int i =1;

		while(stop)
		{
			String neihbour = this.config.getNeighbour(i);
			if(neihbour == null)//FIXME: need debug, not sure null is ok
			{
				stop = false;
			}else
			{
				this.tcpManager.allocateTCPFlow(neihbour.trim());
				i++;
			}
		}

		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName + " : init Connection done");

	}


	/**
	 * for Non-0 DIF
	 * @param DIFName
	 */
	public void initEnrollmentComponent(String DIFName)
	{
		this.DIFName = DIFName;
		this.rib.addAttribute("difName", this.DIFName);
		this.enrollmentCompoment = new Enrollment(this.RIBdaemon,this.irm);
		this.rib.setEnrollmentFlag(true);
		this.IPCState = state.DIF_JOINED;
	}



	/**
	 * enrollment phase in a DIF
	 */
	public void enrollment(String DIFName) {




		boolean iddOn = sendIDDRequestForDIF(DIFName);

		if(iddOn ==false)
		{
			RIBdaemon.localRIB.RIBlog.errorLog("IPC Process: IDD is  not on. Enrollment failed");
			return;
		}

		while(!this.IPCState.equals(state.DIF_JOINED))
		{


			byte[] msg = this.cdapMsgQueue.getReceive();

			handleCDAPMsg(msg);
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": Enrollment phase complete");


	}

	public void registerToIDD(String serviceType, String serviceName)
	{

		IDD.iddEntry.Builder IDDEntry = IDD.iddEntry.newBuilder()
		.addServiceURL(serviceType + "." + serviceName).setNmsURL(this.IPCName);

		//generate M_WRITE to IDD

		CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
		ByteString IDDentry = ByteString.copyFrom(IDDEntry.build().toByteArray());
		ObjValue.setByteval(IDDentry);
		CDAP.objVal_t objvalue = ObjValue.buildPartial();


		CDAP.CDAPMessage M_WRITE_msg = message.CDAPMessage.generateM_WRITE
		(       serviceType,
				this.IPCName,
				objvalue, 
				this.IDDName,//destAEInst
				this.IDDName,//destAEName
				this.IDDName,//destApInst
				this.IDDName,//destApName
				00001, //invokeID
				this.IPCName,
				this.IPCName,
				this.IPCName,
				this.IPCName
		);


		try {
			DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_WRITE_msg.toByteArray());
			this.irm.send(this.irm.getIDDHandle(), payload.toByteArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		RIBdaemon.localRIB.RIBlog.infoLog("IPC Prodcess: register(M_WRITE) to IDD sent with serviceType " + serviceType + " and serviceName " + serviceName);

	}


	/**
	 * send a query for IDD: cdapMessage.getSrcAEInst(),
					cdapMessage.getSrcAEName(),
					cdapMessage.getSrcApInst(),
					cdapMessage.getSrcApName(),
	 * 1) generate flow,
	 * 2) generate M_READ to IDD 
	 * 3) send M_READ on IDDFlow
	 * 4) update state 
	 * 5) log
	 */
	private boolean  sendIDDRequestForDIF(String DIFName) {

		//generate M_READ to IDD
		CDAP.objVal_t.Builder value = CDAP.objVal_t.newBuilder();
		value.setStrval(DIFName);
		value.setIntval(0);

		CDAP.CDAPMessage M_READ_msg = message.CDAPMessage.generateM_READ
		(      "IDDEntry",
				"bu",		
				value.buildPartial(), 
				"IDD",//destAEInst
				"IDD",//destAEName
				IDDName,//destApInst
				IDDName,//destApInst
				00001, //invokeID
				"RINA",//srcAEInst
				"RINA", //srcAEName
				IPCName,//srcApInst
				IPCName//AP
		);

		//for demultiplexing reason add a DTP header, so that on the IDD side it is easy to demulplexing


		//send M_READ on IDDFlow
		try {
			if(this.tcpManager != null)
			{
				TCPFlow IDDFlow =  this.tcpManager.allocateTCPFlow(this.IDDName);
				IDDFlow.sendCDAPMsg(M_READ_msg.toByteArray());
			}else
			{
				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_msg.toByteArray());
				this.irm.send(this.irm.getIDDHandle(),payload.toByteArray() );
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//change state
		this.IPCState = state.WAIT_FOR_IDD_LOOKUP;

		//log
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_READ to IDD sent");
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+" has state: " + this.IPCState);
		return true;
	}

	public void  queryIDDServiceAndJoinDIF(String serviceName) {

		//generate M_READ to IDD
		CDAP.objVal_t.Builder value = CDAP.objVal_t.newBuilder();
		value.setStrval(serviceName);
		value.setIntval(1);//this is to tell IDD which database to search, if it is an DIF Name then it is 0, 1 for service Name

		CDAP.CDAPMessage M_READ_msg = message.CDAPMessage.generateM_READ
		(      "regular",
				serviceName,		
				value.buildPartial(), 
				"IDD",//destAEInst
				"IDD",//destAEName
				IDDName,//destApInst
				IDDName,//destApInst
				00001, //invokeID
				"RINA",//srcAEInst
				"RINA", //srcAEName
				this.IPCName,//srcApInst
				this.IPCName//AP
		);

		//for demultiplexing reason add a DTP header, so that on the IDD side it is easy to demulplexing


		//send M_READ on IDDFlow
		try {
			if(this.tcpManager != null)
			{
				TCPFlow IDDFlow =  this.tcpManager.allocateTCPFlow(this.IDDName);
				IDDFlow.sendCDAPMsg(M_READ_msg.toByteArray());
			}else
			{
				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_msg.toByteArray());
				this.irm.send(this.irm.getIDDHandle(),payload.toByteArray() );
			}

			//log
			RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName + " + M_READ to IDD sent for service " + serviceName);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		byte[] msg = this.cdapMsgQueue.getReceive();


		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		if(cdapMessage.getSrcApName().equals( this.IDDName ) )
		{
			if(cdapMessage.getResult() == 0)
			{ //M_READ_R(OK)
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+": IDD M_READ_R(OK) received");

				rina.idd.IDD.iddEntry result = null;


				try {
					result = rina.idd.IDD.iddEntry.parseFrom(cdapMessage.getObjValue().getByteval());
				} catch (InvalidProtocolBufferException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	

				this.NMSName = result.getNmsURL();
				this.rib.addAttribute("nmsName", this.NMSName);



				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" NMS of the dst service is " + NMSName);


				CDAP.CDAPMessage M_CONNECT_toNMS_msg = message.CDAPMessage.generateM_CONNECT(
						CDAP.authTypes_t.AUTH_PASSWD, 
						message.CDAPMessage.generateAuthValue("BU", "BU"),
						"NMS",//destAEInst
						"NMS",//destAEName
						NMSName,//destApInst
						NMSName,//destApInst
						00001,  //invokeID, 
						"RINA",//srcAEInst
						"RINA",//srcAEName
						IPCName,//srcApInst
						IPCName//srcApName
				) ;

				//send M_CONNECT to NMS on NMS Flow
				try {
					if(this.tcpManager != null)
					{
						TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
						NMSFlow.sendCDAPMsg(M_CONNECT_toNMS_msg.toByteArray());
					}else
					{

						DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_CONNECT_toNMS_msg.toByteArray());
						this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_CONNECT to NMS sent");
				//change state
				this.IPCState = state.WAIT_TO_JOIN_DIF;
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process State: " + this.IPCState);

			}
			else{//M_READ_R(ERROR)
				this.IPCState = state.LISTENING;
				RIBdaemon.localRIB.RIBlog.errorLog("IPC Process State: " + this.IPCState);
				RIBdaemon.localRIB.RIBlog.errorLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": IDD M_READ_R(ERROR) received");
			}
		}else
		{
			RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" : queryIDDServiceAndJoinDIF failed");
			return;

		}


		while(!this.IPCState.equals(state.DIF_JOINED))
		{
			msg = this.cdapMsgQueue.getReceive();

			handleCDAPMsg(msg);
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": Enrollment phase complete");





	}




	/**
	 * get Neighbors Application To PortID mapping
	 */
	public void getNeighboursAPPToPortID() {

		for(int i = 0; i < this.rib.getMemberList().size(); i++)    
		{
			String neighbour = this.rib.getMemberList().get(i);

			if(!neighbour.equals(this.IPCName))
			{
				this.getUpperIPCsUsingIPCFromNMS(neighbour);
			}
		}

	}



	/**
	 * @param upperIPCName
	 * register upper application(IPC) that using this IPC to NMS
	 * here NMS works as a DNS in the DIF
	 * resolve uppering DIF IPC to low DIF IPC
	 */
	public void registerApplicationTONMS(String applicationName, int port) {

		CDAP.objVal_t.Builder value = CDAP.objVal_t.newBuilder();
		value.setIntval(port);

		CDAP.CDAPMessage M_CREATE_msg = message.CDAPMessage.generateM_CREATE(
				"registerApp",//object class
				applicationName,
				value.buildPartial(),
				this.NMSName,
				this.NMSName,
				this.IPCName,//
				this.IPCName//
		);

		try {

			if(this.tcpManager != null)
			{
				TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
				NMSFlow.sendCDAPMsg(M_CREATE_msg.toByteArray());
			}else
			{
				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_CREATE_msg.toByteArray());

				this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF"+ this.DIFName+": M_CREATE registerApp " + applicationName
				+" sent to NMS, and NMS name is " + this.NMSName );

	}

	/**
	 * 
	 * @param ipcName
	 */
	public synchronized void getUpperIPCsUsingIPCFromNMS(String ipcName) {

		//put the Routing Entry in the value
		CDAP.objVal_t.Builder value = CDAP.objVal_t.newBuilder();
		value.setStrval(ipcName);
		value.setSintval(0);
		//FIXME
		//value.setByteval(value);

		CDAP.CDAPMessage M_READ_msg = message.CDAPMessage.generateM_READ
		(      "applicationsUsingThisIPC",
				"applicationsUsingThisIPC",		
				value.buildPartial(), 
				this.NMSName,//dst
				this.NMSName,
				this.NMSName,
				this.NMSName,
				00001,//invoke id
				this.IPCName,//src
				this.IPCName,//
				this.IPCName,//
				this.IPCName//
		);

		//send M_READ 
		try {

			if(this.tcpManager != null)
			{
				TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
				NMSFlow.sendCDAPMsg(M_READ_msg.toByteArray());
			}else
			{
				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_msg.toByteArray());
				this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
			}


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//log
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_READ(applicationsUsingThisIPC) "+ ipcName+" to NMS sent");

	}



	public void run() {



		if(this.tcpManager != null) //DIF 0
		{

			this.routingDaemon.start();


			while(this.IPCState.equals(state.DIF_JOINED))
			{

				byte[] msg = this.cdapMsgQueue.getReceive();
				handleCDAPMsg(msg);

			}

		}else
		{

			if(this.NMSName != null) // do routing only 
			{
				this.routingDaemon.start();
			}

			while(true)
			{

				byte[] msg = this.cdapMsgQueue.getReceive();
				handleCDAPMsg(msg);

			}
		}



	}



	/**
	 * @param msg
	 */
	private synchronized void handleCDAPMsg(byte[] msg) {

		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}



		this.rib.RIBlog.infoLog("IPCProcess(" + this.IPCName +"): cdapMessage received, and opcode is "
				+ cdapMessage.getOpCode() + " and src is " + cdapMessage.getSrcApName()+ " and objclass is " + cdapMessage.getObjClass()
				+ ", objName is " + cdapMessage.getObjName());


		////////////////////////////////////////////////////////////////////
		//////enrollment flag is true, all messages go to enrollment queue for now/////FIXME: 


		if(this.rib.isEnrollmentFlag() == true)
		{
			this.enrollmentCompoment.getMsgQueue().addReceive(msg);
			return;	
		}

		////////////////////////////////////////////////////////////////////////////////////////////


		if(cdapMessage.getObjClass().equals("flow") || cdapMessage.getObjClass().equals("relayService")
				|| cdapMessage.getObjClass().equals("createNewIPCForApp"))//for IRM
		{
			this.irm.getIrmQueue().addReceive(msg);
			return;
		}else if(cdapMessage.getObjClass().equals("PubSub"))
		{
			this.ribDaemonQueue.addReceive(msg);
			return;
		}


		switch(cdapMessage.getOpCode()){

		case M_READ_R:

			handle_M_READ_R(cdapMessage);
			break;

		case M_START_R:

			handle_M_START_R(cdapMessage);
			break;

		case M_CREATE:

			handle_M_CREATE(cdapMessage);
			break;

		case M_WRITE:

			handle_M_WRITE(cdapMessage);
			break;

		case M_WRITE_R:

			handle_M_WRITE_R(cdapMessage);
			break;


		case M_START:

			handle_M_START(cdapMessage);
			break;

		case M_RELEASE_R:

			handle_M_RELEASE_R(cdapMessage);
			break;

		case M_READ:

			handle_M_READ(cdapMessage);
			break;

		case M_STOP:

			handle_M_STOP(cdapMessage);
			break;	



		default:

			RIBdaemon.localRIB.RIBlog.errorLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": Unexpected message received.");
			RIBdaemon.localRIB.RIBlog.errorLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": Opcode is " + cdapMessage.getOpCode());
			break;
		}
	}

	private int testClientByteCount = 0; 
	private void handle_M_READ(CDAPMessage cdapMessage) {

		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_READ received, and objClass is " + cdapMessage.getObjClass());

		String objClass = cdapMessage.getObjClass();


		if(objClass.contains("testClient"))
		{
			int msgSize  = cdapMessage.getObjValue().getByteval().toByteArray().length;
			this.testClientByteCount += msgSize;
			RIBdaemon.localRIB.RIBlog.infoLog("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%IPC Process:("+this.IPCName+")this.testClientByteCount " + testClientByteCount);
		}

	}


	private void handle_M_WRITE_R(CDAPMessage cdapMessage) {
		if(cdapMessage.getObjName().equals("enrollNewMemberForApp") && cdapMessage.getResult() ==0)
		{

			this.DIFFormation = true;
			RIBdaemon.localRIB.RIBlog.infoLog("IPC Process:handle_M_WRITE_R: enrollNewMemberForApp result received " );

		}

	}


	/**
	 * @param cdapMessage
	 */
	private void handle_M_START(CDAPMessage cdapMessage) {


	}


	private void handle_M_STOP(CDAPMessage cdapMessage) {

		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_STOP received");


		int result = -1;

		String objName = cdapMessage.getObjName();

		if(this.appCreateThisIPC == null)
		{
			System.out.println("this.appCreateThisIPC-----------------------------" + this.appCreateThisIPC );
			result = 0;
		}else
		{
			System.out.println("this.appCreateThisIPC-----------------------------" + this.appCreateThisIPC );
			result = 1;
			objName = this.appCreateThisIPC;
		}



		if(cdapMessage.getObjClass().equals("enrollment information"))
		{

			if(IPCState == state.WAIT_FOR_START && cdapMessage.getResult() == 0 ){ 
				CDAP.CDAPMessage M_STOP_R_toNMS_msg = message.CDAPMessage.generateM_STOP_R(
						result,
						cdapMessage.getObjClass(),
						objName,
						cdapMessage.getSrcAEInst(),
						cdapMessage.getSrcAEName(),
						cdapMessage.getSrcApInst(),
						cdapMessage.getSrcApName(),
						00001,
						cdapMessage.getDestAEInst(),
						cdapMessage.getDestAEName(),
						cdapMessage.getDestApInst(),
						cdapMessage.getDestApName()
				) ;



				try {

					if(this.tcpManager != null)
					{
						TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
						NMSFlow.sendCDAPMsg(M_STOP_R_toNMS_msg.toByteArray());
					}else
					{
						DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_STOP_R_toNMS_msg.toByteArray());
						this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
					}


				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_STOP_R(Enrollment) to NMS " + cdapMessage.getSrcApName()  +" sent");
				IPCState = state.DIF_JOINED;
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+" has state: " + this.IPCState);
			}
		}
	}

	/**
	 * @param cdapMessage
	 */
	private void handle_M_WRITE(CDAPMessage cdapMessage) {

		if(IPCState == state.DIF_JOINED && 
				cdapMessage.getSrcApName().equals(this.NMSName) && 
				cdapMessage.getObjClass().equals("DIFMemberList"))
		{


			byte[] byteArray =  cdapMessage.getObjValue().getByteval().toByteArray();
			LinkedList<String>  memberList = null;

			try {
				ByteArrayInputStream   bis   =   new   ByteArrayInputStream(byteArray); 
				ObjectInputStream ois   =   new  ObjectInputStream(bis);   
				memberList  = ( LinkedList<String> ) ois.readObject();

				this.rib.setMemberList(memberList);
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF"+this.DIFName+": Current memberList is " + this.rib.getMemberList());

				//create and send M_CREATE_R(OK)

				CDAP.CDAPMessage M_WRITE_R_toNMS_msg = message.CDAPMessage.generateM_WRITE_R
				(
						0,
						"DIFMemberList",
						cdapMessage.getSrcAEInst(),
						cdapMessage.getSrcAEName(),
						cdapMessage.getSrcApInst(),
						cdapMessage.getSrcApName(),
						00001,
						cdapMessage.getDestAEInst(),
						cdapMessage.getDestAEName(),
						cdapMessage.getDestApInst(),
						cdapMessage.getDestApName()
				) ;


				if(this.tcpManager != null)
				{
					TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
					NMSFlow.sendCDAPMsg(M_WRITE_R_toNMS_msg.toByteArray());
				}else
				{
					DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_WRITE_R_toNMS_msg.toByteArray());
					this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
				}



				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_WRITE_R(OK) to NMS " +cdapMessage.getSrcApName() +" sent");
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process: new member has joined DIF "+this.DIFName+" and the current member are " + memberList);

			}catch(Exception e)
			{

				CDAP.CDAPMessage M_WRITE_R_toNMS_msg = message.CDAPMessage.generateM_WRITE_R
				(
						1,
						"DIFMemberList",
						cdapMessage.getSrcAEInst(),
						cdapMessage.getSrcAEName(),
						cdapMessage.getSrcApInst(),
						cdapMessage.getSrcApName(),
						00001,
						cdapMessage.getDestAEInst(),
						cdapMessage.getDestAEName(),
						cdapMessage.getDestApInst(),
						cdapMessage.getDestApName()
				) ;

				try {
					if(this.tcpManager != null)
					{
						TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
						NMSFlow.sendCDAPMsg(M_WRITE_R_toNMS_msg.toByteArray());
					}else
					{
						DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_WRITE_R_toNMS_msg.toByteArray());
						this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
					}

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_WRITE_R(ERROR) to NMS " +cdapMessage.getSrcApName() +" sent");

			}

		}


	}


	/**
	 * @param cdapMessage
	 */
	private void handle_M_CREATE(CDAPMessage cdapMessage)  {

		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_CREATE received");

		//update local member list obtained from NMS	 	 
		if(this.IPCState == state.JOINING_DIF && 
				cdapMessage.getSrcApName().equals(NMSName) && 
				cdapMessage.getObjClass().equals("DIFMemberList"))
		{
			byte[] byteArray =  cdapMessage.getObjValue().getByteval().toByteArray();
			LinkedList<String>  memberList = null;

			try {
				ByteArrayInputStream   bis   =   new   ByteArrayInputStream(byteArray); 
				ObjectInputStream ois   =   new  ObjectInputStream(bis);   
				memberList  = ( LinkedList<String> ) ois.readObject();

				this.rib.setMemberList(memberList);
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": Current memberList is " + this.rib.getMemberList());

				//create and send M_CREATE_R(OK)

				CDAP.CDAPMessage M_CREATE_R_toNMS_msg = message.CDAPMessage.generateM_CREATE_R
				(
						0,
						cdapMessage.getSrcAEInst(),
						cdapMessage.getSrcAEName(),
						cdapMessage.getSrcApInst(),
						cdapMessage.getSrcApName(),
						00001,
						cdapMessage.getDestAEInst(),
						cdapMessage.getDestAEName(),
						cdapMessage.getDestApInst(),
						cdapMessage.getDestApName()
				) ;

				if(this.tcpManager != null)
				{
					TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
					NMSFlow.sendCDAPMsg(M_CREATE_R_toNMS_msg.toByteArray());
				}else
				{

					DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_CREATE_R_toNMS_msg.toByteArray());
					this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
				}


				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_CREATE_R(OK) to NMS " +cdapMessage.getSrcApName() +" sent");
				IPCState = state.WAIT_FOR_START;
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process State: " + this.IPCState);


			} catch (Exception e) { //send M_CREATE_M(ERROR)
				//e.printStackTrace();
				RIBdaemon.localRIB.RIBlog.errorLog(e.getMessage());
				CDAP.CDAPMessage M_CREATE_R_toNMS_msg = message.CDAPMessage.generateM_CREATE_R
				(
						1,//ERROR
						cdapMessage.getSrcAEInst(),
						cdapMessage.getSrcAEName(),
						cdapMessage.getSrcApInst(),
						cdapMessage.getSrcApName(),
						00001,
						cdapMessage.getDestAEInst(),
						cdapMessage.getDestAEName(),
						cdapMessage.getDestApInst(),
						cdapMessage.getDestApName()
				) ;

				try {
					if(this.tcpManager != null)
					{
						TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
						NMSFlow.sendCDAPMsg(M_CREATE_R_toNMS_msg.toByteArray());
					}else
					{
						this.irm.send(this.irm.getHandle(this.NMSName), M_CREATE_R_toNMS_msg.toByteArray());
					}

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_CREATE_R(ERROR) to NMS sent");
				IPCState = state.LISTENING;
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process State: " + this.IPCState);


			}   
		}else if(this.IPCState == state.DIF_JOINED && cdapMessage.getObjClass().equals("flow")) 
			// this is a flow create msg, and this corresponds with flow allocate
		{
			if(this.upperIPCsCDAPMsgQueue.containsKey(cdapMessage.getObjName()) )
			{
				int handleID= this.flowAllocator.addIncomingFlow(cdapMessage.getObjName(),
						cdapMessage.getObjValue().getStrval(), 
						cdapMessage.getObjValue().getIntval(), 
						cdapMessage.getSrcApName());

				// ask upper IPC if to accepted this flow
				//reply 
				// put dtp message (which contains a cdap message )in the upper IPC's msg queue

				CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();
				obj.setIntval(handleID);


				CDAP.CDAPMessage cdapMsg =  message.CDAPMessage.generateM_CREATE("flow",
						this.IPCName,
						obj.buildPartial(),
						cdapMessage.getObjName(), 
						cdapMessage.getObjName(), 
						cdapMessage.getObjValue().getStrval(),
						cdapMessage.getObjValue().getStrval());

				this.upperIPCsCDAPMsgQueue.get(cdapMessage.getObjName()).addReceive(cdapMsg.toByteArray());

			}
		}

	}



	/**
	 * @param cdapMessage
	 */
	private void handle_M_RELEASE_R(CDAPMessage cdapMessage) {
		// TODO Auto-generated method stub

	}



	/**
	 * handle M_READ_R message
	 * @param cdapMessage
	 */
	private void handle_M_READ_R(CDAPMessage cdapMessage) {


		if(IPCState.equals(state.WAIT_FOR_IDD_LOOKUP) && cdapMessage.getSrcApName().equals( this.IDDName ) )
		{
			if(cdapMessage.getResult() == 0)
			{ //M_READ_R(OK)
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": IDD M_READ_R(OK) received");

				rina.idd.IDD.iddEntry result = null;


				try {
					result = rina.idd.IDD.iddEntry.parseFrom(cdapMessage.getObjValue().getByteval());
				} catch (InvalidProtocolBufferException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	

				this.NMSName = result.getNmsURL();
				this.rib.addAttribute("nmsName", this.NMSName);



				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": NMS of the dst DIF is " + NMSName);

				//allocate flow NMS


				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": new flow allocated for NMS: " + NMSName);
				//generate M_START


				CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();

				enrollmentInformation_t msg = EnrollmentMessage.generate(-1, "STOPPED");

				obj.setByteval(msg.toByteString());

				CDAP.CDAPMessage M_START_Enroll = message.CDAPMessage.generateM_START
				(       "enrollment information",
						"/daf/management/enrollment", 
						obj.buildPartial(),
						CDAP.authTypes_t.AUTH_PASSWD, 
						message.CDAPMessage.generateAuthValue("BU", "BU"),
						"NMS",//destAEInst
						"NMS",//destAEName
						NMSName,//destApInst
						NMSName,//destApInst
						21,
						"RINA",//srcAEInst
						"RINA",//srcAEName
						IPCName,//srcApInst
						IPCName//srcApName
				);


				//send M_START to NMS on NMS Flow
				try {
					if(this.tcpManager != null)
					{
						TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
						NMSFlow.sendCDAPMsg(M_START_Enroll.toByteArray());
					}else
					{

						//						DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_START_Enroll.toByteArray());
						//						this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());

						DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_START_Enroll.toByteArray());
						int NMShandle = this.irm.allocateFlow(this.IPCName, this.NMSName);
						this.irm.send(NMShandle, payload.toByteArray());

					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_START to NMS sent");
				//change state
				this.IPCState = state.WAIT_TO_JOIN_DIF;
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process State: " + this.IPCState);

			}
			else{//M_READ_R(ERROR)
				this.IPCState = state.LISTENING;
				RIBdaemon.localRIB.RIBlog.errorLog("IPC Process State: " + this.IPCState);
				RIBdaemon.localRIB.RIBlog.errorLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": IDD M_READ_R(ERROR) received");
			}
		}
		else if(cdapMessage.getSrcApName().equals(this.NMSName) && this.IPCState.equals(state.DIF_JOINED))
		{

			System.out.println("I am in the IPCProcessImpl,  cdapMessage.getObjClass() is " + cdapMessage.getObjClass() + ",cdapMessage.getResult() is  " + cdapMessage.getResult() );
			if( cdapMessage.getObjClass().equals("resolveApp"))//M_READ_R (OK)
			{
				if(cdapMessage.getResult() == 0 )
				{
					this.flowAllocator.getAppToIPCMapping().put(cdapMessage.getObjName(), cdapMessage.getObjValue().getStrval());
					this.flowAllocator.getAppWellKnownPort().put(cdapMessage.getObjName(), cdapMessage.getObjValue().getIntval());
					RIBdaemon.localRIB.RIBlog.infoLog("IPC Process : M_READ_R(OK) received from  NMS. App   " 
							+ cdapMessage.getObjName() + " is resloved to IPC " + cdapMessage.getObjValue().getStrval() 
							+ " and well know port is  " + cdapMessage.getObjValue().getIntval());
				}else
				{
					this.flowAllocator.getAppToIPCMapping().put(cdapMessage.getObjName(), null);
				}


			}else if (cdapMessage.getResult() == 0 && cdapMessage.getObjClass().equals("queryDIFMemberList"))
			{
				byte[] byteArray =  cdapMessage.getObjValue().getByteval().toByteArray();
				LinkedList<String>  memberList = null;

				try {
					ByteArrayInputStream   bis   =   new   ByteArrayInputStream(byteArray); 
					ObjectInputStream ois   =   new  ObjectInputStream(bis);   
					memberList  = ( LinkedList<String> ) ois.readObject();

					this.rib.setMemberList(memberList);
					RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName
							+" of DIF "+this.DIFName+": Current memberList is " + this.rib.getMemberList());
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}

		}


	}//end of function


	//query dif member list from NMS
	public void queryDIFMemberList()
	{


		CDAP.CDAPMessage M_READ_msg = message.CDAPMessage.generateM_READ
		(      "queryDIFMemberList",
				this.DIFName,		
				"NMS",//destAEInst
				"NMS",//destAEName
				this.NMSName,//destApInst
				this.NMSName,//destApInst
				00001, //invokeID
				"RINA",//srcAEInst
				"RINA", //srcAEName
				this.IPCName,//srcApInst
				this.IPCName//AP
		);


		try {

			if(this.tcpManager != null)
			{
				TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
				NMSFlow.sendCDAPMsg(M_READ_msg.toByteArray());
			}else
			{

				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_msg.toByteArray());
				this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName +" query to NMS sent");

	}


	/**
	 * @param cdapMessage
	 */
	private void handle_M_START_R(CDAPMessage cdapMessage) {



		if(cdapMessage.getObjClass().equals("enrollment information"))
		{


			if((cdapMessage.getResult() == 0) && this.IPCState == state.WAIT_TO_JOIN_DIF ) // CONNECTION successful and ready to accept M_CREATE from NMS
			{
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": NMS M_START_R(OK) received");	 
				this.IPCState = state.JOINING_DIF;
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process State: " + this.IPCState);
			}
			else if((cdapMessage.getResult() == 2) && this.IPCState == state.WAIT_TO_JOIN_DIF)
			{ 
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process State: already joined the DIF before" );
				this.IPCState = state.DIF_JOINED;
				RIBdaemon.localRIB.RIBlog.infoLog("IPC Process State: " + this.IPCState);
			}
			else//M_START_R(ERROR)
			{
				this.IPCState = state.LISTENING;
				RIBdaemon.localRIB.RIBlog.errorLog("IPC Process State: " + this.IPCState);

				RIBdaemon.localRIB.RIBlog.errorLog("IPC Process "+ this.IPCName+" of DIF "+this.DIFName+":  M_START_R(ERROR) received");
			}

		}

	}

	public void addUnderlyingIPC( IPCProcessImpl ipc)
	{ 

		this.underlyingIPCs.put(ipc.getIPCName(), ipc);
		ipc.addUpperIPC(this.IPCName, this.dtpMsgQueue, this.cdapMsgQueue);

		this.irm.allocateFlow(this.IPCName, this.IDDName);
	}


	public synchronized void addUpperIPC(String upperIPCName, MessageQueue dtpMsgQueue, MessageQueue cdapMsgQueue )
	{
		this.upperIPCsDTPMsgQueue.put(upperIPCName, dtpMsgQueue);
		this.upperIPCsCDAPMsgQueue.put(upperIPCName, cdapMsgQueue);
		int wellKnownPort = this.flowAllocator.addNewApp(upperIPCName);

		//comment for now : load balancing
		//		new cpuMonitor(upperIPCName, this.rib.getAppStatus()).start();
		//		this.RIBdaemon.createPub(2, "appStatus");

		this.registerApplicationTONMS(upperIPCName, wellKnownPort);
	}



	 
	public int allocateFlow(String srcName, String dstName) {
		int portID =-1;
		Flow flow = this.flowAllocator.allocate(srcName, dstName);
		if(flow!= null)
		{
			portID = flow.getSrcPort();
		}
		return portID;

	}



	public int allocateFlowResponse(String srcIPCName, String dstIPCName, String dstUnderlyingIPC, int dstUnderlyingIPCPort, int result) {

		int portID =-1;

		if(result == 0)
		{
			Flow flow = this.flowAllocator.allocateAccept(srcIPCName, dstIPCName, dstUnderlyingIPC, dstUnderlyingIPCPort);
			if(flow!= null)
			{
				portID = flow.getSrcPort();
			}
		}else
		{
			this.flowAllocator.allocateReject(srcIPCName, dstIPCName, dstUnderlyingIPC, dstUnderlyingIPCPort);
		}


		return portID;
	}

	public int initDIF(String NMSName, String dstName, String appName) {

		System.out.println("IPCProcess: initDIF: NMSName is " + NMSName +  ", dstName is " +  dstName);
		this.NMSName = NMSName;
		this.rib.addAttribute("nmsName", this.NMSName);
		this.appCreateThisIPC = appName;
		this.rib.addAttribute("appCreateThisIPC", this.appCreateThisIPC);

		int errorCode = -1;

		CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();

		enrollmentInformation_t ei_msg = EnrollmentMessage.generate(-1, "STOPPED");

		obj.setByteval(ei_msg.toByteString());

		CDAP.CDAPMessage M_START_Enroll = message.CDAPMessage.generateM_START
		(       "enrollment information",
				"/daf/management/enrollment", 
				obj.buildPartial(),
				CDAP.authTypes_t.AUTH_PASSWD, 
				message.CDAPMessage.generateAuthValue("BU", "BU"),
				"NMS",//destAEInst
				"NMS",//destAEName
				this.NMSName,//destApInst
				this.NMSName,//destApInst
				21,
				"RINA",//srcAEInst
				"RINA",//srcAEName
				this.IPCName,//srcApInst
				this.IPCName//srcApName
		);


		//send M_START to NMS on NMS Flow
		try {
			if(this.tcpManager != null)
			{
				TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
				NMSFlow.sendCDAPMsg(M_START_Enroll.toByteArray());
			}else
			{

				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_START_Enroll.toByteArray());
				int NMShandle = this.irm.allocateFlow(this.IPCName, this.NMSName);
				this.irm.send(NMShandle, payload.toByteArray());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_START to NMS sent");
		//change state
		this.IPCState = state.WAIT_TO_JOIN_DIF;
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process State: " + this.IPCState);




		while(!this.IPCState.equals(state.DIF_JOINED))
		{
			byte[] msg = this.cdapMsgQueue.getReceive();

			handleCDAPMsg(msg);
		}

		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": Enrollment phase complete");


		CDAP.CDAPMessage M_WRITE_toNMS_msg = message.CDAPMessage.generateM_WRITE(
				"enrollNewMemberForApp",
				dstName,
				"NMS",//destAEInst
				"NMS",//destAEName
				NMSName,//destApInst
				NMSName,//destApInst
				00001,  //invokeID, 
				this.IPCName,//srcAEInst
				this.IPCName,//srcAEName
				this.IPCName,//srcApInst
				this.IPCName//srcApName
		) ;	

		try {
			if(this.tcpManager != null)
			{
				TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
				NMSFlow.sendCDAPMsg(M_WRITE_toNMS_msg.toByteArray());
			}else
			{
				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_WRITE_toNMS_msg.toByteArray());
				this.irm.send(this.irm.getHandle(this.NMSName), payload.toByteArray());
			}

			RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_WRITE(enrollNewMemberForApp) to NMS sent");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		while(!DIFFormation)
		{
			byte[] msg = this.cdapMsgQueue.getReceive();

			handleCDAPMsg(msg);
		}

		errorCode = 0;

		return errorCode;

	}


	public void joinDIF(String NMSName, String appName) {

		System.out.println("I am in the joinDIF xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		this.NMSName = NMSName;
		this.rib.addAttribute("nmsName", this.NMSName);
		this.appCreateThisIPC = appName;
		this.rib.addAttribute("appCreateThisIPC", this.appCreateThisIPC);


		CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();

		enrollmentInformation_t ei_msg = EnrollmentMessage.generate(-1, "STOPPED");

		obj.setByteval(ei_msg.toByteString());

		CDAP.CDAPMessage M_START_Enroll = message.CDAPMessage.generateM_START
		(       "enrollment information",
				"/daf/management/enrollment", 
				obj.buildPartial(),
				CDAP.authTypes_t.AUTH_PASSWD, 
				message.CDAPMessage.generateAuthValue("BU", "BU"),
				"NMS",//destAEInst
				"NMS",//destAEName
				this.NMSName,//destApInst
				this.NMSName,//destApInst
				21,
				"RINA",//srcAEInst
				"RINA",//srcAEName
				this.IPCName,//srcApInst
				this.IPCName//srcApName
		);


		//send M_START to NMS on NMS Flow
		try {
			if(this.tcpManager != null)
			{
				TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(this.NMSName);
				NMSFlow.sendCDAPMsg(M_START_Enroll.toByteArray());
			}else
			{

				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_START_Enroll.toByteArray());
				int NMShandle = this.irm.allocateFlow(this.IPCName, this.NMSName);
				this.irm.send(NMShandle, payload.toByteArray());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": M_START to NMS sent");
		//change state
		this.IPCState = state.WAIT_TO_JOIN_DIF;
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process State: " + this.IPCState);




		while(!this.IPCState.equals(state.DIF_JOINED))
		{
			byte[] msg = this.cdapMsgQueue.getReceive();

			handleCDAPMsg(msg);
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IPC Process "+this.IPCName+" of DIF "+this.DIFName+": Enrollment phase complete");


	}


	/////////////////////////////////////////////////////////////////////////////////////////////////
	///test client
	public void testClient(String serverName)
	{
		int handle  = this.irm.allocateFlow(this.IPCName, serverName);

		System.out.println("handle to " + serverName + " is " + handle + " xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

		CDAP.objVal_t.Builder  obj = CDAP.objVal_t.newBuilder();

		String dummyMsg = "this is a dummy msg";		
		obj.setByteval(ByteString.copyFrom (dummyMsg.getBytes()));

		int i =0;
		while(true)
		{

			CDAP.CDAPMessage M_READ = message.CDAPMessage.generateM_READ(
					"testClient" + i,
					this.IPCName,
					obj.buildPartial(),
					serverName,//destAEInst
					serverName,//destAEName
					serverName,//destApInst
					serverName,//destApInst
					00001,  //invokeID, 
					this.IPCName,//srcAEInst
					this.IPCName,//srcAEName
					this.IPCName,//srcApInst
					this.IPCName//srcApName
			) ;	

			DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ.toByteArray());

			try {
				this.irm.send(handle, payload.toByteArray());

				System.out.println( i + "  msg sent to " + serverName);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				this.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			i++;
		}
	}

	 
	public void deallocate(int portID) {

		this.flowAllocator.deallocate(portID);
	}



	 
	public void send(int portID, byte[] msg) throws Exception {

		this.flowAllocator.send(portID,msg);
	}




	 
	public byte[] receive(int portID) {

		return this.flowAllocator.receive(portID);

	}


	public String checkReachablity(String appName)
	{
		return this.flowAllocator.resolveApplicationByNMS(appName);
	}






	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////IPC RIB API impl/////////////////////////////////////////////////////////////
	/////////////////////////////////////    START  /////////////////////////////////////////////////////////////
	/////////////////////////////////////This used the RIB Daemon API to operate on RIB//////////////////////////
	 
	public int createSub(String appName,int frequency,  String attribute) {
		int subID  = -1;

		subID = this.RIBdaemon.createSub(frequency, attribute);

		if(subID !=-1)
		{
			LinkedHashMap<String,  LinkedList<Integer> > appSubList = this.rib.getAppSubList();

			if(!appSubList.containsKey(appName))
			{
				appSubList.put(appName, new LinkedList<Integer>());
			}

			appSubList.get(appName).add(subID);
		}

		return subID;
	}


	 
	public Object readSub(String appName, int subID) {

		LinkedHashMap<String,  LinkedList<Integer> > appSubList = this.rib.getAppSubList();

		if(appSubList.containsKey(appName) && appSubList.get(appName).contains(subID))
		{
			return this.RIBdaemon.readSub(subID);
		}else //no authorization
		{
			return null;
		}
	}



	 
	public void deleteSub(String appName, int subID) {
		// TODO Auto-generated method stub

	}


	 
	public int createPub(String appName, String attribute) {
		// TODO Auto-generated method stub
		return 0;
	}


	 
	public void deletePub(String appName, int pubID) {
		// TODO Auto-generated method stub

	}




	 
	public void writePub(String appName, int pubID, Object msg) {
		// TODO Auto-generated method stub

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////IPC RIB API impl/////////////////////////////////////////////////////////////
	/////////////////////////////////////    END  ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////	




	/**
	 * @return the dIFName
	 */
	public synchronized String getDIFName() {
		return DIFName;
	}


	/**
	 * @param dIFName the dIFName to set
	 */
	public synchronized void setDIFName(String dIFName) {
		DIFName = dIFName;
	}


	/**
	 * @return the iPCName
	 */
	public synchronized String getIPCName() {
		return IPCName;
	}


	/**
	 * @param iPCName the iPCName to set
	 */
	public synchronized void setIPCName(String iPCName) {
		IPCName = iPCName;
	}


	/**
	 * @return the rIBdaemon
	 */
	public synchronized RIBDaemonImpl getRIBdaemon() {
		return RIBdaemon;
	}


	/**
	 * @param rIBdaemon the rIBdaemon to set
	 */
	public synchronized void setRIBdaemon(RIBDaemonImpl rIBdaemon) {
		RIBdaemon = rIBdaemon;
	}

	/**
	 * @return the cdapMsgQueue
	 */
	public synchronized MessageQueue getCdapMsgQueue() {
		return cdapMsgQueue;
	}


	/**
	 * @return the flowAllocator
	 */
	public synchronized FlowAllocatorImpl getFlowAllocator() {
		return flowAllocator;
	}


	/**
	 * @param flowAllocator the flowAllocator to set
	 */
	public synchronized void setFlowAllocator(FlowAllocatorImpl flowAllocator) {
		this.flowAllocator = flowAllocator;
	}


	/**
	 * @param cdapMsgQueue the cdapMsgQueue to set
	 */
	public synchronized void setCdapMsgQueue(MessageQueue cdapMsgQueue) {
		this.cdapMsgQueue = cdapMsgQueue;
	}



	/**
	 * @return the iDDName
	 */
	public synchronized String getIDDName() {
		return IDDName;
	}


	/**
	 * @param iDDName the iDDName to set
	 */
	public synchronized void setIDDName(String iDDName) {
		IDDName = iDDName;
	}


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
	/**
	 * @return the routingDaemon
	 */
	public synchronized RoutingDaemon getRoutingDaemon() {
		return routingDaemon;
	}


	/**
	 * @param routingDaemon the routingDaemon to set
	 */
	public synchronized void setRoutingDaemon(RoutingDaemon routingDaemon) {
		this.routingDaemon = routingDaemon;
	}

	/**
	 * @return the rib
	 */
	public synchronized RIBImpl getRib() {
		return rib;
	}


	/**
	 * @param rib the rib to set
	 */
	public synchronized void setRib(RIBImpl rib) {
		this.rib = rib;
	}






}
