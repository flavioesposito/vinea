/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.rib.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import message.DTPMessage;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow.Flow;
import rina.flow0.TCPFlow;
import rina.ipcProcess.impl.IPCProcessImpl;
import rina.ipcProcess.impl.TCPFlowManager;
import rina.ipcProcess.util.MessageQueue;
import rina.irm.IRM;
import rina.rib.api.RIBDaemonAPI;
import rina.rib.util.Event;
import rina.rib.util.EventQueue;
import rina.rib.util.PubHandler;
import rina.rib.util.SubHandler;
import rina.routing.RoutingEntry;


/**
 * RIB Daemon Implementation
 * note that JVM will only shut down a program when all user threads have terminate.
 * daemon threads do not keep the program from quitting; user threads keep the program from quitting.
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 */
public class RIBDaemonImpl extends Thread implements RIBDaemonAPI {


	public RIBImpl localRIB = null;

	public SubscriptionOptimizer RIBSubscriptionOptimizer = null;

	private IRM irm = null;

	private TCPFlowManager tcpManager = null;

	private String IPCName = null;

	private String NMSName = null;


	private boolean stop = false;

	private int subIDRange =10000;

	private int pubIDRange = 10000;

	private LinkedHashMap<Integer, Event> subList = null;
	private LinkedHashMap<String, Integer> subnameToID = null;

	private LinkedHashMap<String,  LinkedList<Integer> > appSubList = null;



	private LinkedHashMap<Integer, Event> pubList = null;
	private LinkedHashMap<String, Integer> pubnameToID = null;
	private LinkedHashMap<Integer, PubHandler> pubIDToHandler = null;


	private MessageQueue ribDaemonQueue = null;

	private LinkedHashMap<String, IPCProcessImpl> underlyingIPCs = null;

	public RIBDaemonImpl(RIBImpl rib) {
		this.setDaemon(true); // daemon threads do not keep the program from quitting
		this.localRIB = rib;

		this.IPCName = this.localRIB.getAttribute("ipcName").toString();


		this.ribDaemonQueue = (MessageQueue)this.localRIB.getAttribute("ribDaemonQueue");
		this.RIBSubscriptionOptimizer = new SubscriptionOptimizer();

		this.subList = this.localRIB.getSubList();
		this.pubList = this.localRIB.getSubList();
		this.subnameToID = this.localRIB.getSubnameToID();
		this.pubnameToID = this.localRIB.getPubnameToID();
		this.appSubList = this.localRIB.getAppSubList();
		this.pubIDToHandler = this.localRIB.getPubIDToHandler();



		this.start();
	}

	public RIBDaemonImpl(RIBImpl rib, IRM irm) {
		this.setDaemon(true); // daemon threads do not keep the program from quitting

		this.localRIB = rib;
		this.IPCName = this.localRIB.getAttribute("ipcName").toString();

		this.irm = irm;
		this.RIBSubscriptionOptimizer = new SubscriptionOptimizer();
		this.underlyingIPCs = this.irm.getUnderlyingIPCs();

		this.ribDaemonQueue = (MessageQueue)this.localRIB.getAttribute("ribDaemonQueue");



		this.subList = this.localRIB.getSubList();
		this.pubList = this.localRIB.getSubList();
		this.subnameToID = this.localRIB.getSubnameToID();
		this.pubnameToID = this.localRIB.getPubnameToID();
		this.appSubList = this.localRIB.getAppSubList();
		this.pubIDToHandler = this.localRIB.getPubIDToHandler();

		this.start();
	}

	public RIBDaemonImpl(RIBImpl rib, TCPFlowManager tcpManager) {
		this.setDaemon(true); // daemon threads do not keep the program from quitting

		this.localRIB = rib;
		this.IPCName = this.localRIB.getAttribute("ipcName").toString();


		this.tcpManager = tcpManager;
		this.RIBSubscriptionOptimizer = new SubscriptionOptimizer();


		this.ribDaemonQueue = (MessageQueue)this.localRIB.getAttribute("ribDaemonQueue");


		this.subList = this.localRIB.getSubList();
		this.pubList = this.localRIB.getSubList();
		this.subnameToID = this.localRIB.getSubnameToID();
		this.pubnameToID = this.localRIB.getPubnameToID();
		this.appSubList = this.localRIB.getAppSubList();
		this.pubIDToHandler = this.localRIB.getPubIDToHandler();

		this.start();

	}



	 
	public synchronized int createSub(int frequency, String subName) {

		//FIXME: consider frequency as another dimension

		String subIdentifier = subName + "#"+ frequency;


		if(this.subnameToID.containsKey(subIdentifier) )//if this is an existing sub item
		{
			return this.subnameToID.get(subIdentifier);
		}

		int subID = this.generateSubID();


		Event event = new Event("sub", subID, subName, frequency);

		this.subList.put(subID, event);
		this.subnameToID.put(subIdentifier, subID);

		this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): sub name is " + event.getName() + ", frequency is " + event.getFrenquency());

		if(this.tcpManager!=null)
		{
			new SubHandler(this.localRIB, this.tcpManager, event).start();
		}else
		{
			new SubHandler(this.localRIB, this.irm, event).start();
		}


		return subID;
	}

	 
	public synchronized int createSub(int frequency, String subName, String publisher) {

		String subIdentifier = publisher + "%" + subName + "#"+ frequency;

		if(this.subnameToID.containsKey(subIdentifier) )//if this is an existing sub item
		{
			return this.subnameToID.get(subIdentifier);
		}

		int subID = this.generateSubID();

		Event event = new Event("sub", subID, subName, frequency, publisher);

		this.subList.put(subID, event);
		this.subnameToID.put(subIdentifier, subID);

		this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): sub name is " + event.getName() + ", frequency is " + event.getFrenquency()
				+ ", and publisher is " + event.getPublisher());

		if(this.tcpManager!=null)
		{
			new SubHandler(this.localRIB, this.tcpManager, event).start();
		}else
		{
			new SubHandler(this.localRIB, this.irm, event).start();
		}


		return subID;
	}


	public synchronized int getSubID(int frequency, String subName, String publisher)
	{
		int subID = -1;

		String subIdentifier = publisher + "%" + subName + "#"+ frequency;

		if(this.subnameToID.containsKey(subIdentifier) )//if this is an existing sub item
		{
			subID = this.subnameToID.get(subIdentifier);
		}

		return subID;
	}

	public synchronized int getSubID(int frequency, String subName)
	{
		int subID = -1;

		String subIdentifier =  subName + "#"+ frequency;

		if(this.subnameToID.containsKey(subIdentifier) )//if this is an existing sub item
		{
			subID = this.subnameToID.get(subIdentifier);
		}
		return subID;
	}

	public synchronized int getPubID(int frequency, String pubName)
	{
		int pubID = -1;

		String pubIdentifier =  pubName + "#"+ frequency;

		if(this.pubnameToID.containsKey(pubIdentifier) )//if this is an existing sub item
		{
			pubID = this.pubnameToID.get(pubIdentifier);
		}
		return pubID;
	}


	 
	public synchronized int createPub(int frequency, String pubName) {


		String pubIdentifier = pubName + "#" + frequency;

		if(this.pubnameToID.containsKey(pubIdentifier) )//if this is an existing sub item
		{
			return this.pubnameToID.get(pubIdentifier);
		}

		int pubID = this.generatePubID();

		Event event = new Event("pub", pubID, pubName, frequency);

		this.pubList.put(pubID, event);

		this.pubnameToID.put(pubIdentifier, pubID);

		PubHandler pubHandler = null;

		if(this.tcpManager != null)
		{
			pubHandler = new PubHandler(this.localRIB, this.tcpManager,event);
		}else
		{
			pubHandler = new PubHandler(this.localRIB,this.irm,event);
		}

		this.pubIDToHandler.put(pubID, pubHandler);
		pubHandler.start();

		return pubID;
	}

	 
	public synchronized Object readSub(int subID) {
		Object result = this.subList.get(subID).getAttribute();
		return result;
	}

	 
	public synchronized void deleteSub(int subID) {
		// TODO Auto-generated method stub

	}

	 
	public synchronized void deletePub(int pubID) {
		// TODO Auto-generated method stub

	}

	 
	public synchronized void writePub(int pubID, Object msg) {
		// TODO Auto-generated method stub

	}



	public void run()
	{
		while(!stop){
			byte[] msg = this.ribDaemonQueue.getReceive();

			if(msg != null)
			{
				handleReceiveMessage(msg);
			}
		}
	}


	private void handleReceiveMessage(byte[] msg) {



		CDAP.CDAPMessage cdapMessage = null;
		try {
			cdapMessage = CDAP.CDAPMessage.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): opcode is " +  cdapMessage.getOpCode()
				+" objClass is "+cdapMessage.getObjClass() + " received from " + cdapMessage.getSrcApName());

		switch(cdapMessage.getOpCode()){

		case M_READ_R:

			handle_M_READ_R(cdapMessage);
			break;

		case M_READ:
			hanlde_M_READ(cdapMessage);
			break;

		default:

			this.localRIB.RIBlog.errorLog("RIBDaemon("+this.IPCName + "): Unexpected message received.");
			this.localRIB.RIBlog.errorLog("RIBDaemon("+this.IPCName + "): Opcode is " + cdapMessage.getOpCode());
			break;
		}


	}

	/////////////////////////////////////////////////////////////////////////////////////////////////




	private void hanlde_M_READ(CDAPMessage cdapMessage) {

		String subscriber = cdapMessage.getSrcApName();
		String subName  = cdapMessage.getObjValue().getStrval();
		int frequency = cdapMessage.getObjValue().getIntval();

		if(subName.equals("linkState"))
		{

			this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): M_READ(PubSub/linkState) received from   " + subscriber  + " with frequency " +  frequency); 

			int linkStatePubID = this.getPubID(frequency, "linkState");

			this.pubIDToHandler.get(linkStatePubID).addSubscriber(subscriber);

			this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): add  " +  subscriber + " to LinkState subscriber list"); 


		}else//  if(subName.equals("appStatus"))
		{

			this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + ")subscribe request received from  " + cdapMessage.getSrcApName() 
					+ " and it is for " + subName + " with frequency " + frequency );

			String subIdentifier = subName + "#" +frequency;

			if(this.localRIB.getPubnameToID().containsKey(subIdentifier))
			{
				PubHandler pubHandler = this.localRIB.getPubIDToHandler().get(this.localRIB.getPubnameToID().get(subIdentifier));
				pubHandler.addSubscriber(subscriber);			
				this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): new subscriber(" +  subscriber +") to  " + subIdentifier + "added."  ); 
			}else
			{
				this.localRIB.RIBlog.warnLog("RIBDaemon("+this.IPCName + "): subscriber(" +  subscriber +") to  " + subIdentifier + "failed, the sub event does not exist"  ); 

			}
		}


	}

	private void handle_M_READ_R(CDAPMessage cdapMessage) {


		String publisher = cdapMessage.getSrcApName();
		String subName = cdapMessage.getObjValue().getStrval();
		int frequency = cdapMessage.getObjValue().getIntval();



		if(this.NMSName ==null && this.localRIB.isEnrollmentFlag() == false)
		{
			this.NMSName = this.localRIB.getAttribute("nmsName").toString();
		}

		if(subName.equals("appsReachable") && cdapMessage.getSrcApName().equals(this.NMSName))
		{
			String subIdentifier = subName + "#" +frequency;

			this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): " + "appsReachable(freq: " +  frequency + ")M_READ_R received");
			LinkedList<String> appsReachable = null;

			try {
				ByteArrayInputStream   bis   =   new   ByteArrayInputStream(cdapMessage.getObjValue().getByteval().toByteArray()); 
				ObjectInputStream ois;
				ois = new  ObjectInputStream(bis);
				appsReachable  = ( LinkedList<String> ) ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}   


			this.subList.get(this.subnameToID.get(subIdentifier)).setAttribute(appsReachable);
			System.out.println( this.IPCName + "----------------------------------" + appsReachable);

		}else if(subName.equals("linkState"))
		{

			this.localRIB.RIBlog.infoLog("Routing Daemon("+this.IPCName + ") : M_READ_R (PubSub/linkState) recevied  from " + publisher);

			String original_publisher = cdapMessage.getObjName();

			///upcate link state cost to this publisher, it will be like we receive a probe respond msg
			this.localRIB.getProbeReplyFlag().put(publisher, Boolean.TRUE);

			RoutingEntry re = new RoutingEntry(System.currentTimeMillis(), this.IPCName, cdapMessage.getSrcApName(), this.calculateLinkWeight(cdapMessage.getSrcApName()));

			this.localRIB.getNeighbourCost().put(cdapMessage.getSrcApName(), re);

			/////////////////////////////////////////////////////////////////////////////////////////////


			String subIdentifier = publisher + "%" + subName + "#"+ frequency;

			int inf = 999999;

			byte[] byteArray =  cdapMessage.getObjValue().getByteval().toByteArray();
			LinkedHashMap <String, RoutingEntry> neighbourlinkCost = null;

			ByteArrayInputStream   bis   =   new   ByteArrayInputStream(byteArray); 
			ObjectInputStream ois;
			try {
				ois = new  ObjectInputStream(bis);
				neighbourlinkCost  = ( LinkedHashMap <String, RoutingEntry> ) ois.readObject();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}   

			//update its one allRoutingEntry Map
			this.localRIB.getAllRoutingEntry().put(cdapMessage.getObjName(), neighbourlinkCost);

			//forward to all its neihbours, other than the src, and the orginal sender
			LinkedList<String> neighbour = this.localRIB.getNeighbour();

			if(neighbour!=null)
			{

				for(int i =0; i< neighbour.size();i++)
				{
					try {

						// it is not the src and it is not the String Values in the message
						if( !neighbour.get(i).equals(cdapMessage.getObjName()) && !neighbour.get(i).equals(cdapMessage.getSrcApName()))
						{
							CDAP.CDAPMessage M_READ_R_msg = message.CDAPMessage.generateM_READ_R(
									0,
									"PubSub",//object class
									cdapMessage.getObjName(),  //object name 
									cdapMessage.getObjValue(), 
									neighbour.get(i),//dst
									neighbour.get(i),
									neighbour.get(i),
									neighbour.get(i),
									00001,//invoke id
									this.IPCName,//src
									this.IPCName,//
									this.IPCName,//
									this.IPCName//
							);




							if(this.tcpManager != null)
							{
								this.tcpManager.getTCPFlow(cdapMessage.getSrcApName()).sendCDAPMsg(M_READ_R_msg.toByteArray());

							}else
							{

								int handleID = this.irm.getHandle(neighbour.get(i));

								if( handleID == -1)
								{ 
									handleID = this.irm.allocateFlow(this.IPCName, neighbour.get(i));
								}

								DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_R_msg.toByteArray());

								this.irm.send(handleID,payload.toByteArray());
							}

							this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): M_READ_R(PubSub/linkStates) forwarded to  " + neighbour.get(i) ); 

						} 
					}catch (Exception e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): failed M_READ_R(PubSub/linkStates) forwarded to  " + neighbour.get(i) ); 

					}

				}

			}	
		}
     else if (subName.equals("appStatus"))
	{
		String subIdentifier = publisher + "%" +subName + "#" +frequency;

		this.localRIB.RIBlog.infoLog("RIBDaemon("+this.IPCName + "): " +  subName +"(fren: " +  frequency + ") M_READ_R received from "
				+ publisher);


		Object subAttribute = null;

		try {
			ByteArrayInputStream   bis   =   new   ByteArrayInputStream(cdapMessage.getObjValue().getByteval().toByteArray()); 
			ObjectInputStream ois;
			ois = new  ObjectInputStream(bis);
			subAttribute  = ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}   

		System.out.println("---------------------appStatus on ipc " + publisher +" is " + (LinkedHashMap<String, Double>) subAttribute);

		this.subList.get(this.subnameToID.get(subIdentifier)).setAttribute(subAttribute);

		LinkedHashMap<String, Double> appStatus = (LinkedHashMap<String, Double>) subAttribute;


		/////this will be used by NMS or IDD 
		Set<String> SetCurrentMaps = appStatus.keySet();
		Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();

		String currentAppName  = null;
		while(KeyIterMaps.hasNext())
		{
			currentAppName =  KeyIterMaps.next();

			if(!this.localRIB.getMultiProviderAppStatus().containsKey(currentAppName))
			{
				this.localRIB.getMultiProviderAppStatus().put(currentAppName, new LinkedHashMap<String, Double>() );
			}

			this.localRIB.getMultiProviderAppStatus().get(currentAppName).put(publisher, appStatus.get(currentAppName));


			System.out.println("---------------------this.localRIB.getMultiProviderAppStatus().get(currentAppName):" + currentAppName + ", is " 
					+ this.localRIB.getMultiProviderAppStatus().get(currentAppName));

			this.localRIB.RIBlog.infoLog("current_cpu_Usage_is " +this.localRIB.getMultiProviderAppStatus().get(currentAppName));

			//update the appStatus of the NMS

			LinkedHashMap<String,Double> currentAppStatus = this.localRIB.getMultiProviderAppStatus().get(currentAppName);

			double allAppCPUUsageInDIF = 0;

			Set<String> SetCurrentMaps1 = currentAppStatus.keySet();
			Iterator<String> KeyIterMaps1 = SetCurrentMaps1.iterator();

			while(KeyIterMaps1.hasNext())
			{
				String currentPublisher = KeyIterMaps1.next();
				allAppCPUUsageInDIF += currentAppStatus.get(currentPublisher);

			}

			this.localRIB.getAppStatus().put("regular."+ currentAppName, allAppCPUUsageInDIF);

			this.localRIB.RIBlog.infoLog("current_total_cpu_Usage_in_dif_is  " + this.localRIB.getAppStatus());

		}



	}
}

private final int inf = 999999;
public int calculateLinkWeight(String srcName)
{

	int cost = inf;
	//FIXME: here we use a link cost, acutally it should be determined from probe msg
	//	cost = (int)( Math.random()*100);
	cost = 10;
	if(srcName.equals("appD"))//this is to manually set the link cost for expriment purpose
	{
		cost= 20;
	}
	return cost;

}


/**
 * @return the localRIB
 */
public RIBImpl getLocalRIB() {
	return localRIB;
}



/**
 * @param localRIB the localRIB to set
 */
public void setLocalRIB(RIBImpl localRIB) {
	this.localRIB = localRIB;
}


/**
 * @return the rIBSubscriptionOptimizer
 */
public SubscriptionOptimizer getRIBSubscriptionOptimizer() {
	return RIBSubscriptionOptimizer;
}


/**
 * @param rIBSubscriptionOptimizer the rIBSubscriptionOptimizer to set
 */
public void setRIBSubscriptionOptimizer(
		SubscriptionOptimizer rIBSubscriptionOptimizer) {
	RIBSubscriptionOptimizer = rIBSubscriptionOptimizer;
}


public synchronized void createSubscription(
		double subscriptionID,  //identifier internal to the ipc  should be object
		LinkedHashMap<String,Object> attributeList,
		LinkedList<String> memberList,
		double expression,
		double tolerance,
		String readorwrite ){

	// update data structures
	// generate CDAP message reply
}
/**
 * Creates a new subscription
 * @param subscription ID
 */
public synchronized void createSubscription(double subscriptionID) {

	// update data structures
	// generate CDAP message reply
}
/**
 * delete and existing subscription
 * @param subscription ID
 */
public synchronized void deleteSubscription(double subscriptionID) {
	// update data structures
	// generate CDAP message reply

}
/**allocate
 * read a RIB Subscription
 * @param subscription ID
 * @return queried RIB
 * 
 */
public synchronized RIBImpl readSubscription(double subscriptionID) {
	RIBImpl queriedRIB = null;

	//generate CDAP message reply?
	return queriedRIB;
}


private synchronized int generateSubID()
{
	int subID = -1;

	subID = (int)( Math.random()* this.subIDRange); 

	while(this.subList.containsKey(subID))
	{
		subID = (int)( Math.random()* this.subIDRange); 
	}

	return subID;
}

private synchronized int generatePubID()
{
	int pubID = -1;

	pubID = (int)( Math.random()* this.pubIDRange); 

	while(this.pubList.containsKey(pubID))
	{
		pubID = (int)( Math.random()* this.pubIDRange); 
	}

	return pubID;
}












}//end of class
