/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * 
 * 
 * @author Yuefeng Wang and  Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 *
 */
 


package rina.rib.util;


public class Event {

	private String type;//pub or sub
	private String name;// subName
	private String publisher;
	private int id;
	private int frenquency;
	private Object attribute;// sub value 
	
	public Event(String type, int id, String name, int frenquency)
	{
		this.type = type;
		this.id = id;
		this.name = name;
		this.frenquency = frenquency;
	}
	
	public Event(String type, int id,String name, int frenquency, String publisher)
	{
		this.type = type;
		this.id = id;
		this.name = name;
		this.frenquency = frenquency;
		this.publisher = publisher;
	}
	/**
	 * @return the publisher
	 */
	public synchronized String getPublisher() {
		return publisher;
	}

	/**
	 * @param publisher the publisher to set
	 */
	public synchronized void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	/**
	 * @return the type
	 */
	public synchronized String getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	public synchronized void setType(String type) {
		this.type = type;
	}


	/**
	 * @return the name
	 */
	public synchronized String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public synchronized void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public synchronized int getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public synchronized void setId(int id) {
		this.id = id;
	}


	/**
	 * @return the attribute
	 */
	public synchronized Object getAttribute() {
		return attribute;
	}


	/**
	 * @param attribute the attribute to set
	 */
	public synchronized void setAttribute(Object attribute) {
		this.attribute = attribute;
	}

	/**
	 * @return the frenquency
	 */
	public synchronized int getFrenquency() {
		return frenquency;
	}

	/**
	 * @param frenquency the frenquency to set
	 */
	public synchronized void setFrenquency(int frenquency) {
		this.frenquency = frenquency;
	}
	
	
	
}
