package jp.titech.twitter.web;

import java.util.List;

import jp.titech.twitter.data.Tweet;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserTweetsJSON {

    private List<Tweet> tweets;
    private double englishRate;

    public UserTweetsJSON() {
        // Jackson deserialization
    }
    
    public UserTweetsJSON(List<Tweet> tweets, double englishRate) {
    	
    	for (Tweet tweet : tweets) {
			tweet.stripNonHashtagElements();
		}
    	
    	this.tweets = tweets;
    	this.englishRate = englishRate;
    }
    
    @JsonProperty
    public List<Tweet> getTweets() {
        return this.tweets;
    }
    
    @JsonProperty
    public double getEnglishRate() {
        return this.englishRate;
    }
}
