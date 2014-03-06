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
	public static final int		TIMELINE_TWEET_COUNT			= Integer.valueOf(c.getProperty("twitter.timelineTweetCount"));
	public static final int		CONCATENATION_WINDOW			= Integer.valueOf(c.getProperty("twitter.concatenationWindow"));
	public static final int		MIN_FOLLOWERS					= Integer.valueOf(c.getProperty("twitter.minFollowers"));
	public static final int		MAX_FOLLOWERS					= Integer.valueOf(c.getProperty("twitter.maxFollowers"));
	public static final int		MIN_FRIENDS						= Integer.valueOf(c.getProperty("twitter.minFriends"));
	public static final int		MAX_FRIENDS						= Integer.valueOf(c.getProperty("twitter.maxFriends"));
	public static final int		MIN_TWEETS						= Integer.valueOf(c.getProperty("twitter.minTweets"));
	public static final int		MAX_TWEETS						= Integer.valueOf(c.getProperty("twitter.maxTweets"));
	
	public static final int 	QUERY_RETRY 					= Integer.parseInt(c.getProperty("io.queryRetry"));				// Retry time in case of query failure
	
	public static final String 	DBPEDIA_RDF_DIRECTORY 			= c.getProperty("dbpedia.rdfDirectory");
	public static final String 	DBPEDIA_REPOSITORY_DIRECTORY 	= c.getProperty("dbpedia.repositoryDirectory");
	public static final boolean	DBPEDIA_REMOTE					= Boolean.valueOf(c.getProperty("dbpedia.remote"));				// Remote DBpedia querying yes/no
	
	public static final String	DATA_DIRECTORY					= c.getProperty("data.dataDirectory");
	public static final String	OUTPUT_DIRECTORY				= c.getProperty("data.outputDirectory");
	
	public static final String	STOPWORDS_FILE					= c.getProperty("data.stopwordsFile");
	
	public static final String	SQL_SCRIPT_DIRECTORY			= c.getProperty("sql.scriptDirectory");
	
	public static final String	SPARQL_PREFIXES					= Util.readFile(c.getProperty("sparql.prefixFile"));
	public static final String	SPARQL_SCRIPT_DIRECTORY			= c.getProperty("sparql.scriptDirectory");
	
	public static final boolean	SPOTLIGHT_REMOTE				= Boolean.valueOf(c.getProperty("spotlight.remote"));			// Remote Spotlight querying yes/no
	public static double 		SPOTLIGHT_CONFIDENCE			= Double.parseDouble(c.getProperty("spotlight.confidence"));
	public static int 			SPOTLIGHT_SUPPORT				= Integer.parseInt(c.getProperty("spotlight.support"));
	public static final boolean INCLUDE_EMPTY_SURFACE_FORMS 	= Boolean.valueOf(c.getProperty("spotlight.includeEmptySurfaceForms"));

	public static double		DBPEDIA_LOW_OCCURRENCE_PRUNING_RATE 	= Double.parseDouble(c.getProperty("pruning.dbpediaLowOccurrencePruningRate"));
	public static double		SCHEMA_LOW_OCCURRENCE_PRUNING_RATE 		= Double.parseDouble(c.getProperty("pruning.schemaLowOccurrencePruningRate"));
	public static double		YAGO_LOW_OCCURRENCE_PRUNING_RATE 		= Double.parseDouble(c.getProperty("pruning.yagoLowOccurrencePruningRate"));
	public static double		FREEBASE_LOW_OCCURRENCE_PRUNING_RATE 	= Double.parseDouble(c.getProperty("pruning.freebaseLowOccurrencePruningRate"));
	public static double		CATEGORY_LOW_OCCURRENCE_PRUNING_RATE 	= Double.parseDouble(c.getProperty("pruning.categoryLowOccurrencePruningRate"));
	
	public static int 			DBPEDIA_HIGH_GENERALITY_PRUNING_RATE 	= Integer.parseInt(c.getProperty("pruning.dbpediaHighGeneralityPruningRate"));
	public static int 			SCHEMA_HIGH_GENERALITY_PRUNING_RATE 	= Integer.parseInt(c.getProperty("pruning.schemaHighGeneralityPruningRate"));
	public static int 			YAGO_HIGH_GENERALITY_PRUNING_RATE 		= Integer.parseInt(c.getProperty("pruning.yagoHighGeneralityPruningRate"));
	public static int			CATEGORY_HIGH_GENERALITY_PRUNING_RATE 	= Integer.parseInt(c.getProperty("pruning.categoryHighGeneralityPruningRate"));
	
	public static int 			CATEGORY_TOP_K				 	= Integer.parseInt(c.getProperty("pruning.categoryTopK"));
}
