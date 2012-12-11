/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dbpedia.spotlight.model.DBpediaResourceOccurrence;
import org.dbpedia.spotlight.model.OntologyType;

import jp.titech.twitter.data.AnnotatedTweet;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.matching.spotlight.SpotlightQuery;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;

public class OntologyBuilder {
	
	private int userID;
	private int totalCount;
	private DBpediaQuery dbpediaOntology;

	public OntologyBuilder(int tUserID) {
		userID = tUserID;
		totalCount = 3200;
		dbpediaOntology = DBpediaQuery.getInstance();
	}
	
	public OntologyBuilder(int tUserID, int tCount) {
		userID = tUserID;
		totalCount = (tCount <= 3200) ? tCount : 3200;
		dbpediaOntology = DBpediaQuery.getInstance();
	}

	public void build() {
		Log.getLogger().info("Building ontology for user " + userID + " based on the " + totalCount + " most recent tweets.");
		List<Tweet> tweets = TweetBase.getInstance().getTweets(userID);
		List<AnnotatedTweet> annotatedTweets = new ArrayList<AnnotatedTweet>();
		int count = 0;
		for (Tweet tweet : tweets) {
			
			//tweet.stripURLs();
			//tweet.stripMentions();
			
			tweet.stripEverything();
			
			Log.getLogger().info("Stripped tweet content: " + tweet.getContent());
			
			SpotlightQuery spotlightQuery = SpotlightQuery.getInstance();
			Map<String, List<DBpediaResourceOccurrence>> occurrences = spotlightQuery.annotate(tweet.getContent());
			
			if(!occurrences.isEmpty()) {
				Log.getLogger().info("Found matches!");
				for (String key : occurrences.keySet()) {
					Log.getLogger().info(key + ": " + occurrences.get(key));
				}
			}
			
			AnnotatedTweet aTweet = new AnnotatedTweet(tweet, occurrences);
			
			DBpediaQuery dbpQuery = DBpediaQuery.getInstance();
			
			//dbpQuery.collectYAGOClasses(aTweet);
			//dbpQuery.collectCategories(aTweet);
			
			annotatedTweets.add(aTweet);
			count++;

			if(count == totalCount) break;
		}
		
		Map<OntologyType, Integer> fullMap = Util.mergeOntologyTypeMaps(annotatedTweets);
		
		TweetBase.getInstance().addOntology(userID, fullMap);
		
		Log.getLogger().info("Transitive types: " + fullMap.toString());
	}
}
