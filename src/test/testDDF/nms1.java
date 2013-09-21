package test.testDDF;

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;

public class nms1 {
	
	public static void main(String args[])
	{

		String file = "recursive_dif_formation_nms1.properties";

		RINAConfig config = new RINAConfig(file);
		
		IPCProcessImpl nms1 = new IPCProcessImpl(config);


	}

}
