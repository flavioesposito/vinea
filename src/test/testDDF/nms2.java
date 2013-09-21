package test.testDDF;

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;

public class nms2 {
	
	public static void main(String args[])
	{

		String file = "recursive_dif_formation_nms2.properties";

		RINAConfig config = new RINAConfig(file);
		
		IPCProcessImpl nms2 = new IPCProcessImpl(config);

	}

}
