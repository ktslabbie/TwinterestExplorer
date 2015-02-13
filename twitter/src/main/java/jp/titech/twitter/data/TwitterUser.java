package jp.titech.twitter.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Class to represent a Twitter user.
 * Implements the Comparable interface since we want to be able to check for equality between Twitter users.
 * 
 * @author Kristian
 *
 */
@JsonIgnoreProperties({ "tweets" })
public class TwitterUser implements Comparable<TwitterUser> {

	private long 					userID;
	private String 					screenName;
	private Properties				properties;
	private float					englishRate;
	private List<Tweet>				tweets;
	private int						tweetCount;
	private UserOntology			userOntology;

	/**
	 * Construct a TwitterUser with the given parameters.
	 * 
	 * @param userID
	 * @param screenName
	 * @param userProperties
	 */
	public TwitterUser(long userID, String screenName, Properties properties, float englishRate) {
		this.userID = userID;
		this.screenName = screenName;
		this.properties = properties;
		this.englishRate = englishRate;
		this.tweets = new ArrayList<Tweet>();
		this.tweetCount = 0;
		this.userOntology = new UserOntology();
	}

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
			int followersCount, int friendsCount, int statusesCount, long createdAt, boolean isProtected, float englishRate, String profileImageURL) {

		this.userID = userID;
		this.screenName = screenName;
		this.properties = new Properties(name, description, location, followersCount, friendsCount, statusesCount, createdAt, isProtected, profileImageURL);
		this.englishRate = englishRate;
		this.tweets = new ArrayList<Tweet>();
		this.tweetCount = 0;
		this.userOntology = new UserOntology();
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

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties the user properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public float getEnglishRate() {
		return this.englishRate;
	}

	public void setEnglishRate(float englishRate) {
		this.englishRate = englishRate;
	}

	public List<Tweet> getTweets() {
		return this.tweets;
	}

	public void setTweets(List<Tweet> tweets) {
		this.tweets = tweets;
		this.tweetCount = tweets.size();
	}

	public void addTweet(Tweet tweet) {
		this.tweets.add(tweet);
		this.tweetCount++;
	}

	public boolean hasTweets() {
		return !tweets.isEmpty();
	}
	
	public int getTweetCount() {
		return this.tweetCount;
	}

	public void setTweetCount(int tweetCount) {
		this.tweetCount = tweetCount;
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
		return !userOntology.getOntology().isEmpty();
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

	public static class Properties {


		private String 					name;
		private String 					description;
		private String 					location;
		private int 					followersCount;
		private int 					friendsCount;
		private int 					statusesCount;
		private long 					createdAt;
		private boolean 				isProtectedUser;
		private String					profileImageURL;

		public Properties() {}

		/**
		 * Construct a UserProperties object with the given parameters.
		 * 
		 * @param name
		 * @param description
		 * @param location
		 * @param followersCount
		 * @param friendsCount
		 * @param statusesCount
		 * @param createdAt
		 * @param isProtectedUser
		 * @param profileImageURL
		 */
		public Properties(String name, String description, String location, 
				int followersCount, int friendsCount, int statusesCount, long createdAt, boolean isProtectedUser, String profileImageURL) {

			this.name = name;
			this.description = description;
			this.location = location;
			this.followersCount = followersCount;
			this.friendsCount = friendsCount;
			this.statusesCount = statusesCount;
			this.createdAt = createdAt;
			this.isProtectedUser = isProtectedUser;
			this.profileImageURL = profileImageURL.replace("normal", "200x200");
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

		public long getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(long createdAt) {
			this.createdAt = createdAt;
		}

		public boolean isProtectedUser() {
			return isProtectedUser;
		}

		public void setProtectedUser(boolean isProtectedUser) {
			this.isProtectedUser = isProtectedUser;
		}

		/**
		 * @return the profileImageURL
		 */
		public String getProfileImageURL() {
			return profileImageURL;
		}

		/**
		 * @param profileImageURL the profileImageURL to set
		 */
		public void setProfileImageURL(String profileImageURL) {
			this.profileImageURL = profileImageURL.replace("normal", "200x200");
		}
	}
}
