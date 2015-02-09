package jp.titech.twitter.web;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.mining.UserMiner;
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.util.Vars;

@Path("/api/get-user")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
	
    private final String defaultName;

    public UserResource(String defaultName) {
        this.defaultName = defaultName;
    }

    @GET
    @Timed
    public TwitterUser getUser(@QueryParam("id") Optional<Long> id, @QueryParam("englishRate") Optional<Float> englishRate, 
    							@QueryParam("screenName") Optional<String> screenName, @QueryParam("concatenation") Optional<Integer> concatenation,
    							@QueryParam("confidence") Optional<Float> confidence, @QueryParam("support") Optional<Integer> support) {
    	
    	String name = screenName.or(defaultName);
    	TwitterConnector connector = new TwitterConnector(-1);
    	TwitterUser targetUser = (id.isPresent()) ? connector.getTwitterUserWithID(id.get()) : connector.getTwitterUserWithScreenName(name);
    	
    	// Return null if not found.
    	if(targetUser == null) return null;
    	
    	if(!targetUser.hasTweets()) {
    		
    		// User has no tweets yet, so mine some.
    		UserMiner miner = new UserMiner(targetUser, UserMiner.MINE_NONE, connector);
    		miner.mineUser();
    	}
    	
    	// If the user has enough English tweets...
    	if(targetUser.getEnglishRate() >= englishRate.or(Vars.MIN_ENGLISH_RATE)) {
    		
    		// Choose a DBpedia Spotlight instance based on the current user index (to assure even distribution).
    		/*int pi = index+1;
        	int portIndex = index;
        	if(index % Vars.SPOTLIGHT_PORTS.length == 0) index = -1;*/
        	String spotlightUrl = Vars.SPOTLIGHT_BASE_URL + ":" + Vars.SPOTLIGHT_PORTS[0];
    		
        	// Generate the ontology key.
        	float c = confidence.or(Vars.SPOTLIGHT_CONFIDENCE);
        	int s = support.or(Vars.SPOTLIGHT_SUPPORT);
        	int concat = concatenation.or(Vars.CONCATENATION_WINDOW);
        	String ontologyKey = Joiner.on(":").join(name.toLowerCase(), c, s, concat);
        	
        	// Get or create the user ontology.
			OntologyController ontologyController = new OntologyController(ontologyKey);
			ontologyController.getOrCreateUserOntology(targetUser, spotlightUrl);
    	}
    	
    	return targetUser;
    }
}