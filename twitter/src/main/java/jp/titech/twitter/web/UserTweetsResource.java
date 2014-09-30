package jp.titech.twitter.web;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBaseUtil;

@Path("/api/get-user-tweets")
@Produces(MediaType.APPLICATION_JSON)
public class UserTweetsResource {

    public UserTweetsResource() {}

    @GET
    @Timed
    public UserTweetsJSON getTweets(@QueryParam("name") String name) {
    	TwitterUser user = TweetBaseUtil.getTwitterUserWithScreenName(name);
    	MiningController mc = MiningController.getInstance();
    	mc.mineUser(user);
        
		return new UserTweetsJSON(user.getTweets(), user.getEnglishRate());
    }
}