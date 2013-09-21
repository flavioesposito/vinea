package rina.rib.util;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import message.DTPMessage;

import com.google.protobuf.ByteString;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.ipcProcess.impl.TCPFlowManager;
import rina.irm.IRM;
import rina.rib.impl.RIBImpl;


public class PubHandler extends Thread {


	private RIBImpl rib = null;

	private IRM irm = null;

	private TCPFlowManager tcpManager = null;

	private String IPCName = null;

	private LinkedList<String> subscriberList = null;

	private Event event = null;

	private boolean stop = false;

	private int frequency;

	private String pubName = null;




	public PubHandler(RIBImpl rib, IRM irm, Event event)
	{
		this.rib = rib;
		this.irm = irm;
		this.event = event;
		this.frequency = this.event.getFrenquency();
		this.pubName = this.event.getName();
		this.IPCName = this.rib.getAttribute("ipcName").toString();
		this.subscriberList =  new LinkedList<String> ();
	}

	public PubHandler(RIBImpl rib, TCPFlowManager tcpManager, Event event)
	{
		this.rib = rib;
		this.tcpManager = tcpManager;
		this.event = event;
		this.frequency = this.event.getFrenquency();
		this.pubName = this.event.getName();
		this.IPCName = this.rib.getAttribute("ipcName").toString();
		this.subscriberList = new LinkedList<String> ();
	}

	public void run()
	{
		this.rib.RIBlog.infoLog("PubHandler: pub event: " + this.pubName + " created, and frequency is " + this.event.getFrenquency()  ); 


		while(!stop)
		{
			this.updateEvent();

			this.updateSubscriber(event);

			this.sleep(this.frequency);
		}
	}

	private synchronized void updateEvent() {



		if(this.pubName.equals("neighbour"))
		{


		}else if(this.pubName.equals("appsReachable"))
		{	

			this.event.setAttribute(this.rib.getAttribute("appsReachable"));

		}else if(this.pubName.equals("linkState"))
		{

			this.event.setAttribute(this.rib.getNeighbourCost());
			
			
		}else if(this.pubName.equals("appStatus"))
		{

			this.event.setAttribute(this.rib.getAppStatus());
		}
	}

	private synchronized void updateSubscriber(Event event) 
	{
		for(int i =0; i<this.subscriberList.size();i++)
		{
			String currentMember = this.subscriberList.get(i);

			ByteArrayOutputStream   bos   =   new   ByteArrayOutputStream(); 
			ObjectOutputStream oos;
			try {
				oos = new   ObjectOutputStream(bos);
				oos.writeObject(this.event.getAttribute());   
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}   
			byte[]   bs   =   bos.toByteArray(); 

			CDAP.objVal_t.Builder  objValue = CDAP.objVal_t.newBuilder();
			objValue.setByteval(ByteString.copyFrom(bs));
			objValue.setStrval(this.event.getName());
			objValue.setIntval(this.event.getFrenquency());

			CDAP.CDAPMessage M_READ_R_msg = message.CDAPMessage.generateM_READ_R(
					0,
					"PubSub",//object class
					this.IPCName,
					objValue.buildPartial(),
					currentMember,
					currentMember,
					currentMember,
					currentMember,
					1,
					this.IPCName, 
					this.IPCName, 
					this.IPCName, 
					this.IPCName
			);
			byte[] M_READ_R  = M_READ_R_msg.toByteArray();		


			try {

				if(this.tcpManager != null)
				{
					this.tcpManager.getTCPFlow(currentMember).sendCDAPMsg(M_READ_R);

				}else
				{
					DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_R);

					this.irm.send(this.irm.getHandle(currentMember),payload.toByteArray());
				}

				this.rib.RIBlog.infoLog("PubHandler:  publish  " + this.event.getName() + " with frequency "+ this.event.getFrenquency() +" to "  + currentMember  ); 

			} catch (Exception e) {
				// TODO Auto-generated catch block
				this.rib.RIBlog.errorLog("PubHandler:  publish failed  " + this.event.getName() + " with frequency "+ this.event.getFrenquency() +" to "  + currentMember   ); 

				//e.printStackTrace();
			}

		}

	}

	public  synchronized void  addSubscriber(String subscriberName)
	{
		if(!this.subscriberList.contains(subscriberName))
		{
			this.subscriberList.add(subscriberName);
		}
	}

	public  synchronized void  removeSubscriber(String subscriberName)
	{
		if(this.subscriberList.contains(subscriberName))
		{
			this.subscriberList.remove(subscriberName);
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
