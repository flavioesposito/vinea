package rina.dns;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.google.protobuf.InvalidProtocolBufferException;

import rina.cdap.impl.googleprotobuf.CDAP;
import rina.dns.DNS.DNSRecord;
import rina.flow0.TCPFlow;

import rina.rib.impl.Logger;

public class DNSHandler extends Thread{

	private LinkedHashMap<String, DNS.DNSRecord> dataBase = null;

	private TCPFlow flow;

	private boolean active = true;

	private Logger logger = null;

	private byte[] msg ;

	public DNSHandler(TCPFlow flow,LinkedHashMap<String, DNS.DNSRecord> dataBase, Logger logger)
	{
		this.flow = flow;
		this.dataBase = dataBase;
		this.logger = logger;
	}

	public void run()
	{

		try {
			while(active){
				msg = flow.receive();
				handleReceiveMessage(msg);
			}
		}catch (Exception e) {
			//e.printStackTrace();
		}
		finally{
			if(flow!=null){
				this.logger.infoLog("DNS Handler: connection closed");
				flow.close();

			}
		}
	}

	private void handleReceiveMessage(byte[] msg) {

		DNS.DNSRecord dnsMessage = null;
		try {
			dnsMessage = DNS.DNSRecord.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		String ip = this.flow.getSocket().getInetAddress().toString();

		ip = ip.substring(1);

		this.logger.infoLog("DNSHandler: DNS message received from: "
				+ dnsMessage.getName() +  ". ip is " +  ip);

		switch(dnsMessage.getOpCode()){


		case QUERY:

			this.logger.infoLog("DNSHandler: opCode is QUERY");
			handle_QUERY(dnsMessage);

			break;

		case REG:
			this.logger.infoLog("DNSHandler: opCode is REG");
			handle_REG(dnsMessage, ip);

			break;

		default:

			this.logger.infoLog("DNSHandler: opCode undefined");
			break;
		}


	}

	private void handle_REG(DNSRecord dnsMessage, String ip) {

		DNS.DNSRecord dnsRecord = null;

		if(dnsMessage.getPort() != 0)
		{
			dnsRecord = DNSMessage.generateDNS_REP(dnsMessage.getName(), ip, dnsMessage.getPort());

		}else
		{

			dnsRecord = DNSMessage.generateDNS_REP
			(dnsMessage.getName(), ip, dnsMessage.getControlPort(), dnsMessage.getDataPort());

		}

		this.logger.infoLog("DNSHandler: Record: Name is " + dnsMessage.getName() );
		this.logger.infoLog("DNSHandler: Record: IP is " + ip);
		this.logger.infoLog("DNSHandler: Record: Port is " + dnsMessage.getPort());
	//	this.logger.infoLog("DNSHandler: Record: Control port is " + dnsMessage.getControlPort());
	//	this.logger.infoLog("DNSHandler: Record: Data port is " + dnsMessage.getDataPort());

		this.dataBase.put(dnsMessage.getName(), dnsRecord);

	}

	private void handle_QUERY(DNSRecord dnsMessage) {

		DNS.DNSRecord  dnsRecord = this.dataBase.get(dnsMessage.getName());


		if(dnsRecord == null)
		{
			this.logger.infoLog("DNSHandler: No record found about " + dnsMessage.getName());
			dnsRecord = DNSMessage.generateDNS_REP(dnsMessage.getName(), " ", -1);// no record found
		}
		
		try {

			this.flow.send(dnsRecord.toByteArray());
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
