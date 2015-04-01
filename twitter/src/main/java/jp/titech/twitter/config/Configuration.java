/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.config;

import java.io.FileInputStream;
import java.util.Properties;

import jp.titech.twitter.util.Log;


/**
 * The Class Configuration. This class controls all properties that can be
 * modified to adapt the functionality of TwitterMiner
 */
public class Configuration {
	
	public static final String PROPERTIES = "config/twitterminer.properties";
	private static Configuration instance;
	private final Properties props;
	
	private Configuration(){
		
		props = new Properties();
		try {
			props.load(new FileInputStream(PROPERTIES));
		} catch (Exception e) {
			Log.getLogger().error("Configuration file cannot be opened", e);
			e.printStackTrace();
			System.err.println("<TwitterMiner will now terminate>");
			System.exit(1);
		}
	}
	
	/**
	 * Gets the singleton instance of Configuration.
	 *
	 * @return singleton instance of Configuration
	 */
	public static Configuration getInstance(){
		if(instance == null)
			instance = new Configuration();
		return instance;
	}
	
	
	/**
	 * Return the value associated with the key.
	 *
	 * @param key the key
	 * @return the value
	 */
	public String getProperty(String key){
		String value = props.getProperty(key);
		if(value == null){
			throw new RuntimeException("Configuration: Property '"+key+"' should exist in the properties file ("+PROPERTIES+")");
		}
		return value;
	}
	
}
