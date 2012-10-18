/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import jp.titech.twitter.config.Configuration;

public class Log {
	
	private static Logger logger;
	
	public static Logger getLogger() {
		if(logger == null){
			PropertyConfigurator.configure(Configuration.PROPERTIES);
			logger = Logger.getLogger("TwitterMinerLogger");
		}
		return logger;
	}

	public static void setLogger(Logger logger) {
		Log.logger = logger;
	}
}
