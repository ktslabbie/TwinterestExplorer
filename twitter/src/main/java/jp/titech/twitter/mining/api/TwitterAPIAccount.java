package jp.titech.twitter.mining.api;

import jp.titech.twitter.util.Log;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

class TwitterAPIAccount {
	
	private Twitter twitterAccount;
	private int secondsUntilReset = -1;
	private int index;

	public TwitterAPIAccount(String consumerKey, String cosumerSecret, String accessToken, String accessTokenSecret, int index) {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey(consumerKey)
		  .setOAuthConsumerSecret(cosumerSecret)
		  .setOAuthAccessToken(accessToken)
		  .setOAuthAccessTokenSecret(accessTokenSecret);
		
		twitterAccount = new TwitterFactory(cb.build()).getInstance();
		this.index = index;
	}
	
	public Twitter getTwitterAccount() {
		return this.twitterAccount;
	}
	
	public int getSecondsUntilReset() {
		return this.secondsUntilReset;
	}
	
	public void setSecondsUntilReset(int seconds) {
		this.secondsUntilReset = seconds;
	}

	public int getIndex() {
		return this.index;
	}
	
	@Override
	public String toString() {
		String ret = "";
		
		try {
			ret = "API Account (" + this.index + "): @" + this.twitterAccount.getScreenName() + ". Reset in: " + this.secondsUntilReset;
		} catch (IllegalStateException | TwitterException e) {
			Log.getLogger().error("Cannot obtain screenname. API authentication error?");
			e.printStackTrace();
		}
		
		return ret;
	}
}