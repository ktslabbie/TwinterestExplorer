package jp.titech.twitter.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.util.Log;

/**
 * Helper class with convenience functions for dealing with the database.
 * 
 * @author Kristian
 *
 */
public class TweetBaseUtil {

	private static TweetBase tweetBase = TweetBase.getInstance();

	/**
	 * Get a list of TwitterUsers from a directory with files in the format {identifier}#{username}-{parameters}.
	 * 
	 * @param dir The directory to process
	 * @return a List of TwitterUsers
	 */
	public static List<TwitterUser> getTwitterUsersFromDirectory(File dir) {
		List<TwitterUser> userList = new ArrayList<TwitterUser>();

		for (String fileName : dir.list()) {
			String userName = fileName.split("#")[1].split("-")[0];
			userList.add(tweetBase.getUser(userName));
		}

		return userList;
	}

	/**
	 * Obtain the twitter user with the given ID - either from the local DB (if exists) or fetch from the Twitter Web API.
	 * 
	 * @param userID
	 * @return the TwitterUser
	 */
	public static TwitterUser getTwitterUserWithID(long userID) {

		TwitterUser twitterUser = null;

		if((twitterUser = tweetBase.getUser(userID)) == null) {
			User user = null;

			try {
				Twitter twitter = new TwitterFactory().getInstance();
				user = twitter.showUser(userID);

				if(user != null) // Can a user be null here?
					twitterUser = tweetBase.addUser(user);
				
			} catch (TwitterException e) {
				if(e.getErrorCode() == 63)	{
					Log.getLogger().error("Error getting user (suspended account). Skip this user.");
					return null;
				} else if(e.getErrorCode() == 88){
					int secondsUntil =  e.getRateLimitStatus().getSecondsUntilReset();
					Log.getLogger().error("Error getting user (rate limit exceeded). Retry in " + secondsUntil + " seconds...");
					try {
						Thread.sleep(secondsUntil*1000);
						return getTwitterUserWithID(userID);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return twitterUser;
	}

	public static TwitterUser getTwitterUserWithScreenName(String screenName) {

		TwitterUser twitterUser = null;

		if((twitterUser = tweetBase.getUser(screenName)) == null) {
			
			Log.getLogger().info("User @" + screenName + " not in DB! Try to get from Twitter API...");
			User user = null;

			try {
				Twitter twitter = new TwitterFactory().getInstance();
				user = twitter.showUser(screenName);

				if(user != null) // Can a user be null here?
					twitterUser = tweetBase.addUser(user);
				
			} catch (TwitterException e) {
				if(e.getErrorCode() == 63)	{
					Log.getLogger().error("Error getting user (suspended account). Skip this user.");
					return null;
				} else if(e.getErrorCode() == 88){
					int secondsUntil =  e.getRateLimitStatus().getSecondsUntilReset();
					Log.getLogger().error("Error getting user (rate limit exceeded). Retry in " + secondsUntil + " seconds...");
					try {
						Thread.sleep(secondsUntil*1000);
						return getTwitterUserWithScreenName(screenName);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return twitterUser;
	}


	public static List<Long> getFollowersIDs(long userID, int cursor) {
		IDs ids = null;
		Twitter twitter = new TwitterFactory().getInstance();

		if(!tweetBase.isUserFollowersContained(userID)) {

			try {
				ids = twitter.getFollowersIDs(userID, -1);
				for (long id : ids.getIDs()) {
					tweetBase.addUserFollower(userID, id);
				}

			} catch (TwitterException e) {
				if(e.getErrorCode() == 88){
					int secondsUntil =  e.getRateLimitStatus().getSecondsUntilReset();
					Log.getLogger().error("Error getting followers (rate limit exceeded). Retry in " + secondsUntil + " seconds...");
					try {
						Thread.sleep(secondsUntil*1000);
						return getFollowersIDs(userID, cursor);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}

		return tweetBase.getUserFollowers(userID);
	}
	
	public static void initEnglishRate(TwitterUser user) {
		Log.getLogger().info("Initializing English rates for users already in DB...");
		
		List<Tweet> tweets = user.getTweets();
		int englishCount = 0;
		int tweetCount = 0;
		
		for (Tweet tweet : tweets) {
			tweetCount++;
			if(tweet.getLanguage().equals("en")) englishCount++;
		}
		double englishRate = (double)englishCount / (double)tweetCount;
		
		Log.getLogger().info("English rate for user @" + user.getScreenName() + ": " + englishRate);
		
		tweetBase.updateUserEnglishRate(user.getUserID(), englishRate);
	}
}
