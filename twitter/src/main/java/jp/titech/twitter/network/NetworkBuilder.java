package jp.titech.twitter.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

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
public class NetworkBuilder {

	private TwitterUser 		seedUser;					// The seed user to start out with
	private Queue<TwitterUser> 	processQueue;
	private int					maxUsers;					// Maximum number of users to collect
	private Set<Long>			processed;					// Set of IDs already processed (to speed things up)
	private boolean				getFollowers = true;
	private int					userCount;
	private TwitterConnector 	connector;

	List<String> 				screenNames;

	public NetworkBuilder(TwitterUser seedUser, int maxSize, TwitterConnector connector) {
		this.screenNames = new ArrayList<String>();
		this.seedUser = seedUser;
		processQueue = new LinkedList<TwitterUser>();
		processQueue.add(this.seedUser);
		this.maxUsers = maxSize;
		this.processed = new HashSet<Long>();
		this.userCount = 1;
		this.connector = connector;
	}

	public void build() {
		boolean seedUser = true;
		TwitterUser currentUser;

		while(!processQueue.isEmpty()) {

			currentUser = processQueue.poll();

			if(seedUser || isValidUser(currentUser)) {
				
				seedUser = false;
				
				if(!processed.contains(currentUser.getUserID())) {
					Log.getLogger().info("Adding user # " + userCount + " (@" + currentUser.getScreenName() + ") to list.");
					screenNames.add(currentUser.getScreenName());
					userCount++;
				} else {
					Log.getLogger().info("User (@" + currentUser.getScreenName() + ") already exists. Skipping.");
				}

				if(userCount > maxUsers) break;
				if(!getFollowers) continue;
				
				Log.getLogger().info("Getting followers of @" + currentUser.getScreenName() + "...");
				
				processLocalUsers(currentUser);
				
				if(userCount <= maxUsers) {
					processRemoteUsers(currentUser, -1);
				}
				
			} else {
				Log.getLogger().info("User (@" + currentUser.getScreenName() + ") not valid. Skipping.");
			}
		}
	}
	
	private void processLocalUsers(TwitterUser currentUser) {
		List<Long> followerIDs = TweetBase.getInstance().getUserFollowers(currentUser.getUserID());
		TwitterUser follower;
		for (long id : followerIDs) {
			if(!processed.contains(id)) {
				follower = connector.getTwitterUserWithID(id);
				processFollower(follower, currentUser);
				if(userCount > maxUsers) break;
			}
		}
	}
	
	private void processRemoteUsers(TwitterUser currentUser, long cursor) {
		List<TwitterUser> followers = connector.getFollowersList(currentUser.getUserID(), cursor);
		
		for (TwitterUser follower : followers) {
			if(!processed.contains(follower.getUserID())) {
				processFollower(follower, currentUser);
				if(userCount > maxUsers) break;
			}
		}
		
		cursor = connector.getNextCursor();
		
		if(userCount <= maxUsers && cursor != 0) {
			processRemoteUsers(currentUser, cursor);
		}
	}
	
	private void processFollower(TwitterUser follower, TwitterUser currentUser) {
		if(isValidUser(follower) && follower.getUserID() != currentUser.getUserID()) {
			Log.getLogger().info("Adding user # " + userCount + " (@" + follower.getScreenName() + ") to list.");
			
			processQueue.add(follower);
			screenNames.add(follower.getScreenName());
			userCount++;
		}
		if(follower != null) processed.add(follower.getUserID());
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
	 * @return the seedUser
	 */
	public TwitterUser getSeedUser() {
		return seedUser;
	}

	/**
	 * @param seedUser the seedUser to set
	 */
	public void setSeedUser(TwitterUser seedUser) {
		this.seedUser = seedUser;
	}

	/**
	 * @return the processQueue
	 */
	public Queue<TwitterUser> getProcessQueue() {
		return processQueue;
	}

	/**
	 * @param processQueue the processQueue to set
	 */
	public void setProcessQueue(Queue<TwitterUser> processQueue) {
		this.processQueue = processQueue;
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
