/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 may. 2013
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

public class SearchMiner {
	
	private TweetBase tweetBase;

	public SearchMiner(){
		tweetBase = TweetBase.getInstance();
	}
	
	public void mine() {
		Twitter twitter = new TwitterFactory().getInstance();
	    List<Status> statuses;
	    
		/*try {
			
			
			
			for (int page = 1; page <= pages; page++) {
				statuses = twitter.getUserTimeline(userID, new Paging(page, 200));
				Log.getLogger().info("Mined page " + page + ". " + statuses.size() + " statuses found.");
				processStatuses(statuses);
			}
			
		} catch (TwitterException e) {
			Log.getLogger().error(e.getMessage());
		}*/
	}
	
	private void processStatuses(List<Status> statuses){
		for(Status status : statuses){
			String tweetText;
			UserMentionEntity[] userMentionEntities;
			HashtagEntity[] hashtagEntities;
			URLEntity[] urlEntities;
			MediaEntity[] mediaEntities;
			
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
			
			tweetBase.addTweet(status.getId(), user.getId(), user.getScreenName(), status.getCreatedAt(), tweetText, status.isRetweet(), status.getPlace(), 
					status.getGeoLocation(), userMentionEntities, hashtagEntities, urlEntities, mediaEntities, status.getIsoLanguageCode());
		}
	}
	
}
