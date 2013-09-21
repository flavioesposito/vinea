/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.sp;

import java.util.LinkedList;

/**
 * Data structure to keep track of the pending slice properties
 * @author flavioesposito
 *
 */
public class SliceProperties {
	
	/**
	 * time at which the request was sent
	 */
	private double requestedTime = 0.0;
	/**
	 * time at which the first embedding response arrived
	 */
	private double embedTime = 0.0;
	/**
	 * sliceID
	 */
	private int sliceID = -1;
	/**
	 * in case we want to use strings and not int for ID
	 */
	private String sliceName = null; 
	/**
	 * keep track of who was given the request 
	 */
	private LinkedList<String> trustedInP = null;
	
	/**
	 * dummy  
	 */
	public SliceProperties() {
		
	}
/**
 * 
 * @param requestedTime
 * @param sliceID
 */
public SliceProperties(double requestedTime, int sliceID) {
		this.requestedTime = requestedTime;
		this.sliceID = sliceID;
	}
/**
 * @return the requestedTime
 */
public double getRequestedTime() {
	return requestedTime;
}
/**
 * @param requestedTime the requestedTime to set
 */
public void setRequestedTime(double requestedTime) {
	this.requestedTime = requestedTime;
}
/**
 * @return the sliceID
 */
public int getSliceID() {
	return sliceID;
}
/**
 * @param sliceID the sliceID to set
 */
public void setSliceID(int sliceID) {
	this.sliceID = sliceID;
}
/**
 * @return the sliceName
 */
public String getSliceName() {
	return sliceName;
}
/**
 * @param sliceName the sliceName to set
 */
public void setSliceName(String sliceName) {
	this.sliceName = sliceName;
}
/**
 * @return the embedTime
 */
public double getEmbedTime() {
	return embedTime;
}
/**
 * @param embedTime the embedTime to set
 */
public void setEmbedTime(double embedTime) {
	this.embedTime = embedTime;
}
/**
 * @return the trustedInP
 */
public LinkedList<String> getTrustedInP() {
	return trustedInP;
}
/**
 * @param trustedInP the trustedInP to set
 */
public void setTrustedInP(LinkedList<String> trustedInP) {
	this.trustedInP = trustedInP;
}


}

