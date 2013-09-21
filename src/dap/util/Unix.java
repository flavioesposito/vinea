/**
 * @copyright 2012 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 * 
 * @author Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 */
package dap.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import rina.rib.impl.RIBImpl;

/**
 * @author flavioesposito
 *
 */
public class Unix {
	/**
	 * RIB
	 */
	private RIBImpl rib = null;
	/**
	 * constructor
	 * @param rib
	 */
	public Unix(RIBImpl rib) {
		this.rib = rib;
	}
	
	/**	
	 * Runs any mininet command using Runtime.exec 
	 * @param command
	 */
	public void execute(String command) { 


		try {  
			Process p = Runtime.getRuntime().exec(command);  
			BufferedReader in = new BufferedReader(  
					new InputStreamReader(p.getInputStream()));  
			String line = null;  
			while ((line = in.readLine()) != null) {  
				System.out.println(line);  
			}  
		} catch (IOException e) {
			//e.printStackTrace();
			rib.RIBlog.errorLog(e);  
		}


	}

}
