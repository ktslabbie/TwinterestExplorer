package jp.titech.twitter.web;

import jp.titech.twitter.data.TwitterUser;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TwitterUserJSON {

    private TwitterUser user;

    public TwitterUserJSON() {
        // Jackson deserialization
    }
    
    public TwitterUserJSON(TwitterUser user) {
    	this.user = user;
    }
    
    @JsonProperty
    public TwitterUser getUser() {
        return this.user;
    }
}
