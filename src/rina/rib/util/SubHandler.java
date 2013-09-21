package rina.rib.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import message.DTPMessage;
import rina.cdap.impl.googleprotobuf.CDAP;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow0.TCPFlow;
import rina.ipcProcess.impl.IPCProcessImpl;
import rina.ipcProcess.impl.TCPFlowManager;
import rina.irm.IRM;
import rina.rib.impl.RIBDaemonImpl;
import rina.rib.impl.RIBImpl;
import rina.rib.impl.SubscriptionOptimizer;


public class SubHandler extends Thread {

	private RIBImpl rib = null;

	private IRM irm = null;

	private TCPFlowManager tcpManager = null;

	private String IPCName = null;

	private String NMSName = null;

	private Event event = null;

	private boolean stop = false;

	private int frequency;

	private String subIdentifier = null;

	private final int linkstateFrequency = 5; 
	//Here is to define how frequent you want to sub
	//note: there is another on in the routingDaemon, which is also about linkstate
	//that one is to define how frequent you want to pub, basically you can pub many different frequency.
	//Or accept all sub frequency, for now only one frequency is pubed




	public SubHandler(RIBImpl rib, IRM irm, Event event)
	{
		this.rib = rib;
		this.irm = irm;
		this.event = event;
		this.frequency = this.event.getFrenquency();
		this.IPCName = this.rib.getAttribute("ipcName").toString();


		if(event.getPublisher() ==null)
		{
			this.subIdentifier = this.event.getName() + "#"+ this.event.getFrenquency();
		}else
		{
			this.subIdentifier = this.event.getPublisher() + "%" + this.event.getName() + "#"+ this.event.getFrenquency();
		}
	}

	public SubHandler(RIBImpl rib, TCPFlowManager tcpManager, Event event)
	{
		this.rib = rib;
		this.tcpManager = tcpManager;
		this.event = event;
		this.frequency = this.event.getFrenquency();
		this.IPCName = this.rib.getAttribute("ipcName").toString();

		if(event.getPublisher() ==null)
		{
			this.subIdentifier = this.event.getName() + "#"+ this.event.getFrenquency();
		}else
		{
			this.subIdentifier = this.event.getPublisher() + "%" + this.event.getName() + "#"+ this.event.getFrenquency();
		}
	}


	public void run()
	{
		handleEvent(event);
	}

	private void handleEvent(Event event) {

		String subName = event.getName();

		if(subName.equals("neighbour"))// IPC sub to its rib daemon to figure out who is the Direct neighbor
		{
			this.getNeighbour();

		}else if(subName.equals("appsReachable"))// IPC sub to NMS to know which app is reachable in this DIF
		{

			this.getAppsReachable();

		}else if (subName.equals("linkState"))
		{ 

			this.getLinkState(this.event.getPublisher());

		}else if (subName.equals("appStatus")) // NMS sub to the status of each app using ipc in this DIF 
		{

			this.getGeneralSub();

		}	



	}




	private void getGeneralSub() {

		String publisher = this.event.getPublisher();
		CDAP.objVal_t.Builder value = CDAP.objVal_t.newBuilder();
		value.setStrval(this.event.getName());
		value.setIntval(this.event.getFrenquency());

		CDAP.CDAPMessage M_READ_msg = message.CDAPMessage.generateM_READ
		(      "PubSub",
				this.IPCName,		
				value.buildPartial(), 
				publisher,//dst
				publisher,
				publisher,
				publisher,
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
				TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(publisher);
				NMSFlow.sendCDAPMsg(M_READ_msg.toByteArray());
			}else
			{
				int handleID = this.irm.getHandle(publisher);

				if( handleID == -1)
				{ 
					handleID = this.irm.allocateFlow(this.IPCName, publisher);
				}

				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_msg.toByteArray());
				this.irm.send(handleID, payload.toByteArray());
			}

			//log

			this.rib.RIBlog.infoLog("SubHandler("+this.IPCName + "): " +" M_READ(PubSub/"+ this.event.getName() +") to "+ publisher +" sent");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	private void  getNeighbour() {

		LinkedList<String> oldNeihbourList = new LinkedList<String>();

		while(!stop==true)
		{

			if(this.tcpManager!=null)
			{
				LinkedList<String> memberList = this.rib.getMemberList();

				for(int i=0;i< memberList.size(); i++)
				{
					if(!memberList.get(i).equals(this.IPCName))
					{
						this.rib.getNeighbour().add(memberList.get(i));
					}
				}

			}else
			{

				LinkedHashMap<String, IPCProcessImpl> underlyingIPCs = this.irm.getUnderlyingIPCs();

				if(underlyingIPCs == null)
				{
					this.rib.RIBlog.errorLog("SubHandler("+this.IPCName + "): cannot get neighour as this is DIF 0, and there is no undelrying IPC");
					return;
				}

				Set<String> SetCurrentMaps = underlyingIPCs.keySet();


				Iterator<String> KeyIterMaps = SetCurrentMaps.iterator();

				LinkedList<String> neighbour =  this.rib.getNeighbour();////////////////////////////////////

				LinkedList<String> difMemberlist = this.rib.getMemberList();



				this.rib.RIBlog.infoLog("SubHandler("+this.IPCName + "): " + "dif member list is " +  difMemberlist);


				while(KeyIterMaps.hasNext())
				{
					String underlyingIPCName  = KeyIterMaps.next();
					IPCProcessImpl underlyingIPC = underlyingIPCs.get(underlyingIPCName);

					System.out.println("underlyingIPCName is " + underlyingIPCName + " sssoooooooooooooooooooooooooooooooooooooooooooooooooo");


					int subID = underlyingIPC.createSub(this.IPCName, 5, "appsReachable");

					LinkedList<String> appsReachable = (LinkedList<String>)underlyingIPC.readSub(this.IPCName, subID);

					System.out.println("appsReachable of " +  underlyingIPCName  +" is " + appsReachable);

					if(appsReachable != null) // need time to get from NMS
					{
						for(int i =0; i < difMemberlist.size(); i++)
						{
							if(appsReachable.contains(difMemberlist.get(i)) && (!this.IPCName.equals(difMemberlist.get(i))) )
							{
								if(neighbour == null)
								{
									neighbour = new LinkedList<String> ();
								}

								if(!neighbour.contains(difMemberlist.get(i)))
								{
									String newNeighbour =  difMemberlist.get(i);


									neighbour.add(newNeighbour);
									this.rib.getProbeReplyFlag().put(newNeighbour, Boolean.FALSE);								
									this.rib.RIBlog.infoLog("SubHandler("+this.IPCName + "): Add one member to neighbour List: " + newNeighbour);

									// new member added to neighbour list, subscribe to its linkstate
									int linkStateSubID = this.rib.getRibDaemon().createSub(this.linkstateFrequency, "linkState", newNeighbour);

									//////////////////////////////////////////////////////////////////////////////////////////////
								}
							}
						}
					}
				}

				if(neighbour != null)
				{

					this.rib.getSubList().get(this.rib.getSubnameToID().get(this.subIdentifier)).setAttribute(neighbour);

					this.rib.setNeighbour(neighbour);


					//	System.out.println("neighbour is " + neighbour + " ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]");
				}
			}

			this.sleep(this.frequency);
		}


	}

	private void getLinkState(String neighbourName) {



		CDAP.objVal_t.Builder value = CDAP.objVal_t.newBuilder();
		value.setStrval("linkState");
		value.setIntval(this.event.getFrenquency());


		CDAP.CDAPMessage M_READ_msg = message.CDAPMessage.generateM_READ
		(      "PubSub",
				this.IPCName,		
				value.buildPartial(), 
				neighbourName,//dst
				neighbourName,
				neighbourName,
				neighbourName,
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
				TCPFlow NMSFlow  =  this.tcpManager.allocateTCPFlow(neighbourName);
				NMSFlow.sendCDAPMsg(M_READ_msg.toByteArray());
			}else
			{
				int handleID = this.irm.getHandle(neighbourName);

				if( handleID == -1)
				{ 
					handleID = this.irm.allocateFlow(this.IPCName, neighbourName);
				}

				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_msg.toByteArray());
				this.irm.send(handleID, payload.toByteArray());
			}

			//log

			this.rib.RIBlog.infoLog("SubHandler("+this.IPCName + "): " +" M_READ(PubSub/linkState) to "+ neighbourName +" sent");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	private void getAppsReachable() {



		this.NMSName = this.rib.getAttribute("nmsName").toString();


		CDAP.objVal_t.Builder value = CDAP.objVal_t.newBuilder();
		value.setStrval("appsReachable");
		value.setIntval(this.frequency);


		CDAP.CDAPMessage M_READ_msg = message.CDAPMessage.generateM_READ
		(      "PubSub",
				this.IPCName,
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

			//log

			this.rib.RIBlog.infoLog("SubHandler("+this.IPCName + "): " +" M_READ(PubSub/appsReachable) to NMS sent");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void sleep(int second)
	{
		try {
			Thread.sleep(second*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
