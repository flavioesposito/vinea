/**
 * 
 */
package test.testSE;

import java.util.LinkedList;

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;
import vinea.pnode.*;

/**
 * @author flavioesposito
 *
 */
public class StartPNode {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String file = "pnode1.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl pnodeIPC = new IPCProcessImpl(config);

		String IDDName = config.getIDDName();


		Pnode pnode1 = new Pnode("pnode1", IDDName,file);

		pnode1.addUnderlyingIPC(pnodeIPC);
		
		pnode1.start();
		
		
//		pnode1.rib.RIBlog.debugLog("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//
//		pnode1.rib.RIBlog.debugLog("StartPnode::main: pnode1.irm.getUnderlyingIPCs() = "+pnode1.irm.getUnderlyingIPCs());
//
//		//this.rib.getRibDaemon().createSub(3, "appReachable");
//		int subID = pnodeIPC.createSub(pnode1.getName(), 3, "appsReachable");
//		pnode1.rib.RIBlog.debugLog("StartPnode::main: subID: "+ subID);
//
//		
//		LinkedList<String> a=		(LinkedList<String>)pnodeIPC.readSub(pnode1.getName(), subID);
//		pnode1.rib.RIBlog.debugLog("StartPnode::main: a = "+a);
//		
//		pnode1.rib.RIBlog.debugLog("pnode1.rib.getMemberList(): "+pnode1.rib.getMemberList());


	
	}

}
