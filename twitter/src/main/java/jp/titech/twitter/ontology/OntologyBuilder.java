/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.titech.twitter.ontology.dbpedia.DBpediaResourceOccurrence;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.ner.spotlight.SpotlightQuery;
import jp.titech.twitter.ontology.dbpedia.DBpediaQuery;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

public class OntologyBuilder {

	private TwitterUser user;
	private UserOntology userOntology;
	private int totalTweetCount;
	private boolean ontologyExists;
	private Date startDate, endDate;

	public OntologyBuilder(TwitterUser user) {
		this.user = user;
		totalTweetCount = (Vars.TIMELINE_TWEET_COUNT <= 3200) ? Vars.TIMELINE_TWEET_COUNT : 3200;
		ontologyExists = TweetBase.getInstance().isOntologyContained(user.getUserID());
	}

	public void build() {
		//Log.getLogger().info("Building ontology for user @" + user.getScreenName() + " based on the " + totalTweetCount + " most recent tweets, "
		//						+ "concatenating groups of " + Vars.CONCATENATION_WINDOW + " tweets.");
		
		//if(ontologyExists && startDate == null) {
		if(false) {
			Log.getLogger().info("Ontology already exists in database. Retrieving directly.");
			userOntology = TweetBase.getInstance().getUserOntology(user.getUserID());
			
		} else {
			Log.getLogger().info("Running DBpedia Spotlight on tweet content...");
			
			userOntology = new UserOntology();
			List<Tweet> tweets = TweetBase.getInstance().getTweets(user.getUserID());
			
			int processedTweetCount = 0;
			Util.loadStopwords(Vars.STOPWORDS_FILE);
			
			if(startDate != null && endDate != null){
				Log.getLogger().info("Getting tweets between " + startDate + " and " + endDate);
			}
			
			String concatenatedContent = "";
			int tweetsConcatenatedCount = 0;
			
			for (Tweet tweet : tweets) {
				
				if(startDate != null && endDate != null){
					if(tweet.getCreatedAt().before(startDate) || tweet.getCreatedAt().after(endDate)) {
						Log.getLogger().info("Skipping tweet created at " + tweet.getCreatedAt() + ": outside chosen time interval.");
						continue;
					}
				}
				
				//Log.getLogger().info("Unstripped content: " + tweet.getContent());
				tweet.stripNonHashtagElements();
				//Log.getLogger().info("Stripped content: " + tweet.getContent());
				
				concatenatedContent += tweet.getContent() + " ";
				tweetsConcatenatedCount++;

				if(tweetsConcatenatedCount >= Vars.CONCATENATION_WINDOW || processedTweetCount >= totalTweetCount - 1) {
					Log.getLogger().info("Stripped (and concatenated) tweet content: " + concatenatedContent);
					
					if(concatenatedContent.isEmpty() || concatenatedContent.equals(" ")) continue;
	
					SpotlightQuery spotlightQuery = SpotlightQuery.getInstance();
					Map<String, List<DBpediaResourceOccurrence>> occurrences = spotlightQuery.annotate(concatenatedContent);
	
					if(!occurrences.isEmpty()) {
						for (String key : occurrences.keySet()) {
							Log.getLogger().info("Match: " + key + ": " + occurrences.get(key).get(0)); // Only show the first candidate.
						}
					}
					
					List<DBpediaResourceOccurrence> bestCandidates = this.initBestCandidates(occurrences);
					
					DBpediaQuery dbpQuery = DBpediaQuery.getInstance();
					dbpQuery.collectClasses(bestCandidates, userOntology);
					
					tweetsConcatenatedCount = 0;
					concatenatedContent = "";
				}
				
				processedTweetCount++;

				if(processedTweetCount >= totalTweetCount) break;
			}

			TweetBase.getInstance().addUserOntology(user.getUserID(), userOntology);
		}
	}
	
	/**
	 * Extract the best candidates from the full list of occurrences.
	 * 
	 * @return A list of the 1st ranked candidate DBpedia resource occurrences
	 */
	private List<DBpediaResourceOccurrence> initBestCandidates(Map<String, List<DBpediaResourceOccurrence>> occurrences) {
		List<DBpediaResourceOccurrence> bestCandidates = new ArrayList<DBpediaResourceOccurrence>();
		Collection<List<DBpediaResourceOccurrence>> col = occurrences.values();

		for (Iterator<List<DBpediaResourceOccurrence>> iterator = col.iterator(); iterator.hasNext();) {
			List<DBpediaResourceOccurrence> candidates = iterator.next();
			if(!candidates.isEmpty())
				bestCandidates.add(candidates.get(0));
		}
		
		return bestCandidates;
	}
	
	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public UserOntology getUserOntology() {
		return this.userOntology;
	}
}
