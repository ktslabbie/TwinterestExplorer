package jp.titech.twitter.web;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.TwitterUserSimple;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TwitterNetworkJSON {

    private List<TwitterUserSimple> twitterUsers;

    public TwitterNetworkJSON() {
        // Jackson deserialization
    }
    
    public TwitterNetworkJSON(DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph) {
    	this.twitterUsers = new ArrayList<TwitterUserSimple>();
    	for (TwitterUser user : twitterUserGraph.vertexSet()) {
    		this.twitterUsers.add(new TwitterUserSimple(user));
    	}
    }
    
    @JsonProperty
    public List<TwitterUserSimple> getNetwork() {
        return this.twitterUsers;
    }
}
