/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */

/**
 * Subscription Optimizer 
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 */

package rina.rib.impl;


public class SubscriptionOptimizer {
	
	/**
	 * Identifier internal to the process that uniquely identifies this subscription
	 */
	private double subscriptionID = -1.0;
	/**
	 * <Read/Write> indicates whether this subscription is to get information from other
	 * members or to retrieve information from other members.
	 */
	public enum WriteOrRead {
	    WRITE, READ 
	}
	/**
	 * Initialization of the subscription 
	 */
	private WriteOrRead write_or_read = null;//WriteOrRead.READ or WriteOrRead.WRITE; 
	
	/**
	 * Specifies what is the action of the subscription in the OIB/RIB 
	 */
	public enum NotifyOrRecord{
	    NOTIFY, RECORD, BOTH
	}
	/**
	 * Initialization of the subscription 
	 */
	private NotifyOrRecord notify_or_record = null;
	/**
	 * Number of millisecond after which the update is sent 
	 * Expression that defines when this subscription is to be invoked. 
	 */
	private double relationExpression = -1.0;
	/**
	 * time period that provides an indication of the tolerance that it be performed precisely 
	 * on the relation expression. (This may allow the Daemon to optimize requests that would occur �near� each other.)
	 */
	private double tolerance = 0.0;
	/**
	 * The information to be read or written 
	 */
	

	
	
	/**
	 *  Constructor
	 */
	public SubscriptionOptimizer() {
		// TODO Auto-generated constructor stub
	}

}
