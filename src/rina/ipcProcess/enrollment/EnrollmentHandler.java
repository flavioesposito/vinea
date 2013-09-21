/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.ipcProcess.enrollment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import message.DTPMessage;


import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import rina.cdap.impl.googleprotobuf.CDAP;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.config.RINAConfig;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow.Flow;
import rina.flow0.TCPFlow;


import rina.idd.*;
import rina.ipcProcess.impl.IPCProcessImpl;
import rina.ipcProcess.impl.TCPFlowManager;
import rina.ipcProcess.util.MessageQueue;
import rina.irm.IRM;

import rina.rib.impl.RIBDaemonImpl;
import rina.rib.impl.RIBImpl;
import rina.rib.util.PubHandler;



/**
 *   Zero DIF: Enrollment Component 
 *   When a RINA process wants to join a certain DIF, it will first contact NMS(DIF manager).
 *   Enrollment Component is used to handle each joining request 
 *   
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 */

public class EnrollmentHandler extends Thread  {


	private RIBDaemonImpl RIBdaemon = null;

	private RIBImpl rib = null;

	private String DIFName = null;

	private TCPFlowManager tcpManager = null;

	private IRM irm = null;

	private String IPCName = null;

	private String IDDName = null;

	/**
	 * msg queue
	 */
	private MessageQueue msgQueue = null;


	private LinkedHashMap<String, LinkedList<ApplicationEntry>> appToIPCMapping = null;

	private LinkedList<String> appsReachable = null;


	/**
	 * nms active flag
	 */
	private boolean NMS_IS_ACTIVE = true;
	/**
	 * state
	 */
	private state enrollmentState = null;


	/**
	 * temp name that a new member that joined the dif
	 */
	private String newMember;


	private LinkedHashMap<String, Boolean> ipcStatus = null;
	private LinkedHashMap<String, Boolean> ipcProbeStatus = null;

	private int defaultFrequency = 7;



	/**
	 * NMS state
	 * @version 1.0
	 * @since 1.0
	 *
	 */
	public enum state {LISTENING, 
		WAIT_FOR_START,
		WAIT_FOR_DELETE,
		UPDATING_RIB,
		UPDATING_IDD,
		WAIT_FOR_DNS,
		WAIT_FOR_IDD,
		NULL
	};
	/**
	 * string got from config file
	 */
	private String authenPolicyFromConfig = null;
	/**
	 * CDAP auth type to use converting the string got from config file
	 */
	private CDAP.authTypes_t authenPolicy = null;



	private LinkedHashMap<String, String> askToEnrollMemberStatus = null;

	/**
	 * DIF 0
	 * @param RIBdaemon
	 * @param tcpManager
	 * @param msgQueue
	 * @param newMember
	 */
	public EnrollmentHandler(RIBDaemonImpl RIBdaemon, TCPFlowManager tcpManager,  MessageQueue msgQueue, String newMember) 
	{


		this.RIBdaemon = RIBdaemon;
		this.rib = this.RIBdaemon.getLocalRIB();
		this.tcpManager = tcpManager;

		this.DIFName = (String)this.rib.getAttribute("difName");
		this.IPCName = (String)this.rib.getAttribute("ipcName");
		this.IDDName = (String)this.rib.getAttribute("iddName");
		this.msgQueue = msgQueue;

		this.newMember = newMember;

		this.ipcStatus = (LinkedHashMap<String, Boolean>)this.rib.getAttribute("ipcStatus");
		this.ipcProbeStatus = (LinkedHashMap<String, Boolean>)this.rib.getAttribute("ipcProbeStatus");
		this.appToIPCMapping = ( LinkedHashMap<String, LinkedList<ApplicationEntry>> )this.rib.getAttribute("appToIPCMapping");
		this.appsReachable = (LinkedList<String>)this.rib.getAttribute("appsReachable");
		this.askToEnrollMemberStatus = (LinkedHashMap<String, String>)this.RIBdaemon.localRIB.getAttribute("askToEnrollMemberStatus");  


		this.enrollmentState = state.LISTENING;

		this.loadAuthenPolicy();
	}


	/**
	 * Non-O
	 * @param RIBdaemon
	 * @param irm
	 * @param msgQueue
	 * @param newMember
	 */
	public EnrollmentHandler(RIBDaemonImpl RIBdaemon, IRM irm,  MessageQueue msgQueue, String newMember) 
	{


		this.RIBdaemon = RIBdaemon;
		this.rib = this.RIBdaemon.getLocalRIB();
		this.irm = irm;

		this.DIFName = (String)this.rib.getAttribute("difName");
		this.IPCName = (String)this.rib.getAttribute("ipcName");
		this.IDDName = (String)this.rib.getAttribute("iddName");
		this.msgQueue = msgQueue;

		this.newMember = newMember;

		this.ipcStatus = (LinkedHashMap<String, Boolean>)this.rib.getAttribute("ipcStatus");
		this.ipcProbeStatus = (LinkedHashMap<String, Boolean>)this.rib.getAttribute("ipcProbeStatus");
		this.appToIPCMapping = ( LinkedHashMap<String, LinkedList<ApplicationEntry>> )this.rib.getAttribute("appToIPCMapping");
		this.appsReachable = (LinkedList<String>)this.rib.getAttribute("appsReachable");
		this.askToEnrollMemberStatus = (LinkedHashMap<String, String>)this.RIBdaemon.localRIB.getAttribute("askToEnrollMemberStatus");  


		this.enrollmentState = state.LISTENING;

		this.authenPolicy = CDAP.authTypes_t.AUTH_PASSWD;
	}

	/**
	 * load the authentication policy from the configuration file
	 */
	private void loadAuthenPolicy() {


		this.authenPolicyFromConfig = (String) this.rib.getAttribute("authenPolicy");

		if(this.authenPolicyFromConfig.equals("AUTH_NONE"))
		{
			this.authenPolicy = CDAP.authTypes_t.AUTH_NONE;
		}
		else if (this.authenPolicyFromConfig.equals("AUTH_PASSWD"))
		{
			this.authenPolicy = CDAP.authTypes_t.AUTH_PASSWD;
		}
		else if (this.authenPolicyFromConfig.equals("AUTH_SSHDSA"))
		{
			this.authenPolicy = CDAP.authTypes_t.AUTH_SSHDSA;
		}
		else if (this.authenPolicyFromConfig.equals("AUTH_SSHRSA"))
		{
			this.authenPolicy = CDAP.authTypes_t.AUTH_SSHRSA;
		}

	}

	/**
	 * NMS connection thread listening 
	 */
	public void run()
	{ 

		while(NMS_IS_ACTIVE)
		{
			byte[] msg = this.msgQueue.getReceive();
			handleReceiveMessage(msg);
		}

	}

	/**
	 * handle message received
	 * @param msg
	 */
	public void handleReceiveMessage(byte[] msg) {

		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: message received opcode is  " + cdapMessage.getOpCode());


		switch(cdapMessage.getOpCode()){

		case M_START:
			handle_M_START(cdapMessage);
			break;

		case M_READ:
			handle_M_READ(cdapMessage);
			break;		

		case M_CREATE_R:

			handle_M_CREATE_R(cdapMessage);	
			break;


		case M_CREATE:

			handle_M_CREATE(cdapMessage);	
			break;

		case M_START_R:
			handle_M_START_R(cdapMessage);
			break;


		case M_WRITE:
			handle_M_WRITE(cdapMessage);
			break;	

		case M_WRITE_R:
			handle_M_WRITE_R(cdapMessage);
			break;	

		case M_STOP:
			handle_M_STOP(cdapMessage);
			break;

		case M_STOP_R:
			handle_M_STOP_R(cdapMessage);
			break;


		default:
			System.out.print("Something is wrong!!! opcpde not handled");
			break;
		}

	}

	private void handle_M_CREATE(CDAPMessage cdapMessage) {
		if(cdapMessage.getObjClass().equals("registerApp"))
		{
			String appName = cdapMessage.getObjName();

			if(!this.appToIPCMapping.containsKey(appName))
			{
				LinkedList<ApplicationEntry> ipcs = new LinkedList<ApplicationEntry>();
				this.appToIPCMapping.put(appName, ipcs);
				this.appsReachable.add(appName);
			}

			int wellKnownPort = cdapMessage.getObjValue().getIntval();

			this.appToIPCMapping.get(appName).add(new ApplicationEntry(cdapMessage.getSrcApName(),wellKnownPort));

			RIBdaemon.localRIB.RIBlog.infoLog("NMSHandler: M_CREATE (registerApp) received. app is " 
					+ appName + ", underlying IPC is " + cdapMessage.getSrcApName() + " well known port is "  + wellKnownPort);

			
			if(rib.getAttribute("Pubfrequency")!=null) {
				this.RIBdaemon.createSub(Integer.parseInt(rib.getAttribute("Pubfrequency").toString()), "appStatus", cdapMessage.getSrcApName());
			}else {
			this.RIBdaemon.createSub(this.defaultFrequency, "appStatus", cdapMessage.getSrcApName());
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////publish to IDD, app status
			//actually this is only 

			this.registerToIDD("regular", appName);


			/////////////////////////////////////////////////////////////////////////////////////////////////////////



		}else if(cdapMessage.getObjClass().equals("flow"))
		{
			this.irm.getIrmQueue().addReceive(cdapMessage.toByteArray());
		}


	}


	public void registerToIDD(String serviceType,String serviceName)
	{

		IDD.iddEntry.Builder IDDEntry = IDD.iddEntry.newBuilder()
		.addServiceURL(serviceType + "." + serviceName).setNmsURL(this.IPCName);

		//generate M_WRITE to IDD

		CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
		ByteString IDDentry = ByteString.copyFrom(IDDEntry.build().toByteArray());
		ObjValue.setByteval(IDDentry);
		CDAP.objVal_t objvalue = ObjValue.buildPartial();

		String IDDName = this.rib.getAttribute("iddName").toString();

		CDAP.CDAPMessage M_WRITE_msg = message.CDAPMessage.generateM_WRITE
		(       serviceName,
				this.newMember,
				objvalue, 
				IDDName,//destAEInst
				IDDName,//destAEName
				IDDName,//destApInst
				IDDName,//destApName
				00001, //invokeID
				this.IPCName,
				this.IPCName,
				this.IPCName,
				this.IPCName
		);


		try {

			if(this.tcpManager != null)
			{
				this.tcpManager.getTCPFlow(IDDName).sendCDAPMsg(M_WRITE_msg.toByteArray());

			}else
			{
				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_WRITE_msg.toByteArray());
				this.irm.send(this.irm.getIDDHandle(), payload.toByteArray());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		RIBdaemon.localRIB.RIBlog.infoLog("IPC Prodcess: register(M_WRITE) to IDD sent with  serviceName " + serviceName);

	}


	/**
	 * handle_M_READ
	 * @param cdapMessage
	 */
	private void handle_M_READ(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("resloveApp"))
		{


			String appName = cdapMessage.getObjName();

			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component : M_READ resloveApp received, and it is to reslove "  +  appName); 

			int result;
			String underlyingIPCName = null;
			int wellKnownPort = -1;
			CDAP.objVal_t.Builder  obj;
			if(!this.appToIPCMapping.containsKey(appName))
			{ 
				RIBdaemon.localRIB.RIBlog.errorLog("Enrollment Component: No entry found for resloveApp "  ); 
				obj = CDAP.objVal_t.newBuilder();
				result = -1;
			}else
			{

				result = 0;

				LinkedList<ApplicationEntry> ipcs = this.appToIPCMapping.get(appName);
				ApplicationEntry appEntry = null;

				if(ipcs.size() >1) // more than one ipc provides the service, then check the cpu usage
				{

					RIBdaemon.localRIB.RIBlog.errorLog("Enrollment Component: more than on IPC have this service, so check cpu usage"  ); 

					double [] cpuUsage = new double[ipcs.size()];


					for(int i=0; i<ipcs.size();i++)
					{

						String ipcName = ipcs.get(i).ipcName;

						int subID = this.RIBdaemon.getSubID(this.defaultFrequency, "appStatus", ipcName);

						LinkedHashMap<String, Double> appStatus = (LinkedHashMap<String, Double> )this.RIBdaemon.readSub(subID);

						if(appStatus.containsKey(appName))
						{
							cpuUsage[i] = appStatus.get(appName);
						}else
						{
							cpuUsage[i] = 999999;
						}

					}


					double smallest;
					int index ;
					smallest = cpuUsage[0];
					index = 0;
					for(int i= 1; i< cpuUsage.length;i++)
					{
						if(smallest > cpuUsage[i])
						{
							smallest = cpuUsage[i];
							index = i;
						}
					}

					appEntry = this.appToIPCMapping.get(appName).get(index);//return the smallest cpu Usage

				}else // only one provides the service, then it is it
				{
					appEntry = this.appToIPCMapping.get(appName).get(0);
				}

				underlyingIPCName = appEntry.ipcName;
				wellKnownPort = appEntry.wellKnownPort;

				obj = CDAP.objVal_t.newBuilder();
				obj.setStrval(underlyingIPCName);
				obj.setIntval(wellKnownPort);

			}



			CDAP.CDAPMessage M_READ_R_msg = message.CDAPMessage.generateM_READ_R(
					result,
					"resolveApp",//object class
					appName,  //object name 
					obj.buildPartial(),//object value
					cdapMessage.getSrcAEInst(),
					cdapMessage.getSrcAEName(),
					cdapMessage.getSrcApInst(),
					cdapMessage.getSrcApName(),
					00001,//invoke id
					cdapMessage.getDestAEInst(),
					cdapMessage.getDestAEName(),
					cdapMessage.getDestApInst(),
					cdapMessage.getDestApName()
			);

			byte[] M_READ_R  = M_READ_R_msg.toByteArray();	


			//			IPCProcess.getTcpManager().send(cdapMessage.getSrcApName(),M_READ_R);

			try {

				if(this.tcpManager != null)
				{
					this.tcpManager.getTCPFlow(cdapMessage.getSrcApName()).sendCDAPMsg(M_READ_R);

				}else
				{
					DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_R);

					this.irm.send(this.irm.getHandle(cdapMessage.getSrcApName()),payload.toByteArray());
				}

				RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component:  M_READ_R(resolveApp) replied to  " + cdapMessage.getSrcApName() + " reply is: "+ underlyingIPCName ); 


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
		else if(cdapMessage.getObjClass().equals("queryDIFMemberList"))
		{

			ByteArrayOutputStream   bos   =   new   ByteArrayOutputStream(); 
			ObjectOutputStream oos;
			try {
				oos = new   ObjectOutputStream(bos);
				oos.writeObject(RIBdaemon.localRIB.getMemberList());   
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}   
			byte[]   bs   =   bos.toByteArray(); 

			CDAP.objVal_t.Builder  member = CDAP.objVal_t.newBuilder();
			member.setByteval(ByteString.copyFrom(bs));


			CDAP.CDAPMessage M_READ_R_msg = message.CDAPMessage.generateM_READ_R(
					0,
					"queryDIFMemberList",//object class
					cdapMessage.getObjName(),  //object name 
					member.buildPartial(),//member list
					cdapMessage.getSrcAEInst(),
					cdapMessage.getSrcAEName(),
					cdapMessage.getSrcApInst(),
					cdapMessage.getSrcApName(),
					cdapMessage.getInvokeID(),
					cdapMessage.getDestAEInst(), 
					cdapMessage.getDestAEName(),
					cdapMessage.getDestApInst(),
					cdapMessage.getDestApName()
			);
			byte[] M_READ_R  = M_READ_R_msg.toByteArray();		

			//			 IPCProcess.getTcpManager().send(cdapMessage.getSrcApName(),M_READ_R);
			try {


				if(this.tcpManager != null)
				{
					this.tcpManager.getTCPFlow(cdapMessage.getSrcApName()).sendCDAPMsg(M_READ_R);

				}else
				{
					DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_R);

					this.irm.send(this.irm.getHandle(cdapMessage.getSrcApName()),payload.toByteArray());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component:  M_READ_R(queryDIFMemberList) replied to  " + cdapMessage.getSrcApName()  ); 


		}else if (cdapMessage.getObjClass().equals("PubSub"))
		{
			String subscriber = cdapMessage.getSrcApName();

			String subName  = cdapMessage.getObjValue().getStrval();
			int frequency = cdapMessage.getObjValue().getIntval();

			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: subscribe request received from  " + cdapMessage.getSrcApName() 
					+ " and it is for " + subName + " with frequency " + frequency );

			String subIdentifier = subName + "#" +frequency;

			if(this.rib.getPubnameToID().containsKey(subIdentifier))
			{
				PubHandler pubHandler = this.rib.getPubIDToHandler().get(this.rib.getPubnameToID().get(subIdentifier));
				pubHandler.addSubscriber(subscriber);			
				RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: new subscriber(" +  subscriber +") to  " + subIdentifier + "added."  ); 
			}else
			{
				RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: subscriber(" +  subscriber +") to  " + subIdentifier + "failed, the sub event does not exist"  ); 

			}

		}

	}


	private void handle_M_WRITE(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("probeNMS"))
		{
			if(this.ipcStatus.containsKey(cdapMessage.getObjName()))
			{
				RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: probeNMS message received from " + cdapMessage.getObjName());
				this.ipcProbeStatus.put(cdapMessage.getObjName(), Boolean.TRUE);
			}
		}else if(cdapMessage.getObjClass().equals("enrollNewMemberForApp"))
		{
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: enrollNewMemberForApp message received from " + cdapMessage.getSrcApName());
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: start to enroll new member into the DIF " + cdapMessage.getObjName());

			String newMember = cdapMessage.getObjName();

			this.askToEnrollMemberStatus.put(newMember, cdapMessage.getSrcApName());

			this.enrollNewMemberForApp(newMember);
		}

	}


	private void enrollNewMemberForApp(String appName) {

		CDAP.CDAPMessage M_CREATE_msg = message.CDAPMessage.generateM_CREATE(
				"createNewIPCForApp",//object class
				this.IPCName,  //object name 
				appName,
				appName,
				appName,
				appName,
				000001,
				this.IPCName,
				this.IPCName,
				this.IPCName,
				this.IPCName
		);

		try {

			DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_CREATE_msg.toByteArray());
			int handle = this.irm.allocateFlow(this.IPCName, appName);
			this.irm.send(handle,payload.toByteArray());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		RIBdaemon.localRIB.RIBlog.infoLog("Enrollment ComponentHandler: M_CREATE(createNewIPCForApp) sent to " + appName);


	}



	/**
	 * @param cdapMessage
	 */
	private void handle_M_WRITE_R(CDAPMessage cdapMessage) {

		System.out.println("cdapMessage.getSrcApName() is " + cdapMessage.getSrcApName());
		System.out.println("cdapMessage.getResult() is " + cdapMessage.getResult());
		System.out.println("this.IDDName is " + this.IDDName);
		System.out.println("this.enrollmentState is " + this.enrollmentState);


		if( cdapMessage.getResult() == 0  && 
				this.enrollmentState.equals(state.UPDATING_IDD) && 
				cdapMessage.getSrcApName().equals(this.IDDName) )
		{
			//send M_STOP to tell  the new member, IPC enrollment phase is done
			CDAP.CDAPMessage M_STOP_msg = message.CDAPMessage.generateM_STOP(
					0, 
					"enrollment information",//Obj class
					"/daf/management/enrollment",//Obj name
					"IPCProcess",
					"IPCProcess",
					cdapMessage.getObjName(),
					cdapMessage.getObjName(),
					cdapMessage.getInvokeID(),

					cdapMessage.getDestAEInst(),
					cdapMessage.getDestAEName(),
					cdapMessage.getDestApInst(),
					cdapMessage.getDestApName()


			);

			byte[] M_STOP  = M_STOP_msg.toByteArray();



			try {


				if(this.tcpManager != null)
				{
					this.tcpManager.getTCPFlow(cdapMessage.getObjName()).sendCDAPMsg(M_STOP);

				}else
				{
					DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_STOP);

					this.irm.send(this.irm.getHandle(cdapMessage.getObjName()),payload.toByteArray());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: M_STOP(Enrollment) sent to " + cdapMessage.getObjName());

			this.enrollmentState = state.WAIT_FOR_START;
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component State: " + this.enrollmentState);		

		}
		else if(this.enrollmentState.equals(state.UPDATING_IDD) && 
				cdapMessage.getSrcApName().equals(this.IDDName) 
				&& cdapMessage.getResult() != 0)
		{
			//update idd fails
			RIBdaemon.localRIB.RIBlog.warnLog("Enrollment Component: update IDD failed");
		}else
		{
			RIBdaemon.localRIB.RIBlog.warnLog("Enrollment Component: update IDD done");
		}


	}

	/**
	 * handle M_START
	 * @param cdapMessage
	 */
	private void handle_M_START(CDAPMessage cdapMessage) {

		if(cdapMessage.getObjClass().equals("enrollment information"))
		{

			//if(this.RIBdaemon.localRIB.getMemberList().contains(cdapMessage.getSrcApName()))
			if(this.ipcStatus.containsKey(cdapMessage.getSrcApName()))
			{			

				CDAP.CDAPMessage M_START_R_msg = message.CDAPMessage.generateM_START_R(
						2,//DIFJoined		
						cdapMessage.getSrcAEInst(),
						cdapMessage.getSrcAEName(),
						cdapMessage.getSrcApInst(),
						cdapMessage.getSrcApName(),
						cdapMessage.getInvokeID(),
						cdapMessage.getDestAEInst(), 
						cdapMessage.getDestAEName(),
						cdapMessage.getDestApInst(),
						cdapMessage.getDestApName()
				);




				byte[] M_START_R = M_START_R_msg.toByteArray();


				//			IPCProcess.getTcpManager().send(cdapMessage.getSrcApName(), M_START_R);

				try {
					if(this.tcpManager != null)
					{
						this.tcpManager.getTCPFlow(cdapMessage.getSrcApName()).sendCDAPMsg(M_START_R);

					}else
					{
						DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_START_R);

						this.irm.send(this.irm.getHandle(cdapMessage.getSrcApName()),payload.toByteArray());
					}


				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: " + cdapMessage.getSrcApName() + " is already enrolled");	

				return;
			}

			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: authenPolicy is " + this.authenPolicy);	

			switch(this.authenPolicy){

			case AUTH_NONE:
				break;
			case AUTH_SSHRSA:
				//TODO:
				break;
			case AUTH_SSHDSA:
				//TODO:
				break;		

			default:


				String username = cdapMessage.getAuthValue().getAuthName();
				String pwd = cdapMessage.getAuthValue().getAuthPassword();

				//generate M_START_R
				int authenticationResult;

				if(this.tcpManager !=null)
				{
					authenticationResult = authenticationIsCorrect(username,pwd);
				}else //skip authentication for non-0 DIF FIXME
				{
					authenticationResult = 0;
				}

				RIBdaemon.localRIB.RIBlog.infoLog("the result of authentication is " + authenticationResult );
				RIBdaemon.localRIB.RIBlog.infoLog("srcAPName" + cdapMessage.getSrcApName());

				CDAP.CDAPMessage M_START_R_msg = message.CDAPMessage.generateM_START_R(
						authenticationResult,		
						cdapMessage.getObjClass(),
						cdapMessage.getObjName(),
						cdapMessage.getSrcAEInst(),
						cdapMessage.getSrcAEName(),
						cdapMessage.getSrcApInst(),
						cdapMessage.getSrcApName(),
						cdapMessage.getInvokeID(),
						cdapMessage.getDestAEInst(), 
						cdapMessage.getDestAEName(),
						cdapMessage.getDestApInst(),
						cdapMessage.getDestApName()
				);


				byte[] M_START_R = M_START_R_msg.toByteArray();


				//			IPCProcess.getTcpManager().send(cdapMessage.getSrcApName(), M_START_R);

				try {

					if(this.tcpManager != null)
					{
						this.tcpManager.getTCPFlow(cdapMessage.getSrcApName()).sendCDAPMsg(M_START_R);

					}else
					{
						DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_START_R);

						this.irm.send(this.irm.getHandle(cdapMessage.getSrcApName()),payload.toByteArray());
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: M_START_R sent to " + cdapMessage.getSrcApName());


				//			try {
				//				Thread.sleep(500);
				//			} catch (InterruptedException e) {
				//				// TODO Auto-generated catch block
				//				e.printStackTrace();
				//			};


				if(authenticationResult ==  0) //means accept new member, then send M_CREATE to update RIB info
				{
					//add new joining member to the member list

					enrollmentState =  state.UPDATING_RIB;
					RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component State: " + this.enrollmentState);		

					RIBdaemon.localRIB.writeMemberListElement(cdapMessage.getSrcApName());

					this.ipcStatus.put(cdapMessage.getSrcApName(), Boolean.TRUE);
					this.ipcProbeStatus.put(cdapMessage.getSrcApName(), Boolean.FALSE);


					//send M_CREATE(which contains the member list of the DIF) to update the new member's RIB
					ByteArrayOutputStream   bos   =   new   ByteArrayOutputStream(); 
					ObjectOutputStream oos;
					try {
						oos = new   ObjectOutputStream(bos);
						oos.writeObject(RIBdaemon.localRIB.getMemberList());   
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}   
					byte[]   bs   =   bos.toByteArray(); 

					CDAP.objVal_t.Builder  member = CDAP.objVal_t.newBuilder();
					member.setByteval(ByteString.copyFrom(bs));


					CDAP.CDAPMessage M_CREATE_msg = message.CDAPMessage.generateM_CREATE(
							"DIFMemberList",//object class
							"DIFMemberList",  //object name 
							member.buildPartial(),//member list
							cdapMessage.getSrcAEInst(),
							cdapMessage.getSrcAEName(),
							cdapMessage.getSrcApInst(),
							cdapMessage.getSrcApName(),
							cdapMessage.getInvokeID(),
							cdapMessage.getDestAEInst(), 
							cdapMessage.getDestAEName(),
							cdapMessage.getDestApInst(),
							cdapMessage.getDestApName()
					);
					byte[] M_CREATE  = M_CREATE_msg.toByteArray();				

					//				IPCProcess.getTcpManager().send(cdapMessage.getSrcApName(),M_CREATE);


					try {

						if(this.tcpManager != null)
						{
							this.tcpManager.getTCPFlow(cdapMessage.getSrcApName()).sendCDAPMsg(M_CREATE);

						}else
						{
							DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_CREATE);

							this.irm.send(this.irm.getHandle(cdapMessage.getSrcApName()),payload.toByteArray());
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: M_CREATE sent to " + cdapMessage.getSrcApName());

				}// end if
			}
		}

	}

	/**
	 * authenticationIsCorrect
	 * @param username
	 * @param pwd
	 * @return
	 */
	private int authenticationIsCorrect(String username, String pwd) {

		RIBdaemon.localRIB.RIBlog.infoLog("user name and password is " + username + " " + pwd );

		String user = (String)this.rib.getAttribute("userName");

		String password =  (String)this.rib.getAttribute("passWord");


		RIBdaemon.localRIB.RIBlog.infoLog("user name and password is " + user + " " + password );

		if(username.equals(user) && pwd.equals(password))
		{ return 0;}
		else
		{return 1;}


	}


	/**
	 * handle M_START_R
	 * @param cdapMessage
	 */
	private void handle_M_START_R(CDAPMessage cdapMessage) {


	}

	private void handle_M_STOP_R(CDAPMessage cdapMessage) {


		RIBdaemon.localRIB.RIBlog.infoLog("NMS: M_STOP_R received");

System.out.println("cdapMessage.getObjClass()--------------------------------------" + cdapMessage.getObjClass());
System.out.println("cdapMessage.getResult()--------------------------------------" + cdapMessage.getResult());

		if(cdapMessage.getObjClass().equals("enrollment information") && ( cdapMessage.getResult() == 0 || cdapMessage.getResult() == 1)) //OK // 1 means the ipc joins the DIF because of for other app needs a DIF 
		{

			if(cdapMessage.getResult() ==1)
			{
				String appOnIPC= cdapMessage.getObjName();

				if(this.askToEnrollMemberStatus.containsKey(appOnIPC))
				{

					String client = this.askToEnrollMemberStatus.get(appOnIPC);
					
					
					this.askToEnrollMemberStatus.remove(appOnIPC);

					this.RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: tell the client IPC "  + client + ", that the DIF is formed" );

					CDAP.CDAPMessage M_WRITE_R = message.CDAPMessage.generateM_WRITE_R
					(
							0,
							"enrollNewMemberForApp",
							client,
							client,
							client,
							client,
							00001,
							this.IPCName,
							this.IPCName,
							this.IPCName,
							this.IPCName
					) ;


					try {
						if(this.tcpManager != null)
						{
							this.tcpManager.getTCPFlow(client).sendCDAPMsg(M_WRITE_R.toByteArray());
						}else
						{
							DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_WRITE_R.toByteArray());
							this.irm.send(this.irm.getHandle(client),payload.toByteArray());
						}

						RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: M_WRITE_R sent to(enrollNewMemberForApp) " + client);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			this.RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: New member " + cdapMessage.getSrcApName() + " is enrolled into the DIF");



			enrollmentState = state.LISTENING;
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component State: " + this.enrollmentState);		

			LinkedList<String> memberlist = this.rib.getMemberList();

			System.out.println("ddddddddddddddddddd member list is " +  memberlist);


			ByteArrayOutputStream   bos   =   new   ByteArrayOutputStream(); 
			ObjectOutputStream oos;
			try {
				oos = new   ObjectOutputStream(bos);
				oos.writeObject(RIBdaemon.localRIB.getMemberList());   
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}   
			byte[]   bs   =   bos.toByteArray(); 

			CDAP.objVal_t.Builder  member = CDAP.objVal_t.newBuilder();
			member.setByteval(ByteString.copyFrom(bs));


			for(int i = 0; i < memberlist.size(); i++)
			{	
				String ApName = memberlist.get(i);
				if(!ApName.equals(this.newMember) && !ApName.equals(this.IPCName))
				{
					CDAP.CDAPMessage M_WRITE_msg = message.CDAPMessage.generateM_WRITE(
							"DIFMemberList",//object class
							"DIFMemberList",  //object name 
							member.buildPartial(),//object value
							"RINA",
							"RINA",
							ApName,
							ApName,
							00001,//invoke id
							"NMS",//
							"NMS",//
							this.IPCName,//
							this.IPCName//
					);

					byte[] M_WRITE  = M_WRITE_msg.toByteArray();				
					//					IPCProcess.getTcpManager().send(ApName, M_WRITE);

					try {
						if(this.tcpManager != null)
						{
							this.tcpManager.getTCPFlow(ApName).sendCDAPMsg(M_WRITE);
						}else
						{
							DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_WRITE);

							this.irm.send(this.irm.getHandle(ApName),payload.toByteArray());
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}



					RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: M_WRITE sent to " + ApName);
				}

			}

		}
		else
		{
			RIBdaemon.localRIB.RIBlog.errorLog("Enrollment Component: M_STOP_R(ERROR) received");
			this.enrollmentState = state.LISTENING;
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component State: " + this.enrollmentState);	


		}



	}
	/**
	 * handle M_CREATE_R
	 * @param cdapMessage
	 */
	private void handle_M_CREATE_R(CDAPMessage cdapMessage) {


		if(cdapMessage.getResult() == 0 && this.enrollmentState ==  state.UPDATING_RIB)//M_CREATE_R(OK)
		{
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: M_CREATE_R(OK) received");


			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component cdapMessage.getSrcApName()" + cdapMessage.getSrcApName());



			//////////////update IDD

			IDD.iddEntry.Builder IDDEntry = IDD.iddEntry.newBuilder()
			.addSupportingDIF("BUDIF")
			.addServiceURL(cdapMessage.getSrcApName())
			//			.setDIFName(this.IPCProcess.getDIFName())
			.setDIFName(this.DIFName)
			.setNmsURL(this.IPCName);//address of nms
			//  .setSupportingDIF(0, "InternetDIF")
			//  .setServiceURL(0, ApplicationName);



			CDAP.objVal_t.Builder  idd_Entry = CDAP.objVal_t.newBuilder();
			idd_Entry.setByteval(ByteString.copyFrom(IDDEntry.build().toByteArray()));


			CDAP.CDAPMessage M_WRITE_msg_to_IDD = message.CDAPMessage.generateM_WRITE(
					"DIFMemberList",//object class
					cdapMessage.getDestApName(),  //object name 
					idd_Entry.buildPartial(),//idd entry
					"IDD",
					"IDD",
					this.IDDName,
					this.IDDName,
					00001,//invoke id
					"NMS",//
					"NMS",//
					this.IPCName,//
					this.IPCName//

			);

			byte[] M_WRITE  = M_WRITE_msg_to_IDD.toByteArray();				

			//			this.IPCProcess.getTcpManager().send(this.IPCProcess.getIDDName(), M_WRITE);


			try {


				if(this.tcpManager != null)
				{
					this.tcpManager.getTCPFlow(this.IDDName).sendCDAPMsg(M_WRITE);
				}else
				{
					DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_WRITE);

					this.irm.send(this.irm.getHandle(this.IDDName),payload.toByteArray());


				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component:  M_WRITE sent to IDD: " + this.IDDName);
			this.enrollmentState = state.UPDATING_IDD;
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component State: " + this.enrollmentState);	



		}else//M_CREATE_R(ERROR)
		{
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: M_CREATE_R(ERROR) received");
			this.enrollmentState = state.LISTENING;
			RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component State: " + this.enrollmentState);	

		}





	}

	/**
	 * handle M_STOP
	 * when receiving M_STOP, this Enrollment Component instance will stop listening to the incoming  flow
	 * @param cdapMessage
	 */
	private void handle_M_STOP(CDAPMessage cdapMessage) {


		this.NMS_IS_ACTIVE = false;
		//send M_START to starting the new member
		CDAP.CDAPMessage M_STOP_R_msg = message.CDAPMessage.generateM_STOP_R(
				0, 
				"Obj Class",//Obj class
				"Obj Name",//Obj name
				"IPCProcess",
				"IPCProcess",
				cdapMessage.getObjName(),
				cdapMessage.getObjName(),
				cdapMessage.getInvokeID(),

				cdapMessage.getDestAEInst(),
				cdapMessage.getDestAEName(),
				cdapMessage.getDestApInst(),
				cdapMessage.getDestApName()


		);

		byte[] M_STOP_R  = M_STOP_R_msg.toByteArray();

		//		IPCProcess.getTcpManager().send(cdapMessage.getObjName(),M_STOP_R);

		try {

			if(this.tcpManager != null)
			{
				this.tcpManager.getTCPFlow(cdapMessage.getObjName()).sendCDAPMsg(M_STOP_R);
			}else
			{
				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_STOP_R);

				this.irm.send(this.irm.getHandle(cdapMessage.getObjName()),payload.toByteArray());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component: M_STOP_R sent to " + cdapMessage.getSrcAEName());

		this.enrollmentState = state.NULL;
		RIBdaemon.localRIB.RIBlog.infoLog("Enrollment Component terminated: State: " + this.enrollmentState);		





	}

}



