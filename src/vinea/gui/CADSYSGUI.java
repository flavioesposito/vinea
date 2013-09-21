/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package vinea.gui;


import javax.swing.JFrame;

import rina.config.RINAConfig;
import vinea.gui.panel.PrototypeGui;

/**
 * @author Flavio Esposito. Computer Science Department, Boston University
 * @version 1.0 
 *
 */
public class CADSYSGUI {
	private static RINAConfig config =null;
	
	/**
	 *  Constructor
	 */
	public CADSYSGUI(){
		this.config = new RINAConfig("rina.properties");
	}

	public static void main(String args[]) {
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		//JFrame win=new JFrame("Recursive InterNetwork Architecture (RINA) Prototype GUI");
		JFrame win=new JFrame("CADSys Control Panel GUI");
		CADSYSGUI cadsysGui = new CADSYSGUI();
		PrototypeGui  gui=new PrototypeGui();        
		gui.initComponents(config);
		gui.go(win);
		
		
	
	}

	
	
}
