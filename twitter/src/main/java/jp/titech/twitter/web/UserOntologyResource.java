package jp.titech.twitter.web;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBaseUtil;
import jp.titech.twitter.util.Vars;

@Path("/api/get-user-ontology")
@Produces(MediaType.APPLICATION_JSON)
public class UserOntologyResource {
	
    private final String defaultName;

    public UserOntologyResource(String defaultName) {
        this.defaultName = defaultName;
    }

    @GET
    @Timed
    public TwitterUserJSON getUser(@QueryParam("id") Optional<Long> id, @QueryParam("name") Optional<String> name, @QueryParam("concatenation") Optional<Integer> concatenation) {
    	TwitterUser targetUser;
    	if(concatenation.isPresent()) {
    		Vars.CONCATENATION_WINDOW = concatenation.get();
    	}
    	
    	if(id.isPresent()) {
    		targetUser = TweetBaseUtil.getTwitterUserWithID(id.get());
    	} else {
    		targetUser = TweetBaseUtil.getTwitterUserWithScreenName(name.or(defaultName));
    	}
    	
    	if(!targetUser.hasTweets()) {
    		MiningController mc = MiningController.getInstance();
        	mc.mineUser(targetUser);
    	}
    	
    	if(targetUser.getEnglishRate() > 0.8) {
    		OntologyController ontologyController = OntologyController.getInstance();
    		ontologyController.createUserOntology(targetUser);
            return new TwitterUserJSON(targetUser);
    	} else return null;
    }
}