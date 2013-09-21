package rina.messages;

import rina.dns.DNS;
import rina.messages.EnrollmentInformationT.enrollmentInformation_t;

/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */

/**
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */



public class EnrollmentMessage {
	
	public static enrollmentInformation_t generate(long address, String operationalStatus)
	{
		enrollmentInformation_t.Builder  enrollmentMessage = enrollmentInformation_t.newBuilder();
		enrollmentMessage.setAddress(address);
		enrollmentMessage.setOperationalStatus(operationalStatus);
		return enrollmentMessage.build();
	}
	

}
