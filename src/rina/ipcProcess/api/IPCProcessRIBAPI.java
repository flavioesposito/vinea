/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

/**
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 */
package rina.ipcProcess.api;

public interface IPCProcessRIBAPI {
	
	public int  createSub(String appName,int frequency, String attribute);
	
	public void deleteSub(String appName,int subID);
	
	
	public int  createPub(String appName,String attribute);
	
	public void deletePub(String appName,int pubID);
	
	
	public Object readSub(String appName,int subID);
	
	public void writePub(String appName,int pubID,Object msg);

}
