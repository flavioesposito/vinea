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


/**
 * Resource Information Base (RIB) API
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public interface RIBAPI {
	
	/**
	 * 
	 * @return the attribute list
	 */
	public  LinkedHashMap getAttributeList();
	/**
	 * @param attributeList to set
	 */
	public  void setAttributeList(LinkedHashMap attributeList);
	/**
	 * 
	 * @return the member list 
	 */
	public  LinkedList<String> getMemberList();
	/**
	 * 
	 * @param memberList
	 */
	public  void setMemberList(LinkedList<String> memberList);
	/**
	 * 
	 * @param index
	 * @return a specific member in the memeber list
	 */
	public  String readElement(int index);
	/**
	 * 
	 * @param attribute List to be written
	 */
	public  void writeAttributeList(LinkedHashMap<String, Object> attributeList);
	/**
	 * 
	 * @param member to be written
	 */
	public  void writeMemberListElement(String member);
	/**
	 * 
	 * @param member to remove
	 */
	public  void removeMemberListElement(String member);
	/**
	 * 
	 * @return last member in the list
	 */
	public  String readMemberListFirstElement();
	/**
	 * 
	 * @return first member in the list
	 */
	public String readMemberListLastElement();
	/**
	 * 
	 * @param attribute to be read
	 */
	public  Object getAttribute(String attribute);
	/**
	 * 
	 * @param attribute to remove
	 */
	public void removeAttribute(String attribute);
	/**
	 * 
	 * @param attributeName
	 * @param attribute to add
	 */
	public  void  addAttribute(String attributeName, Object attribute);
	


}
