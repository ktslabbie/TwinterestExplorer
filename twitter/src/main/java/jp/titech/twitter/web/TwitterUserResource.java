package jp.titech.twitter.web;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.mining.api.TwitterConnector;

@Path("/api/get-twitter-user")
@Produces(MediaType.APPLICATION_JSON)
public class TwitterUserResource {
	
    private final String defaultName;

    public TwitterUserResource(String defaultName) {
        this.defaultName = defaultName;
    }

    @GET
    @Timed
    public TwitterUserJSON getUser(@QueryParam("id") Optional<Long> id, @QueryParam("name") Optional<String> name) {
    	TwitterUser targetUser;
    	TwitterConnector connector = new TwitterConnector();
    	
    	if(id.isPresent()) {
    		targetUser = connector.getTwitterUserWithID(id.get());
    	} else {
    		targetUser = connector.getTwitterUserWithScreenName(name.or(defaultName));
    	}
    	
        return new TwitterUserJSON(targetUser);
    }
}