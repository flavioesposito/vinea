package test.testDDF;


import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;

public class appA {
	
	public static void main(String args[])
	{

		String file = "recursive_dif_formation_ipc1.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl ipc1 = new IPCProcessImpl(config);

		String IDDName = config.getIDDName();

		IPCProcessImpl appA = new IPCProcessImpl("appA", IDDName);

		appA.addUnderlyingIPC(ipc1);
		
		appA.start();

		appA.getIrm().allocateFlow("appA", "appC");
		
		


	}

}
