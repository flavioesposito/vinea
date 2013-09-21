/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.rib.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;

import rina.rib.api.RIBAPI;
//import rina.routing.RoutingEntry;
import rina.rib.util.Event;
import rina.rib.util.PubHandler;
import rina.routing.RoutingEntry;

/**
 * The OIB/RIB Daemon is a key element for the DIF or DAF. For DAFs
 * (Distributed Application Facilities), this is the Object Information Base (OIB) 
 * Daemon, for DIFs (Distributed IPC Facilities), this is the Resource Information
 * Base (RIB) Daemon. The members of a DIF/DAF need to share information
 * relevant to their collaboration. Different aspects of the DAF/DIF will want
 * different information that will need to be updated with different frequency or
 * upon the occurrence of some event. The OIB/RIB Daemon provides this service
 * and optimizes the operation by combining requests where possible.
 * 
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public class RIBImpl implements RIBAPI {

	private LinkedHashMap<String, Object> attributeList = null;
	/**
	 * the members of the DAF/DIF from/to which attributes are exchanged. 
	 */
	private LinkedList<String> memberList = null;

	/**
	 * logger
	 */
	public Logger RIBlog = null;
	
	//tells if it can do enrollment, by default it cannot
	private boolean enrollmentFlag = false;
	
	private boolean DIF0Flag = false;



	////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////routing related, start/////////////////////////////////////////////////////////
	/**
	 * All the direct neighbour of this IPC
	 */
	private LinkedList<String> neighbour = null;
	private LinkedHashMap<String, Boolean> probeReplyFlag = null;


	/**
	 * RoutingEntry for all neighbours
	 * key is a neighbour, value is a routing entry
	 */
	private LinkedHashMap <String, RoutingEntry>  neighbourCost = null;

	/**
	 * this is the raw data received from its neighbor about the connectivity info of the who map
	 * and it will be used to build the map 
	 */

	private LinkedHashMap<String, LinkedHashMap <String, RoutingEntry> > allRoutingEntry = null;


	/**
	 * information about the whole DIF containing the network topology
	 * <application Name, app's neighbour cost >
	 */
	private LinkedHashMap <String,LinkedHashMap> map = null;


	/**
	 * forwarding table<destnation, netxthop>
	 */
	private LinkedHashMap <String, String> forwardingTable = null;  



	//////////////routing related, end/////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////////////////////////
	//related to pub/sub

	private LinkedHashMap<Integer, Event> subList = null;
	
	private LinkedHashMap<String, Integer> subnameToID = null;

	private LinkedHashMap<String,  LinkedList<Integer> > appSubList = null;

	private LinkedHashMap<Integer, Event> pubList = null;
	private LinkedHashMap<String, Integer> pubnameToID = null;
	
	private LinkedHashMap<Integer, PubHandler> pubIDToHandler = null;
	




	/////////////////////////////////////////////////////////////////////////////////////////

   //this store the cpu usage of the upper app(ipc)

    private LinkedHashMap<String,Double> appStatus = null;
    
    private LinkedHashMap<String, LinkedHashMap <String, Double> > multiProviderAppStatus = null;


	/////////////////////////////////////////////////////////////////////////////
    
    
    //this is a pointer to RIBDaemon, so that it can be used to do pub/sub within the IPC
    private RIBDaemonImpl ribDaemon = null;

    




	/**
	 * Constructor
	 */
	public RIBImpl()
	{
		this.attributeList = new LinkedHashMap<String, Object>();
		this.memberList =  new LinkedList<String>();
		this.RIBlog = new Logger();


		this.neighbour  = new LinkedList<String>();
		this.probeReplyFlag = new LinkedHashMap<String, Boolean>();
		this.neighbourCost = new LinkedHashMap <String, RoutingEntry>();
		this.forwardingTable = new LinkedHashMap<String,String>();
		this.map = new  LinkedHashMap <String,LinkedHashMap>();
		this.allRoutingEntry =  new LinkedHashMap<String, LinkedHashMap <String, RoutingEntry> >();


		//////////////////////////////////////////////////////////////////////////////////////////
		//related to pub/sub
		this.subList = new LinkedHashMap<Integer, Event>();
		this.pubList = new LinkedHashMap<Integer, Event>();
		this.subnameToID = new LinkedHashMap<String, Integer>();
		this.pubnameToID = new LinkedHashMap<String, Integer>();
		this.appSubList = new  LinkedHashMap<String,  LinkedList<Integer> > ();
		this.pubIDToHandler = new LinkedHashMap<Integer, PubHandler> ();
		/////////////////////////////////////////////////////////////////////////////////////////
		
		
		this.appStatus = new  LinkedHashMap<String,Double>();
		
		this.multiProviderAppStatus = new LinkedHashMap<String, LinkedHashMap <String, Double> >();
		
		

	}






	/**
	 * @return the multiProviderAppStatus
	 */
	public synchronized LinkedHashMap<String, LinkedHashMap<String, Double>> getMultiProviderAppStatus() {
		return multiProviderAppStatus;
	}




	/**
	 * @param multiProviderAppStatus the multiProviderAppStatus to set
	 */
	public synchronized void setMultiProviderAppStatus(
			LinkedHashMap<String, LinkedHashMap<String, Double>> multiProviderAppStatus) {
		this.multiProviderAppStatus = multiProviderAppStatus;
	}



	/**
	 * @return the appStatus
	 */
	public synchronized LinkedHashMap<String, Double> getAppStatus() {
		return appStatus;
	}








	/**
	 * @param appStatus the appStatus to set
	 */
	public synchronized void setAppStatus(LinkedHashMap<String, Double> appStatus) {
		this.appStatus = appStatus;
	}








	/**
	 * @return the pubIDToHandler
	 */
	public synchronized LinkedHashMap<Integer, PubHandler> getPubIDToHandler() {
		return pubIDToHandler;
	}








	/**
	 * @param pubIDToHandler the pubIDToHandler to set
	 */
	public synchronized void setPubIDToHandler(
			LinkedHashMap<Integer, PubHandler> pubIDToHandler) {
		this.pubIDToHandler = pubIDToHandler;
	}








	/**
	 * @return the probeReplyFlag
	 */
	public synchronized LinkedHashMap<String, Boolean> getProbeReplyFlag() {
		return probeReplyFlag;
	}








	/**
	 * @param probeReplyFlag the probeReplyFlag to set
	 */
	public synchronized void setProbeReplyFlag(
			LinkedHashMap<String, Boolean> probeReplyFlag) {
		this.probeReplyFlag = probeReplyFlag;
	}








	/**
	 * @return the subList
	 */
	public synchronized LinkedHashMap<Integer, Event> getSubList() {
		return subList;
	}








	/**
	 * @param subList the subList to set
	 */
	public synchronized void setSubList(LinkedHashMap<Integer, Event> subList) {
		this.subList = subList;
	}








	/**
	 * @return the subnameToID
	 */
	public synchronized LinkedHashMap<String, Integer> getSubnameToID() {
		return subnameToID;
	}








	/**
	 * @param subnameToID the subnameToID to set
	 */
	public synchronized void setSubnameToID(
			LinkedHashMap<String, Integer> subnameToID) {
		this.subnameToID = subnameToID;
	}








	/**
	 * @return the appSubList
	 */
	public synchronized LinkedHashMap<String, LinkedList<Integer>> getAppSubList() {
		return appSubList;
	}








	/**
	 * @param appSubList the appSubList to set
	 */
	public synchronized void setAppSubList(
			LinkedHashMap<String, LinkedList<Integer>> appSubList) {
		this.appSubList = appSubList;
	}








	/**
	 * @return the pubList
	 */
	public synchronized LinkedHashMap<Integer, Event> getPubList() {
		return pubList;
	}








	/**
	 * @param pubList the pubList to set
	 */
	public synchronized void setPubList(LinkedHashMap<Integer, Event> pubList) {
		this.pubList = pubList;
	}








	/**
	 * @return the pubnameToID
	 */
	public synchronized LinkedHashMap<String, Integer> getPubnameToID() {
		return pubnameToID;
	}








	/**
	 * @param pubnameToID the pubnameToID to set
	 */
	public synchronized void setPubnameToID(
			LinkedHashMap<String, Integer> pubnameToID) {
		this.pubnameToID = pubnameToID;
	}








	/**
	 * @return the attributeList
	 */
	public synchronized LinkedHashMap getAttributeList() {
		return attributeList;
	}
	/**
	 * @param attributeList the attributeList to set
	 */
	public synchronized void setAttributeList(LinkedHashMap attributeList) {
		this.attributeList = attributeList;
	}
	/**
	 * @return the memberList
	 */
	public synchronized LinkedList<String> getMemberList() {
		return memberList;
	}
	/**
	 * @param memberList the memberList to set
	 */
	public synchronized void setMemberList(LinkedList<String> memberList) {
		this.memberList = memberList;
	}


	/** localRIB.getMemberList()
	 * @param index of member to return
	 * @return element of the member list	
	 */
	public synchronized String readElement(int index) {
		return memberList.get(index);
	}


	/**
	 * 
	 * @param attributeList
	 */
	public synchronized void writeAttributeList(LinkedHashMap<String, Object> attributeList) {
		setAttributeList(attributeList);

	}


	/**
	 * 
	 * @param member
	 */
	public synchronized void writeMemberListElement(String member) {
		this.memberList.add(member);
	}


	public synchronized int getMemberListSize() {
		return this.memberList.size();
	}

	/**
	 * 
	 * @param member
	 */
	public synchronized void removeMemberListElement(String member) {
		if(this.memberList.contains(member))
		{
			this.memberList.remove(member);
		}
		else
		{
			RIBlog.warnLog("tyring to remove non existing member from memberlist");
		}
	}


	public synchronized boolean hasMember(String member)
	{
		return this.memberList.contains(member);
	}


	/**
	 * @return first element of memberlist
	 */
	public synchronized String readMemberListFirstElement() {
		return this.memberList.getFirst();
	}
	/**
	 * @return last element of memberlist
	 */
	public synchronized String readMemberListLastElement() {
		return this.memberList.getLast();
	}


	/**
	 * @param attribute name (key)
	 * @return attribute object if exists or null
	 */
	public synchronized Object getAttribute(String attribute) {

		return this.attributeList.get(attribute);

	}

	/**
	 * 
	 * @param attribute
	 */
	public synchronized void removeAttribute(String attribute) {

		this.attributeList.remove(attribute);
	}

	/**
	 * 
	 * @param attributeName
	 * @param attribute
	 */
	@SuppressWarnings("unchecked")
	public synchronized void  addAttribute(String attributeName, Object attribute) {
		//TODO: check if exists already or if there is no attribute and return error

		this.attributeList.put(attributeName, attribute);


	}



	/**
	 * @return the map
	 */
	public LinkedHashMap<String, LinkedHashMap> getMap() {
		return map;
	}



	/**
	 * @return the neighbour
	 */
	public synchronized LinkedList<String> getNeighbour() {
		return neighbour;
	}








	/**
	 * @param neighbour the neighbour to set
	 */
	public synchronized void setNeighbour(LinkedList<String> neighbour) {
		this.neighbour = neighbour;
	}








	/**
	 * @param map the map to set
	 */
	public synchronized void setMap(LinkedHashMap<String, LinkedHashMap> map) {
		this.map = map;
	}



	/**
	 * @return the forwardingTable
	 */
	public synchronized LinkedHashMap<String, String> getForwardingTable() {
		return forwardingTable;
	}



	/**
	 * @param forwardingTable the forwardingTable to set
	 */
	public synchronized void setForwardingTable(LinkedHashMap<String, String> forwardingTable) {
		this.forwardingTable = forwardingTable;
	}








	/**
	 * @return the neighbourCost
	 */
	public synchronized LinkedHashMap<String, RoutingEntry> getNeighbourCost() {
		return neighbourCost;
	}








	/**
	 * @param neighbourCost the neighbourCost to set
	 */
	public synchronized void setNeighbourCost(
			LinkedHashMap<String, RoutingEntry> neighbourCost) {
		this.neighbourCost = neighbourCost;
	}








	/**
	 * @return the allRoutingEntry
	 */
	public synchronized LinkedHashMap<String, LinkedHashMap<String, RoutingEntry>> getAllRoutingEntry() {
		return allRoutingEntry;
	}




	/**
	 * @return the enrollmentFlag
	 */
	public synchronized boolean isEnrollmentFlag() {
		return enrollmentFlag;
	}






	/**
	 * @param enrollmentFlag the enrollmentFlag to set
	 */
	public synchronized void setEnrollmentFlag(boolean enrollmentFlag) {
		this.enrollmentFlag = enrollmentFlag;
		
		if(this.enrollmentFlag == true)
		{
			this.RIBlog.infoLog("RIBImpl: this IPC can enroll new IPCs");
		}
	}






	/**
	 * @param allRoutingEntry the allRoutingEntry to set
	 */
	public synchronized void setAllRoutingEntry(
			LinkedHashMap<String, LinkedHashMap<String, RoutingEntry>> allRoutingEntry) {
		this.allRoutingEntry = allRoutingEntry;
	}








	/**
	 * @return the dIF0Flag
	 */
	public synchronized boolean isDIF0Flag() {
		return DIF0Flag;
	}








	/**
	 * @param dIF0Flag the dIF0Flag to set
	 */
	public synchronized void setDIF0Flag(boolean dIF0Flag) {
		DIF0Flag = dIF0Flag;
	}





    
	/**
	 * @param ribDaemon the ribDaemon to set
	 */
	public synchronized void setRibDaemon(RIBDaemonImpl ribDaemon) {
		this.ribDaemon = ribDaemon;
	}






	/**
	 * @return the ribDaemon
	 */
	public synchronized RIBDaemonImpl getRibDaemon() {
		return ribDaemon;
	}








}
