/**
 * 
 */
package dap.cad.fork;

import rina.rib.impl.Logger;

import java.util.regex.Pattern;

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;
import dap.cad.pnode.Pnode;

/**
 * Forks a physical node 
 * @author Flavio Esposito
 * @version 1.0
 */
public class ForkPnode {

	Logger log = null;
	/**
	 * constructor
	 * @param config file of the pnode
	 */
	public ForkPnode(String file) {

		this.log = new Logger();
		String pnodeName = null;
		if(file==null) {
			pnodeName = "pnode";
		}else {
			String[] separate = file.split(Pattern.quote("."));
			pnodeName = separate[0];
		}
		log.infoLog("Forking Pnode "+pnodeName+ "...");

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl pnodeIPC = new IPCProcessImpl(config);

		String IDDName = config.getIDDName();

		Pnode pnode = new Pnode(pnodeName, IDDName,file);

		pnode.addUnderlyingIPC(pnodeIPC);

		pnode.start();
		


	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		String configFile = null;
		if (args.length==0) {
			configFile = "pnode.properties";
		}else if(args.length==1){
			configFile = args[0];
		}else {
			System.err.println("Wrong number or arguments!");
			printInstructions();

		}
		try{

			 new ForkPnode(configFile);

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
		System.err.println("   or leave blank if default 'pnode.properties' is present in the same folder");
		System.err.println("Example:");
		System.err.println("   $ java -jar ForkPnode.jar pnode.properties");
		System.err.println("or $ ant pnode");

	}

	
	
	

}
