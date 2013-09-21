/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.rib.api;



import java.util.LinkedHashMap;
import java.util.LinkedList;

import rina.rib.api.*;


/**
 * Resource Information Base (RIB) API
 * Note that there is no writeSubscription. We delete and create a new one
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 * this version does not implement those interfaces yet.
 *
 */
public interface RIBDaemonAPI {

	public int  createSub(int frequency, String attribute);
	
	public int  createSub(int frequency, String attribute, String publisher);
	
	public void deleteSub(int subID);
	
	public int  createPub(int frequency, String attribute);
	public void deletePub(int pubID);
	
	public Object readSub(int subID);
	public void writePub(int pubID,Object msg);
	
	

	/////////////////////////////////////////////////////////////////////
	public void createSubscription(double subscriptionID,  
			LinkedHashMap<String,Object> attributeList,
			LinkedList<String> memberList,
			double expression,
			double tolerance,
			String readorwrite
	);

	public void deleteSubscription(double subscriptionID);

	public RIBAPI readSubscription(double subscriptionID);




}
