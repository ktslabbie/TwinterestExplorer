package jp.titech.twitter.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
public class TwitterUserSimple {

	private long 					userID;
	private String 					screenName, name, description, location, profileImageURL;
	private int 					followersCount, friendsCount, statusesCount;
	private double					englishRate;
	private Date 					createdAt;

	
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
	 */
	public TwitterUserSimple(TwitterUser user) {

		this.userID = user.getUserID();
		this.screenName = user.getScreenName();
		this.name = user.getName();
		this.description = user.getDescription();
		this.location = user.getLocation();
		this.followersCount = user.getFollowersCount();
		this.friendsCount = user.getFriendsCount();
		this.statusesCount = user.getStatusesCount();
		this.createdAt = user.getCreatedAt();
		this.englishRate = user.getEnglishRate();
		this.profileImageURL = user.getProfileImageURL();
		
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
	 */
	public TwitterUserSimple(long userID, String screenName, String name, String description, String location, 
			int followersCount, int friendsCount, int statusesCount, Date createdAt, double englishRate, String profileImageURL) {

		this.userID = userID;
		this.screenName = screenName;
		this.name = name;
		this.description = description;
		this.location = location;
		this.followersCount = followersCount;
		this.friendsCount = friendsCount;
		this.statusesCount = statusesCount;
		this.createdAt = createdAt;
		this.setEnglishRate(englishRate);
		this.profileImageURL = profileImageURL;
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
	
	public double getEnglishRate() {
		return englishRate;
	}

	public void setEnglishRate(double englishRate) {
		this.englishRate = englishRate;
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
		this.profileImageURL = profileImageURL;
	}	
}
