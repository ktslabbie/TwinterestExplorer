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
	private TwitterUser twitterUser;
	private int tweetCount, englishCount;
	private int miningMode = 2;
	private boolean finished;
	
	public static final int MINE_NONE 	= 0;
	public static final int MINE_NEW 	= 1;
	public static final int MINE_ALL 	= 2;

	public UserMiner(TwitterUser user, int miningMode){
		this.twitter = new TwitterFactory().getInstance();
		this.twitterUser = user;
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
		
		if(miningMode == 0 && twitterUser.hasTweets()) return;
		
		List<Status> statuses;
	    tweetCount = 0;
	    englishCount = 0;
	    
	    int pages = Vars.TIMELINE_TWEET_COUNT / 200 + 1;
	    
		Log.getLogger().info("Mining user: @" + twitterUser.getScreenName() + ". Mining " + Vars.TIMELINE_TWEET_COUNT + " tweets, over " + pages +" pages.");
	    
		try {
			for (int page = 1; page <= pages; page++) {
				statuses = twitter.getUserTimeline(twitterUser.getUserID(), new Paging(page, 200));
				Log.getLogger().info("Retrieved page " + page + ". " + statuses.size() + " statuses found.");
				processStatuses(statuses);
				if(finished) break;
			}
			
			this.saveEnglishRate();
			
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
			
			if(status.getLang().equals("en")) {
				englishCount++;
			}
			
			if(!twitterUser.hasTweet(status.getId())) {
				
				User user = status.getUser();
				Tweet tweet = new Tweet(status.getId(), user.getId(), user.getScreenName(), status.getCreatedAt());
			
				if(status.isRetweet()) {
					Status retweetedStatus = status.getRetweetedStatus();
					tweetText = retweetedStatus.getText();
					userMentionEntities = retweetedStatus.getUserMentionEntities();
					hashtagEntities = retweetedStatus.getHashtagEntities();
					urlEntities = retweetedStatus.getURLEntities();
					mediaEntities = retweetedStatus.getMediaEntities();
					Log.getLogger().info("Retweeted on: " + status.getCreatedAt() + ", Language: " + status.getLang() + ", Content: " + tweetText);
				} else {
					tweetText = status.getText();
					userMentionEntities = status.getUserMentionEntities();
					hashtagEntities = status.getHashtagEntities();
					urlEntities = status.getURLEntities();
					mediaEntities = status.getMediaEntities();
					Log.getLogger().info("Tweeted on: " + status.getCreatedAt() + ", Language: " + status.getLang() + ", Content: " + tweetText);
				}
				
				tweet.setContent(tweetText);
				tweet.setRetweet(status.isRetweet());
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
				
				twitterUser.addTweet(tweet);
				TweetBase.getInstance().addTweet(tweet);
				
			} else {
				if(miningMode == 1) {
					Log.getLogger().info("Found an already existing tweet. We are done.");
					finished = true;
				}
				return;
			}
		}
	}
	
	public void saveEnglishRate() {
		if(twitterUser.getEnglishRate() == -1.0) {
			double englishRate = (double)englishCount / (double)tweetCount;
			twitterUser.setEnglishRate(englishRate);
			TweetBase.getInstance().updateUserEnglishRate(twitterUser.getUserID(), englishRate);
		}
	}
}
