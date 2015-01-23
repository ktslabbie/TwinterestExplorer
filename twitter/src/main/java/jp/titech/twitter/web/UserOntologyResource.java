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
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.util.Log;
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
    public TwitterUserJSON getUser(@QueryParam("index") Optional<Integer> index, @QueryParam("id") Optional<Long> id, 
    							@QueryParam("name") Optional<String> name, @QueryParam("concatenation") Optional<Integer> concatenation,
    							@QueryParam("c") Optional<Double> c, @QueryParam("s") Optional<Integer> s) {
    	
    	TwitterUser targetUser;
    	
    	if(c.isPresent()) Vars.SPOTLIGHT_CONFIDENCE = c.get();
    	if(s.isPresent()) Vars.SPOTLIGHT_SUPPORT = s.get();
    	if(concatenation.isPresent()) Vars.CONCATENATION_WINDOW = concatenation.get();
    	
    	TwitterConnector connector = new TwitterConnector(index.or(-1));
    	
    	if(id.isPresent())
    		targetUser = connector.getTwitterUserWithID(id.get());
    	else
    		targetUser = connector.getTwitterUserWithScreenName(name.or(defaultName));
    	
    	
    	if(!targetUser.hasTweets()) {
    		MiningController mc = MiningController.getInstance();
        	mc.mineUser(targetUser, connector);
    	}
    	
    	int portIndex = index.or(Vars.SPOTLIGHT_PORTS.length);
    	String spotlightUrl = Vars.SPOTLIGHT_BASE_URL + ":" + Vars.SPOTLIGHT_PORTS[portIndex % Vars.SPOTLIGHT_PORTS.length];
    	
    	if(targetUser.getEnglishRate() > Vars.MIN_ENGLISH_RATE) {
			OntologyController ontologyController = OntologyController.getInstance();
			ontologyController.createUserOntology(targetUser, spotlightUrl);
    	}
    	
    	return new TwitterUserJSON(targetUser);
    }
}