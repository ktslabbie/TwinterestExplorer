package jp.titech.twitter.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Lists;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

/**
 * Class to build graphs of Twitter users.
 * 
 * @author Kristian
 *
 */
public class KeywordUserBuilder {

	private String		 		keyword;					// The keyword to search for
	private int					maxUsers;					// Maximum number of users to collect
	private TwitterConnector 	connector;
	private List<String>	 	screenNames;

	public KeywordUserBuilder(String keyword, int maxSize, TwitterConnector connector) {
		this.screenNames = new ArrayList<String>();
		this.keyword = keyword;
		this.maxUsers = maxSize;
		this.connector = connector;
	}

	public void build() {
		Set<TwitterUser> twitterUsers = new HashSet<TwitterUser>();
		
		while(twitterUsers.size() < maxUsers) {
			Set<TwitterUser> newUsers = connector.getKeywordUsersList(keyword);
			
			for(TwitterUser user : newUsers) {
				if(isValidUser(user)) {
					Log.getLogger().info("Adding user @" + user.getScreenName() + " to the result.");
					twitterUsers.add(user);
				}
			}
		}
		
		int count = 0;
		
		for(Iterator<TwitterUser> it = twitterUsers.iterator(); it.hasNext();) {
			TwitterUser user = it.next();
			screenNames.add(user.getScreenName());
			count++;
			if(count == maxUsers) break;
		}
	}

	/**
	 * Check for user validity based on the constraints imposed in the properties file.
	 * 
	 * @param currentUser
	 * @return true if valid
	 */
	private boolean isValidUser(TwitterUser currentUser) {
		return  currentUser != null &&
				currentUser.getProperties().getFollowersCount()	>= Vars.MIN_FOLLOWERS &&
				currentUser.getProperties().getFollowersCount()	<= Vars.MAX_FOLLOWERS &&
				currentUser.getProperties().getFriendsCount() 	>= Vars.MIN_FRIENDS &&
				currentUser.getProperties().getFriendsCount() 	<= Vars.MAX_FRIENDS && 
				currentUser.getProperties().getStatusesCount() 	>= Vars.MIN_TWEETS &&
				currentUser.getProperties().getStatusesCount() 	<= Vars.MAX_TWEETS && 
				!currentUser.getProperties().isProtectedUser();
	}

	/**
	 * @return the maxUsers
	 */
	public int getMaxUsers() {
		return maxUsers;
	}

	/**
	 * @param maxUsers the maxUsers to set
	 */
	public void setMaxUsers(int maxSize) {
		this.maxUsers = maxSize;
	}

	/**
	 * @return the screenNames
	 */
	public List<String> getScreenNames() {
		return screenNames;
	}

	/**
	 * @param screenNames the screenNames to set
	 */
	public void setScreenNames(List<String> screenNames) {
		this.screenNames = screenNames;
	}
}
