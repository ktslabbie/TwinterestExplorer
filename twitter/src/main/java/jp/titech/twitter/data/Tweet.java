/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.data;
import java.util.Date;
import java.util.List;

import jp.titech.twitter.util.Util;

/**
 * A class to represent a tweet.
 * 
 * @author Kristian
 *
 */
public class Tweet {

	private long 			tweetID, userID;
	private String 			screenName, content, locationName, language;
	private boolean 		isRetweet;
	private Date 			createdAt;
	private List<String> 	userMentions, hashtags, URLs, media;
	
	/**
	 * Constructor for a Tweet.
	 * 
	 * @param tweetID
	 * @param userID
	 * @param screenName
	 * @param createdAt
	 * @param content
	 * @param isRetweet
	 * @param userMentions
	 * @param hashtags
	 * @param URLs
	 * @param media
	 * @param locationName
	 * @param language
	 */
	public Tweet(long tweetID, long userID, String screenName,  Date createdAt, String content, boolean isRetweet, List<String> userMentions, 
			List<String> hashtags, List<String> URLs, List<String> media, String locationName, String language) {
		
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
		this.language = language;
	}

	/**
	 * @return the tweetID
	 */
	public long getTweetID() {
		return tweetID;
	}

	/**
	 * @param tweetID the tweetID to set
	 */
	public void setTweetID(long tweetID) {
		this.tweetID = tweetID;
	}

	/**
	 * @return the userID
	 */
	public long getUserID() {
		return userID;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(long userID) {
		this.userID = userID;
	}

	/**
	 * @return the screenName
	 */
	public String getScreenName() {
		return screenName;
	}

	/**
	 * @param screenName the screenName to set
	 */
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the locationName
	 */
	public String getLocationName() {
		return locationName;
	}

	/**
	 * @param locationName the locationName to set
	 */
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
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

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * @return the userMentions
	 */
	public List<String> getUserMentions() {
		return userMentions;
	}

	/**
	 * @param userMentions the userMentions to set
	 */
	public void setUserMentions(List<String> userMentions) {
		this.userMentions = userMentions;
	}

	/**
	 * @return the hashtags
	 */
	public List<String> getHashtags() {
		return hashtags;
	}

	/**
	 * @param hashtags the hashtags to set
	 */
	public void setHashtags(List<String> hashtags) {
		this.hashtags = hashtags;
	}

	/**
	 * @return the uRLs
	 */
	public List<String> getURLs() {
		return URLs;
	}



	/**
	 * @param uRLs the uRLs to set
	 */
	public void setURLs(List<String> uRLs) {
		URLs = uRLs;
	}

	/**
	 * @return the media
	 */
	public List<String> getMedia() {
		return media;
	}

	/**
	 * @param media the media to set
	 */
	public void setMedia(List<String> media) {
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
				+ ", createdAt=" + createdAt + ", hashtags=" + hashtags + ", language=" + language + "]";
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
