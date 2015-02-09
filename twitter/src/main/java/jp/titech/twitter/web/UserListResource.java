package jp.titech.twitter.web;

import java.util.ArrayList;
import java.util.List;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.util.Log;

@Path("/api/get-user-list")
@Produces(MediaType.APPLICATION_JSON)
public class UserListResource {

    public UserListResource() {}

    @GET
    @Timed
    public List<TwitterUser> getUserList(@QueryParam("list") String userRelevanceList) {
    	
    	if(userRelevanceList != null && !userRelevanceList.isEmpty()) {
    		List<TwitterUser> users = new ArrayList<TwitterUser>();
    		TwitterConnector connector = new TwitterConnector();
    		
    		for(String screenName : userRelevanceList.split(",")) {
    			Log.getLogger().info("Retrieving @" + screenName + "...");
    			users.add(connector.getTwitterUserWithScreenName(screenName));
    		}
    		
    		return users;
    		
    	} else {
    		return null;
    	}
    }
}