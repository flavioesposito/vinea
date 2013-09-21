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
package rina.idd;


import rina.cdap.impl.googleprotobuf.*;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.config.RINAConfig;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow0.TCPFlow;
import rina.idd.IDD.iddEntry.Builder;
import rina.rib.impl.RIBDaemonImpl;
import rina.rib.impl.RIBImpl;
import rina.routing.RoutingEntry;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import message.DTPMessage;


import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ByteString;

/**
 *   IDD Handler 
 *   When an application process requests the allocation of IPC resources, 
 *   it is the task of the Inter-DIF directory (IDD) to determine what DIF the Allocate 
 *   should be delivered to. If the Application is available on a DIF that this processing 
 *   system is not a member of, then the Inter-DIF Directory will either instantiate an IPC Process to join the DIF, 
 *   or cooperate with other Inter-DIF Directories to create a DIF with sufficient scope to allow IPC.
 *   IDD could resolove both DIF name and service (or application) name, aka (URL).

 */
public class IDDHandler extends Thread{

	/**
	 * IDD process State
	 */
	private enum state {LISTENING,
		RESOLVING_DIF_NAME,
		RESOLVING_SERVICE_NAME,
		REGISTERING,
		AUTHENTICATING


	};



	/**
	 * RIB daemon
	 */
	private RIBDaemonImpl RIBdaemon = null;


	private RINAConfig config = null;

	/**
	 * ServiceURL (application name) is the primary key
	 */
	private LinkedHashMap<String,LinkedList<IDD.iddEntry.Builder>> IDDDatabaseServiceName =null;

	/**
	 *  DIF_NAME is the primary key
	 */
	private LinkedHashMap<String,IDD.iddEntry.Builder > IDDDatabaseDIFName =null;
	/**
	 * IDD FLOW IS ACTIVE flag
	 */
	private boolean IDD_FLOW_IS_ACTIVE = true;
	/**
	 * flow
	 */
	private TCPFlow flow;
	/**
	 * message
	 */
	private byte[] msg;
	/**
	 * IDD state
	 */
	private state iddState = null;


	private String IPCName = null;
	private String ClientIPCName = null;
	
	private RIBImpl rib = null;


	/**
	 * 
	 * Constructor 
	 * @param flow: incoming client flow
	 * @param IDD RIB daemon
	 */
	public IDDHandler(TCPFlow flow, RIBDaemonImpl RIBdaemon){
		this.flow=flow;


		this.RIBdaemon =  RIBdaemon;


		this.config = (RINAConfig)this.RIBdaemon.localRIB.getAttribute("config");

		this.IPCName = this.config.getIPCName();
		
		this.rib = this.RIBdaemon.localRIB;

	}


	/**
	 * thread listening for the IDD connections
	 */
	@SuppressWarnings("unchecked")
	public void run()
	{	//set up the stateRIBdeamon

		this.iddState =  state.LISTENING;
		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler State: " + this.iddState);

		LinkedHashMap<String, Object> tempAttributeList = this.RIBdaemon.localRIB.getAttributeList();
		if(!tempAttributeList.containsKey("iddState")) tempAttributeList.put("iddState", this.iddState);
		RIBdaemon.localRIB.setAttributeList(tempAttributeList);


		///////////////////////////////////////////////////
		///first message is a DTP message telling where it is from 
		byte[] msg = null;
		try {
			msg = this.flow.receive();
		} catch (Exception e1) {
			e1.printStackTrace();
		} 

		DTP.DTPMessage dtpMessage = null;

		try {
			dtpMessage = DTP.DTPMessage.parseFrom(msg);

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}


		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: New client is added: " +   dtpMessage.getSrcIPCName());


		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: opcode is " +   dtpMessage.getOpCode());

		this.flow.setSrcName(this.IPCName);
		this.flow.setDstName(dtpMessage.getSrcIPCName());

		///////////////////////////////////////////////////////////

		try {
			while(IDD_FLOW_IS_ACTIVE){
				msg = flow.receive();

				try {
					dtpMessage = DTP.DTPMessage.parseFrom(msg);

				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

				DTP.DTPMessage payload = null;



				try {
					payload = DTP.DTPMessage.parseFrom(dtpMessage.getPayload());

				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

				this.ClientIPCName = dtpMessage.getSrcIPCName();

				switch(dtpMessage.getOpCode())
				{
				case M_CDAP:
					handleReceiveCDAPMessage(payload.getPayload().toByteArray());
					break;
				case M_DTP:
					handleReceiveDTPMessage(payload.getPayload().toByteArray());
					break;

				default:
					RIBdaemon.localRIB.RIBlog.errorLog("IDD Handler: received msg is not handled");
					break;
				}

			}
		}catch (Exception e) {
			//e.printStackTrace();
		}
		finally{
			if(flow!=null){
				flow.close();
				RIBdaemon.localRIB.RIBlog.infoLog("Flow Closed " + Thread.currentThread().getName());
			}
		}
	}

	private void handleReceiveDTPMessage(byte[] msg) {

		LinkedList<String>  srcNameList = new LinkedList<String> ();
		LinkedList<Integer>  srcPortList = new LinkedList<Integer> ();

		boolean stop = false;
		byte[] dtpMsg = msg;
		byte[] cdapMsg = null;

		while(!stop)
		{


			DTP.DTPMessage dtpMessage = null;

			try {
				dtpMessage = DTP.DTPMessage.parseFrom(dtpMsg);

			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}

			DTP.DTPMessage payloadWithHeader = null;

			try {
				payloadWithHeader = DTP.DTPMessage.parseFrom(dtpMessage.getPayload());

			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}


			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: DTPMessage received and opcode is " +  dtpMessage.getOpCode());

			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: DTPMessage received and srcIPCName is  " +  dtpMessage.getSrcIPCName());
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: DTPMessage received and srcPortID is " +  dtpMessage.getSrcPortID());



			if(dtpMessage.getOpCode().equals(DTP.opCode_t.M_CDAP))
			{
				cdapMsg = payloadWithHeader.getPayload().toByteArray();
				srcNameList.add(dtpMessage.getSrcIPCName());
				srcPortList.add(Integer.valueOf(dtpMessage.getSrcPortID()));

				stop = true;
			}else
			{
				dtpMsg = payloadWithHeader.getPayload().toByteArray();
				srcNameList.add(dtpMessage.getSrcIPCName());
				srcPortList.add(Integer.valueOf(dtpMessage.getSrcPortID()));
			}

		}

		this.handleReceiveCDAPMessage(cdapMsg,srcNameList,srcPortList );

	}

	public byte[] addAllDTPHeader(byte[] msg, LinkedList<String>  srcNameList, LinkedList<Integer>  srcPortList)
	{

		byte[] result = msg;
		int count = srcNameList.size();

		DTP.DTPMessage payloadWithHeader = DTPMessage.generatePayloadM_CDAP(result);

		DTP.DTPMessage dtpMessage = DTPMessage.generateM_CDAP( srcNameList.get(count-1), srcPortList.get(count-1), this.IPCName, 0, payloadWithHeader.toByteArray());

		System.out.println("IDDHandler: header srcName is " + srcNameList.get(count -1));
		System.out.println("IDDHandler: header srcPort is " + srcPortList.get(count -1));
		result = dtpMessage.toByteArray();


		for(int i =  count -2 ; i>=0; i--)
		{
			payloadWithHeader = DTPMessage.generatePayloadM_CDAP(result);

			dtpMessage = DTPMessage.generateM_CDAP( srcNameList.get(i), srcPortList.get(i), this.IPCName, 0, payloadWithHeader.toByteArray());

			System.out.println("IDDHandler: header srcName is " + srcNameList.get(i));
			System.out.println("IDDHandler: header srcPort is " + srcPortList.get(i));
			result = dtpMessage.toByteArray();
		}

		return result;

	}



	/**
	 * handle message received
	 * before sending IDD query, the RINA process should first send M_CONNCECT to IDD
	 * @param msg
	 */
	public void handleReceiveCDAPMessage(byte[] msg) {


		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		switch(cdapMessage.getOpCode()){


		case M_CREATE:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_CREATE received");
			iddState =state.REGISTERING;
			handle_M_CREATE(cdapMessage);
			iddState =state.LISTENING;
			break;

		case M_WRITE:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_WRITE received");
			iddState =state.REGISTERING;
			handle_M_WRITE(cdapMessage);
			iddState =state.LISTENING;

			break;

		case M_READ:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_READ received");

			if(isServiceURL(cdapMessage)){
				iddState =state.RESOLVING_SERVICE_NAME;
				RIBdaemon.localRIB.RIBlog.infoLog("IDD Process State: " + this.iddState);
			}else{
				iddState =state.RESOLVING_DIF_NAME;
				RIBdaemon.localRIB.RIBlog.infoLog("IDD Process State: " + this.iddState);
			}

			handle_M_READ(cdapMessage);
			iddState =state.LISTENING;
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Process State: " + this.iddState);
			break;

		case M_STOP:
			handle_M_STOP(cdapMessage);
			iddState =state.LISTENING;
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Process State: " + this.iddState);
			break;

		case M_DELETE:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_DELETE received");
			handle_M_DELETE(cdapMessage);
			iddState =state.LISTENING;
			break;
			
		case M_READ_R:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_READ_R received");
			handle_M_READ_R(cdapMessage);
			iddState =state.LISTENING;
			break;

		default:
			RIBdaemon.localRIB.RIBlog.errorLog("IDD Handler: opcode: "+ cdapMessage.getOpCode()+" not handled by IDD");


			break;
		}

	}

	/**
	 * handle message received
	 * before sending IDD query, the RINA process should first send M_CONNCECT to IDD
	 * @param msg
	 */
	public void handleReceiveCDAPMessage(byte[] msg, LinkedList<String>  srcNameList, LinkedList<Integer>  srcPortList ) {


		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: handle received cdap message with given header info");

		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		switch(cdapMessage.getOpCode()){


		case M_CREATE:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_CREATE received");
			iddState =state.REGISTERING;
			handle_M_CREATE(cdapMessage,srcNameList,srcPortList);
			iddState =state.LISTENING;
			break;

		case M_WRITE:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_WRITE received");
			iddState =state.REGISTERING;
			handle_M_WRITE(cdapMessage,srcNameList,srcPortList);
			iddState =state.LISTENING;

			break;

		case M_READ:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_READ received");

			if(isServiceURL(cdapMessage)){
				iddState =state.RESOLVING_SERVICE_NAME;
				RIBdaemon.localRIB.RIBlog.infoLog("IDD Process State: " + this.iddState);
			}else{
				iddState =state.RESOLVING_DIF_NAME;
				RIBdaemon.localRIB.RIBlog.infoLog("IDD Process State: " + this.iddState);
			}

			handle_M_READ(cdapMessage,srcNameList,srcPortList);
			iddState =state.LISTENING;
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Process State: " + this.iddState);
			break;

		case M_STOP:
			handle_M_STOP(cdapMessage,srcNameList,srcPortList);
			iddState =state.LISTENING;
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Process State: " + this.iddState);
			break;

		case M_DELETE:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_DELETE received");
			handle_M_DELETE(cdapMessage,srcNameList,srcPortList);
			iddState =state.LISTENING;
			break;
			
		case M_READ_R:
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_READ_R received");
			handle_M_READ_R(cdapMessage);
			iddState =state.LISTENING;
			break;

		default:
			RIBdaemon.localRIB.RIBlog.errorLog("IDD Handler: opcode: "+ cdapMessage.getOpCode()+" not handled by IDD");


			break;
		}

	}



	/**
	 *  handle M_READ:
	 *  resolves queries for Service  names or DIF names and reply with M_READ_R
	 * @param cdapMessage
	 */

	@SuppressWarnings("unchecked")
	private void handle_M_READ(CDAP.CDAPMessage cdapMessage,  LinkedList<String>  srcNameList, LinkedList<Integer>  srcPortList) {
		//construct empty IDDEntryToSend 
		IDD.iddEntry.Builder  IDDEntryToSend = null;

		//decapsulate the IDD entry from the CDAP message
		String query = cdapMessage.getObjValue().getStrval();

		//look up the entry in the IDD database to create M_READ_R
		LinkedHashMap<String, Object> tempIDDDatabase= null;


		if(AreWeResolvingDIFNAME(cdapMessage.getObjValue().getIntval()))
		{
			tempIDDDatabase = (LinkedHashMap<String, Object>) RIBdaemon.localRIB.getAttributeList().get("IDDDatabaseDIFName");

		}else 
		{
			tempIDDDatabase = (LinkedHashMap<String, Object>) RIBdaemon.localRIB.getAttributeList().get("IDDDatabaseServiceName");
		}


		if(cdapMessage.getObjClass().equals("relayService") || cdapMessage.getObjClass().contains("regular"))
		{
			query = cdapMessage.getObjClass() + "." + cdapMessage.getObjName();
			tempIDDDatabase = (LinkedHashMap<String, Object>) RIBdaemon.localRIB.getAttributeList().get("IDDDatabaseServiceName");
			
		}
		
		System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK, query is " + query);

		if(tempIDDDatabase.containsKey(query))  
		{
		 
			System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK, find the query");
			
			System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKcdapMessage.getObjValue().getIntval()" + cdapMessage.getObjValue().getIntval());
			
			if(AreWeResolvingDIFNAME(cdapMessage.getObjValue().getIntval())  && !cdapMessage.getObjClass().equals("relayService") 
					&& !cdapMessage.getObjClass().contains("regular"))
			{
				System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK:IDDDatabaseDIFName");
				  IDDEntryToSend = (Builder) tempIDDDatabase.get(query);

			}else 
			{
				System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK:IDDDatabaseServiceName");
				
				
				IDD.iddEntry  currentIDDEntry = null;
				
				LinkedList list = (LinkedList)tempIDDDatabase.get(query);
				
				if(list.size() == 1)// only one result 
				{
					RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: answer query, one result of IDDDatabaseServiceName");
					IDDEntryToSend = (Builder) list.get(0);
					
				}else  // more than one result, check cpu usage
				{
					RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: answer query, more than one result of IDDDatabaseServiceName");
					
					
					
					double [] cpuUsage = new double[list.size()];


					for(int i=0; i<list.size();i++)
					{

						currentIDDEntry =  ( (Builder) list.get(i) ).buildPartial(); 
						
						String NMSName = currentIDDEntry.getNmsURL();

						LinkedHashMap<String, Double> appStatus = this.rib.getMultiProviderAppStatus().get(query);

						if(appStatus.containsKey(NMSName))
						{
							cpuUsage[i] = appStatus.get(NMSName);
						}else
						{
							cpuUsage[i] = 999999;
						}
						System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK: NMSName is  " + NMSName);
						System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK: cpuUsage is   " + cpuUsage[i]);

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

					System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK:index is " + index);
					IDDEntryToSend = (Builder)list.get(index);//return the smallest cpu Usage
					
					
					
				}
				
			}
			
		}
		//generate M_CREATE_R(OK) message
		CDAP.objVal_t objvalue =null;

		CDAP.CDAPMessage M_READ_R_msg  = null;

		if(IDDEntryToSend != null)
		{
			CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
			ByteString IDDentry = ByteString.copyFrom(IDDEntryToSend.build().toByteArray());
			ObjValue.setByteval(IDDentry);
			objvalue = ObjValue.buildPartial();


			M_READ_R_msg = message.CDAPMessage.generateM_READ_R(
					0, 						   //result
					cdapMessage.getObjClass(), //required from CDAP specs 0.7.2
					cdapMessage.getObjName(),  //required from CDAP specs 0.7.2
					objvalue,					//IDDrecord
					cdapMessage.getSrcAEInst(), 
					cdapMessage.getSrcAEName(), 
					cdapMessage.getSrcApInst(),
					cdapMessage.getSrcApName(),
					cdapMessage.getInvokeID(), 
					cdapMessage.getDestAEInst(), //dest of the M_CREATE_R is src of the M_CREATE 
					cdapMessage.getDestAEName(),
					cdapMessage.getDestApInst(),
					cdapMessage.getDestApName()
			); 
		}
		else 
		{
			RIBdaemon.localRIB.RIBlog.debugLog("IDD: IDDrecord queried was not found and a null entry was sent in the M_READ_R");

			M_READ_R_msg = message.CDAPMessage.generateM_READ_R(
					-1, 						   //result no entry
					cdapMessage.getObjClass(), //required from CDAP specs 0.7.2
					cdapMessage.getObjName(),  //required from CDAP specs 0.7.2
					cdapMessage.getSrcAEInst(), 
					cdapMessage.getSrcAEName(), 
					cdapMessage.getSrcApInst(),
					cdapMessage.getSrcApName(),
					cdapMessage.getInvokeID(), 
					cdapMessage.getDestAEInst(), //dest of the M_CREATE_R is src of the M_CREATE 
					cdapMessage.getDestAEName(),
					cdapMessage.getDestApInst(),
					cdapMessage.getDestApName()
			); 
		}


		//send the M_CREATE_R over the flow		
		//		byte[] M_READ_R = M_READ_R_msg.toByteArray();

		//		DTP.DTPMessage DTP_reply = DTPMessage.generateM_CDAP(this.ClientIPCName, this.IPCName, M_READ_R);



		try {
			//			flow.send(DTP_reply.toByteArray());

			this.flow.sendDTPMsg(this.addAllDTPHeader(M_READ_R_msg.toByteArray(),srcNameList, srcPortList));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: (handle M_READ with header),  M_READ_R sent");
	}


	private void handle_M_READ(CDAP.CDAPMessage cdapMessage) {


		//construct empty IDDEntryToSend 
		IDD.iddEntry.Builder  IDDEntryToSend = null;

		//decapsulate the IDD entry from the CDAP message
		String query = cdapMessage.getObjValue().getStrval();

		//look up the entry in the IDD database to create M_READ_R
		LinkedHashMap<String, Object> tempIDDDatabase= null;


		if(AreWeResolvingDIFNAME(cdapMessage.getObjValue().getIntval()))
		{
			tempIDDDatabase = (LinkedHashMap<String, Object>) RIBdaemon.localRIB.getAttributeList().get("IDDDatabaseDIFName");

		}else 
		{
			tempIDDDatabase = (LinkedHashMap<String, Object>) RIBdaemon.localRIB.getAttributeList().get("IDDDatabaseServiceName");
		}


		if(cdapMessage.getObjClass().equals("relayService") || cdapMessage.getObjClass().contains("regular"))
		{
			query = cdapMessage.getObjClass() + "." + cdapMessage.getObjName();
			tempIDDDatabase = (LinkedHashMap<String, Object>) RIBdaemon.localRIB.getAttributeList().get("IDDDatabaseServiceName");
		}
		
		System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK, query is " + query);

		if(tempIDDDatabase.containsKey(query))  
		{
		 
			System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK, find the query");
			
			System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKcdapMessage.getObjValue().getIntval()" + cdapMessage.getObjValue().getIntval());
			
			if(AreWeResolvingDIFNAME(cdapMessage.getObjValue().getIntval()))
			{
				System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK:IDDDatabaseDIFName");
				  IDDEntryToSend = (Builder) tempIDDDatabase.get(query);

			}else 
			{
				System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK:IDDDatabaseServiceName");
				
				
				IDD.iddEntry  currentIDDEntry = null;
				
				LinkedList list = (LinkedList)tempIDDDatabase.get(query);
				
				if(list.size() == 1)// only one result 
				{
					RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: answer query, one result of IDDDatabaseServiceName");
					IDDEntryToSend = (Builder) list.get(0);
					
				}else  // more than one result, check cpu usage
				{
					RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: answer query, more than one result of IDDDatabaseServiceName");
					
					
					
					double [] cpuUsage = new double[list.size()];


					for(int i=0; i<list.size();i++)
					{

						currentIDDEntry =  ( (Builder) list.get(i) ).buildPartial(); 
						
						String NMSName = currentIDDEntry.getNmsURL();

						LinkedHashMap<String, Double> appStatus = this.rib.getMultiProviderAppStatus().get(query);

						if(appStatus.containsKey(NMSName))
						{
							cpuUsage[i] = appStatus.get(NMSName);
						}else
						{
							cpuUsage[i] = 999999;
						}
						System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK: NMSName is  " + NMSName);
						System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK: cpuUsage is   " + cpuUsage[i]);

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

					System.out.println("OoooooooooooooooooooooooooooooooooooooooKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK:index is " + index);
					IDDEntryToSend = (Builder)list.get(index);//return the smallest cpu Usage
					
					
					
				}
				
			}
			
		}
		//generate M_CREATE_R(OK) message
		CDAP.objVal_t objvalue =null;

		CDAP.CDAPMessage M_READ_R_msg  = null;

		if(IDDEntryToSend != null)
		{
			CDAP.objVal_t.Builder ObjValue  = CDAP.objVal_t.newBuilder();
			ByteString IDDentry = ByteString.copyFrom(IDDEntryToSend.build().toByteArray());
			ObjValue.setByteval(IDDentry);
			objvalue = ObjValue.buildPartial();


			M_READ_R_msg = message.CDAPMessage.generateM_READ_R(
					0, 						   //result
					cdapMessage.getObjClass(), //required from CDAP specs 0.7.2
					cdapMessage.getObjName(),  //required from CDAP specs 0.7.2
					objvalue,					//IDDrecord
					cdapMessage.getSrcAEInst(), 
					cdapMessage.getSrcAEName(), 
					cdapMessage.getSrcApInst(),
					cdapMessage.getSrcApName(),
					cdapMessage.getInvokeID(), 
					cdapMessage.getDestAEInst(), //dest of the M_CREATE_R is src of the M_CREATE 
					cdapMessage.getDestAEName(),
					cdapMessage.getDestApInst(),
					cdapMessage.getDestApName()
			); 
		}
		else 
		{
			RIBdaemon.localRIB.RIBlog.debugLog("IDD: IDDrecord queried was not found and a null entry was sent in the M_READ_R");

			M_READ_R_msg = message.CDAPMessage.generateM_READ_R(
					-1, 						   //result no entry
					cdapMessage.getObjClass(), //required from CDAP specs 0.7.2
					cdapMessage.getObjName(),  //required from CDAP specs 0.7.2
					cdapMessage.getSrcAEInst(), 
					cdapMessage.getSrcAEName(), 
					cdapMessage.getSrcApInst(),
					cdapMessage.getSrcApName(),
					cdapMessage.getInvokeID(), 
					cdapMessage.getDestAEInst(), //dest of the M_CREATE_R is src of the M_CREATE 
					cdapMessage.getDestAEName(),
					cdapMessage.getDestApInst(),
					cdapMessage.getDestApName()
			); 
		}


		//send the M_CREATE_R over the flow		
		//		byte[] M_READ_R = M_READ_R_msg.toByteArray();

		//		DTP.DTPMessage DTP_reply = DTPMessage.generateM_CDAP(this.ClientIPCName, this.IPCName, M_READ_R);


		try {
			//			flow.send(DTP_reply.toByteArray());

			this.flow.sendCDAPMsg(M_READ_R_msg.toByteArray());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_READ_R sent");

	}


	private void handle_M_READ_R(CDAPMessage cdapMessage) {


		if(cdapMessage.getObjClass().equals("PubSub"))
		{
			String publisher = cdapMessage.getSrcApName();
			String subName = cdapMessage.getObjValue().getStrval();
			int frequency = cdapMessage.getObjValue().getIntval();


			if (subName.equals("appStatus"))
			{
				String subIdentifier = publisher + "%" +subName + "#" +frequency;

				RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: " +  subName +"(fren: " +  frequency + ") M_READ_R received from " + publisher);


				Object subAttribute = null;

				try {
					ByteArrayInputStream   bis   =   new   ByteArrayInputStream(cdapMessage.getObjValue().getByteval().toByteArray()); 
					ObjectInputStream ois;
					ois = new  ObjectInputStream(bis);
					subAttribute  = ois.readObject();
				} catch (Exception e) {
					e.printStackTrace();
				}   

				System.out.println("---------------------appStatus is " + (LinkedHashMap<String, Double>) subAttribute);


				LinkedHashMap<String, Double> appStatus = (LinkedHashMap<String, Double>) subAttribute;

				/////this will be used by NMS or IDD 
				Set<String> SetCurrentMaps = appStatus.keySet();
				Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();

				String currentAppName  = null;
				while(KeyIterMaps.hasNext())
				{
					currentAppName = KeyIterMaps.next();
					
					if(!this.rib.getMultiProviderAppStatus().containsKey(currentAppName))
					{
						this.rib.getMultiProviderAppStatus().put(currentAppName, new LinkedHashMap<String, Double>() );
					}

					this.rib.getMultiProviderAppStatus().get(currentAppName).put(publisher, appStatus.get(currentAppName));
					
					
					RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: this.rib.getMultiProviderAppStatus().get(currentAppName):" + currentAppName + ", is " 
							+ this.rib.getMultiProviderAppStatus().get(currentAppName));

					this.rib.RIBlog.infoLog("current_cpu_Usage_is " +this.rib.getMultiProviderAppStatus().get(currentAppName));
					
					LinkedHashMap<String,Double> currentAppStatus = this.rib.getMultiProviderAppStatus().get(currentAppName);
					
					double allAppCPUUsageInDIF = 0;
					
					Set<String> SetCurrentMaps1 = currentAppStatus.keySet();
					Iterator<String> KeyIterMaps1 = SetCurrentMaps1.iterator();
					
					while(KeyIterMaps1.hasNext())
					{
						String currentPublisher = KeyIterMaps1.next();
						allAppCPUUsageInDIF += currentAppStatus.get(currentPublisher);
						
					}

					this.rib.getAppStatus().put(currentAppName, allAppCPUUsageInDIF);
					
					this.rib.RIBlog.infoLog("current_total_cpu_Usage_is  " + this.rib.getAppStatus());
				}

			}
		}
	}




	/**
	 * Check if auth info are correct. 
	 * we assume IDD always accept the process sending query.
	 * @param username
	 * @param pwd
	 * @return
	 * 
	 */
	@SuppressWarnings("unused")
	private synchronized int authenticationIsCorrect(String username, String pwd) {
		return 0;
		//TODO: have a database for authentication
	}

	/**
	 * handle M_WRITE
	 * @param cdapMessage
	 */
	private void handle_M_WRITE(CDAPMessage cdapMessage) {

		//decapsulate the IDD entry from the CDAP message
		int result = -1;//OK


		String IDDEntryName = null;

		ByteString IDDEntryBytes = cdapMessage.getObjValue().getByteval();
		IDD.iddEntry IDDEntry = null;		
		try {
			IDDEntry = IDD.iddEntry.parseFrom(IDDEntryBytes);
			//add the new entry

			String DIF_NAME = IDDEntry.getDIFName();

			//	String ApplicationName = IDDEntry.getServiceURL(0);		


			CDAP.CDAPMessage M_WRITE_R_msg = null;
			
			if(IDDEntry.getServiceURLList().isEmpty())
			{
				IDDEntryName = DIF_NAME;

				writeNewIDDEntry("IDDDatabaseDIFName",DIF_NAME, IDDEntry);	     //IDDDatabaseDIFName.put(DIF_NAME, IDDEntry.toBuilder());						

				RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: DIF NAME "+DIF_NAME+" entry written on IDDDatabaseDIFName");

			}else
			{
				String ApplicationName = IDDEntry.getServiceURL(0);
				IDDEntryName = ApplicationName;


				writeNewIDDEntry("IDDDatabaseServiceName",ApplicationName,IDDEntry); //IDDDatabaseServiceName.put(ApplicationName, IDDEntry.toBuilder());
				
				
				RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: Application Name "+ApplicationName+" entry written on IDDDatabaseServiceName");


				if(ApplicationName.contains("regular")) // for now there are another service called  "relay service", so don't subscribe to that for now
				{
					//subscibe to NMS to get the status of the application
					
					return;//no M_WRITE_R needed
				}

			}

			result = 0;	

		} catch (Exception e) {
			e.printStackTrace();

		}

		//		if(cdapMessage.getObjClass().equals("newNMSRegistration"))
		//		{
		//			//NO reply
		//			return;
		//		}

		//generate M_WRITE_R(OK) message
		CDAP.CDAPMessage M_WRITE_R_msg = message.CDAPMessage.generateM_WRITE_R(
				result,		
				IDDEntryName,
				IDDEntryName,
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

		//send M_WRITE_R
		byte[] M_WRITE_R = M_WRITE_R_msg.toByteArray();
		try {
			this.flow.sendCDAPMsg(M_WRITE_R);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: M_WRITE_R sent");

	}
	private void handle_M_WRITE(CDAPMessage cdapMessage, LinkedList<String>  srcNameList, LinkedList<Integer>  srcPortList) {

		//decapsulate the IDD entry from the CDAP message
		int result = -1;//OK


		String IDDEntryName = null;

		ByteString IDDEntryBytes = cdapMessage.getObjValue().getByteval();
		IDD.iddEntry IDDEntry = null;		
		try {
			IDDEntry = IDD.iddEntry.parseFrom(IDDEntryBytes);
			//add the new entry

			String DIF_NAME = IDDEntry.getDIFName();

			//	String ApplicationName = IDDEntry.getServiceURL(0);		


			if(IDDEntry.getServiceURLList().isEmpty())
			{
				IDDEntryName = DIF_NAME;

				writeNewIDDEntry("IDDDatabaseDIFName",DIF_NAME, IDDEntry);	     //IDDDatabaseDIFName.put(DIF_NAME, IDDEntry.toBuilder());						

				RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: DIF NAME "+DIF_NAME+" entry written on IDDDatabaseDIFName");

			}else
			{
				String ApplicationName = IDDEntry.getServiceURL(0);
				IDDEntryName = ApplicationName;


				writeNewIDDEntry("IDDDatabaseServiceName",ApplicationName,IDDEntry); //IDDDatabaseServiceName.put(ApplicationName, IDDEntry.toBuilder());
				RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: Application Name "+ApplicationName+" entry written on IDDDatabaseServiceName");


				if(ApplicationName.contains("regular")) // for now there are another service called  "relay service", so don't subscribe to that for now
				{
					//subscibe to NMS to get the status of the application
					
					return;//no M_WRITE_R needed
				}

	
			}

			result = 0;	

		} catch (Exception e) {
			e.printStackTrace();

		}

		//		if(cdapMessage.getObjClass().equals("newNMSRegistration"))
		//		{
		//			//NO reply
		//			return;
		//		}

		//generate M_WRITE_R(OK) message
		CDAP.CDAPMessage M_WRITE_R_msg = message.CDAPMessage.generateM_WRITE_R(
				result,							
				IDDEntryName,
				IDDEntryName,
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

		//send M_WRITE_R
		byte[] M_WRITE_R = M_WRITE_R_msg.toByteArray();
		try {
			this.flow.sendDTPMsg(this.addAllDTPHeader(M_WRITE_R, srcNameList, srcPortList));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IDDHandler: (handle M_WRITE with header), M_WRITE_R  sent");

	}



	/**
	 * handle M_CREATE
	 * @param cdapMessage
	 */
	private void handle_M_CREATE(CDAP.CDAPMessage cdapMessage){

		//decapsulate the IDD entry from the CDAP message
		ByteString IDDEntryBytes = cdapMessage.getObjValue().getByteval();
		IDD.iddEntry IDDEntry = null;		
		try {
			IDDEntry = IDD.iddEntry.parseFrom(IDDEntryBytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		//add the new entry
		String DIF_NAME = IDDEntry.getDIFName();

		String ApplicationName = IDDEntry.getServiceURL(0);


		if(IDDEntry.getServiceURLList().isEmpty())
		{
			writeNewIDDEntry(DIF_NAME,"IDDDatabaseDIFName", IDDEntry);	     //IDDDatabaseDIFName.put(DIF_NAME, IDDEntry.toBuilder());						
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: DIF NAME "+DIF_NAME+" entry written on IDDDatabaseDIFName");

		}else
		{
			writeNewIDDEntry(ApplicationName,"IDDDatabaseServiceName",IDDEntry); //IDDDatabaseServiceName.put(ApplicationName, IDDEntry.toBuilder());
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: Application Name "+ApplicationName+" entry written on IDDDatabaseServiceName");

		}


		//generate M_CREATE_R(OK) message
		CDAP.CDAPMessage M_CREATE_R_msg = message.CDAPMessage.generateM_CREATE_R(
				0,
				cdapMessage.getObjClass(), //required from CDAP specs 0.7.2
				cdapMessage.getObjName(), //required from CDAP specs 0.7.2
				CDAP.objVal_t.getDefaultInstance(), // if it's ok 
				cdapMessage.getSrcAEInst(), //src of the M_CREATE is dest of the M_CREATE_R
				cdapMessage.getSrcAEName(),
				cdapMessage.getSrcApInst(),
				cdapMessage.getSrcApName(), 
				cdapMessage.getInvokeID(), 
				cdapMessage.getDestAEInst(), //dest of the M_CREATE is src of the M_CREATE_R 
				cdapMessage.getDestAEName(),
				cdapMessage.getDestApInst(),
				cdapMessage.getDestApName()
		); 

		//send M_CREATE_R	
		byte[] M_CREATE_R = M_CREATE_R_msg.toByteArray();
		try {
			this.flow.sendCDAPMsg(M_CREATE_R);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_CREATE_R sent");
	}	


	private void handle_M_CREATE(CDAP.CDAPMessage cdapMessage,LinkedList<String>  srcNameList, LinkedList<Integer>  srcPortList){

		//decapsulate the IDD entry from the CDAP message
		ByteString IDDEntryBytes = cdapMessage.getObjValue().getByteval();
		IDD.iddEntry IDDEntry = null;		
		try {
			IDDEntry = IDD.iddEntry.parseFrom(IDDEntryBytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		//add the new entry
		String DIF_NAME = IDDEntry.getDIFName();

		String ApplicationName = IDDEntry.getServiceURL(0);


		if(IDDEntry.getServiceURLList().isEmpty())
		{
			writeNewIDDEntry(DIF_NAME,"IDDDatabaseDIFName", IDDEntry);	     //IDDDatabaseDIFName.put(DIF_NAME, IDDEntry.toBuilder());						
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: DIF NAME "+DIF_NAME+" entry written on IDDDatabaseDIFName");

		}else
		{
			writeNewIDDEntry(ApplicationName,"IDDDatabaseServiceName",IDDEntry); //IDDDatabaseServiceName.put(ApplicationName, IDDEntry.toBuilder());
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: Application Name "+ApplicationName+" entry written on IDDDatabaseServiceName");

		}


		//generate M_CREATE_R(OK) message
		CDAP.CDAPMessage M_CREATE_R_msg = message.CDAPMessage.generateM_CREATE_R(
				0,
				cdapMessage.getObjClass(), //required from CDAP specs 0.7.2
				cdapMessage.getObjName(), //required from CDAP specs 0.7.2
				CDAP.objVal_t.getDefaultInstance(), // if it's ok 
				cdapMessage.getSrcAEInst(), //src of the M_CREATE is dest of the M_CREATE_R
				cdapMessage.getSrcAEName(),
				cdapMessage.getSrcApInst(),
				cdapMessage.getSrcApName(), 
				cdapMessage.getInvokeID(), 
				cdapMessage.getDestAEInst(), //dest of the M_CREATE is src of the M_CREATE_R 
				cdapMessage.getDestAEName(),
				cdapMessage.getDestApInst(),
				cdapMessage.getDestApName()
		); 

		//send M_CREATE_R	
		byte[] M_CREATE_R = M_CREATE_R_msg.toByteArray();
		try {
			this.flow.sendDTPMsg(this.addAllDTPHeader(M_CREATE_R, srcNameList, srcPortList));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_CREATE_R sent");
	}	





	/**
	 * when receiving M_STOP, this IDD instance will stop listening to the incoming client flow and generate an M_STOP_R(OK)
	 * @param cdapMessage
	 */
	private void handle_M_STOP(CDAPMessage cdapMessage) {
		//generete M_STOP_R
		CDAP.CDAPMessage M_STOP_R_msg = message.CDAPMessage.generateM_STOP_R(	
				0, 						   //result
				cdapMessage.getObjClass(), //required from CDAP specs 0.7.2
				cdapMessage.getObjName(),  //required from CDAP specs 0.7.2
				cdapMessage.getSrcAEInst(), 
				cdapMessage.getSrcAEName(), 
				cdapMessage.getSrcApInst(),
				cdapMessage.getSrcApName(),
				cdapMessage.getInvokeID(), 
				cdapMessage.getDestAEInst(), //dest of the M_CREATE_R is src of the M_CREATE 
				cdapMessage.getDestAEName(),
				cdapMessage.getDestApInst(),
				cdapMessage.getDestApName()
		); 

		//send the M_CREATE_R oDatabaseNamever the flow		
		byte[] M_STOP_R = M_STOP_R_msg.toByteArray();
		try {
			this.flow.sendCDAPMsg(M_STOP_R);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_STOP_R sent");
		//terminate the flow
		this.IDD_FLOW_IS_ACTIVE = false;
	}
	private void handle_M_STOP(CDAPMessage cdapMessage,  LinkedList<String>  srcNameList, LinkedList<Integer>  srcPortList) {
		//generete M_STOP_R
		CDAP.CDAPMessage M_STOP_R_msg = message.CDAPMessage.generateM_STOP_R(	
				0, 						   //result
				cdapMessage.getObjClass(), //required from CDAP specs 0.7.2
				cdapMessage.getObjName(),  //required from CDAP specs 0.7.2
				cdapMessage.getSrcAEInst(), 
				cdapMessage.getSrcAEName(), 
				cdapMessage.getSrcApInst(),
				cdapMessage.getSrcApName(),
				cdapMessage.getInvokeID(), 
				cdapMessage.getDestAEInst(), //dest of the M_CREATE_R is src of the M_CREATE 
				cdapMessage.getDestAEName(),
				cdapMessage.getDestApInst(),
				cdapMessage.getDestApName()
		); 

		//send the M_CREATE_R oDatabaseNamever the flow		
		byte[] M_STOP_R = M_STOP_R_msg.toByteArray();
		try {
			this.flow.sendDTPMsg(this.addAllDTPHeader(M_STOP_R, srcNameList, srcPortList));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_STOP_R sent");
		//terminate the flow
		this.IDD_FLOW_IS_ACTIVE = false;
	}


	/**
	 * check if the query is ServiceURL
	 * @param cdapMessage
	 * @return true if SERVICE_URL request, false if DIF_NAME request 
	 */
	private boolean isServiceURL(CDAP.CDAPMessage cdapMessage) {
		CDAP.objVal_t query = cdapMessage.getObjValue();
		if(query.getIntval()==0) return true; //SERVICE_URL request
		else return false; //DIF_NAME request
	}


	/**
	 * check if the query is DIF name
	 * @param flags
	 * @return true if it was asked to resolve the DIF_NAME to IDD
	 */
	private boolean AreWeResolvingDIFNAME(int flag) {
		if(flag == 0) //assuming we set flag to 		
		{
			return true;
		}
		else 
		{
			return false;
		}
	}

	/**
	 * @return the iDDDatabaseServiceName
	 */
	public synchronized LinkedHashMap<String, LinkedList<IDD.iddEntry.Builder>> getIDDDatabaseServiceName() {
		return IDDDatabaseServiceName;
	}

	/**
	 * @param databaseServiceName the iDDDatabaseServiceName to set
	 */
	public synchronized void setIDDDatabaseServiceName(
			LinkedHashMap<String,LinkedList<IDD.iddEntry.Builder>> databaseServiceName) {
		IDDDatabaseServiceName = databaseServiceName;
	}

	/**
	 * @return the iDDDatabaseDIFName
	 */
	public synchronized LinkedHashMap<String, IDD.iddEntry.Builder> getIDDDatabaseDIFName() {
		return IDDDatabaseDIFName;
	}

	/**
	 * @param databaseDIFName the iDDDatabaseDIFName to set
	 */
	public synchronized void setIDDDatabaseDIFName(
			LinkedHashMap<String, IDD.iddEntry.Builder> databaseDIFName) {
		IDDDatabaseDIFName = databaseDIFName;
	}




	/**
	 * remove IDD Entry
	 * @param URL to remove
	 */
	@SuppressWarnings("unchecked")
	public synchronized int removeIDDEntry(String key){

		boolean someEntryGotDeleted = false;
		LinkedHashMap<String, Object> attributeList = RIBdaemon.localRIB.getAttributeList();
		LinkedHashMap<String,IDD.iddEntry.Builder> TempIDDDatabase = null;
		try{
			if(attributeList.containsKey("IDDDatabaseDIFName")){
				TempIDDDatabase = (LinkedHashMap<String,IDD.iddEntry.Builder>) attributeList.get("IDDDatabaseDIFName");
				attributeList.remove("IDDDatabaseDIFName");
				someEntryGotDeleted = true;
				if(TempIDDDatabase.containsKey(key)) TempIDDDatabase.remove(key);
				attributeList.put("IDDDatabaseDIFName", TempIDDDatabase);
			}
			else if(attributeList.containsKey("IDDDatabaseServiceName")){
				TempIDDDatabase = (LinkedHashMap<String,IDD.iddEntry.Builder>) attributeList.get("IDDDatabaseServiceName");
				attributeList.remove("IDDDatabaseServiceName");
				someEntryGotDeleted = true;
				if(TempIDDDatabase.containsKey(key)) TempIDDDatabase.remove(key);
				attributeList.put("IDDDatabaseServiceName", TempIDDDatabase);
			}
		}catch(Exception e) { //send M_DELETE_R(ERROR)
			return -1;
		}
		if(someEntryGotDeleted){
			RIBdaemon.localRIB.writeAttributeList(attributeList);
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: entry "+key+" deleted");
		}
		else
			RIBdaemon.localRIB.RIBlog.warnLog("IDD Handler: no entry "+key+" was in any of the IDD tables");
		return 0;
	}


	/**
	 * @param DatabaseName (attribute name of the attributeList) Name of the database (table/attribute) where the entry goes
	 * @param key of the attribute: 
	 * @param IDD Entry to add
	 */
	@SuppressWarnings("unchecked")
	private synchronized boolean writeNewIDDEntry(String DatabaseName, String key, IDD.iddEntry IDDEntry) {

		/*
		 * FIXME: Cannot handle multiple services in one single DIF
		 * the registeration for that DIF name will only have the new idd entry
		 * instead, what we need to do is to add the new service to the service list in the idd entry,
		 * which is a repeated type in the proto file.
		 */

		if(DatabaseName.equals("IDDDatabaseDIFName"))
		{
			
		( (LinkedHashMap) this.RIBdaemon.localRIB.getAttribute(DatabaseName) ).put(key, IDDEntry.toBuilder());
		
		}else if (DatabaseName.equals("IDDDatabaseServiceName"))
		{
			
			if(	! ( (LinkedHashMap) this.RIBdaemon.localRIB.getAttribute(DatabaseName) ).containsKey(key))
			{
				( (LinkedHashMap) this.RIBdaemon.localRIB.getAttribute(DatabaseName) ).put(key, new LinkedList<IDD.iddEntry.Builder >());
			}
			( ( LinkedList)( (LinkedHashMap) this.RIBdaemon.localRIB.getAttribute(DatabaseName) ).get(key)).add(IDDEntry.toBuilder());
		
		}
		return true;


	}


	/**
	 * @param cdapMessage
	 */
	private void handle_M_DELETE(CDAPMessage cdapMessage,LinkedList<String>  srcNameList, LinkedList<Integer>  srcPortList) {

		//remove entry
		ByteString IDDEntryBytes = cdapMessage.getObjValue().getByteval();
		IDD.iddEntry IDDEntry = null;		
		try {
			IDDEntry = IDD.iddEntry.parseFrom(IDDEntryBytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		//add the new entry
		String DIF_NAME = IDDEntry.getDIFName();

		String ApplicationName = IDDEntry.getServiceURL(0);

		int result = -2;
		if(IDDEntry.getServiceURLList().isEmpty())
		{
			result = removeIDDEntry(DIF_NAME);	     //IDDDatabaseDIFName.put(DIF_NAME, IDDEntry.toBuilder());						
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: DIF NAME "+DIF_NAME+" entry deleted from IDDDatabaseDIFName");

		}else
		{
			result = removeIDDEntry(ApplicationName);
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: Application Name "+ApplicationName+" entry written on IDDDatabaseServiceName");
		}


		//generate M_DELETE_R message
		CDAP.CDAPMessage M_DELETE_R_msg = message.CDAPMessage.generateM_DELETE_R(
				result,
				"Obj Class",
				"Obj Name",
				cdapMessage.getSrcAEInst(),
				cdapMessage.getSrcAEName(),
				cdapMessage.getSrcApInst(),
				cdapMessage.getSrcApName(),
				00001,//invokeID
				cdapMessage.getDestAEInst(),
				cdapMessage.getDestAEName(),
				cdapMessage.getDestApInst(),
				cdapMessage.getDestApName() 
		); 


		//send M_DELETE_R	
		byte[] M_DELETE_R = M_DELETE_R_msg.toByteArray();
		try {
			this.flow.sendDTPMsg(this.addAllDTPHeader(M_DELETE_R, srcNameList, srcPortList));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(result ==0)
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_DELETE_R(OK) sent");
		else
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_DELETE_R(ERROR) sent");


	}

	private void handle_M_DELETE(CDAPMessage cdapMessage) {

		//remove entry
		ByteString IDDEntryBytes = cdapMessage.getObjValue().getByteval();
		IDD.iddEntry IDDEntry = null;		
		try {
			IDDEntry = IDD.iddEntry.parseFrom(IDDEntryBytes);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		//add the new entry
		String DIF_NAME = IDDEntry.getDIFName();

		String ApplicationName = IDDEntry.getServiceURL(0);

		int result = -2;
		if(IDDEntry.getServiceURLList().isEmpty())
		{
			result = removeIDDEntry(DIF_NAME);	     //IDDDatabaseDIFName.put(DIF_NAME, IDDEntry.toBuilder());						
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: DIF NAME "+DIF_NAME+" entry deleted from IDDDatabaseDIFName");

		}else
		{
			result = removeIDDEntry(ApplicationName);
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: Application Name "+ApplicationName+" entry written on IDDDatabaseServiceName");
		}


		//generate M_DELETE_R message
		CDAP.CDAPMessage M_DELETE_R_msg = message.CDAPMessage.generateM_DELETE_R(
				result,
				"Obj Class",
				"Obj Name",
				cdapMessage.getSrcAEInst(),
				cdapMessage.getSrcAEName(),
				cdapMessage.getSrcApInst(),
				cdapMessage.getSrcApName(),
				00001,//invokeID
				cdapMessage.getDestAEInst(),
				cdapMessage.getDestAEName(),
				cdapMessage.getDestApInst(),
				cdapMessage.getDestApName() 
		); 


		//send M_DELETE_R	
		byte[] M_DELETE_R = M_DELETE_R_msg.toByteArray();
		try {
			this.flow.sendCDAPMsg(M_DELETE_R);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(result ==0)
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_DELETE_R(OK) sent");
		else
			RIBdaemon.localRIB.RIBlog.infoLog("IDD Handler: M_DELETE_R(ERROR) sent");


	}


}//end of class
