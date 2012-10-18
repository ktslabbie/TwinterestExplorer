/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.util;

import jp.titech.twitter.config.Configuration;

public class Vars {

	private static Configuration c = Configuration.getInstance();
	
	/**
	 * Twitter variables
	 */
	public static final String 	TWITTER_SEARCH_API	 	= c.getProperty("twitter.searchAPI");
	public static final int 	QUERY_RETRY 			= Integer.parseInt(c.getProperty("io.queryRetry"));				// Retry time in case of query failure
	
	public static final boolean	SPOTLIGHT_REMOTE		= Boolean.valueOf(c.getProperty("spotlight.remote"));			// Remote querying yes/no
	public static double 		SPOTLIGHT_CONFIDENCE	= Double.parseDouble(c.getProperty("spotlight.confidence"));
	public static int 			SPOTLIGHT_SUPPORT		= Integer.parseInt(c.getProperty("spotlight.support"));
}
