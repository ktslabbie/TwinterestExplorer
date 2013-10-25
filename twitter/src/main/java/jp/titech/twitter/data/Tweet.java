/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.data;

import java.util.ArrayList;
import java.util.Date;

import jp.titech.twitter.util.Util;

public class Tweet {

	private long tweetID, userID;
	private String screenName, content, locationName;
	private boolean isRetweet;
	private Date createdAt;
	private ArrayList<String> userMentions, hashtags, URLs, media;
	
	
	public Tweet(long tweetID, long userID, String screenName,  Date createdAt, String content, boolean isRetweet, ArrayList<String> userMentions, ArrayList<String> hashtags, ArrayList<String> URLs, ArrayList<String> media, String locationName) {
		
		this.tweetID = tweetID;
		this.userID = userID;
		this.screenName = screenName;
		this.createdAt = createdAt;
		this.content = content;
		this.isRetweet = isRetweet;
		this.userMentions = userMentions;
		this.hashtags = hashtags;
		this.URLs = URLs;
		this.media = media;
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

	/**
	 * @return the isRetweet
	 */
	public boolean isRetweet() {
		return isRetweet;
	}

	/**
	 * @param isRetweet the isRetweet to set
	 */
	public void setRetweet(boolean isRetweet) {
		this.isRetweet = isRetweet;
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

	/**
	 * @return the userMentions
	 */
	public ArrayList<String> getUserMentions() {
		return userMentions;
	}

	/**
	 * @param userMentions the userMentions to set
	 */
	public void setUserMentions(ArrayList<String> userMentions) {
		this.userMentions = userMentions;
	}

	/**
	 * @return the uRLs
	 */
	public ArrayList<String> getURLs() {
		return URLs;
	}

	/**
	 * @param uRLs the uRLs to set
	 */
	public void setURLs(ArrayList<String> uRLs) {
		URLs = uRLs;
	}

	/**
	 * @return the media
	 */
	public ArrayList<String> getMedia() {
		return media;
	}

	/**
	 * @param media the media to set
	 */
	public void setMedia(ArrayList<String> media) {
		this.media = media;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Tweet [tweetID=" + tweetID + ", userID=" + userID
				+ ", screenName=" + screenName + ", content=" + content
				+ ", locationName=" + locationName + ", isRetweet=" + isRetweet
				+ ", createdAt=" + createdAt + ", hashtags=" + hashtags + "]";
	}
	
	/**
	 * Strips the username mentions from tweet text.
	 */
	public void stripUserMentions() {
		//setContent(getContent().replaceAll("@([A-Za-z0-9_]+)", ""));
		if(!getUserMentions().isEmpty()) {
			for (String userMention : getUserMentions()) {
				setContent(getContent().replaceAll("@"+userMention, ""));
			}
		}
	}
	
	/**
	 * Strips the hashtags from tweet text.
	 */
	public void stripHashtags() {
		if(!getHashtags().isEmpty()) {
			for (String hashtag : getHashtags()) {
				setContent(getContent().replaceAll("#"+hashtag, ""));
			}
		}
	}

	/**
	 * Strips the URLs from tweet text.
	 */
	public void stripURLs() {
		//setContent(getContent().replaceAll("http://.+?(com|net|org)/{0,1}", ""));
		if(!getURLs().isEmpty()) {
			for (String url : getURLs()) {
				setContent(getContent().replaceAll(url, ""));
			}
		}
	}
	
	/**
	 * Strips the media mentions from tweet text.
	 */
	public void stripMedia() {
		if(!getMedia().isEmpty()) {
			for (String media : getMedia()) {
				setContent(getContent().replaceAll(media, ""));
			}
		}
	}
	
	
	/**
	 * Strips the stopwords and netslang from tweet text.
	 */
	public void stripStopwords() {
		setContent(Util.removeStopwords(getContent()));
	}

	/**
	 *  Strips user mentions, hashtags, URLs and media from tweet text.
	 */
	public void stripEverything() {
		this.stripUserMentions();
		this.stripHashtags();
		this.stripURLs();
		this.stripMedia();
		this.stripStopwords();
		setContent(getContent().trim());
	}
}
