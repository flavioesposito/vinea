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

import java.util.LinkedList;

import message.DTPMessage;
import rina.cdap.impl.googleprotobuf.CDAP;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.ipcProcess.impl.TCPFlowManager;
import rina.irm.IRM;
import rina.rib.impl.RIBImpl;

public class ProbeNeighbour extends Thread{

	private String IPCName = null;
	private RIBImpl rib = null;
	private IRM irm = null;
	private TCPFlowManager tcpManager = null;
	private boolean stop = false;
	private final int probeFrequency = 5;
	private final int inf = 999999;



	public ProbeNeighbour(RIBImpl rib, IRM irm)
	{
		this.rib = rib;
		this.irm = irm;
		this.IPCName = this.rib.getAttribute("ipcName").toString();
	}

	public ProbeNeighbour(RIBImpl rib, TCPFlowManager tcpManager)
	{
		this.rib = rib;
		this.tcpManager = tcpManager;
		this.IPCName = this.rib.getAttribute("ipcName").toString();
	}



	public void run()
	{
		this.rib.RIBlog.infoLog("Routing Daemon[ ProbeNeighbour ] starts ("+this.IPCName + ")");


		//For now we assume DIF 0 are fully connected, and don't probe at DIF 0
		if(this.tcpManager != null)
		{
			return;
		}

		while(!stop)
		{

			this.probe();

			this.sleep(this.probeFrequency);

			this.checkFailedNeighbour();	

		}
	}




	private void probe()
	{
		LinkedList<String> neighbour = this.rib.getNeighbour();

		CDAP.objVal_t.Builder  currentTime = CDAP.objVal_t.newBuilder();
		currentTime.setSint64Val(System.currentTimeMillis());



		for(int i =0; i< neighbour.size();i++)
		{
			System.out.println("========================================== neighbour.get(i) is " + neighbour.get(i));
			CDAP.CDAPMessage M_READ_msg = message.CDAPMessage.generateM_READ
			(       "routingDaemon",
					"probe",		
					currentTime.buildPartial(),
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


			try {

				int handleID = this.irm.getHandle(neighbour.get(i));
				if( handleID ==- 1)
				{
					handleID = this.irm.allocateFlow(this.IPCName, neighbour.get(i));
				}

				DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(M_READ_msg.toByteArray());
				this.irm.send(handleID, payload.toByteArray());
				this.rib.getProbeReplyFlag().put(neighbour.get(i), Boolean.FALSE);
				this.rib.RIBlog.infoLog("Routing Daemon[ ProbeNeighbour ]("+this.IPCName + ") : M_READ(probe) to " + neighbour.get(i) + " sent");


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * check Failed Neighbor
	 */
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
				RoutingEntry re = new RoutingEntry(System.currentTimeMillis(),this.IPCName,neighbour[i].toString(), inf);
				this.rib.getNeighbourCost().put(neighbour[i].toString(),re);

			}else
			{
				//this.rib.getNeighbour().add((String)neighbour[i]);
			}
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
