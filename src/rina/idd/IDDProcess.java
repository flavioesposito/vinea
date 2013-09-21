/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */

/**
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */

package rina.idd;


import rina.config.RINAConfig;
import rina.dns.DNS;
import rina.dns.DNSMessage;
import rina.flow0.TCPFlow;
import rina.ipcProcess.util.MessageQueue;
import rina.rib.impl.RIBDaemonImpl;
import rina.rib.impl.RIBImpl;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * One instance of the IDD process listens for connections
 * in this version the IDD is centralized but we can have multiple instances of it
 *
 */
public class IDDProcess implements  Runnable {


	/**
	 * config file
	 */
	public RINAConfig config = null;
	
	private RIBImpl rib = null;

	//////////////////////////////////////////////////////////////////

	/**
	 * Resource Information Base Deamon
	 */
	private RIBDaemonImpl RIBdaemon = null;
	/**
	 * An external process can set this flag to false to terminate IDDprocess
	 */
	private boolean IDD_SERVER_LISTENING = true;
	
	
	private String IPCName = null;
	private  int TCPPort ;
	
	
	
	
	private String DNSName = null;
	
	private int DNSPort = -1;
	
	private TCPFlow dnsFlow = null;


	/**
	 * ServiceURL is the primary key
	 */
	private LinkedHashMap<String,LinkedList<IDD.iddEntry.Builder>> IDDDatabaseServiceName =null;
	/**
	 *  DIF_NAME is the primary key
	 */
	private LinkedHashMap<String, IDD.iddEntry.Builder> IDDDatabaseDIFName =null;



	public IDDProcess(RINAConfig config){

		this.config = config;
		
		this.IPCName = this.config.getIPCName();
		this.TCPPort = this.config.getTCPPort();
		
		this.DNSName = this.config.getDNSName();
		this.DNSPort = this.config.getDNSPort();

		this.rib = new RIBImpl();
		this.rib.addAttribute("ipcName", "idd");
	
		MessageQueue  ribDaemonQueue = new MessageQueue ();
		this.rib.addAttribute("ribDaemonQueue", ribDaemonQueue);
		this.RIBdaemon = new RIBDaemonImpl(this.rib);
	
		this.rib.addAttribute("config", this.config);
		
		this.IDDDatabaseDIFName = new LinkedHashMap<String, IDD.iddEntry.Builder>();
		this.IDDDatabaseServiceName = new LinkedHashMap<String, LinkedList<IDD.iddEntry.Builder>>();

		new Thread(this).start();	
	}


	/**
	 * IDD thread listening process
	 */
	public void run() {

		this.registerToDNS();

		
		this.rib.RIBlog.infoLog("IDDProcess: started");


		LinkedHashMap<String, Object> IDDAttributeList = null;

		//initialize the local RIB with the database whose primary key is DIFname
		if(IDDAttributeList ==null)
			IDDAttributeList = RIBdaemon.localRIB.getAttributeList();

		IDDAttributeList.put("IDDDatabaseDIFName", IDDDatabaseDIFName);

		RIBdaemon.localRIB.writeAttributeList(IDDAttributeList);
		//RIBdaemon.localRIB.RIBlog.infoLog("IDDProcess: IDD IDDDatabaseDIFName written on Attribute List");




		//update the local RIB with the database whose primary key is Application (or Service) name
		if(IDDAttributeList ==null)
			IDDAttributeList = RIBdaemon.localRIB.getAttributeList();

		IDDAttributeList.put("IDDDatabaseServiceName", IDDDatabaseServiceName); 
		RIBdaemon.localRIB.writeAttributeList(IDDAttributeList);
		//RIBdaemon.localRIB.RIBlog.infoLog("IDDProcess: IDD IDDDatabaseServiceName written on Attribute List");





		//Create a thread to listen to the data port

		IDDPortListener dataPortListerner = new IDDPortListener(this.TCPPort,this.RIBdaemon);
		dataPortListerner.start();
		RIBdaemon.localRIB.RIBlog.infoLog("IDDProcess:  port listener Started");





	}//end of run


	public void registerToDNS()
	{

		this.dnsFlow = new TCPFlow(this.DNSName, this.DNSPort);

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

		this.rib.RIBlog.infoLog("IDD TCPFlowManager: Registration to DNS finished");

	}


}



