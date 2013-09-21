package test.testSE;

import rina.config.RINAConfig;
import rina.idd.IDDProcess;

public class seIDD {
	

	public static void main(String[] args) {
		String file = "idd.properties";

		RINAConfig config = new RINAConfig(file);
		IDDProcess idd = new IDDProcess(config);
	}

}
