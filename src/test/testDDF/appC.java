package test.testDDF;

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;

public class appC {

	public static void main(String args[])
	{

		String file = "recursive_dif_formation_ipc4.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl ipc4 = new IPCProcessImpl(config);



		String IDDName = config.getIDDName();

		IPCProcessImpl appC = new IPCProcessImpl("appC", IDDName);

		appC.addUnderlyingIPC(ipc4);
		
		appC.start();


	}

}
