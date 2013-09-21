/**
 * 
 */
package test.testSE;

import rina.config.RINAConfig;
import rina.dap.Application;
import rina.ipcProcess.impl.IPCProcessImpl;


public class PnodeTest extends Application{

	public PnodeTest(String appName, String IDDName) {
		super(appName, IDDName);
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String file = "pnode.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl pnodeIPC = new IPCProcessImpl(config);
		
     	String IDDName = config.getIDDName();

     	
    	PnodeTest pnode = new PnodeTest("pnode", IDDName);
		
     	pnode.addUnderlyingIPC(pnodeIPC);
	
    	pnode.start();
     

		
	}

}
