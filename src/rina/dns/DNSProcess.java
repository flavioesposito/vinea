package rina.dns;

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
 * @author Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */


import java.util.LinkedHashMap;

import rina.config.RINAConfig;
import rina.flow0.TCPFlow;
import rina.rib.impl.Logger;

public class DNSProcess  extends Thread{


	private LinkedHashMap<String, DNS.DNSRecord> dataBase = null;
	
	private Logger logger = null;

	private String DNSName;

	private String DNSIP;

	private int DNSPort;
	
	private RINAConfig config;

	private boolean running = true;
	
	private TCPFlow listeningFlow = null;

	public DNSProcess(RINAConfig config)
	{
		this.dataBase = new LinkedHashMap<String, DNS.DNSRecord>();
		this.config = config;
		this.DNSPort = Integer.parseInt(this.config.getProperty("rina.dns.port"));
		this.DNSName = this.config.getProperty("rina.dns.name");
		this.logger = new Logger();
		
	}


	public DNSProcess(String DNSName, String DNSIP, int DNSPort)
	{
		this.dataBase = new LinkedHashMap<String, DNS.DNSRecord>();
		this.DNSName = DNSName;
		this.DNSIP = DNSIP;
		this.DNSPort = DNSPort;
		this.logger = new Logger();
	}

	public void stopDNS()
	{
		
		this.running = false;
	//	listeningFlow.close();
		listeningFlow = null;
	}
	
	public void run()
	{
		
		this.logger.infoLog("DNS Process started.");

		listeningFlow = new TCPFlow(this.DNSPort); 

		
		while(running)
		{
			try {
				TCPFlow clientFlow = listeningFlow.accept();	
				
				this.logger.infoLog("DNS Process: new request received from "
						+ clientFlow.getSocket().getInetAddress() + " : " 
						+ clientFlow.getSocket().getPort());
				
				new DNSHandler(clientFlow, this.dataBase, this.logger).start();

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1); //without this line it loops forever with the error (for example if the port is already in use)
			}
		}
	}


}
