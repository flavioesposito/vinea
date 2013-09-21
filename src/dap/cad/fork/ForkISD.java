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
import rina.idd.IDDProcess;


/**
 * Forks Inter-Slice Directory Service (ISD):
 * ISD is a necessary components of the CADSys architecture for two main reasons: 
 * 
 * 1) CADsys is not bind to IP addresses and to its (mobility and multihoming) limitations
 * 2) The unique address is SliceID/address_withi_slice: 
 * Physical nodes can participate into embedding of multiple slices and send messages to 
 * addresses within the private address of each slice
 * @author Flavio Esposito
 * @version 1.0
 */
public class ForkISD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		String configFile = null;
		if (args.length==0) {
			configFile = "isd.properties";
		}else if(args.length==1){
			configFile = args[0];
		}else {
			System.err.println("Wrong number or arguments: ");
			printInstructions();

		}
		try{

			ForkISD idd = new ForkISD(configFile);

		}       
		catch(Exception e){
			System.err.println(e);
			printInstructions();


		}


	}

	/**
	 * Print execution instructions
	 */
	public static void printInstructions() {

		System.err.println("Usage: ");
		System.err.println("   Specify Network Management System configuration file ");
		System.err.println("   or leave blank if default 'isd.properties' is present in the same folder");
		System.err.println("Example:");
		System.err.println("   $ java -jar isd.jar IsdConfigFile.properties");
		System.err.println("or $ ant ISD");

	}



	public ForkISD(String file) {


		RINAConfig config = new RINAConfig(file);
		new IDDProcess(config); //the listening thread starts by itself


	} 




}

