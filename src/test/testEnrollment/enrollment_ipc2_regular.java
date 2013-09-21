package test.testEnrollment;

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;

public class enrollment_ipc2_regular {
	
	public static void main(String args[])
	{
		String file = "testEnrollment.ipc2.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl ipc2 = new IPCProcessImpl(config);

		ipc2.start();
	}

}
