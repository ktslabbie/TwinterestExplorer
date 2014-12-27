package jp.titech.twitter.web;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.TwitterUserSimple;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TwitterUserListJSON {

    private List<TwitterUser> twitterUsers;

    public TwitterUserListJSON() {
        // Jackson deserialization
    }
    
    public TwitterUserListJSON(List<TwitterUser> twitterUsers) {
    	this.twitterUsers = twitterUsers;
    }
    
    @JsonProperty
    public List<TwitterUser> getTwitterUserList() {
        return this.twitterUsers;
    }
}
