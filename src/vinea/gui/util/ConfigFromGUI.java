/**
 * @copyright 2012 Computer Science Department, laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package vinea.gui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Handle configuration load and save from GUI
 * @author Flavio Esposito and Yuefeng Wang. Computer Science Department, Boston University
 * @version 1.0
 *
 */
public class ConfigFromGUI {

	/**
	 * dummy constructor
	 */
	public ConfigFromGUI(){}
	/**
	 * loads configuration from file at the specified path
	 * @param path file path
	 * @param properties destination properties
	 */
	public static void loadConfFromFile(String path, Properties properties){
		if((path!=null)&&(path.length()!=0))
			loadConfFromFile(new File(path), properties);
	}
	/**
	 * loads configuration from File object
	 * @param f File object
	 * @param properties destination properties
	 */
	public static void loadConfFromFile(File f, Properties properties){
		
	    try
	    {
	      FileInputStream input = new FileInputStream(f);
	      properties.load(input);	 
	      input.close();
	
	    }
	    catch (FileNotFoundException e)
	    {
	      System.err.println(f + " not found");
	    }
	    catch (IOException e){
	    	System.err.println(f + " io exception");
	    }
	    catch (Exception e){
	    	System.err.println("reading config "+f + " exception");
	    	e.printStackTrace();
	    }
	}
	/**
	 * saves configuration to file at the specified path
	 * @param path file path
	 * @param properties properties to be saved
	 */
	public static void saveConfToFile(String path, Properties properties){
		if((path!=null)&&(path.length()!=0))
			saveConfToFile(new File(path), properties);
	}
	/**
	 * saves configuration to File object
	 * @param f File object
	 * @param properties properties to be saved
	 */
	public static void saveConfToFile(File f, Properties properties){
		
	    try
	    {
		      FileOutputStream output = new FileOutputStream(f);
		      properties.store(output,"#Generated Configuration, be careful about the relative path of configuration file and other files\n");
		      output.close();
	    }
	    catch (FileNotFoundException e)
	    {
	      System.err.println(f + " not found");
	    }
	    catch (IOException e){
	    	System.err.println(f + " io exception");
	    }
	    catch (Exception e){
	    	System.err.println("reading config "+f + " exception");
	    	e.printStackTrace();
	    }
	}


	/**
	 * loads configuration from file at the specified path, with messagebox if any error exists.
	 * @param path file path
	 * @param properties destination properties
	 * @param frame parent frame
	 */
	public static void loadConfFromFileGUI(String path, Properties properties, JFrame frame){
		if((path!=null)&&(path.length()!=0))
			loadConfFromFileGUI(new File(path), properties, frame);
	
	}
	/**
	 * loads configuration from File object, with messagebox if any error exists.
	 * @param f File object
	 * @param properties destination properties
	 * @param frame parent frame
	 */
	
	public static void loadConfFromFileGUI(File f, Properties properties, JFrame frame){
	    try
	    {
	      FileInputStream input = new FileInputStream(f);
	      properties.load(input);	 
	      
	      input.close();
	
	    }
	    catch (FileNotFoundException e)
	    {
			JOptionPane.showMessageDialog(frame,""+ f + " not found", "Error!", JOptionPane.ERROR_MESSAGE);
	    	return;
	    }
	    catch (IOException e){
			JOptionPane.showMessageDialog(frame,""+ f + " io exception", "Error!", JOptionPane.ERROR_MESSAGE);
			return;
	    }
	    catch (Exception e){
			JOptionPane.showMessageDialog(frame,"reading config "+ f + "error", "Error!", JOptionPane.ERROR_MESSAGE);
	    	e.printStackTrace();
	    	return;
	    }
	}
	
	/**
	 * saves configuration to file at the specified path, with messagebox if any error exists.
	 * @param path file path
	 * @param properties properties to be saved
	 * @param frame parent frame
	 */
	public static void saveConfToFileGUI(String path, Properties properties, JFrame frame){
		if((path!=null)&&(path.length()!=0))
			saveConfToFileGUI(new File(path), properties, frame);
	
	}
	
	/**
	 * saves configuration to File object, with messagebox if any error exists.
	 * @param f File object
	 * @param properties properties to be saved
	 * @param frame parent frame
	 */
	public static void saveConfToFileGUI(File f, Properties properties, JFrame frame){
	    try
	    {
		      FileOutputStream output = new FileOutputStream(f);
		      properties.store(output,"#Generated Configuration, be careful about the relative path of configuration file and other files\n");
		      output.close();
	    }
	    catch (FileNotFoundException e)
	    {
			JOptionPane.showMessageDialog(frame,""+ f + " not found", "Error!", JOptionPane.ERROR_MESSAGE);
	    	return;
	    }
	    catch (IOException e){
			JOptionPane.showMessageDialog(frame,""+ f + " io exception", "Error!", JOptionPane.ERROR_MESSAGE);
			return;
	    }
	    catch (Exception e){
			JOptionPane.showMessageDialog(frame,"reading config "+ f + "error", "Error!", JOptionPane.ERROR_MESSAGE);
	    	e.printStackTrace();
	    	return;
	    }
	}

	
	

}
