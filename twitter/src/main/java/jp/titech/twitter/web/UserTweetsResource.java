/*package jp.titech.twitter.web;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.mining.api.TwitterConnector;

@Path("/api/get-user-tweets")
@Produces(MediaType.APPLICATION_JSON)
public class UserTweetsResource {

    public UserTweetsResource() {}

    @GET
    @Timed
    public UserTweetsJSON getTweets(@QueryParam("name") String name) {
    	TwitterConnector connector = new TwitterConnector();
    	TwitterUser user = connector.getTwitterUserWithScreenName(name);
    	
    	
    	MiningController mc = MiningController.getInstance();
    	mc.mineUser(user, connector);
    	
        //if(user.getEnglishRate() > 0.8)
			return new UserTweetsJSON(user.getTweets(), user.getEnglishRate());
        //else return new UserTweetsJSON(null, user.getEnglishRate());
    }
}*/