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

/**
 * Forks Domain Name Service (DNS) 
 * note that we must have a file called dns.properties 
 * 
 * @author Flavio Esposito
 * @version 1.0
 */
public class ForkDNS {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String configFile = null;
		if (args.length==0) {
			configFile = "dns.properties";
		}else if(args.length==1){
			configFile = args[0];
		}else {
			System.err.println("Wrong number or arguments: ");
			printInstructions();
			
		}
		try{
			RINAConfig config = new RINAConfig(configFile);
			DNSProcess dns = new DNSProcess(config);
			dns.start();		
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
		System.err.println("   Specify dns configuration file ");
		System.err.println("   or leave blank if default 'dns.properties' is present in the same folder");
		System.err.println("Example:");
		System.err.println("   $ java -jar ForkDNS.jar dnsConfigFile.properties");
		System.err.println("or $ ant DNS");
		
	}
}


