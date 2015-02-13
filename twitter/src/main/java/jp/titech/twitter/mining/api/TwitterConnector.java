package jp.titech.twitter.mining.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.sun.jersey.impl.ApiMessages;

import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.util.Log;

/**
 * Helper class with convenience functions for dealing with the Twitter API and database.
 * 
 * @author Kristian
 *
 */
public class TwitterConnector {

	private TweetBase tweetBase;
	private long nextCursor = -1;
	private TwitterAPIAccountManager accountManager;
	private TwitterAPIAccount twitterAPIAccount;
	
	public TwitterConnector() {
		tweetBase = TweetBase.getInstance();
		this.accountManager = TwitterAPIAccountManager.getInstance();
		this.twitterAPIAccount = this.accountManager.getRandomAccount();
		
	}
	
	public TwitterConnector(int apiAccountIndex) {
		tweetBase = TweetBase.getInstance();
		this.accountManager = TwitterAPIAccountManager.getInstance();
		this.twitterAPIAccount = this.accountManager.getTwitterAccount(apiAccountIndex);
	}

	/**
	 * Obtain the twitter user with the given ID - either from the local DB (if exists) or fetch from the Twitter Web API.
	 * 
	 * @param userID
	 * @return the TwitterUser
	 */
	public TwitterUser getTwitterUserWithID(long userID) {

		TwitterUser twitterUser = null;

		if((twitterUser = tweetBase.getUser(userID)) == null) {
			User user = null;

			try {
				user = twitterAPIAccount.getTwitterAccount().showUser(userID);

				if(user != null) // Can a user be null here?
					twitterUser = tweetBase.addUser(convertUser(user));

			} catch (TwitterException e) {
				if(e.getErrorCode() == 63)	{
					Log.getLogger().error("Error getting user (suspended account). Skip this user.");
					return null;
				} else if(e.getErrorCode() == 88) {
					int secondsUntil = e.getRateLimitStatus().getSecondsUntilReset();
					Log.getLogger().error("Error getting user (rate limit exceeded). Retry with a new account (or wait)...");
					retry(secondsUntil);
					return getTwitterUserWithID(userID);
				}
			}
		}
		return twitterUser;
	}

	public TwitterUser getTwitterUserWithScreenName(String screenName) {

		TwitterUser twitterUser = null;

		if((twitterUser = tweetBase.getUser(screenName)) == null) {

			Log.getLogger().info("User @" + screenName + " not in DB! Try to get from Twitter API...");
			User user = null;

			try {
				user = twitterAPIAccount.getTwitterAccount().showUser(screenName);

				if(user != null) // null = no user with this screenName on Twitter. 
					twitterUser = tweetBase.addUser(convertUser(user));

			} catch (TwitterException e) {
				if(e.getErrorCode() == 63)	{
					Log.getLogger().error("Error getting user (suspended account). Skip this user.");
					return null;
				} else if(e.getErrorCode() == 88){
					int secondsUntil = e.getRateLimitStatus().getSecondsUntilReset();
					Log.getLogger().error("Error getting user (rate limit exceeded). Retry with a new account (or wait)...");
					retry(secondsUntil);
					return getTwitterUserWithScreenName(screenName);
				}
			}
		}
		return twitterUser;
	}

	public List<TwitterUser> getFollowersList(long userID, long cursor) {
		PagableResponseList<User> followers = null;
		List<TwitterUser> users = new ArrayList<TwitterUser>();

		try {
			followers = twitterAPIAccount.getTwitterAccount().getFollowersList(userID, cursor, 5000);
			nextCursor = followers.getNextCursor();

			for (User follower: followers) {
				users.add(tweetBase.addUser(convertUser(follower)));
				tweetBase.addUserFollower(userID, follower.getId());
			}

		} catch (TwitterException e) {
			if(e.getErrorCode() == 88){
				int secondsUntil = e.getRateLimitStatus().getSecondsUntilReset();
				Log.getLogger().error("Error getting followers (rate limit exceeded). Retry with a new account...");
				retry(secondsUntil);
				return getFollowersList(userID, cursor);
			}
		}

		return users;
	}
	
	public Set<TwitterUser> getKeywordUsersList(String keyword) {
		Set<TwitterUser> twitterUsers = new HashSet<TwitterUser>();
		
		try {
			Query q = new Query();
			q.setLang("en"); q.setCount(100); q.setQuery(keyword);
			if(nextCursor > 0) q.setMaxId(nextCursor);
			
			QueryResult res = twitterAPIAccount.getTwitterAccount().search(q);
			
			List<Status> statuses = res.getTweets();
			nextCursor = statuses.get(0).getId();
			
			for(Status s : statuses) {
				twitterUsers.add(tweetBase.addUser(convertUser(s.getUser())));
				if(s.getId() < nextCursor) nextCursor = s.getId();
			}
			
			nextCursor--;
			return twitterUsers;

		} catch (TwitterException e) {
			if(e.getErrorCode() == 88){
				int secondsUntil = e.getRateLimitStatus().getSecondsUntilReset();
				Log.getLogger().error("Error getting keyword users (rate limit exceeded). Retry with a new account...");
				retry(secondsUntil);
				return getKeywordUsersList(keyword);
			}
		}

		return twitterUsers;
	}

	public void resetNextCursor() {
		nextCursor = -1;
	}

	public long getNextCursor() {
		return nextCursor;
	}

	/**
	 * Retrieve the tweets from a user's timeline from the Twitter API.
	 * If we get a rate-limit exception, ask for a new account from the account manager.
	 * If we get any other exception (usually "Not authorized" or "Deleted user"), return null.
	 * 
	 * @param userID
	 * @param paging
	 * @return
	 */
	public List<Status> getUserTimeline(long userID, Paging paging) {
		List<Status> statusList = new ArrayList<Status>();
		
		try {
			statusList = twitterAPIAccount.getTwitterAccount().getUserTimeline(userID, paging);
		} catch (TwitterException e) {
			Log.getLogger().error(e.getMessage());

			if(e.getErrorCode() == 88){
				int secondsUntil = e.getRateLimitStatus().getSecondsUntilReset();
				Log.getLogger().error("Error getting tweets (rate limit exceeded). Retry with a new account...");
				retry(secondsUntil);
				return getUserTimeline(userID, paging);
			}
			
			return null;
		}
		return statusList;
	}
	
	private void retry(int secondsUntil) {
		twitterAPIAccount.setSecondsUntilReset(secondsUntil);
		
		TwitterAPIAccount newAccount = accountManager.getNextAccount(twitterAPIAccount.getIndex());
		if(newAccount == null) {
			Log.getLogger().error("No available accounts. Wait " + secondsUntil + " seconds.");
			try {
				Thread.sleep((secondsUntil+10)*1000);
				twitterAPIAccount.setSecondsUntilReset(-1);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
		} else {
			twitterAPIAccount = newAccount;
		}
	}
	
	private TwitterUser convertUser(User user) {
		return new TwitterUser(user.getId(), user.getScreenName(), user.getName(), user.getDescription(), user.getLocation(),
				user.getFollowersCount(), user.getFriendsCount(), user.getStatusesCount(), user.getCreatedAt().getTime(), user.isProtected(), -1.0f, user.getProfileImageURL());
	}
}
