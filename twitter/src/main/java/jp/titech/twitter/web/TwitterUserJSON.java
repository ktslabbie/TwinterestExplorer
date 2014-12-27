package jp.titech.twitter.web;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TwitterUserJSON {

    private TwitterUser user;

    public TwitterUserJSON() {
        // Jackson deserialization
    }
    
    public TwitterUserJSON(TwitterUser user) {
    	this.user = user;
    	
    	/*for (Tweet tweet : this.user.getTweets()) {
			tweet.stripNonHashtagElements();
		}*/
    }
    
    @JsonProperty
    public TwitterUser getUser() {
        return this.user;
    }
}
