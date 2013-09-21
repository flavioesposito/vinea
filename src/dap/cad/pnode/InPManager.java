/**
 * 
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * 
 */
package dap.cad.pnode;


import rina.config.RINAConfig;
import rina.dap.Application;
import rina.ipcProcess.impl.IPCProcessImpl;

/**
 * does authentication for DIF0
 *
 */
public class InPManager extends Application{

	public InPManager(String appName, String IDDName) {
		super(appName, IDDName);
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String file = "sliceManager.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl pnodeIPC = new IPCProcessImpl(config);
		
  

     	
  //  	Pnode pnode = new Pnode("pnode", IDDName);
		
     //	pnode.addUnderlyingIPC(pnodeIPC);
		
   //  	pnode.start();
     


		
	}

}
