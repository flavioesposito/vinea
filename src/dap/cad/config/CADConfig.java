/**
 * @copyright 2013 Computer Science Department laboratory, Boston University. 
 * All rights reserved. Permission to use, copy, modify, and distribute this software and its documentation
 * for any purpose and without fee is hereby granted, provided that the above copyright notice appear in all 
 * copies and that both the copyright notice and this permission notice appear in supporting documentation. 
 * The laboratory of the Computer Science Department at Boston University makes no 
 * representations about the suitability of this software for any purpose. 
 * It is provided "as is" without express or implied warranty. 
 */
package dap.cad.config;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import rina.rib.impl.Logger;





/**
 * Read or set the configuration parameters common to every physical node
 * @version 1.0
 * @author Flavio Esposito
 *
 */
public class CADConfig {

	/**
	 * configuration file
	 */
	private String configFile = null;
	/**
	 * auxiliar property object
	 */
	public Properties cadProperties = null;

	/**
	 * auxiliar input stream to read from configuration file
	 */
	private InputStream inputStream = null;
	/**
	 * logger
	 */
	private Logger log = null;



	/**
	 * 
	 * @param configFile
	 */
	public CADConfig(String configFile){

		this.log = new Logger();
		this.configFile = configFile;
		this.loadCadProperties();

	}



	/**
	 *  Reads and loads properties from the "file.properties" file
	 */
	public void loadCadProperties() {

		this.cadProperties = new Properties();
		try{
			InputStream inputStream = new FileInputStream(this.configFile);
			this.cadProperties.load(inputStream);

			this.log.infoLog("CADConfig: configuration file: "+this.configFile+" loaded");

		}catch(IOException e){
			e.printStackTrace();
		}
		finally {
			if( null != inputStream ) 
				try { 
					inputStream.close(); 
				} catch( IOException e ) 
				{ 
					e.printStackTrace();
				}
		}

	}



	/**
	 * set a new property 
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value){
		//TODO: store the properties in the RIB
		this.cadProperties.setProperty(key, value);
	}
	/**
	 * get a property from the configuration file
	 * @param key
	 * @return value
	 */
	public  String getProperty(String key){

		String property = this.cadProperties.get(key).toString();

		return property;


	}

	/**
	 * @return the configFile
	 */
	public synchronized String getConfigFile() {
		return configFile;
	}

	/**
	 * @param configFile the configFile to set
	 */
	public synchronized void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	/**
	 * @return the cadProperties
	 */
	public synchronized Properties getcadProperties() {
		return cadProperties;
	}

	/**
	 * @param cadProperties the cadProperties to set
	 */
	public synchronized void setcadProperties(Properties cadProperties) {
		this.cadProperties = cadProperties;
	}

}

