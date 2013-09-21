/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

/**
 * This is a component of the IPC Process that responds to allocation Requests from Application Processes
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */

package rina.ipcProcess.impl;

import java.util.LinkedHashMap;

import message.DTPMessage;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.config.RINAConfig;
import rina.dns.DNS;
import rina.dns.DNSMessage;
import rina.dtp.impl.googleprotobuf.DTP;
import rina.flow.Flow;
import rina.flow0.TCPFlow;
import rina.flowAllocator.impl.util.FlowAllocated;
import rina.flowAllocator.impl.util.IncomingCom;
import rina.flowAllocator.impl.util.OutgoingComHandler;
import rina.ipcProcess.util.MessageQueue;
import rina.rib.impl.RIBImpl;

public class TCPFlowManager {

	private String IPCName;

	private String DIFName;

	private int TCPPort;

	private RIBImpl rib = null;

	public RINAConfig config = null;

	private TCPFlow listeningTCPFlow = null;

	/*
	 * thread dedicated to incoming TCP connections 
	 */
	private IncomingCom incomingTCPConnection = null; 

	private MessageQueue msgQueue = null;

	private LinkedHashMap<String, TCPFlow> tcpFlowAllocated = null;


	/**
	 * the following four is related to DNS 
	 */
	private String DNSName;

	private int DNSPort;

	private TCPFlow dnsFlow = null;

	private LinkedHashMap<String, DNS.DNSRecord> dataBase = null;



	public TCPFlowManager(RIBImpl rib, MessageQueue msgQueue)

	{
		this.tcpFlowAllocated = new  LinkedHashMap<String, TCPFlow>();


		this.msgQueue = msgQueue;

		this.rib = rib;

		this.config = (RINAConfig)this.rib.getAttribute("config");


		this.IPCName =  this.rib.getAttribute("ipcName").toString();

		if( this.rib.getAttribute("difName") != null)
		{
			this.DIFName = this.rib.getAttribute("difName").toString();
		}


		this.DNSName = this.config.getDNSName();

		this.DNSPort = this.config.getDNSPort();

		this.dataBase = new LinkedHashMap<String, DNS.DNSRecord>();

		//create TCP listening  thread 
		this.TCPPort = this.config.getTCPPort();

		this.listeningTCPFlow = new TCPFlow(TCPPort);

		this.incomingTCPConnection = new IncomingCom(this.listeningTCPFlow, this.msgQueue, this.tcpFlowAllocated);

		this.incomingTCPConnection.start();


		this.registerToDNS();
	}


	public synchronized  TCPFlow allocateTCPFlow(String IPCName)
	{

		TCPFlow tcpFlow = null;

		if(this.tcpFlowAllocated.containsKey(IPCName))
		{
			tcpFlow = this.tcpFlowAllocated.get(IPCName);
			this.rib.RIBlog.infoLog("TCPFlowManager: flow allocated before.");
			return tcpFlow;
		}

		if(!this.dataBase.containsKey(IPCName))
		{
			this.queryDNS(IPCName);
		}


		DNS.DNSRecord dr = this.dataBase.get(IPCName);


		String ip = dr.getIp();
		int tcpPort = dr.getPort();

		if(ip.equals(" "))
		{
			this.rib.RIBlog.errorLog("TCPFlowManager: " + IPCName + " is not found on DNS Server" );
			return null;
		}


		tcpFlow = new TCPFlow(ip, tcpPort);
		tcpFlow.setSrcName(this.IPCName);//this.IPCName is the name of host IPC which contains this TCP Manager
		tcpFlow.setDstName(IPCName); // IPCName is a local variable only in this method


		//should get a message back from the other side about his IPC Name
		// but for now we omit this part FIXME

		DTP.DTPMessage dtpMsg =  DTPMessage.generateM_DTP(IPCName, this.IPCName);

		try {
			tcpFlow.send(dtpMsg.toByteArray());

		} catch (Exception e) {
			// TODO Auto-generated catch block

			//remove this flow from flowAllocated
			tcpFlow.close();
			e.printStackTrace();

			this.rib.RIBlog.errorLog("TCPFlowManager: TCP Flow to " +  this.IPCName + " failed");

			return null;
		}

		this.tcpFlowAllocated.put(IPCName, tcpFlow);

		this.rib.RIBlog.infoLog("TCPFlowManager: New TCP Flow is added: " + IPCName);


		//attach a handler thread for this flow to receive msg and put it in the msgQueue
		try{

			new OutgoingComHandler(this.msgQueue, tcpFlow).start();			

		}catch(Exception e)
		{

			this.rib.RIBlog.errorLog(e.getMessage());
		}

		return tcpFlow;


	}

	public synchronized TCPFlow getTCPFlow(String IPCName)
	{
		if(this.tcpFlowAllocated.containsKey(IPCName))
		{
			return this.tcpFlowAllocated.get(IPCName);
		}
		return null;

	}

	public synchronized boolean deallocateTCPFlow(String IPCName)
	{
		if ( this.tcpFlowAllocated.containsKey(IPCName) )
		{
			this.tcpFlowAllocated.get(IPCName).close();
			this.tcpFlowAllocated.remove(IPCName);
			return true;
		}
		return false;
	}

	public void registerToDNS()
	{

		dnsFlow = new TCPFlow(this.DNSName, this.DNSPort);

		if(dnsFlow.getSocket() == null)
		{
			this.rib.RIBlog.infoLog("TCPFlowManager: Registration to DNS failed");
			return;
		}

		DNS.DNSRecord register = DNSMessage.generateDNS_REG(this.IPCName,this.TCPPort);

		try {
			dnsFlow.send(register.toByteArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.rib.RIBlog.infoLog("TCPFlowManager: Registration to DNS finished");

	}

	public void queryDNS(String IPCName)

	{
		DNS.DNSRecord query = DNSMessage.generateDNS_QUERY(IPCName);

		try {
			dnsFlow.send(query.toByteArray());
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		byte[] reply = null;
		try {
			reply = dnsFlow.receive();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		DNS.DNSRecord dnsMessage = null;
		try {
			dnsMessage = DNS.DNSRecord.parseFrom(reply);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		this.dataBase.put(IPCName, dnsMessage);

		this.rib.RIBlog.infoLog("TCPFlowManager: DNS Query of " + IPCName +  " finished");

	}


	/**
	 * here is a hack of the ipc.send in the previous version FIXME or CHANGEME
	 * @param IPCName
	 * @param message
	 * @throws Exception
	 */
	public void send(String IPCName, byte[] message) 
	{
		TCPFlow tcpFlow = this.allocateTCPFlow(IPCName);

		try {
			tcpFlow.send(message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
