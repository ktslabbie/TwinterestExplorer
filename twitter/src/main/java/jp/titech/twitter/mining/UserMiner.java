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
import jp.titech.twitter.db.TweetBaseUtil;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;
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

	private Twitter twitter;
	private TwitterUser user;
	private int tweetCount, englishCount;
	private int miningMode = 2;
	private boolean finished;
	
	public static final int MINE_NONE 	= 0;
	public static final int MINE_NEW 	= 1;
	public static final int MINE_ALL 	= 2;

	public UserMiner(TwitterUser user, int miningMode){
		this.twitter = new TwitterFactory().getInstance();
		this.user = user;
		this.tweetCount = 0;
		this.englishCount = 0;
		this.finished = false;
		this.miningMode = miningMode;
		
		if(!user.hasTweets()) {
			Log.getLogger().info("Gathering user @" + user.getScreenName() + "'s tweets from DB (if present)...");
			user.setTweets(TweetBase.getInstance().getTweets(user.getUserID()));
		}
	}
	
	public void mineUser() {
		
		if(miningMode == 0 && user.hasTweets()) return;
		
		List<Status> statuses;
	    tweetCount = 0;
	    englishCount = 0;
	    
	    int pages = Vars.TIMELINE_TWEET_COUNT / 200 + 1;
	    
		Log.getLogger().info("Mining user: @" + user.getScreenName() + ". Mining " + Vars.TIMELINE_TWEET_COUNT + " tweets, over " + pages +" pages.");
	    
		try {
			for (int page = 1; page <= pages; page++) {
				statuses = twitter.getUserTimeline(user.getUserID(), new Paging(page, 200));
				Log.getLogger().info("Retrieved page " + page + ". " + statuses.size() + " statuses found.");
				processStatuses(statuses);
				if(finished) break;
			}
			
		} catch (TwitterException e) {
			Log.getLogger().error(e.getMessage());
			
			if(e.getErrorCode() == 88){
				int secondsUntil =  e.getRateLimitStatus().getSecondsUntilReset();
				Log.getLogger().error("Error getting tweets (rate limit exceeded). Retry in " + secondsUntil + " seconds...");
				try {
					Thread.sleep(secondsUntil*1000);
					mineUser();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private void processStatuses(List<Status> statuses) {
		String tweetText;
		UserMentionEntity[] userMentionEntities;
		HashtagEntity[] hashtagEntities;
		URLEntity[] urlEntities;
		MediaEntity[] mediaEntities;
		
		for(Status status : statuses) {
			tweetCount++;
			
			if(status.getIsoLanguageCode().equals("en")) {
				englishCount++;
			}
			
			if(!user.hasTweet(status.getId())) {
			
				if(status.isRetweet()) {
					Status retweetedStatus = status.getRetweetedStatus();
					tweetText = retweetedStatus.getText();
					userMentionEntities = retweetedStatus.getUserMentionEntities();
					hashtagEntities = retweetedStatus.getHashtagEntities();
					urlEntities = retweetedStatus.getURLEntities();
					mediaEntities = retweetedStatus.getMediaEntities();
					Log.getLogger().info("Retweeted on: " + status.getCreatedAt() + ", Language: " + status.getIsoLanguageCode() + ", Content: " + tweetText);
				} else {
					tweetText = status.getText();
					userMentionEntities = status.getUserMentionEntities();
					hashtagEntities = status.getHashtagEntities();
					urlEntities = status.getURLEntities();
					mediaEntities = status.getMediaEntities();
					Log.getLogger().info("Tweeted on: " + status.getCreatedAt() + ", Language: " + status.getIsoLanguageCode() + ", Content: " + tweetText);
				}
				
				User user = status.getUser();
				
				TweetBase.getInstance().addTweet(status.getId(), user.getId(), user.getScreenName(), status.getCreatedAt(), tweetText, status.isRetweet(), status.getPlace(), 
						status.getGeoLocation(), userMentionEntities, hashtagEntities, urlEntities, mediaEntities, status.getIsoLanguageCode());
			} else {
				if(miningMode == 1) {
					Log.getLogger().info("Found an already existing tweet. We are done.");
					finished = true;
				}
				return;
			}
		}
	}
	
	public double getEnglishRate() {
		return (double)englishCount / (double)tweetCount;
	}
}
