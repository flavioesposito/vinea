/**
 * 
 */
package dap.cad.fork;

import rina.config.RINAConfig;
import rina.idd.IDDProcess;
import dap.cad.sp.SliceProvider;

/**
 * Forks a Service Provider: send requests and it may also: 
 * 1) binds the final VN reserving resources
 * 2) behave as a physical node
 * 
 * @author Flavio Esposito
 * @version 1.0
 */
public class ForkServiceProvider {
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		String configFile = null;
		if (args.length==0) {
			configFile = "sp.properties";
		}else if(args.length==1){
			configFile = args[0];
		}else {
			System.err.println("Wrong number or arguments!");
			printInstructions();

		}
		try{

			ForkServiceProvider sp = new ForkServiceProvider(configFile, "idd");

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
		System.err.println("   or leave blank if default 'sp.properties' is present in the same folder");
		System.err.println("Example:");
		System.err.println("   $ java -jar ForkSP.jar sp.properties");
		System.err.println("or $ ant SP");

	}

	
	
	/**
	 * constructor
	 * @param spName
	 * @param iddName
	 */
	public ForkServiceProvider (String spName, String iddName) {
		
		SliceProvider sp = new SliceProvider(spName, iddName);
		//start it to listen for responses
		sp.start();
	}

}
