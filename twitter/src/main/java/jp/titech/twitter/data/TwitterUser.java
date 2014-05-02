package jp.titech.twitter.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;

import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;

/**
 * Class to represent a Twitter user.
 * Implements the Comparable interface since we want to be able to check for equality between Twitter users.
 * 
 * @author Kristian
 *
 */
public class TwitterUser implements Comparable<TwitterUser> {

	private long 					userID;
	private String 					screenName, name, description, location;
	private int 					followersCount, friendsCount, statusesCount;
	private Date 					createdAt;
	private boolean 				isProtected;
	private double					englishRate;
	private List<Tweet>				tweets;
	private UserOntology			userOntology;
	private Map<String, Integer>	termFrequencyMap;
	private Map<String, Double>		tfidfMap;

	/**
	 * Construct a TwitterUser with the given parameters.
	 * 
	 * @param userID
	 * @param screenName
	 * @param name
	 * @param description
	 * @param location
	 * @param followersCount
	 * @param friendsCount
	 * @param statusesCount
	 * @param createdAt
	 * @param isProtected
	 */
	public TwitterUser(long userID, String screenName, String name, String description, String location, 
			int followersCount, int friendsCount, int statusesCount, Date createdAt, boolean isProtected, double englishRate) {

		this.userID = userID;
		this.screenName = screenName;
		this.name = name;
		this.description = description;
		this.location = location;
		this.followersCount = followersCount;
		this.friendsCount = friendsCount;
		this.statusesCount = statusesCount;
		this.createdAt = createdAt;
		this.isProtected = isProtected;
		this.englishRate = englishRate;
		this.tweets = new ArrayList<Tweet>();
		this.userOntology = new UserOntology();
		this.termFrequencyMap = new HashMap<String, Integer>();
		this.tfidfMap = new HashMap<String, Double>();
	}

	public long getUserID() {
		return userID;
	}

	public void setUserID(long userID) {
		this.userID = userID;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getFollowersCount() {
		return followersCount;
	}

	public void setFollowersCount(int followersCount) {
		this.followersCount = followersCount;
	}

	public int getFriendsCount() {
		return friendsCount;
	}

	public void setFriendsCount(int friendsCount) {
		this.friendsCount = friendsCount;
	}

	public int getStatusesCount() {
		return statusesCount;
	}

	public void setStatusesCount(int statusesCount) {
		this.statusesCount = statusesCount;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}
	
	public double getEnglishRate() {
		return this.englishRate;
	}
	
	public void setEnglishRate(double englishRate) {
		this.englishRate = englishRate;
	}

	public List<Tweet> getTweets() {
		if(!this.hasTweets()) {
			Log.getLogger().info("Gathering tweets of user " + this.toString() + " from DB...");
			this.tweets = TweetBase.getInstance().getTweets(this.userID);
		}	
		
		return this.tweets;
	}

	public void setTweets(List<Tweet> tweets) {
		this.tweets = tweets;
	}
	
	public void addTweet(Tweet tweet) {
		this.tweets.add(tweet);
	}
	
	public boolean hasTweet(long tweetID) {
		for (Tweet tweet : tweets)
			if(tweet.getTweetID() == tweetID) return true;
		return false;
	}
	
	public boolean hasTweets() {
		return !tweets.isEmpty();
	}
	
	/**
	 * @return the userOntology
	 */
	public UserOntology getUserOntology() {
		return userOntology;
	}

	/**
	 * @param userOntology the userOntology to set
	 */
	public void setUserOntology(UserOntology userOntology) {
		this.userOntology = userOntology;
	}
	
	public boolean hasUserOntology() {
		return !userOntology.isEmpty();
	}

	/**
	 * Custom compareTo method to make it possible to check two TwitterUsers for equality (based on user ID).
	 */
	public int compareTo(TwitterUser usr) {
		if(this.userID == usr.userID) {
			return 0;
		} else if(this.userID < usr.userID) {
			return -1;
		} else {
			return 1;
		}
	}

	/**
	 * Custom toString method (just print the Twitter screen name).
	 */
	@Override
	public String toString() {
		return "@" + screenName;
	}
	
	@Override
	public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(userID).toHashCode();
    }
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		
		if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final TwitterUser other = (TwitterUser) obj;
	    if ((this.userID == 0) || other.userID == 0) {
	        return false;
	    }
	    if (this.userID != other.userID) {
	        return false;
	    }
	    return true;
	}

	public Map<String, Integer> getTermFrequencyMap() {
		if(this.tfidfMap.isEmpty()) {
			for (Tweet tweet : this.tweets) {
				for (String term : tweet.getContent().split("\\s+")) {
					if(this.termFrequencyMap.get(term) != null) this.termFrequencyMap.put(term, this.termFrequencyMap.get(term)+1);
					else this.termFrequencyMap.put(term, 1);
				}
			}
		}
		
		return this.termFrequencyMap;
	}
	
	public Map<String, Double> getTFIDFMap() {
		return this.tfidfMap;
	}

	public void setTFIDFMap(Map<String, Double> userTFIDFMap) {
		this.tfidfMap = userTFIDFMap;
	}

	public String tfidfMapString() {
		Map<String, Double> tfidfSortedMap = Util.sortByValue(this.tfidfMap);
		String out = "Term\tTF-IDF\n";
		
		for (String term : tfidfSortedMap.keySet()) {
			out += term + "\t" + tfidfSortedMap.get(term) + "\n";
		}
		
		return out;
	}
}
