/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

package rina.irm;	

public class HandleEntry
{
	public String appName;
	public String ipcName;
	public int portID;

	public HandleEntry(String appName,String ipcName, int portID)
	{
		this.appName = appName;
		this.ipcName = ipcName;
		this.portID = portID;
	}
}