package test.testEnrollment;

import rina.config.RINAConfig;
import rina.idd.IDDProcess;

public class IDD {


	public static void main(String[] args) {
		String file = "testEnrollment.idd.properties";

		RINAConfig config = new RINAConfig(file);
		IDDProcess IDD = new IDDProcess(config);
	}

}



