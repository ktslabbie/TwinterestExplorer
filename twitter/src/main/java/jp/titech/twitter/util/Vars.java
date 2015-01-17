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
	public static final String 	TWITTER_SEARCH_API	 			= c.getProperty("twitter.searchAPI");
	public static final String	API_ACCOUNTS_FILE				= c.getProperty("twitter.apiAccountsFile");
	public static final int		TIMELINE_TWEET_COUNT			= Integer.valueOf(c.getProperty("twitter.timelineTweetCount"));
	public static int			CONCATENATION_WINDOW			= Integer.valueOf(c.getProperty("twitter.concatenationWindow"));
	public static final int		MIN_FOLLOWERS					= Integer.valueOf(c.getProperty("twitter.minFollowers"));
	public static final int		MAX_FOLLOWERS					= Integer.valueOf(c.getProperty("twitter.maxFollowers"));
	public static final int		MIN_FRIENDS						= Integer.valueOf(c.getProperty("twitter.minFriends"));
	public static final int		MAX_FRIENDS						= Integer.valueOf(c.getProperty("twitter.maxFriends"));
	public static final int		MIN_TWEETS						= Integer.valueOf(c.getProperty("twitter.minTweets"));
	public static final int		MAX_TWEETS						= Integer.valueOf(c.getProperty("twitter.maxTweets"));
	public static final double	MIN_ENGLISH_RATE				= Double.valueOf(c.getProperty("twitter.minEnglishRate"));
	public static final String	MINING_MODE						= c.getProperty("twitter.miningMode");
	
	public static final int 	QUERY_RETRY 					= Integer.parseInt(c.getProperty("io.queryRetry"));				// Retry time in case of query failure
	
	public static final String  REDIS_URL						= c.getProperty("redis.url");
	
	public static final String  DBPEDIA_NAMESPACE				= c.getProperty("dbpedia.namespace");
	
	public static final String	DATA_DIRECTORY					= c.getProperty("data.dataDirectory");
	public static final String	USER_DIRECTORY					= c.getProperty("data.userDirectory");
	public static final String	EVALUATION_DIRECTORY			= c.getProperty("data.evaluationDirectory");
	public static final String	OUTPUT_DIRECTORY				= c.getProperty("data.outputDirectory");
	
	public static final String	STOPWORDS_FILE					= c.getProperty("data.stopwordsFile");
	
	public static final String	SQL_SCRIPT_DIRECTORY			= c.getProperty("sql.scriptDirectory");
	
	public static final String	SPARQL_PREFIXES					= Util.readFile(c.getProperty("sparql.prefixFile"));
	public static final String	SPARQL_SCRIPT_DIRECTORY			= c.getProperty("sparql.scriptDirectory");
	
	public static final String 	SPOTLIGHT_DEFAULT_URL			= c.getProperty("spotlight.defaultURL");
	public static final String 	SPOTLIGHT_BASE_URL				= c.getProperty("spotlight.baseURL");
	public static final String[] SPOTLIGHT_PORTS				= c.getProperty("spotlight.ports").split(",");
	public static double 		SPOTLIGHT_CONFIDENCE			= Double.parseDouble(c.getProperty("spotlight.confidence"));
	public static int 			SPOTLIGHT_SUPPORT				= Integer.parseInt(c.getProperty("spotlight.support"));

	public static final double  GENERALITY_BIAS 				=  Double.parseDouble(c.getProperty("similarity.generalityBias"));

	public static String		PRUNING_MODE							= c.getProperty("pruning.pruningMode");
	
	public static double		DBPEDIA_LOW_OCCURRENCE_PRUNING_RATE 	= Double.parseDouble(c.getProperty("pruning.dbpediaLowOccurrencePruningRate"));
	public static double		SCHEMA_LOW_OCCURRENCE_PRUNING_RATE 		= Double.parseDouble(c.getProperty("pruning.schemaLowOccurrencePruningRate"));
	public static double		YAGO_LOW_OCCURRENCE_PRUNING_RATE 		= Double.parseDouble(c.getProperty("pruning.yagoLowOccurrencePruningRate"));
	public static double		FREEBASE_LOW_OCCURRENCE_PRUNING_RATE 	= Double.parseDouble(c.getProperty("pruning.freebaseLowOccurrencePruningRate"));
	public static double		CATEGORY_LOW_OCCURRENCE_PRUNING_RATE 	= Double.parseDouble(c.getProperty("pruning.categoryLowOccurrencePruningRate"));
	
	public static int 			DBPEDIA_HIGH_GENERALITY_PRUNING_RATE 	= Integer.parseInt(c.getProperty("pruning.dbpediaHighGeneralityPruningRate"));
	public static int 			SCHEMA_HIGH_GENERALITY_PRUNING_RATE 	= Integer.parseInt(c.getProperty("pruning.schemaHighGeneralityPruningRate"));
	public static int 			YAGO_HIGH_GENERALITY_PRUNING_RATE 		= Integer.parseInt(c.getProperty("pruning.yagoHighGeneralityPruningRate"));
	public static int			CATEGORY_HIGH_GENERALITY_PRUNING_RATE 	= Integer.parseInt(c.getProperty("pruning.categoryHighGeneralityPruningRate"));
	
	public static int 			CATEGORY_TOP_K				 			= Integer.parseInt(c.getProperty("pruning.categoryTopK"));
	
	// String for printing experimental results. Ex: [2 0.2 0]
	public static String		PARAMETER_STRING					= "[" + CONCATENATION_WINDOW + " " + SPOTLIGHT_CONFIDENCE + " " + SPOTLIGHT_SUPPORT + "]";
}
