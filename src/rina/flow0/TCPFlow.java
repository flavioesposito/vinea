/**
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

package rina.flow0;

import java.net.*;
import java.util.*;
import java.io.*;

import message.DTPMessage;
import rina.dtp.impl.googleprotobuf.DTP;

/**
 * TCP flow 
 * @author Yuefeng Wang and Flavio Esposito  . Computer Science Department, Boston University
 * @version 1.0
 */
public class TCPFlow {

	/**
	 * source address
	 */
	private byte[] addr;
	/**
	 * Internet address
	 */
	private InetAddress inetAddr;
	/**
	 * destination port
	 */
	private int dstport;
	/**
	 * destination address
	 */
	private byte[] dnsAddr;
	/**
	 * TCP socket
	 */
	private Socket socket;
	/**
	 * TCP server socket
	 */
	private ServerSocket serverSocket;
	/**
	 * local port 
	 */
	private int localPort;
	/**
	 * URL of the machine to be resolved into an IP address
	 */
	private String url;


	/**
	 * this is src and dst of this TCP flow
	 */
	private String srcName;
	private String dstName;


	/**
	 * Dummy Constructor
	 */
	public TCPFlow(){}





	/**
	 * Constructor
	 * create a local tcp flow listening to  a certain port 
	 * @param local Port
	 */
	public TCPFlow(int localPort)
	{
		this.localPort = localPort;
		try{
			serverSocket = new ServerSocket(this.localPort);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Constructor
	 * @param url
	 * @param port
	 */
	public TCPFlow(String url, int port)
	{
		this.url = url;
		this.dstport = port;

		try{
			inetAddr = InetAddress.getByName(this.url);
			socket = new Socket(inetAddr, this.dstport);
		}catch(Exception e)
		{e.printStackTrace(); 
		}
	}



	/**
	 * send a message byte[] from the TCP socket
	 * @param message in byte[] 
	 * @throws Exception 
	 */
	public synchronized void send( byte[] message) throws Exception
	{   
		//	try{   
		OutputStream  out =  socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		int len =  message.length;

		if(len <= Short.MAX_VALUE)
		{	
			//send length first
			dos.writeShort(len);
			//dos.writeByte(len);
			//send message
			dos.write(message, 0, len);

		}else
		{
			System.out.println("Error: Message too large, fragment it first");
		}
		//			}catch(Exception e){
		//				e.printStackTrace();
		//				System.out.println("Exception: " +e);
		//	
		//			}


	}

	public synchronized void sendDTPMsg( byte[] dtpMsg) throws Exception
	{   

		DTP.DTPMessage payload = DTPMessage.generatePayloadM_DTP(dtpMsg);
		
		DTP.DTPMessage DTPMsg = DTPMessage.generateM_DTP(this.dstName, this.srcName, payload.toByteArray());

		byte[] msgToSend = DTPMsg.toByteArray();

		OutputStream  out =  socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		int len =  msgToSend.length;

		if(len <= Short.MAX_VALUE)
		{	
			//send length first
			dos.writeShort(len);
			//send message
			dos.write(msgToSend, 0, len);

		}else
		{
			System.out.println("Error: Message too large, fragment it first");
		}

	}

	public synchronized void sendCDAPMsg( byte[] cdapMsg) throws Exception
	{   

		DTP.DTPMessage payload = DTPMessage.generatePayloadM_CDAP(cdapMsg);
		
		DTP.DTPMessage DTPMsg = DTPMessage.generateM_CDAP(this.dstName, this.srcName, payload.toByteArray());

		byte[] msgToSend = DTPMsg.toByteArray();

		OutputStream  out =  socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		int len =  msgToSend.length;

		if(len <= Short.MAX_VALUE)
		{	
			//send length first
			dos.writeShort(len);
			//send message
			dos.write(msgToSend, 0, len);

		}else
		{
			System.out.println("Error: Message too large, fragment it first");
		}

	}



	/**
	 * receive a message from the tcp socket 
	 * @return the array of byte received over the TCP socket
	 * @throws Exception 
	 * @throws Exception 
	 */
	public  byte[] receive() throws Exception 
	{   
		byte[] data = null;
		//			try{
		InputStream in = socket.getInputStream();
		DataInputStream dis = new DataInputStream(in);
		//int len = 0;
		//while(len == 0) {len = in.available();} 
		int length = 0 ;
		length = dis.readShort();
		//length = dis.readByte();

		data  = new byte[length];
		dis.readFully(data, 0 , length);
		//			}catch(Exception e)
		//			{
		//				e.printStackTrace();
		//			}

		return data;
	}

	/**
	 * accept an incoming client socket
	 * @return a listening TCP flow
	 */
	public TCPFlow accept() {

		TCPFlow ListeningTcpFlow = new TCPFlow();
		try {
			ListeningTcpFlow.socket = serverSocket.accept();
		} catch (IOException e) {
			System.out.println("Exception: " +e);
			e.printStackTrace();
		}
		return ListeningTcpFlow;
	}

	public void close() {
		if(serverSocket != null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Exception: " + e);
				e.printStackTrace();
			}
		}
		if(socket != null){
			try {
				socket .close();
			} catch (IOException e) {
				System.out.println("Exception: " + e);
				e.printStackTrace();
			}
		}
	}


	/**
	 * 
	 * @return destination port
	 */
	public int getDstport() {
		return dstport;
	}
	/**
	 * 
	 * @param destination port
	 */
	public void setDstport(int dstport) {
		this.dstport = dstport;
	}
	/**
	 * 
	 * @param localPort
	 */
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	/**
	 * getUrl
	 * @return url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * setUrl
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * getAddr
	 * @return addr
	 */
	public byte[] getAddr() {
		return addr;
	}
	/**
	 * setAddr
	 * @param addr
	 */
	public void setAddr(byte[] addr) {
		this.addr = addr;
	}
	/**
	 * getInetAddr
	 * @return inetAddr
	 */
	public InetAddress getInetAddr() {
		return inetAddr;
	}
	/**
	 * setInetAddr
	 * @param inetAddr
	 */
	public void setInetAddr(InetAddress inetAddr) {
		this.inetAddr = inetAddr;
	}
	/**
	 * getPort
	 * @return port
	 */
	public int getDstPort() {
		return dstport;
	}
	/**
	 * setPort
	 * @param port
	 */
	public void setDstPort(int port) {
		this.dstport = port;
	}
	/**
	 * getDnsAddr
	 * @return dnsAddr
	 */
	public byte[] getDnsAddr() {
		return dnsAddr;
	}
	/**
	 * setDnsAddr
	 * @param dnsAddr
	 */
	public void setDnsAddr(byte[] dnsAddr) {
		this.dnsAddr = dnsAddr;
	}
	/**
	 * getSocket
	 * @return socket
	 */
	public Socket getSocket() {
		return socket;
	}
	/**
	 * setSocket
	 * @param socket
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	/**
	 * getServerSocket
	 * @return serverSocket
	 */
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	/**
	 * setServerSocket
	 * @param ServerSocket serverSocket
	 */
	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	/**
	 * getlocalPort
	 * @return  localPort
	 */
	public int getLocalPort() {
		return localPort;	
	}
	/**
	 * setlocalPort
	 * @param localPort
	 */
	public void setlocalPort(int localPort) {
		this.localPort = localPort;
	}


	/**
	 * @return the srcName
	 */
	public synchronized String getSrcName() {
		return srcName;
	}





	/**
	 * @param srcName the srcName to set
	 */
	public synchronized void setSrcName(String srcName) {
		this.srcName = srcName;
	}





	/**
	 * @return the dstName
	 */
	public synchronized String getDstName() {
		return dstName;
	}





	/**
	 * @param dstName the dstName to set
	 */
	public synchronized void setDstName(String dstName) {
		this.dstName = dstName;
	}



} //end of class
