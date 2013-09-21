package test.testEnrollment;

import rina.config.RINAConfig;
import rina.ipcProcess.impl.IPCProcessImpl;

public class enrollment_ipc1_enroll {
	

	public static void main(String args[])
	{
		
		/**
		 * this IPC does enrollment
		 */
		String file = "testEnrollment.ipc1.properties";

		RINAConfig config = new RINAConfig(file);

		IPCProcessImpl ipc1 = new IPCProcessImpl(config);

		ipc1.start();
	}

}
