/**
 * 
 */
package test.testSE;

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;
import vinea.pnode.*;

/**
 * 
 * @author flavio
 *
 */
public class StartPnode3 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String file = "pnode3.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl pnodeIPC = new IPCProcessImpl(config);

		String IDDName = config.getIDDName();


		Pnode pnode3 = new Pnode("pnode3", IDDName,file);

		pnode3.addUnderlyingIPC(pnodeIPC);
		
		pnode3.start();
		
		



	
	}
	 
}
