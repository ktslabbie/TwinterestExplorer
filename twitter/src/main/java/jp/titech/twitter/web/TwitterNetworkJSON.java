package jp.titech.twitter.web;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TwitterNetworkJSON {

    private List<TwitterUser> twitterUsers;

    public TwitterNetworkJSON() {
        // Jackson deserialization
    }
    
    public TwitterNetworkJSON(DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph) {
    	this.twitterUsers = new ArrayList<TwitterUser>();
    	for (TwitterUser user : twitterUserGraph.vertexSet()) {
    		this.twitterUsers.add(user);
    	}
    }
    
    @JsonProperty
    public List<TwitterUser> getNetwork() {
        return this.twitterUsers;
    }
}
