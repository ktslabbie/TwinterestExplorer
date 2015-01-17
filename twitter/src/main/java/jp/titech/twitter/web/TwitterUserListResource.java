package jp.titech.twitter.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.network.NetworkBuilder;
import jp.titech.twitter.util.Log;

@Path("/api/get-user-list")
@Produces(MediaType.APPLICATION_JSON)
public class TwitterUserListResource {

    public TwitterUserListResource() {
    }

    @GET
    @Timed
    public TwitterUserListJSON getNetwork(@QueryParam("list") String userRelevanceList) {
    	
    	if(userRelevanceList != null && !userRelevanceList.isEmpty()) {
    		List<TwitterUser> users = new ArrayList<TwitterUser>();
    		TwitterConnector connector = new TwitterConnector();
    		
    		for(String screenName : userRelevanceList.split(",")) {
    			Log.getLogger().info("Retrieving @" + screenName + "...");
    			users.add(connector.getTwitterUserWithScreenName(screenName));
    		}
    		
    		return new TwitterUserListJSON(users);
    		
    	} else {
    		return null;
    	}
    }
}