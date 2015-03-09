package jp.titech.twitter.web;

import java.util.List;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.network.NetworkBuilder;

@Path("/api/get-followers-list")
@Produces(MediaType.APPLICATION_JSON)
public class UserFollowersListResource {
	
	private final String defaultName;

    public UserFollowersListResource(String defaultName) {
    	this.defaultName = defaultName;
    }

    @GET
    @Timed
    public List<String> getFollowersList(@QueryParam("screenName") Optional<String> screenName, @QueryParam("userCount") Optional<Integer> userCount) {
    	String name = screenName.or(defaultName);
		TwitterConnector connector = new TwitterConnector(-1);
		TwitterUser seedUser = connector.getTwitterUserWithScreenName(name);
		
		// We just want to get the followers of the seed user up to userCount number of followers.
		NetworkBuilder nb = new NetworkBuilder(seedUser, userCount.or(100), connector);
		nb.build();
		
		List<String> names = nb.getScreenNames();
		names.remove(0); // Remove the seed user.

		return names;
    }
}