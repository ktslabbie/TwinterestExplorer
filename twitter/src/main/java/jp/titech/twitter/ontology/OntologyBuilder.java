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
import jp.titech.twitter.matching.Annotator;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;

public class OntologyBuilder {
	
	private int userID;
	private DBpediaOntology dbpediaOntology;

	public OntologyBuilder(int tUserID) {
		userID = tUserID;
		dbpediaOntology = DBpediaOntology.getInstance();
	}

	public void build() {
		List<Tweet> tweets = TweetBase.getInstance().getTweets(userID);
		List<AnnotatedTweet> annotatedTweets = new ArrayList<AnnotatedTweet>();
		int count = 0;
		for (Tweet tweet : tweets) {
			
			//tweet.stripURLs();
			//tweet.stripMentions();
			
			tweet.stripEverything();
			
			Log.getLogger().info("Stripped tweet content: " + tweet.getContent());
			
			Annotator annotator = new Annotator();
			AnnotatedTweet aTweet = annotator.annotate(tweet);
			
			annotatedTweets.add(aTweet);
			count++;

			if(count == 1000) break;
			//Log.getLogger().info("BEST CANDIDATES: " + annotatedTweet.getBestCandidates());
			//Log.getLogger().info("TYPES: " + annotatedTweet.getTypes());
		}
		
		Map<OntologyType, Integer> fullMap = Util.mergeOntologyTypeMaps(annotatedTweets);
		
		Log.getLogger().info("Transitive types: " + fullMap.toString());
	}
}
