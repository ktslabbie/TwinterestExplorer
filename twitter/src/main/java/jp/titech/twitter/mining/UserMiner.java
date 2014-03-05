/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.mining;

import java.util.List;

import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.util.Log;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class UserMiner {

	private TweetBase tweetBase;
	private int tweetCount, englishCount;

	public UserMiner(){
		tweetBase = TweetBase.getInstance();
	}
	
	public void mineUser(long userID, int count){
		
		Twitter twitter = new TwitterFactory().getInstance();
	    List<Status> statuses;
	    tweetCount = 0;
	    englishCount = 0;
	    
		try {
			Log.getLogger().info("UserID to mine: " + userID);
			
			int pages = count / 200 + 1;
			Log.getLogger().info("Mining " + count + " tweets, over " + pages +" pages.");
			
			for (int page = 1; page <= pages; page++) {
				statuses = twitter.getUserTimeline(userID, new Paging(page, 200));
				Log.getLogger().info("Mined page " + page + ". " + statuses.size() + " statuses found.");
				processStatuses(statuses);
			}
			
		} catch (TwitterException e) {
			Log.getLogger().error(e.getMessage());
		}
	}
	
	public void mineUser(String screenName, int count){
		
		Twitter twitter = new TwitterFactory().getInstance();
	    List<Status> statuses;
	    tweetCount = 0;
	    englishCount = 0;
	    
		try {
			Log.getLogger().info("Screenname to mine: " + screenName);
			
			int pages = count / 200 + 1;
			Log.getLogger().info("Mining " + count + " tweets, over " + pages +" pages.");
			
			for (int page = 1; page <= pages; page++) {
				statuses = twitter.getUserTimeline(screenName, new Paging(page, 200));
				Log.getLogger().info("Mined page " + page + ". " + statuses.size() + " statuses found.");
				processStatuses(statuses);
			}
			
		} catch (TwitterException e) {
			Log.getLogger().error(e.getMessage());
			
			if(e.getErrorCode() == 88){
				int secondsUntil =  e.getRateLimitStatus().getSecondsUntilReset();
				Log.getLogger().error("Error getting tweets (rate limit exceeded). Retry in " + secondsUntil + " seconds...");
				try {
					Thread.sleep(secondsUntil*1000);
					mineUser(screenName, count);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void processStatuses(List<Status> statuses){
		for(Status status : statuses){
			String tweetText;
			UserMentionEntity[] userMentionEntities;
			HashtagEntity[] hashtagEntities;
			URLEntity[] urlEntities;
			MediaEntity[] mediaEntities;
			tweetCount++;
			
			if(status.isRetweet()){
				Status retweetedStatus = status.getRetweetedStatus();
				tweetText = retweetedStatus.getText();
				userMentionEntities = retweetedStatus.getUserMentionEntities();
				hashtagEntities = retweetedStatus.getHashtagEntities();
				urlEntities = retweetedStatus.getURLEntities();
				mediaEntities = retweetedStatus.getMediaEntities();
				Log.getLogger().info("Retweeted on: " + status.getCreatedAt() + " Content: " + tweetText);
			} else {
				tweetText = status.getText();
				userMentionEntities = status.getUserMentionEntities();
				hashtagEntities = status.getHashtagEntities();
				urlEntities = status.getURLEntities();
				mediaEntities = status.getMediaEntities();
				Log.getLogger().info("Tweeted on: " + status.getCreatedAt() + " Content: " + tweetText);
			}
			
			User user = status.getUser();
			
			Log.getLogger().info("Status lang code: " + status.getIsoLanguageCode());
			
			if(status.getIsoLanguageCode().equals("en")) {
				englishCount++;
			}
			
			tweetBase.addTweet(status.getId(), user.getId(), user.getScreenName(), status.getCreatedAt(), tweetText, status.isRetweet(), status.getPlace(), 
					status.getGeoLocation(), userMentionEntities, hashtagEntities, urlEntities, mediaEntities, status.getIsoLanguageCode());
		}
	}
	
	public double getEnglishRate() {
		return (double)englishCount / (double)tweetCount;
	}
	
}
