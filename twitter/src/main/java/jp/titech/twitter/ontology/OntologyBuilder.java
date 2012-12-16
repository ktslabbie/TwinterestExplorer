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
import jp.titech.twitter.ner.spotlight.SpotlightQuery;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

public class OntologyBuilder {

	private int userID;
	private int totalCount;
	private boolean ontologyExists;
	private Map<OntologyType, Integer> ontology;

	public OntologyBuilder(int tUserID) {
		userID = tUserID;
		totalCount = 3200;
		ontologyExists = TweetBase.getInstance().isContained(tUserID, Vars.SPOTLIGHT_CONFIDENCE, Vars.SPOTLIGHT_SUPPORT);
	}

	public OntologyBuilder(int tUserID, int tCount) {
		userID = tUserID;
		totalCount = (tCount <= 3200) ? tCount : 3200;
		ontologyExists = TweetBase.getInstance().isContained(tUserID, Vars.SPOTLIGHT_CONFIDENCE, Vars.SPOTLIGHT_SUPPORT);
	}

	public void build() {
		Log.getLogger().info("Building ontology for user " + userID + " based on the " + totalCount + " most recent tweets.");
		
		if(ontologyExists){
			Log.getLogger().info("Ontology already exists in database. Retrieving directly.");
			ontology = TweetBase.getInstance().getOntology(userID);
		} else {
			Log.getLogger().info("Running DBpedia Spotlight on tweet content...");
			List<Tweet> tweets = TweetBase.getInstance().getTweets(userID);
			List<AnnotatedTweet> annotatedTweets = new ArrayList<AnnotatedTweet>();
			int count = 0;
			for (Tweet tweet : tweets) {

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

				dbpQuery.collectYAGOClasses(aTweet);
				Log.getLogger().info("Full YAGO map: " + aTweet.getYAGOTypes());
				dbpQuery.collectCategories(aTweet);
				Log.getLogger().info("Full Category map: " + aTweet.getCategories());

				annotatedTweets.add(aTweet);
				count++;

				if(count == totalCount) break;
			}

			ontology = Util.mergeOntologyTypeMaps(annotatedTweets);

			TweetBase.getInstance().addOntology(userID, ontology);
		}

		Log.getLogger().info("Transitive types: " + ontology.toString());

	}

	/**
	 * @return the ontology
	 */
	public Map<OntologyType, Integer> getOntology() {
		return ontology;
	}

	/**
	 * @param ontology the ontology to set
	 */
	public void setOntology(Map<OntologyType, Integer> ontology) {
		this.ontology = ontology;
	}
	
	
}
