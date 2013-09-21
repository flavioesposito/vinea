package test.testEnrollment;

import rina.config.RINAConfig;
import rina.dns.DNSProcess;

public class DNSServer {
	
	public static void main(String[] args) 
	{

		String file = "dns.properties";	
		RINAConfig config = new RINAConfig(file);
		
		DNSProcess dns = new DNSProcess(config);
		dns.start();
	}


}
