/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.irm;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import message.DTPMessage;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow.Flow;
import rina.flowAllocator.impl.FlowAllocatorImpl;
import rina.ipcProcess.impl.IPCProcessImpl;
import rina.ipcProcess.impl.HandleMsgReceiver;
import rina.ipcProcess.util.MessageQueue;
import rina.irm.api.IRMAPI;
import rina.rib.impl.RIBImpl;

/**
 * IPC Resource Manager(IRM)
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public class IRM extends Thread implements IRMAPI {




	private String appName = null; 

	
	private LinkedHashMap<String, IPCProcessImpl> underlyingIPCs = null;


	private RIBImpl rib = null;

	//<handleID - <ipcName, port>>
	private LinkedHashMap<Integer, HandleEntry > handleMap = null;


	private int handleIDRange = 10000;

	private MessageQueue irmQueue = null;

	private int newIPCCounter = 0;

	private int IDDHandle = -1;

	private String IDDName = null;

	private LinkedHashMap<String, MessageQueue> relayCreateQueue = null;


	private MessageQueue dtpMsgQueue = null;

	private MessageQueue cdapMsgQueue = null;

	public IRM() {}
	
	public IRM(String appName,RIBImpl rib, LinkedHashMap<String, IPCProcessImpl> underlyingIPCs, 
			MessageQueue dtpMsgQueue, MessageQueue cdapMsgQueue )
	{
		this.appName = appName;
		this.rib = rib;
		this.underlyingIPCs = underlyingIPCs;
		this.dtpMsgQueue = dtpMsgQueue;
		this.cdapMsgQueue = cdapMsgQueue;
		this.handleMap = new LinkedHashMap<Integer, HandleEntry >();
		this.irmQueue = new MessageQueue();
		this.relayCreateQueue = new LinkedHashMap<String, MessageQueue>();
		this.IDDName = this.rib.getAttribute("iddName").toString();

		this.start();

	}




	 
	public int allocateFlow(String srcName, String dstName) {
		int handleID = -1;

		System.out.println("this is in IRM allocateFlow , src is " + srcName + ",dst is " + dstName);

		if(dstName.equals(this.IDDName))
		{
			if(this.IDDHandle != -1)
			{
				return this.IDDHandle;
			}else
			{

				Set<String> SetCurrentMaps = this.underlyingIPCs.keySet();
				Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();

				String underlyingIPCName = KeyIterMaps.next();
				IPCProcessImpl underlyingIPC = this.underlyingIPCs.get(underlyingIPCName);

				int portID = underlyingIPC.allocateFlow(srcName, dstName);

				this.IDDHandle = this.generateHandleID();

				this.handleMap.put(this.IDDHandle, new HandleEntry(dstName,underlyingIPCName, portID));

				this.rib.RIBlog.infoLog("IRM: get a handle to IDD, and it is " +  this.IDDHandle);

				MessageQueue flowQueue = underlyingIPC.getFlowAllocator().getFlowMsgQueue(portID);

				new HandleMsgReceiver(this.dtpMsgQueue,flowQueue);
			}

		}
		else
		{


			String underlyingIPCName = this.getUnderlyingIPC(srcName, dstName);


			if(underlyingIPCName == null)
			{
				if(!this.rib.getMemberList().contains(dstName))
				{
					this.rib.RIBlog.infoLog("IRM: allocate flow needs to create a new underlying DIF");
					handleID = this.createDIF(srcName,dstName);
					
					
				}else // the dst is in the same dif with this IPC, 
				{
					this.rib.RIBlog.infoLog("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					this.rib.RIBlog.infoLog("IRM: Dst is in the same dif with this IPC, but not its neighbour, so relay is needed");

					handleID = this.generateHandleID();

					String nextHop  = this.rib.getForwardingTable().get(dstName);

					this.rib.RIBlog.infoLog("IRM: next hop of " + dstName + " is " + nextHop);

					int handleOfNextHop = this.getHandle(nextHop);

					underlyingIPCName = this.handleMap.get(handleOfNextHop).ipcName;

					int underlyingPort =  this.handleMap.get(handleOfNextHop).portID;

					this.handleMap.put(handleID, new HandleEntry(dstName,underlyingIPCName, underlyingPort));

				}

			}else
			{
				this.rib.RIBlog.infoLog("IRM: allocate flow using the existing underlying DIF with ipc is " + underlyingIPCName);

				int portID = this.underlyingIPCs.get(underlyingIPCName).allocateFlow(srcName, dstName);

				if(portID != -1)
				{

					handleID = this.generateHandleID();

					this.handleMap.put(handleID, new HandleEntry(dstName,underlyingIPCName, portID));

					MessageQueue flowQueue = this.underlyingIPCs.get(underlyingIPCName).getFlowAllocator().getFlowMsgQueue(portID);

					new HandleMsgReceiver(this.dtpMsgQueue,flowQueue);

				}else
				{
					this.rib.RIBlog.errorLog("IRM: allocate flow failed, the other side rejects");
				}
			}
		}
		return handleID;
	}




	 
	public void deallocate(int handleID) {


	}

	 
	public void send(int handleID, byte[] msg) throws Exception {


		HandleEntry flowEntry = this.handleMap.get(handleID);

		String dstName = flowEntry.appName;
		String oldUnderlyingIPCName = flowEntry.ipcName;
		int oldPortID = flowEntry.portID;

		if(dstName.equals(this.IDDName)) // if IDD just sent, as it will not change for now FIXME
		{	
			this.underlyingIPCs.get(oldUnderlyingIPCName).send(oldPortID, msg);
			return;
		}
		//here check the next hop first by looking at the routing table, basically to update the handleMap
		//FIXME: here should be better change to : when forwarding table changes, then update this handleMap
		
		String nextHop  = this.rib.getForwardingTable().get(dstName);
		if(nextHop != null)//
		{

			this.rib.RIBlog.infoLog("IRM(send):  next hop of " + dstName + " is " + nextHop);

			int handleOfNextHop = this.getHandle(nextHop);

			String underlyingIPCName = this.handleMap.get(handleOfNextHop).ipcName;

			int underlyingPort =  this.handleMap.get(handleOfNextHop).portID;

			if(!oldUnderlyingIPCName.equals(underlyingIPCName) || oldPortID != underlyingPort) // nextHop changed
			{
				this.handleMap.put(handleID, new HandleEntry(dstName,underlyingIPCName, underlyingPort));
				this.underlyingIPCs.get(underlyingIPCName).send(underlyingPort, msg);
				return;
			}

		}
		////////////////////////////////////////////////////////

		
		this.underlyingIPCs.get(oldUnderlyingIPCName).send(oldPortID, msg);
		
		this.rib.RIBlog.debugLog("Msg was sent out by IRM to destName: "+dstName);


	}
	
	////////////////////////////////////////////////////////////////////////////////////
	public void sendCDAP(int handleID, byte[] msg) throws Exception
	{
		DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(msg);
		send( handleID, payload.toByteArray());
		
	}
	
	public void sendDTP(int handleID, byte[] msg) throws Exception
	{
		DTP.DTPMessage payload = DTPMessage.generatePayloadM_DTP(msg);
		send( handleID, payload.toByteArray());
	}
	////////////////////////////////////////////////////////////////////////////////////

	 
	public byte[] receive(int handleID) {

		HandleEntry flowEntry = this.handleMap.get(handleID);

		String underlyingIPCName = flowEntry.ipcName;
		int portID = flowEntry.portID;

		return this.underlyingIPCs.get(underlyingIPCName).receive(portID);
	}


	public void run()
	{
		while(true)
		{
			byte[] msg = this.irmQueue.getReceive();
			handleCDAPMsg(msg);
		}
	}


	private void handleCDAPMsg(byte[] msg) {

		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		this.rib.RIBlog.infoLog("IRM: CDAP opcode is  " + cdapMessage.getOpCode());
		this.rib.RIBlog.infoLog("IRM: objectClass is " + cdapMessage.getObjClass());
		this.rib.RIBlog.infoLog("IRM: objectName is " + cdapMessage.getObjName());


		switch(cdapMessage.getOpCode()){


		case M_CREATE:

			handle_M_CREATE(cdapMessage);
			break;


		case M_READ_R:

			handle_M_READ_R(cdapMessage);
			break;			

		default:

			this.rib.RIBlog.errorLog("IRM: Unexpected message received.");
			this.rib.RIBlog.errorLog("IRM: Opcode is " + cdapMessage.getOpCode());
			break;
		}

	}


	private void handle_M_READ_R(CDAPMessage cdapMessage) {
		if(cdapMessage.getObjClass().equals("relayService"))
		{
			//			MessageQueue msq = new MessageQueue();
			//			msq.addReceive(cdapMessage.toByteArray());
			//			
			//			this.relayCreateQueue.put(cdapMessage.getObjName(),msq );

			this.relayCreateQueue.get(cdapMessage.getObjName()).addReceive(cdapMessage.toByteArray());

		}

	}




	/**
	 * @param cdapMessage
	 */
	private void handle_M_CREATE(CDAPMessage cdapMessage)  {

		this.rib.RIBlog.infoLog("IRM: M_CREATE received");

		//send M_CREATE back to with handle ID

		if(cdapMessage.getObjClass().equals("flow"))
		{
			this.rib.RIBlog.infoLog("IRM(" + this.appName +"): M_CREATE(flow) received");

			int dstUnderlyingIPCPort = cdapMessage.getObjValue().getIntval();
			String dstUnderlyingIPC = cdapMessage.getObjValue().getStrval();
			String dstIPCName = cdapMessage.getObjName();

			//			System.out.println("IRM handle M_CREATE (flow): dstIPCName is " + dstIPCName );
			//			System.out.println("IRM handle M_CREATE (flow): dstUnderlyingIPC is " + dstUnderlyingIPC );
			//			System.out.println("IRM handle M_CREATE (flow): dstUnderlyingIPCPort is " + dstUnderlyingIPCPort );
			//			

			// check to agree or not based on current resources
			//if agrees return 0 other wise return -1
			//here we assume it always accepts

			int result = this.checkFlowAvailability();

			this.allocateFlowResponse(this.appName, dstIPCName, dstUnderlyingIPC,dstUnderlyingIPCPort, result);



		}else if(cdapMessage.getObjClass().equals("createNewIPCForApp"))
		{
			String NMS = cdapMessage.getObjName();

			String newIPCName = this.appName +  ".ipc."+ newIPCCounter++;

			this.rib.RIBlog.infoLog("IRM: start to create a new IPC, and name is " + newIPCName);
			this.rib.RIBlog.infoLog("IRM: NMS of the target DIF is  " + NMS);


			IPCProcessImpl newIPC = new IPCProcessImpl(newIPCName, IDDName);



			Set<String> SetCurrentMaps = this.underlyingIPCs.keySet();
			Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();

			String currentIPCName  = null;
			while(KeyIterMaps.hasNext())
			{
				currentIPCName = KeyIterMaps.next();
				newIPC.addUnderlyingIPC(this.underlyingIPCs.get(currentIPCName));
			}


			newIPC.joinDIF(NMS,this.appName);

			newIPC.start();

			//ipc sends msg to B, and ask for relay service from relayIPC
			//the the relay enrolls both sides 


			////////////////////////////////////////////////////////////////////////
			this.underlyingIPCs.put(newIPC.getIPCName(), newIPC);
			newIPC.addUpperIPC(this.appName, this.dtpMsgQueue, this.cdapMsgQueue);
			///////////////////////////////////////////////////////////////////////////





		}

	}

	private void allocateFlowResponse(String srcIPCName, String dstIPCName,String dstUnderlyingIPC, int dstUnderlyingIPCPort, int result) {

		String underlyingIPCName = this.getUnderlyingIPC(srcIPCName, dstIPCName, dstUnderlyingIPC);

		System.out.println("IRM: allocateFlowResponse: underlyingIPCName is " + underlyingIPCName);

		if(result == 0)
		{
			int srcPortID = this.underlyingIPCs.get(underlyingIPCName).allocateFlowResponse
			(srcIPCName, dstIPCName,  dstUnderlyingIPC, dstUnderlyingIPCPort, result);

			int handleID =  this.generateHandleID();
			this.handleMap.put(handleID, new HandleEntry(dstIPCName,underlyingIPCName, srcPortID));

			this.rib.RIBlog.infoLog("IRM: incoming flow created with handlID " + handleID + ",underlyingIPC is" + underlyingIPCName 
					+ ", portID is " + srcPortID);

			MessageQueue flowQueue = this.underlyingIPCs.get(underlyingIPCName).getFlowAllocator().getFlowMsgQueue(srcPortID);

			new HandleMsgReceiver(this.dtpMsgQueue,flowQueue);

		}else //rejct
		{
			this.underlyingIPCs.get(underlyingIPCName).allocateFlowResponse
			(srcIPCName, dstIPCName,  dstUnderlyingIPC, dstUnderlyingIPCPort, result);

			this.rib.RIBlog.infoLog("IRM: incoming flow rejected");
		}

	}


	private int checkFlowAvailability() {
		// TODO Auto-generated method stub
		return 0;
	}


	private int createDIF(String srcName, String dstName) {


		int handle = -1;


		if(IDDName == null)
		{
			IDDName = this.rib.getAttribute("iddName").toString();
		}

		MessageQueue msgQueue = new MessageQueue();
		this.relayCreateQueue.put(dstName, msgQueue);


		CDAP.CDAPMessage cdapMsg_M_READ = message.CDAPMessage.generateM_READ
		("relayService", dstName, this.IDDName, this.IDDName, this.appName,this.appName);


		try {
			DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(cdapMsg_M_READ.toByteArray());

			this.send(this.IDDHandle, payload.toByteArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		byte[] reply = 	this.relayCreateQueue.get(dstName).getReceive();
		//FIXME: what if IDD dies, not reply, you are stuck here forever

		CDAP.CDAPMessage M_READ_R = null;
		try {
			M_READ_R = CDAP.CDAPMessage.parseFrom(reply);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		rina.idd.IDD.iddEntry result = null;

		try {
			result = rina.idd.IDD.iddEntry.parseFrom(M_READ_R.getObjValue().getByteval());
		} catch (InvalidProtocolBufferException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	


		String relayIPC = result.getNmsURL();


		this.rib.RIBlog.infoLog("IRM: realy service reply result is " +  relayIPC);

		if(relayIPC == null)
		{
			return -1;
		}



		/// unitl hopcount reaches maximum 
		// relayIPC = askIDD(dstName) 
		// if relayIPC is reachable
		//        then create DIF and enroll
		// if relayIPC is not reachable
		// hopcount++
		// continue
		// ..........

		String newIPCName = this.appName +  ".ipc."+ newIPCCounter++;

		IPCProcessImpl newIPC = new IPCProcessImpl(newIPCName, IDDName);


		//add all underlying IPC to the new create IPC's underlying IPC

		Set<String> SetCurrentMaps = this.underlyingIPCs.keySet();
		Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();

		String currentIPCName  = null;
		while(KeyIterMaps.hasNext())
		{
			currentIPCName = KeyIterMaps.next();
			newIPC.addUnderlyingIPC(this.underlyingIPCs.get(currentIPCName));
		}



		
		newIPC.initDIF(relayIPC, dstName, this.appName);

		newIPC.start();
		
		

		//ipc sends msg to B, and ask for relay service from relayIPC
		//the the relay enrolls both sides 


		////////////////////////////////////////////////////////////////////////
		this.underlyingIPCs.put(newIPC.getIPCName(), newIPC);
		newIPC.addUpperIPC(this.appName, this.dtpMsgQueue, this.cdapMsgQueue);
		///////////////////////////////////////////////////////////////////////////


		this.rib.RIBlog.infoLog("IRM: createDIF(): underlying DIF is created");




		handle = this.allocateFlow(newIPC, srcName, dstName);	
		
	


		//		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//		////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//		///testPUBSUB here/
		//
		//
		//		try {
		//			Thread.sleep(2000);
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//
		//		IPCProcessImpl ipc = this.underlyingIPCs.get("ipc1");
		//
		//		int appsReachableSubID = ipc.getRIBdaemon().createSub("appsReachable");
		//
		//		System.out.println("subID is  " + appsReachableSubID);
		//
		//		try {
		//			Thread.sleep(2000);
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//
		//		LinkedList<String> appsR = (LinkedList<String>)ipc.getRIBdaemon().readSub(appsReachableSubID);
		//		System.out.println("appsReachable of ipc is " + appsR);
		//
		//
		//		int neighbourSubID = newIPC.getRIBdaemon().createSub("neighbour");
		//		System.out.println("neighbourSubID is  " + neighbourSubID);
		//
		//		try {
		//			Thread.sleep(2000);
		//		} catch (InterruptedException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		//
		//		LinkedList<String> neighbour = (LinkedList<String>)newIPC.getRIBdaemon().readSub(neighbourSubID);
		//
		//		System.out.println("neighbour is " +  neighbour);
		//
		//
		//		////testPUBSUB done
		//		////////////////////////////////////////////////////////////////////////////////////////////////////////
		//		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		//		///////////////////////////////////////////////////////////////////////////////////////////////////////////


		return handle;
	}

	//using underlyingIPC to allocate a flow from srcName to dstName
	private int allocateFlow(IPCProcessImpl underlyingIPC, String srcName, String dstName) {

		int handleID = -1;

		this.rib.RIBlog.infoLog("IRM: allocate flow using a given underlying IPC " + underlyingIPC.getIPCName());

		int portID = underlyingIPC.allocateFlow(srcName, dstName);

		if(portID != -1)
		{

			handleID = this.generateHandleID();

			this.handleMap.put(handleID, new HandleEntry(dstName,underlyingIPC.getIPCName(), portID));

			MessageQueue flowQueue = underlyingIPC.getFlowAllocator().getFlowMsgQueue(portID);

			new HandleMsgReceiver(this.dtpMsgQueue,flowQueue);

		}else
		{
			this.rib.RIBlog.errorLog("IRM: allocate flow failed, the other side rejects");
		}

		return 0;
	}




	/*
	 * @param srcName
	 * @param dstName
	 * @return
	 */
	private String getUnderlyingIPC(String srcName, String dstName) {

		String underlyingDstIPCName = null;

		Set<String> SetCurrentMaps = this.underlyingIPCs.keySet();
		Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();

		String currentIPCName  = null;
		while(KeyIterMaps.hasNext())
		{
			currentIPCName = KeyIterMaps.next();
			IPCProcessImpl currentIPC = this.underlyingIPCs.get(currentIPCName);

			underlyingDstIPCName = currentIPC.checkReachablity(dstName);

			if(underlyingDstIPCName != null)
			{
				return currentIPCName;
			}

		}

		return null;
	}



	public int getHandle(String dstName)
	{


		int handle = -1;

		Set<Integer> SetCurrentMaps = this.handleMap.keySet();
		Iterator<Integer> KeyIterMaps = SetCurrentMaps.iterator();

		while(KeyIterMaps.hasNext())
		{

			handle = KeyIterMaps.next();

			//			System.out.println("getHandle(): dstName is " +  dstName + ", handle is " + handle);
			//			System.out.println("this.handleMap.get(handle).appName is " + this.handleMap.get(handle).appName);

			if(this.handleMap.get(handle).appName.equals(dstName))
			{

				return handle;

			}

		}

		return -1;
	}



	private String getUnderlyingIPC(String srcName, String dstName, String dstUnderlyingIPCName) {

		System.out.println("IRM in IPC " + this.appName + ", dstUnderlyingIPCName is " + dstUnderlyingIPCName);

		String underlyingDstIPCName = null;

		Set<String> SetCurrentMaps = this.underlyingIPCs.keySet();
		Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();

		String currentIPCName  = null;
		while(KeyIterMaps.hasNext())
		{
			System.out.println("IRM in IPC currentIPCName is " + currentIPCName);

			currentIPCName = KeyIterMaps.next();
			IPCProcessImpl currentIPC = this.underlyingIPCs.get(currentIPCName);

			underlyingDstIPCName = currentIPC.checkReachablity(dstName);

			System.out.println("IRM in IPC " + this.appName + ", underlyingDstIPCName is " + underlyingDstIPCName);

			if(underlyingDstIPCName != null)
			{

				if(underlyingDstIPCName.equals(dstUnderlyingIPCName))
				{
					return currentIPCName;
				}
			}

		}

		return null;
	}

	private synchronized int generateHandleID()
	{
		int hanldeID = -1;

		hanldeID = (int)( Math.random()* this.handleIDRange); 

		while(this.handleMap.containsKey(hanldeID))
		{
			hanldeID = (int)( Math.random()* this.handleIDRange); 
		}

		System.out.println("handle generated is " +  hanldeID);
		return hanldeID;
	}
	/**
	 * @return the irmQueue
	 */
	public synchronized MessageQueue getIrmQueue() {
		return irmQueue;
	}

	/**
	 * @param irmQueue the irmQueue to set
	 */
	public synchronized void setIrmQueue(MessageQueue irmQueue) {
		this.irmQueue = irmQueue;
	}


	/**
	 * @return the handleMap
	 */
	public synchronized LinkedHashMap<Integer, HandleEntry> getHandleMap() {
		return handleMap;
	}




	/**
	 * @return the iDDHandle
	 */
	public synchronized int getIDDHandle() {
		return IDDHandle;
	}




	/**
	 * @param iDDHandle the iDDHandle to set
	 */
	public synchronized void setIDDHandle(int iDDHandle) {
		IDDHandle = iDDHandle;
	}



	/**
	 * @return the underlyingIPCs
	 */
	public synchronized LinkedHashMap<String, IPCProcessImpl> getUnderlyingIPCs() {
		return underlyingIPCs;
	}




	/**
	 * @param underlyingIPCs the underlyingIPCs to set
	 */
	public synchronized void setUnderlyingIPCs(
			LinkedHashMap<String, IPCProcessImpl> underlyingIPCs) {
		this.underlyingIPCs = underlyingIPCs;
	}


	/**
	 * @param handleMap the handleMap to set
	 */
	public synchronized void setHandleMap(
			LinkedHashMap<Integer, HandleEntry> handleMap) {
		this.handleMap = handleMap;
	}

}	

