package jp.titech.twitter.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

import jp.titech.twitter.util.Log;

public class TwitterUser implements Comparable<TwitterUser> {

	private long userID;
	private String screenName;
	private String name;
	private String description;
	private String location;
	private int followersCount;
	private int friendsCount;
	private int statusesCount;
	private Date createdAt;
	private boolean isProtected;
	private List<Tweet> tweets;

	public TwitterUser(long userID) {
		this.userID = userID;
		this.tweets = new ArrayList<Tweet>();
	}

	public TwitterUser(String screenName) {
		this.userID = 0;
		this.screenName = screenName;
		this.tweets = new ArrayList<Tweet>();
	}

	public TwitterUser(long userID, String screenName, String name, String description, String location, 
			int followersCount, int friendsCount, int statusesCount, Date createdAt, boolean isProtected) {

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
		this.tweets = new ArrayList<Tweet>();
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

	public List<Tweet> getTweets() {
		return tweets;
	}

	public void setTweets(List<Tweet> tweets) {
		this.tweets = tweets;
	}

	
	public int compareTo(TwitterUser usr) {
		//Log.getLogger().info("Comparing " + this.screenName + " (" + this.userID + ") to " + usr.screenName + " (" + usr.userID + ")...");
		
		if(this.userID == usr.userID) {
			//Log.getLogger().info("Same!");
			return 0;
		} else if(this.userID < usr.userID) {
			//Log.getLogger().info("Different!");
			return -1;
		} else {
			//Log.getLogger().info("Different!");
			return 1;
		}
	}

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
}
