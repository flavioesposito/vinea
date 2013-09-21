/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * 
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */
package rina.routing;

import java.io.Serializable;

public class RoutingEntry implements Serializable {
	

	private String src = null;
	private String dst = null;
	private long timeStamp = 0;
	private double cost = 0.0;

	public RoutingEntry(long timeStamp, String src, String dst, double cost){
		this.timeStamp = timeStamp;
		this.src = src;
		this.dst = dst;
		this.cost = cost;
	}
	

	
	/**
	 * @return the src
	 */
	public synchronized String getSrc() {
		return src;
	}

	/**
	 * @param src the src to set
	 */
	public synchronized void setSrc(String src) {
		this.src = src;
	}

	/**
	 * @return the dst
	 */
	public synchronized String getDst() {
		return dst;
	}

	/**
	 * @param dst the dst to set
	 */
	public synchronized void setDst(String dst) {
		this.dst = dst;
	}

	/**
	 * @return the timeStamp
	 */
	public synchronized long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public synchronized void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the cost
	 */
	public synchronized double getCost() {
		return cost;
	}

	/**
	 * @param cost the cost to set
	 */
	public synchronized void setCost(double cost) {
		this.cost = cost;
	}




}
