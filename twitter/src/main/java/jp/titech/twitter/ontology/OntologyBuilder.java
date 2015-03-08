/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.ontology;

import java.util.List;
import jp.titech.twitter.ontology.dbpedia.DBpediaResourceOccurrence;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.ner.spotlight.SpotlightQuery;
import jp.titech.twitter.ontology.dbpedia.RedisQuery;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

public class OntologyBuilder {

	private final TwitterUser user;
	private final UserOntology userOntology;
	private String spotlightURL;
	private final int totalTweetCount;
	//private boolean ontologyExists;
	private long startDate = -1, endDate = -1;

	public OntologyBuilder(TwitterUser user, String spotlightURL) {
		this.user = user;
		this.spotlightURL = spotlightURL;
		totalTweetCount = (Vars.TIMELINE_TWEET_COUNT <= 3200) ? Vars.TIMELINE_TWEET_COUNT : 3200;
		userOntology = new UserOntology();
		//userOntology = TweetBase.getInstance().getUserOntology(user.getUserID());
	}

	public void build() {
		//Log.getLogger().info("Building ontology for user @" + user.getScreenName() + " based on the " + totalTweetCount + " most recent tweets, "
		//						+ "concatenating groups of " + Vars.CONCATENATION_WINDOW + " tweets.");
		
		Log.getLogger().info("Ontology not in DB. Running DBpedia Spotlight on tweet content...");
		
		List<Tweet> tweets = user.getTweets();
		
		if(tweets.isEmpty()) { 
			Log.getLogger().error("Tweets empty. WTF?");
			return;
		}

		int processedTweetCount = 0;

		if(startDate > -1 && endDate > -1) {
			Log.getLogger().info("Getting tweets between " + startDate + " and " + endDate);
		}

		String concatenatedContent = "";
		int tweetsConcatenated = 0;

		for (Tweet tweet : tweets) {

			if(startDate > -1 && endDate > -1) {
				if(tweet.getCreatedAt() < startDate || tweet.getCreatedAt() > endDate) {
					Log.getLogger().info("Skipping tweet created at " + tweet.getCreatedAt() + ": outside chosen time interval.");
					continue;
				}
			}

			concatenatedContent += tweet.getContent() + " ";
			tweetsConcatenated++;

			if(tweetsConcatenated >= Vars.CONCATENATION_WINDOW || processedTweetCount >= totalTweetCount - 1) {
				
				//if(concatenatedContent.startsWith("'")) {
				//	concatenatedContent = concatenatedContent.substring(1);
				//}
				//Log.getLogger().info("Stripped (and concatenated) tweet content: " + concatenatedContent);

				if(concatenatedContent.isEmpty() || concatenatedContent.equals(" ")) continue;

				SpotlightQuery spotlightQuery = new SpotlightQuery(spotlightURL);
				List<DBpediaResourceOccurrence> bestCandidates = spotlightQuery.annotate(concatenatedContent);

				/*Log.getLogger().info("Listing best cans.");
					for(DBpediaResourceOccurrence can : bestCandidates) {
						Log.getLogger().info("Best cans: " + can.getResource().getFullUri());
					}*/

				/*if(!occurrences.isEmpty()) {
						for (String key : occurrences.keySet()) {
							//Log.getLogger().info("Match: " + key + ": " + occurrences.get(key).get(0)); // Only show the first candidate.
						}
					}*/

				//List<DBpediaResourceOccurrence> bestCandidates = this.initBestCandidates(occurrences);

				RedisQuery redisQuery = new RedisQuery();
				redisQuery.collectAllTypes(bestCandidates, userOntology);

				tweetsConcatenated = 0;
				concatenatedContent = "";
			}

			processedTweetCount++;

			if(processedTweetCount >= totalTweetCount) break;
		}

		//if(!userOntology.isEmpty())
		//	TweetBase.getInstance().addUserOntology(user.getUserID(), userOntology);
	
	}
	
	/**
	 * Extract the best candidates from the full list of occurrences.
	 * 
	 * @return A list of the 1st ranked candidate DBpedia resource occurrences
	 */
	/*private List<DBpediaResourceOccurrence> initBestCandidates(Map<String, List<DBpediaResourceOccurrence>> occurrences) {
		List<DBpediaResourceOccurrence> bestCandidates = new ArrayList<DBpediaResourceOccurrence>();
		Collection<List<DBpediaResourceOccurrence>> col = occurrences.values();

		for (Iterator<List<DBpediaResourceOccurrence>> iterator = col.iterator(); iterator.hasNext();) {
			List<DBpediaResourceOccurrence> candidates = iterator.next();
			if(!candidates.isEmpty())
				bestCandidates.add(candidates.get(0));
		}
		
		return bestCandidates;
	}*/
	
	/**
	 * @return the startDate
	 */
	public long getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public long getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}
	
	public UserOntology getUserOntology() {
		return this.userOntology;
	}

	/**
	 * @return the spotlightURL
	 */
	public String getSpotlightURL() {
		return spotlightURL;
	}

	/**
	 * @param spotlightURL the spotlightURL to set
	 */
	public void setSpotlightURL(String spotlightURL) {
		this.spotlightURL = spotlightURL;
	}

	
}
