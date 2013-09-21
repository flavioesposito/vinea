/**
 * @copyright 2012 Computer Science Department, Recursive InterNetworking Architecture (RINA) laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The RINA laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package rina.rib.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Logger: log the information
 * It uses log4j, the configuration file is  /src/log4j.properties, the log file is rinaBU./log
 * @author Yuefeng Wang and Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0
 */
public class Logger {
	
	/**
	 * Logger
	 */
	static Log log = LogFactory.getLog(Logger.class);
	
	/**
	 * Dummy Constructor
	 */
	public Logger() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * general Logger
	 * @param stringToLog
	 */
	public void infoLog(Object stringToLog) {


		log.info(stringToLog);
	}

	/**
	 * error log
	 * @param stringToLog
	 * @throws IOException
	 */
	public void errorLog(Object stringToLog) {
		
		log.error(stringToLog);
	
	}
	
	/**
	 * debug log
	 * @param stringToLog
	 * @throws IOException
	 */
	public void debugLog(Object stringToLog) {
		log.debug(stringToLog);
	}
	/**
	 * warn log
	 * @param stringToLog
	 * @throws IOException
	 */
	public void warnLog(Object stringToLog) {
		log.warn(stringToLog);
	}
	
	
	
}//end of class
