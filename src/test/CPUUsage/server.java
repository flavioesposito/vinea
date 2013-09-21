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

public class server {

	public static void main(String args[])
	{
		String file = "testCPU_ipc1.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl ipc1 = new IPCProcessImpl(config);

		ipc1.start();

		String IDDName = config.getIDDName();
		
		IPCProcessImpl server1 = new IPCProcessImpl("server1", IDDName, true);
	

		server1.addUnderlyingIPC(ipc1);

		while(true)
		{
			System.out.println("server starts to work ...........................");
			byte[] msg = server1.getCdapMsgQueue().getReceive();
			handleMsg(msg);
		}

	}

	private static void handleMsg(byte[] msg) {
		
		System.out.println("server starts to calculate888888888888888888888888888888888888 ...........................");
		double result = 1;
		int loopSize = 100000;
		
	   for(int count =0; count < loopSize; count++)
		{
			for(int i =1 ;  i<500 ; i++)
			{
				result*=i;
			}
		}

	}

}
