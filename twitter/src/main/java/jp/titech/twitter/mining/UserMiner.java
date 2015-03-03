/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.mining;

import java.util.List;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class UserMiner {

	private TwitterUser twitterUser;
	private int timelineTweetCount, tweetCount, englishCount;
	private int miningMode;
	private boolean finished;
	private TwitterConnector connector;
	
	public static final int MINE_NONE 	= 0;
	public static final int MINE_NEW 	= 1;
	public static final int MINE_ALL 	= 2;

	public UserMiner(TwitterUser user, int miningMode, int timelineTweetCount, TwitterConnector connector){
		this.twitterUser = user;
		this.timelineTweetCount = timelineTweetCount;
		this.tweetCount = 0;
		this.englishCount = 0;
		this.finished = false;
		this.miningMode = miningMode;
		this.connector = connector;
	}
	
	public TwitterUser mineUser() {
		if(miningMode == 0 && twitterUser.hasTweets()) return twitterUser;
		
		List<Status> statuses = null;
	    tweetCount = 0;
	    englishCount = 0;
	    
	    int pages = ((timelineTweetCount-1)/200) + 1; // -1 to make it exclusive the 200, 400, 600, etc. edge cases.
	    int rest  = timelineTweetCount % 200;
	    
		Log.getLogger().info("Mining user: @" + twitterUser.getScreenName() + ". Mining " + timelineTweetCount + " tweets.");
	    
		for (int page = 1; page <= pages; page++) {
			
			// Obtain statuses from the Twitter API.
			statuses = connector.getUserTimeline(twitterUser.getUserID(), new Paging(page, 200));
			
			if(statuses == null) return twitterUser;
			
			// If this is the last page, but we still need some tweets...
			if(page == pages && rest > 0 && rest <= statuses.size()) {
				statuses = statuses.subList(0, rest-1);
			}
			
			processStatuses(statuses);
			if(finished) break;
		}
		
		// Calculate and save the rate of English tweets for this user.
		float englishRate = (float)englishCount / (float)tweetCount;
		twitterUser.setEnglishRate(englishRate);
		
		// We should save now to make sure we don't re-mine again, regardless of minimum English rate.
		TweetBase.getInstance().updateUserTweets(twitterUser);
		
		return twitterUser;
	}
	
	/**
	 * Method to process tweets before saving them to the database.
	 * 
	 * We clean the content of tweets by extracting hashtags, urls, etc.
	 * 
	 * @param statuses The statuses to process.
	 */
	private void processStatuses(List<Status> statuses) {
		UserMentionEntity[] userMentionEntities;
		HashtagEntity[] hashtagEntities;
		URLEntity[] urlEntities;
		MediaEntity[] mediaEntities;
		
		
		for(Status status : statuses) {
			tweetCount++;
			if(status.getLang().equals("en")) englishCount++;
			
			//if(!twitterUser.hasTweet(status.getId())) {  // This check is too slow. TODO: good way to deal with miningmode = 1
			Tweet tweet = new Tweet(status.getId(), twitterUser.getUserID(), status.getCreatedAt().getTime());
		
			if(status.isRetweet()) {
				Status retweetedStatus = status.getRetweetedStatus();
				tweet.setRetweet(true);
				tweet.setContent(retweetedStatus.getText());
				userMentionEntities = retweetedStatus.getUserMentionEntities();
				hashtagEntities = retweetedStatus.getHashtagEntities();
				urlEntities = retweetedStatus.getURLEntities();
				mediaEntities = retweetedStatus.getMediaEntities();
			} else {
				tweet.setContent(status.getText());
				userMentionEntities = status.getUserMentionEntities();
				hashtagEntities = status.getHashtagEntities();
				urlEntities = status.getURLEntities();
				mediaEntities = status.getMediaEntities();
			}
			
			if(status.getPlace() != null) tweet.setLocationName(status.getPlace().getFullName());
			tweet.setLanguage(status.getLang());
			
			for(UserMentionEntity entity : userMentionEntities) {
				tweet.addUserMention(entity.getText());
			}
			
			for(HashtagEntity entity : hashtagEntities) {
				tweet.addHashtag(entity.getText());
			}
			
			for(URLEntity entity : urlEntities) {
				tweet.addURL(entity.getText());
			}
			
			for(MediaEntity entity : mediaEntities) {
				tweet.addMedia(entity.getText());
			}
			
			tweet.stripNonHashtagElements();
			twitterUser.addTweet(tweet);
		}
	}
}
