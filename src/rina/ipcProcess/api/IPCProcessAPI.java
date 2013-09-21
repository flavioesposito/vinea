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


public interface IPCProcessAPI {


	public int allocateFlow(String srcName, String dstName);

	public void deallocate(int portID);

	public void send(int portID,  byte[] msg) throws Exception;

	public byte[] receive(int portID);
	
	


}
