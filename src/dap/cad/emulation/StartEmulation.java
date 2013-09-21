/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 */
package dap.cad.emulation;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

import dap.cad.slicespec.impl.googleprotobuf.SliceSpec;
import dap.cad.sp.SliceProvider;
import dap.cad.sp.slicegenerator.SliceGenerator;

/**
 * generate pool of slice requests to embed for evaluation purposes
 * (this class is not part of the system and it is only used to generate plots) 
 * @author Flavio Esposito
 * @version 1.0
 */
public class StartEmulation extends Thread {
	/**
	 * 
	 */
	private static Properties mProperties =new Properties();
	/**
	 * parse even requests from here
	 */
	private String eventFile = null;
	
	/**
	 * set of slices to embed
	 */
	private LinkedHashMap<Integer,SliceSpec.Slice.Builder> slicesToEmbed = new LinkedHashMap<Integer,SliceSpec.Slice.Builder>();;

	/**
	 * Dummy Constructor
	 */
	public StartEmulation() {}

	/**
	 * constructor
	 * @param eventfilePath
	 */
	public StartEmulation(String eventfilePath) {

		this.eventFile = eventfilePath;
	}
	/**
	 * parse event file, generate requests and passes it to the Slice Provider 
	 */
	public void run() {
		loadEventFromFile(this.eventFile, mProperties);
		
		int slicesToEmbed = GenerateRequests();

		if (slicesToEmbed ==1) { 
				new SliceProvider("sp", "idd", this.slicesToEmbed, true); //true for a single slice 
		}else if(slicesToEmbed >1) {
			new SliceProvider("sp", "idd", this.slicesToEmbed, false);
		}


	}
	/**
	 * calls 
	 * @param mProperties2
	 * @return 
	 */
	public int GenerateRequests() {
		 
		int slicesToEmbed = 0;
		SliceGenerator sg = new SliceGenerator();

		Set<Object> events =mProperties.keySet();
		Iterator<Object> KeyIter = events.iterator();
		while(KeyIter.hasNext()) {
			Object entry = KeyIter.next();
			String eventsTXTlineNumber = entry.toString();
			System.out.println("key "+eventsTXTlineNumber );
			System.out.println("value: "+mProperties.get(entry));
			SliceSpec.Slice.Builder slice = sg.generateSlice(eventsTXTlineNumber, mProperties.get(entry).toString());
			this.slicesToEmbed.put(slice.getSliceID(), slice);
			slicesToEmbed++;
			
		}
		return slicesToEmbed;
	}

	/**
	 * 
	 * @param path
	 * @param properties
	 */
	public static void loadEventFromFile(String path, Properties properties){
		if((path!=null)&&(path.length()!=0))
			loadEventFromFile(new File(path), properties);
	}

	/**
	 * loads configuration from File object
	 * @param f File object
	 * @param properties destination properties
	 */
	public static void loadEventFromFile(File f, Properties properties){

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
	 * main
	 * @param args
	 */
	public static void main(String[] args) {

		// control for correct arguments
		URL location = StartEmulation.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println("location: "+location.getFile());
        
		String input = null;
		if(args.length==0) {
			input = "/home/mininet/cadsys/config/events.txt"; 
		}else if(args.length==1) {
			input = args[0];
		}else {
			System.err.println("Wrong number of arguments or \"config/events.txt\" file does not exist!");
			System.err.println("-----------------------------------------------------------------");
			System.err.println("");

			System.err.println("USAGE: ");
			System.err.println("java -jar StartEmulation.jar /pathTo/config/events.txt");
		}
		//start emulation
		StartEmulation emulation = new StartEmulation(input);
		emulation.start();
	}


}
