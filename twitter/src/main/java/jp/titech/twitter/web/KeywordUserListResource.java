package jp.titech.twitter.web;

import java.util.List;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.network.KeywordUserBuilder;

@Path("/api/get-keyword-user-list")
@Produces(MediaType.APPLICATION_JSON)
class KeywordUserListResource {
	
	private final String defaultKeyword;

    public KeywordUserListResource(String defaultKeyword) {
    	this.defaultKeyword = defaultKeyword;
    }

    @GET
    @Timed
    public List<String> getFollowersList(@QueryParam("keyword") Optional<String> keyword, @QueryParam("userCount") Optional<Integer> userCount) {
    	String kw = keyword.or(defaultKeyword);
		TwitterConnector connector = new TwitterConnector(-1);
		
		// We just want to get the followers of the seed user up to userCount number of followers.
		KeywordUserBuilder nb = new KeywordUserBuilder(kw, userCount.or(100), connector);
		nb.build();

		return nb.getScreenNames();
    }
}