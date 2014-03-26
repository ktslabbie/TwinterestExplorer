/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.ontology;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.dbpedia.spotlight.model.DBpediaResourceOccurrence;
import org.dbpedia.spotlight.model.OntologyType;

import jp.titech.twitter.data.AnnotatedTweet;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.ner.spotlight.SpotlightQuery;
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
		ontologyExists = TweetBase.getInstance().isContained(user.getUserID(), Vars.SPOTLIGHT_CONFIDENCE, Vars.SPOTLIGHT_SUPPORT);
	}

	public void build() {
		Log.getLogger().info("Building ontology for user " + user.getUserID() + " based on the " + totalTweetCount + " most recent tweets, "
								+ "concatenating groups of " + Vars.CONCATENATION_WINDOW + " tweets.");
		
		if(ontologyExists && startDate == null) {
			
			Log.getLogger().info("Ontology already exists in database. Retrieving directly.");
			userOntology = TweetBase.getInstance().getUserOntology(user.getUserID());
			user.setUserOntology(userOntology);
			
		} else {
			
			Log.getLogger().info("Running DBpedia Spotlight on tweet content...");
			
			List<Tweet> tweets = TweetBase.getInstance().getTweets(user.getUserID());
			List<AnnotatedTweet> annotatedTweets = new ArrayList<AnnotatedTweet>();
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
				
				tweet.stripEverything();
				concatenatedContent += tweet.getContent() + " ";
				tweetsConcatenatedCount++;

				if(tweetsConcatenatedCount >= Vars.CONCATENATION_WINDOW || processedTweetCount >= totalTweetCount - 1) {
					Log.getLogger().info("Stripped (and concatenated) tweet content: " + concatenatedContent);
					
					if(concatenatedContent.isEmpty() || concatenatedContent.equals(" ")) continue;
	
					SpotlightQuery spotlightQuery = SpotlightQuery.getInstance();
					Map<String, List<DBpediaResourceOccurrence>> occurrences = spotlightQuery.annotate(concatenatedContent);
	
					if(!occurrences.isEmpty()) {
						for (String key : occurrences.keySet()) {
							Log.getLogger().info("Match: " + key + ": " + occurrences.get(key));
						}
					}
	
					AnnotatedTweet aTweet = new AnnotatedTweet(tweet, occurrences);
	
					DBpediaQuery dbpQuery = DBpediaQuery.getInstance();
	
					dbpQuery.collectYAGOClasses(aTweet);
					Log.getLogger().info("Full YAGO map: " + aTweet.getYAGOTypes());
					dbpQuery.collectCategories(aTweet);
					Log.getLogger().info("Full Category map: " + aTweet.getCategories());
	
					annotatedTweets.add(aTweet);
					
					tweetsConcatenatedCount = 0;
					concatenatedContent = "";
				}
				
				processedTweetCount++;

				if(processedTweetCount >= totalTweetCount) break;
			}

			userOntology.setOntology(Util.mergeOntologyTypeMaps(annotatedTweets));

			TweetBase.getInstance().addUserOntology(user.getUserID(), userOntology);
		}
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

	/**
	 * @return the userOntology
	 */
	public UserOntology getUserOntology() {
		return userOntology;
	}
	
	
}
