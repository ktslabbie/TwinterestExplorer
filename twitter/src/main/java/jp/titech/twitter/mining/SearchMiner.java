/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.mining;

import java.util.List;

import jp.titech.twitter.mining.Miner;
import jp.titech.twitter.util.Log;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class SearchMiner extends Miner {

	public SearchMiner(){
		super();
	}
	
	@Override
	public void mine() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mineUser(long userID, int count){
		
		Twitter twitter = new TwitterFactory().getInstance();
	    List<Status> statuses;
	    
		try {
			
			Log.getLogger().info("MyID: " + twitter.getId());
			statuses = twitter.getUserTimeline(userID, new Paging(1, count));
			
			for(Status status : statuses){
				Log.getLogger().info("Tweeted on: " + status.getCreatedAt() + " Content: " + status.getText());
				User user = status.getUser();
				tweetBase.addTweet(status.getId(), user.getId(), user.getScreenName(), status.getCreatedAt(), status.getText(), status.getPlace(), status.getGeoLocation(), status.getHashtagEntities());
				Log.getLogger().info("Successfully added to DB!");
			}
			
			/*Query query = new Query("obama");
		    QueryResult result = twitter.search(query);
		    for (Tweet tweet : result.getTweets()) {
		        System.out.println(tweet.getFromUser() + ":" + tweet.getText());
		    }*/
			
		} catch (TwitterException e) {
			Log.getLogger().error(e.getMessage());
		} 
		
		/*WebResource r = client.resource(Vars.TWITTER_SEARCH_API);
		String response = w.accept(
		        MediaType.APPLICATION_JSON_TYPE,
		        MediaType.APPLICATION_XML_TYPE).
		        header("q", "%40twitterapi%20-via").
		        get(String.class);
		
		Form f = new Form();
	    f.add("q", "@twitterapi @anywhere");
		
		 String response = r.path("search.json").
			        queryParams(f).
			        get(String.class);
		 
		 Data data = new Gson().fromJson(response, Data.class);
		 
		 Log.getLogger().info(data);*/
		 
	}
}
