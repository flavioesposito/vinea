

package vinea.gui.util;
import java.io.File;
import javax.swing.JFileChooser;

/**
 * File chooser: Extends JFileChooser, a Java library that provides a simple mechanisms to 
 * allow users choose a file
 * 
 * This file is based on NED from Tatiana Kichkaylo at NYU and by Weishuai Yang at Binghamton Univ.
 */

public class MyFileChooser extends JFileChooser {

        public static ExtFileFilter graphFilter = new ExtFileFilter( "alt", "ITM alt files" );
        public static ExtFileFilter configFilter = new ExtFileFilter( "cfg", "Config files" );
        
        public MyFileChooser() {}

        private static class ExtFileFilter extends javax.swing.filechooser.FileFilter {
	    	private String ext;
	    	private String descr;
	
	    	public ExtFileFilter( String ext, String descr ) {
	    	    this.ext = ext;
	    	    this.descr = descr;
	    	} 
	    	public boolean accept( File f ) {
	    	    if ( f.isHidden() ) return false;
	    	    if ( f.isDirectory() ) return true;
	    	    String nm = f.getName();
	    	    if ( nm.indexOf( "." )<0 ) return false;
	    	    nm = nm.substring( nm.lastIndexOf( "." )+1 );
	    	    return nm.equals( ext );
	    	}
            public String getDescription() { return descr; }
        } 
    }