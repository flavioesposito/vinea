/**
 * 
 */
package test.testSE;

import rina.config.RINAConfig;
import rina.dap.Application;
import rina.ipcProcess.impl.IPCProcessImpl;


public class PnodeExample extends Application{

	public PnodeExample(String appName, String IDDName) {
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

     	
     	PnodeExample pnode = new PnodeExample("pnode", IDDName);
		
     	pnode.addUnderlyingIPC(pnodeIPC);
	
    	pnode.start();
     

		
	}

}
