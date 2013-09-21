/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.gui.panel;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import rina.config.RINAConfig;
import dap.cad.gui.util.MyFileChooser;

public class GuiControl {
	
	/**
	 * instance of PrototypeGui
	 */
	private PrototypeGui mPrototypeGui; 
	

    /**
     * reference to control panel
     */
    private ControlPanel mControlPanel;
    
    /**
     * file chooser
     */
    private MyFileChooser fileChooser = new MyFileChooser();
    
    private RINAConfig config;
	/**
	 * 	Constructor
	 * @param mPrototypeGui to reference to PrototypeGui
	 */
	public GuiControl(PrototypeGui mPrototypeGui, RINAConfig config){
		
		this.config = config;
		this.mPrototypeGui = mPrototypeGui; 
		this.mControlPanel = new ControlPanel(this,this.config);
	}
	
	
	/**
     * opens configuration file
     */
    public void openConf(){
		mControlPanel.actionPerformed(new ActionEvent(this,0,"Load"));
		
    }
    /**
     * saves configuration file
     */
    public void saveConf(){
		mControlPanel.actionPerformed(new ActionEvent(this,0,"Save"));
    }


	public void run() {
		mControlPanel.actionPerformed(new ActionEvent(this,0,"Run"));
	}


	public void stop() {
		mControlPanel.actionPerformed(new ActionEvent(this,0,"Pause"));		
	}
	
	
	 /**
     * quit gui
     */
    public void quit(){
    //	RINASimulator.getInstance().stopIt();
    	mPrototypeGui.quit(); 
    }
    /**
     * setting options
     */
    public void options(){	
    	JOptionPane.showMessageDialog(getGui().getFrame(), 
    		       " CADSys Prototype (Version "+getVersion()+")"+
    		       "\n "+
    		       //"\n Add your options here"+
    		       		"   ",
    		        "About", JOptionPane.INFORMATION_MESSAGE);	
    	
    }
    /**
     * Prototype information
     */
    public void about(){
	    JOptionPane.showMessageDialog(getGui().getFrame(), 
	       " CADSys Prototype (Version "+getVersion()+")"+
	       "\n Flavio Esposito (flavio@cs.bu.edu)" +
	       "\n Computer Science Department, Boston University " +
	       "\n "+
	       "\n Documentation available at http://csr.bu.edu/cad "+
	       "\n Copyright 2013 All rights reserved \n" +
	       		"   ",
	        "About", JOptionPane.INFORMATION_MESSAGE);	
    }
 	/**
 	 * gets Prototype version
 	 * 
 	 * @return version
 	 */
 	public double getVersion(){
 		return 1.0;
 	}    
    /**
     * get gui
     * @return gui object
     */
    public PrototypeGui getGui(){
    	return mPrototypeGui;
    }


	public void openGraph() {
		JOptionPane.showMessageDialog(getGui().getFrame(), 
			       " RINA Prototype (Version "+getVersion()+")"+
			       "\n Functionality not implemented yet "+
			       		"   ",
			        "About", JOptionPane.INFORMATION_MESSAGE);	
		
	}
	
}//end of class
