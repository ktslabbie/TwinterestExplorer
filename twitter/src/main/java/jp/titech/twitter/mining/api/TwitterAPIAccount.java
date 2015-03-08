package jp.titech.twitter.mining.api;

import jp.titech.twitter.util.Log;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

class TwitterAPIAccount {
	
	private final Twitter twitterAccount;
	private int secondsUntilReset = -1;
	private final int index;

	public TwitterAPIAccount(String consumerKey, String cosumerSecret, String accessToken, String accessTokenSecret, int index) {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		cb.setDebugEnabled(true).setApplicationOnlyAuthEnabled(true)
		  .setOAuthConsumerKey(consumerKey)
		  .setOAuthConsumerSecret(cosumerSecret);
		
		twitterAccount = new TwitterFactory(cb.build()).getInstance();
		
		try {
			OAuth2Token token = twitterAccount.getOAuth2Token();
			twitterAccount.setOAuth2Token(token);
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
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
			ret = "Application-only API Account (" + this.index + "). Reset in: " + this.secondsUntilReset;
		} catch (IllegalStateException e) {
			Log.getLogger().error("Cannot obtain screenname. API authentication error?");
			e.printStackTrace();
		}
		
		return ret;
	}
}