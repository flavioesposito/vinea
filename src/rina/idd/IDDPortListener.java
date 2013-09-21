package rina.idd;

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

import rina.flow0.TCPFlow;
import rina.rib.impl.RIBDaemonImpl;

public class IDDPortListener extends Thread{

	/**
	 * Resource Information Base Deamon
	 */
	private RIBDaemonImpl RIBdaemon = null;

	private int port;

	private boolean LISTENING = true;

	public IDDPortListener(int port,RIBDaemonImpl RIBdaemon)
	{
		this.port = port;
		this.RIBdaemon = RIBdaemon;
	}

	public void run(){
		TCPFlow listeningFlow = new TCPFlow(this.port); 

		while(LISTENING)
		{
			try {
				TCPFlow clientFlow = listeningFlow.accept();
				//new IDDHandler(clientFlow, IDDDatabaseDIFName, IDDDatabaseServiceName).start();.
				
				new IDDHandler(clientFlow, this.RIBdaemon).start();
				
				
//FIXME: note IDDHandlerData is used to handle message from non-zero DIF message in the preious verison
//FIXME
				
				
//				if(this.dataPortFlag == false)
//				{
//					new IDDHandler(clientFlow, this.RIBdaemon).start();
//				}else
//				{
//					new IDDHandlerData(clientFlow, this.RIBdaemon).start();
//				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}//end of while

	}

}
