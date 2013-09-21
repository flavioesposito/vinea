package test.testDDF;

import java.util.LinkedList;

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;

public class appB {
	
	public static void main(String args[])
	{

		String file2 = "recursive_dif_formation_ipc2.properties";

		RINAConfig config2 = new RINAConfig(file2);

		IPCProcessImpl ipc2 = new IPCProcessImpl(config2);


		
		String file3 = "recursive_dif_formation_ipc3.properties";

		RINAConfig config3 = new RINAConfig(file3);

		IPCProcessImpl ipc3 = new IPCProcessImpl(config3);


		String IDDName = config2.getIDDName();
		
		LinkedList<IPCProcessImpl> underlyingIPCList  = new LinkedList<IPCProcessImpl>();
		
		underlyingIPCList.add(ipc2);
		underlyingIPCList.add(ipc3);
		
		
		IPCProcessImpl appB = new IPCProcessImpl("appB", IDDName);
		
		appB.addUnderlyingIPC(ipc2);
		appB.addUnderlyingIPC(ipc3);
		
		appB.start();
		
		appB.registerToIDD("relayService", "appC");
		
		appB.initEnrollmentComponent("relayService + appC");
		
		
		
		

	
	}

}
