/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.fork;

import rina.config.RINAConfig;
import rina.dns.DNSProcess;
import rina.ipcProcess.impl.IPCProcessImpl;

/**
 * Forks Infrastructure Provider (InP) Manager
 * This component of the CADSys architecture 
 * authenticates new InPs (or in w.l.o.g. physical nodes) that wants to participate to the embedding
 * @author Flavio Esposito
 *
 */
public class ForkInPManager {

	
	public static void main(String[] args) {

		String configFile = null;
		if (args.length==0) {
			configFile = "nms.properties";
		}else if(args.length==1){
			configFile = args[0];
		}else {
			System.err.println("Wrong number or arguments: ");
			printInstructions();
			
		}
			
			ForkInPManager nms = new ForkInPManager(configFile);

	}
	
	/**
	 * Print execution instructions
	 */
	public static void printInstructions() {
		
		System.err.println("Usage: ");
		System.err.println("   Specify Network Management System configuration file ");
		System.err.println("   or leave blank if default 'nms.properties' is present in the same folder");
		System.err.println("Example:");
		System.err.println("   $ java -jar nms.jar nmsConfigFile.properties");
		System.err.println("or $ ant NMS");
		
	}


	
	public ForkInPManager(String file) {
		

		RINAConfig config = new RINAConfig(file);
		
		//IPCProcessImpl InP_nms1 =
				new IPCProcessImpl(config);

	} 

	
}
