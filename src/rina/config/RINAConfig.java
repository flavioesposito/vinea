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

package rina.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import rina.rib.impl.Logger;




/**
 * Read or set the configuration parameters common to every IPC
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 *
 */
public class RINAConfig {

	/**
	 * configuration file
	 */
	private String configFile = null;
	/**
	 * auxiliar property object
	 */
	private Properties rinaProperties = null;

	/**
	 * auxiliar input stream to read from configuration file
	 */
	private InputStream inputStream = null;
	/**
	 * logger
	 */
	private Logger log = null;


	/**
	 * 
	 * @param configFile
	 */
	public RINAConfig(String configFile){

		this.log = new Logger();
		this.configFile = configFile;
		this.loadRinaProperties();

	}



	/**
	 *  Reads and loads properties from the "rina.properties" file
	 */
	public void loadRinaProperties() {

		this.rinaProperties = new Properties();
		try{
			InputStream inputStream = new FileInputStream(this.configFile);
			this.rinaProperties.load(inputStream);

			this.log.infoLog("RINAConfig: configuration file: "+this.configFile+" loaded");

		}catch(IOException e){
			e.printStackTrace();
		}
		finally {
			if( null != inputStream ) 
				try { 
					inputStream.close(); 
				} catch( IOException e ) 
				{ 
					e.printStackTrace();
				}
		}

	}


	public String getIPCName()
	{
		String IPCName = (String) this.rinaProperties.getProperty("rina.ipc.name");
		this.log.infoLog("RINAConfig: IPCName: "+ IPCName ) ;
		return IPCName;
	}

	public int getTCPPort()
	{
		int TCPPort = Integer.parseInt((String) this.rinaProperties.getProperty("TCPPort"));
		this.log.infoLog("RINAConfig: TCPPort: "+TCPPort );
		return TCPPort;

	}


	public int getDNSPort()
	{

		int DNSPort = Integer.parseInt((String) this.rinaProperties.getProperty("rina.dns.port"));
		this.log.infoLog("RINAConfig: DNSPort: "+ DNSPort );
		return DNSPort;

	}


	public String getDNSName()
	{
		String DNSName = (String) this.rinaProperties.getProperty("rina.dns.name");
		this.log.infoLog("RINAConfig: DNSName: "+ DNSName ) ;
		return DNSName;
	}



	public int getIDDPort()
	{
		int IDD_PORT = Integer.parseInt((String) this.rinaProperties.getProperty("rina.idd.localPort"));
		this.log.infoLog("RINAConfig: IDD local port is: "+IDD_PORT );
		return IDD_PORT;
	}

	/**
	 * 
	 * @return IDD local port
	 */
	public int getIDDDataPort()
	{
		int IDD_PORT = Integer.parseInt((String) this.rinaProperties.getProperty("rina.idd.dataPort"));
		this.log.infoLog("RINAConfig: IDD data port is: "+IDD_PORT );
		return IDD_PORT;
	}

	/**
	 * get IDD Name
	 */
	public String getIDDName()
	{

		String IDD_NAME = this.rinaProperties.getProperty("rina.idd.name");
		this.log.infoLog("RINAConfig: IDD name is: "+IDD_NAME);
		return IDD_NAME;

	}


	public String getUserName()
	{

		String userName = this.rinaProperties.getProperty("rina.ipc.userName");
		this.log.infoLog("RINAConfig: User name is: "+userName);
		return userName;

	}

	public String getPassWord()
	{

		String passWord = this.rinaProperties.getProperty("rina.ipc.passWord");
		this.log.infoLog("RINAConfig: Pass word is: "+passWord);
		return passWord;

	}

	public String getDIFName()
	{

		String difName = this.rinaProperties.getProperty("rina.dif.name");
		this.log.infoLog("RINAConfig: The name of the DIF that is going to join  is: "+ difName);
		return difName;

	}

	public String getNeighbour(int i)
	{
		String neighbour = this.rinaProperties.getProperty("neighbour." + i);
		if(neighbour != null )
		{
			this.log.infoLog("RINAConfig: name of neihbour " + i +" is " + neighbour);
		}
		return neighbour;

	}


	/**
	 * set a new property 
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value){
		//TODO: store the properties in the RIB
		this.rinaProperties.setProperty(key, value);
	}
	/**
	 * get a property from the configuration file
	 * @param key
	 * @return value
	 */
	public  String getProperty(String key){

		return this.rinaProperties.get(key).toString();

	}

	/**
	 * @return the configFile
	 */
	public synchronized String getConfigFile() {
		return configFile;
	}

	/**
	 * @param configFile the configFile to set
	 */
	public synchronized void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/**
	 * @return the rinaProperties
	 */
	public synchronized Properties getRinaProperties() {
		return rinaProperties;
	}

	/**
	 * @param rinaProperties the rinaProperties to set
	 */
	public synchronized void setRinaProperties(Properties rinaProperties) {
		this.rinaProperties = rinaProperties;
	}



	public boolean enrollmentFlag() {
		
		String flag = this.rinaProperties.getProperty("rina.enrollment.flag");
		
		boolean flag_B = false;
		
		if(flag != null)
		{
			flag_B = Boolean.parseBoolean(flag);
			
		}
	
		
		return flag_B;
		
	
		
	}

}
