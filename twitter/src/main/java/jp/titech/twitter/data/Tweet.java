/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.data;

import java.util.ArrayList;
import java.util.Date;

public class Tweet {

	private long tweetID, userID;
	private String screenName, content, locationName;
	private Date createdAt;
	private ArrayList<String> hashtags;
	
	public Tweet(long tweetID, long userID, String screenName,  Date createdAt, String content, ArrayList<String> hashtags, String locationName) {
		
		this.tweetID = tweetID;
		this.userID = userID;
		this.screenName = screenName;
		this.content = content;
		this.createdAt = createdAt;
		this.hashtags = hashtags;
		this.locationName = locationName;
	}

	public long getTweetID() {
		return tweetID;
	}

	public void setTweetID(long tweetID) {
		this.tweetID = tweetID;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	public ArrayList<String> getHashtags() {
		return hashtags;
	}

	public void setHashtags(ArrayList<String> hashtags) {
		this.hashtags = hashtags;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	@Override
	public String toString() {
		return "Tweet [tweetID=" + tweetID + ", userID=" + userID
				+ ", screenName=" + screenName + ", content=" + content
				+ ", locationName=" + locationName + ", createdAt=" + createdAt
				+ ", hashtags=" + hashtags + "]";
	}
}
