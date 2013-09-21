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


package rina.dns;

public class DNSMessage {


	public static DNS.DNSRecord generateDNS_REG(String name, int controlPort, int dataPort	)
	{
		DNS.DNSRecord.Builder  dnsMessage = DNS.DNSRecord.newBuilder();
		dnsMessage.setOpCode(DNS.opCode_t.REG);
		dnsMessage.setName(name);
		dnsMessage.setControlPort(controlPort);
		dnsMessage.setDataPort(dataPort);
		return dnsMessage.buildPartial();
	}
	
	
	public static DNS.DNSRecord generateDNS_REG(String name, int port)
	{
		DNS.DNSRecord.Builder  dnsMessage = DNS.DNSRecord.newBuilder();
		dnsMessage.setOpCode(DNS.opCode_t.REG);
		dnsMessage.setName(name);
		dnsMessage.setPort(port);
		return dnsMessage.buildPartial();
	}
	
	public static DNS.DNSRecord generateDNS_QUERY(String name)
	{
		DNS.DNSRecord.Builder  dnsMessage = DNS.DNSRecord.newBuilder();
		dnsMessage.setOpCode(DNS.opCode_t.QUERY);
		dnsMessage.setName(name);
		return dnsMessage.buildPartial();
	}
	
	public static DNS.DNSRecord generateDNS_REP(String name, String ip, int controlPort, int dataPort)
	{
		DNS.DNSRecord.Builder  dnsMessage = DNS.DNSRecord.newBuilder();
		dnsMessage.setOpCode(DNS.opCode_t.REP);
		dnsMessage.setName(name);
		dnsMessage.setIp(ip);
		dnsMessage.setControlPort(controlPort);
		dnsMessage.setDataPort(dataPort);
		
		return dnsMessage.buildPartial();
	}
	
	
	public static DNS.DNSRecord generateDNS_REP(String name, String ip, int port)
	{
		DNS.DNSRecord.Builder  dnsMessage = DNS.DNSRecord.newBuilder();
		dnsMessage.setOpCode(DNS.opCode_t.REP);
		dnsMessage.setName(name);
		dnsMessage.setIp(ip);
		dnsMessage.setPort(port);
		
		return dnsMessage.buildPartial();
	}
	
}
