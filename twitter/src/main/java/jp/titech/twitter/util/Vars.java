/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.util;

import jp.titech.twitter.config.Configuration;

public class Vars {

	private static final Configuration c = Configuration.getInstance();
	
	/**
	 * Twitter variables
	 */
	public static final String	API_ACCOUNTS_FILE				= c.getProperty("twitter.apiAccountsFile");
	public static final int		TIMELINE_TWEET_COUNT			= Integer.valueOf(c.getProperty("twitter.timelineTweetCount"));
	public static final int     CONCATENATION_WINDOW			= Integer.valueOf(c.getProperty("twitter.concatenationWindow"));
	public static final int		MIN_FOLLOWERS					= Integer.valueOf(c.getProperty("twitter.minFollowers"));
	public static final int		MAX_FOLLOWERS					= Integer.valueOf(c.getProperty("twitter.maxFollowers"));
	public static final int		MIN_FRIENDS						= Integer.valueOf(c.getProperty("twitter.minFriends"));
	public static final int		MAX_FRIENDS						= Integer.valueOf(c.getProperty("twitter.maxFriends"));
	public static final int		MIN_TWEETS						= Integer.valueOf(c.getProperty("twitter.minTweets"));
	public static final int		MAX_TWEETS						= Integer.valueOf(c.getProperty("twitter.maxTweets"));
	public static final float	MIN_ENGLISH_RATE				= Float.valueOf(c.getProperty("twitter.minEnglishRate"));
	//public static final String	MINING_MODE						= c.getProperty("twitter.miningMode");
	
	//public static final int 	QUERY_RETRY 					= Integer.parseInt(c.getProperty("io.queryRetry"));				// Retry time in case of query failure
	
	public static final String  POSTGRES_SERVER_NAME			= c.getProperty("postgres.serverName");
	public static final String  POSTGRES_DATABASE_NAME			= c.getProperty("postgres.databaseName");
	public static final String  POSTGRES_USER					= c.getProperty("postgres.user");
	public static final String  POSTGRES_PASSWORD				= c.getProperty("postgres.password");
	
	public static final String  REDIS_URL						= c.getProperty("redis.url");
	
	public static final String  DBPEDIA_NAMESPACE				= c.getProperty("dbpedia.namespace");
	
	public static final String	DATA_DIRECTORY					= c.getProperty("data.dataDirectory");
	public static final String	USER_DIRECTORY					= c.getProperty("data.userDirectory");
	public static final String	EVALUATION_DIRECTORY			= c.getProperty("data.evaluationDirectory");
	public static final String	OUTPUT_DIRECTORY				= c.getProperty("data.outputDirectory");
	
	public static final String	STOPWORDS_FILE					= c.getProperty("data.stopwordsFile");
	
	public static final String	SQL_SCRIPT_DIRECTORY			= c.getProperty("sql.scriptDirectory");

	public static final String 	 SPOTLIGHT_BASE_URL				= c.getProperty("spotlight.baseURL");
	public static final String[] SPOTLIGHT_PORTS				= c.getProperty("spotlight.ports").split(",");
	public static final float 	 SPOTLIGHT_CONFIDENCE			= Float.valueOf(c.getProperty("spotlight.confidence"));
	public static final int 	 SPOTLIGHT_SUPPORT				= Integer.parseInt(c.getProperty("spotlight.support"));

	public static float  		GENERALITY_BIAS 					=  Float.valueOf(c.getProperty("similarity.generalityBias"));
}
