package test.CPUUsage;

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

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;

public class client {

	public static void main(String args[])
	{
		String file = "testCPU_ipc2.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl ipc2 = new IPCProcessImpl(config);

		ipc2.start();

		String IDDName = config.getIDDName();

		IPCProcessImpl client = new IPCProcessImpl("client", IDDName, true);

		client.addUnderlyingIPC(ipc2);
		
		client.testClient("server1");
	

	}

}
