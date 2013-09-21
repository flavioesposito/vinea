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

package rina.flowAllocator.impl.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import rina.flow.*;
import rina.flow0.TCPFlow;


/**
 * FlowAllocted stores all flow allocated for the upper dif ipc.
 * Since flow allocator is to alloate flow for upper dif ipc
 * Note: srcPortID is used as the handle ID, since it is unique
 *
 */
public class FlowAllocated {


	/***
	 * upper DIF IPCs using this IPC  to port ID mapping 
	 */
	private LinkedHashMap<Integer, String> portToIPC = null;


	/**
	 * this will store <srcIPCName, <dstIPCName, portID> > 
	 */
	private LinkedHashMap<String, LinkedHashMap<String, LinkedList<Integer > > > ipcToIPCToPortID = null;


	/**
	 *  this will store <HandID, flow>
	 */
	private LinkedHashMap<Integer, Flow> flowAllocated = null ;


	private double portRange = 10000;



	public FlowAllocated()
	{
		this.flowAllocated = new LinkedHashMap<Integer, Flow>();

		this.portToIPC = new  LinkedHashMap< Integer, String>();

		this.ipcToIPCToPortID = new  LinkedHashMap<String, LinkedHashMap<String, LinkedList<Integer> > >();

	}


	public synchronized boolean containFlow(String srcIPCName, String dstIPCName)
	{
		if(this.ipcToIPCToPortID.containsKey(srcIPCName))
		{
			return this.ipcToIPCToPortID.get(srcIPCName).containsKey(dstIPCName);
		}
		return false;
	}


	//here only return the first port that can be used
	public synchronized int getPortID(String srcIPCName, String dstIPCName)
	{
		int portID = this.ipcToIPCToPortID.get(srcIPCName).get(dstIPCName).get(0);
	
		return portID;
	}
	
	//here only return the first flow that can be used
	public synchronized Flow getFlow(String srcIPCName, String dstIPCName )
	{
		int portID = this.getPortID(srcIPCName, dstIPCName);
		
		return this.flowAllocated.get(portID);
	}


	public synchronized int addNewApp(String appName) {
		int wellKnownPort = this.generatePortId();
		
		this.portToIPC.put(wellKnownPort, appName);
		
		return wellKnownPort;
	}

	public synchronized void addFlow(Flow flow)
	{
		int portID = this.generatePortId();

		this.portToIPC.put( portID, flow.getSrcIPCName());

		if(!this.ipcToIPCToPortID.containsKey(flow.getSrcIPCName()))
		{
			this.ipcToIPCToPortID.put(flow.getSrcIPCName(), new LinkedHashMap<String, LinkedList<Integer > >());
		}


		flow.setSrcPort(portID);

		if(!this.ipcToIPCToPortID.get(flow.getSrcIPCName()).containsKey(flow.getDstIPCName()))
		{
			this.ipcToIPCToPortID.get(flow.getSrcIPCName()).put(flow.getDstIPCName(), new LinkedList<Integer>());
		}
		
		this.ipcToIPCToPortID.get(flow.getSrcIPCName()).get(flow.getDstIPCName()).add(new Integer(portID));

		this.flowAllocated.put(portID, flow);

	}

	

	public  synchronized void removeFlow(int handleID)
	{

		this.ipcToIPCToPortID.get(this.flowAllocated.get(handleID).getSrcIPCName())
		.get(this.flowAllocated.get(handleID).getDstIPCName()).remove(new Integer(handleID));

		this.portToIPC.remove(handleID);

		this.flowAllocated.remove(handleID);
	}
	
	public synchronized Flow getFlow(int portID)
	{
		return this.flowAllocated.get(portID);
	}

	public synchronized int generatePortId()
	{
		int port = -1;

		port = (int)( Math.random()* portRange); 

		while(this.portToIPC.containsKey(port) )
		{
			port = (int)( Math.random()* portRange); 
		}

		return port;
	}

	
	public synchronized String getIPCName(int portID)
	{
		return this.portToIPC.get(portID);
	}
	
	/**
	 * @return the portToIPC
	 */
	public synchronized LinkedHashMap<Integer, String> getPortToIPC() {
		return portToIPC;
	}


	/**
	 * @param portToIPC the portToIPC to set
	 */
	public synchronized void setPortToIPC(LinkedHashMap<Integer, String> portToIPC) {
		this.portToIPC = portToIPC;
	}






}
