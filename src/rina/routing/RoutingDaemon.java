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
 *
 */
package rina.routing;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import message.DTPMessage;
import rina.cdap.impl.googleprotobuf.CDAP;
import rina.cdap.impl.googleprotobuf.CDAP.CDAPMessage;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.ipcProcess.impl.TCPFlowManager;
import rina.ipcProcess.util.MessageQueue;
import rina.irm.IRM;
import rina.rib.impl.RIBDaemonImpl;
import rina.rib.impl.RIBImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class RoutingDaemon extends Thread{

	private String IPCName = null;

	private RIBDaemonImpl RIBdaemon = null;

	private RIBImpl rib = null;

	private IRM irm = null;

	private TCPFlowManager tcpManager = null;

	private final int defaultFrequency = 2; 

	private final int linkstateFrequency = 5;
	//Here is to define how frequent you want to pub, basically you can pub many different frequency.
	//Or accept all sub frequency, for now only one frequency is pubed
	//note: there is another on in the subHandler, which is also about linkstate
	//that one  is to define how frequent you want to sub



	private int appsReachableSubID = -1;

	private int neighbourSubID = -1;


	private boolean stop = false;


	private MessageQueue routingDaemonQueue = null;




	public RoutingDaemon( RIBDaemonImpl RIBdaemon, TCPFlowManager tcpManager )
	{

		this.RIBdaemon = RIBdaemon;
		this.rib = this.RIBdaemon.localRIB;
		this.IPCName = this.rib.getAttribute("ipcName").toString();
		this.tcpManager = tcpManager;
		this.routingDaemonQueue = (MessageQueue)this.rib.getAttribute("routingDaemonQueue");


	}



	public RoutingDaemon( RIBDaemonImpl RIBdaemon, IRM irm )
	{
		this.RIBdaemon = RIBdaemon;
		this.rib = this.RIBdaemon.localRIB;
		this.IPCName = this.rib.getAttribute("ipcName").toString();
		this.irm = irm;
		this.routingDaemonQueue = (MessageQueue)this.rib.getAttribute("routingDaemonQueue");

	}



	public void run()
	{


		this.initPubs();
		this.initSubs();


		//wait the bootstrap of the upper DIF
		this.sleep(20);

		if(this.tcpManager!=null) //no routing at DIF zero for now
		{
			return; 
		}
		this.rib.RIBlog.infoLog("Routing Daemon starts ("+this.IPCName + ")");


		while(!stop)
		{

			this.sleep(this.linkstateFrequency);
			this.checkFailedNeighbour();
			this.buildRoutingMap();
			this.buildForwrdingTable();
			this.resetProbeState();

		}



	}

	private void initPubs() {

		if(this.tcpManager!=null) //no routing at DIF zero for now
		{
			return; 
		}

		this.RIBdaemon.createPub(this.linkstateFrequency, "linkState"); // this will be subscribed by neighbour


	}




	private void initSubs() {

		if(this.rib.isEnrollmentFlag() == false)
		{
			this.appsReachableSubID = this.RIBdaemon.createSub(this.defaultFrequency, "appsReachable");
			this.rib.addAttribute("appsReachableSubID", this.appsReachableSubID);
		}

		this.neighbourSubID = this.RIBdaemon.createSub(this.defaultFrequency, "neighbour");
		this.rib.addAttribute("neighbourSubID", this.neighbourSubID);


	}


	private final int inf = 999999;
	private void checkFailedNeighbour()
	{

		//first put the cost to itself 0
		RoutingEntry reit = new RoutingEntry(System.currentTimeMillis(),this.IPCName,this.IPCName, 0);
		this.rib.getNeighbourCost().put(this.IPCName,reit);


		Object[] neighbour =  this.rib.getProbeReplyFlag().keySet().toArray();

		for(int i = 0; i< neighbour.length; i ++)
		{
			if(this.rib.getProbeReplyFlag().get((String)neighbour[i]).equals(Boolean.FALSE) )//no reply received, link is down
			{
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^no message received from " + (String)neighbour[i]);
				RoutingEntry re = new RoutingEntry(System.currentTimeMillis(),this.IPCName,neighbour[i].toString(), inf);
				this.rib.getNeighbourCost().put(neighbour[i].toString(),re);

			}else
			{
				System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ message received from " + (String)neighbour[i]);
			}
		}

	}

	private void resetProbeState()
	{
		Object[] neighbour =  this.rib.getProbeReplyFlag().keySet().toArray();

		for(int i = 0; i< neighbour.length; i ++)
		{
			this.rib.getProbeReplyFlag().put((String)neighbour[i], Boolean.FALSE);		
		}

	}

	public void buildRoutingMap() {
		//		
		//	    /**
		//	     * this is the raw data received from its neighbor about the connectivity info of the who map
		//	     * and it will be used to build the map
		//	     */
		//	   private LinkedHashMap<String, LinkedHashMap <String, RoutingEntry> > allRoutingEntry = null;

		this.rib.getAllRoutingEntry().put(this.IPCName,this.rib.getNeighbourCost());

		LinkedHashMap<String, LinkedHashMap <String, RoutingEntry> > allRoutingEntry = this.rib.getAllRoutingEntry();


		Object[] hosts = allRoutingEntry.keySet().toArray();


		int n =  allRoutingEntry.keySet().size();

		System.out.println(" allRoutingEntry.keySet().size() " + n );

		for(int i =0; i<n;i++)
		{
			System.out.println(" allRoutingEntry.keySet() " + hosts[i].toString() );

		}



		for(int i = 0; i < allRoutingEntry.size(); i++)
		{
			//System.out.println("hosts[i] " + hosts[i].toString());

			LinkedHashMap <String, RoutingEntry>  neighbourCost = allRoutingEntry.get(hosts[i].toString());

			Object[] tempHost = neighbourCost.keySet().toArray();

			LinkedHashMap <String, Integer> tempCost = new LinkedHashMap <String, Integer>();

			if(neighbourCost.size() == 0)
			{
				//System.out.println("I was here");
				break;
			}


			for(int j = 0; j < neighbourCost.size(); j++)
			{
				if(tempHost[j].toString().equals(hosts[i]))
				{
					//	tempCost.put(tempHost[j].toString(), Integer.toString(0) );

					continue;
				}

				RoutingEntry tempRT = neighbourCost.get(tempHost[j].toString());

				//FIXME: here the cost is cast to an integet to fit the existing routing method Dijkstra
				//System.out.println("hosts[i] " + hosts[i].toString());
				//	System.out.println("tempHost[j].toString() " + tempHost[j].toString());

				//	System.out.println("tempRT.getCost() " + tempRT.getCost());

				tempCost.put(tempHost[j].toString(), (int)tempRT.getCost()  );
			}

			this.rib.getMap().put(hosts[i].toString(), tempCost);



		}

		LinkedHashMap map = this.rib.getMap();
		Object keySet[] = map.keySet().toArray();

		this.rib.RIBlog.infoLog("Routing Daemon ("+this.IPCName + " )$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ size of map is " + map.size());
		for(int i = 0; i< map.size();i++)
		{
			this.rib.RIBlog.infoLog("Routing Daemon ("+this.IPCName + " )$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ (String)keySet[i] " + (String)keySet[i]);
			this.rib.RIBlog.infoLog("Routing Daemon ("+this.IPCName + " )$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ map.get( (String)keySet[i])  " + ( LinkedHashMap<String, Integer> )map.get( (String)keySet[i]) );
		}


		this.rib.RIBlog.infoLog("Routing Daemon ("+this.IPCName + ") : buildRoutingMap() done ");

	}

	/**
	 * build the forwarding table of the application
	 */
	public void buildForwrdingTable()
	{

		this.rib.setForwardingTable( Dijkstra.buildForwardingTable(this.rib.getMap(), this.IPCName) );

		this.rib.RIBlog.infoLog("Routing Daemon ("+this.IPCName + ") : buildForwrdingTable() done ");

		this.rib.RIBlog.infoLog("Routing Daemon ("+this.IPCName + ") : forwarding table is " +  this.rib.getForwardingTable());

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
