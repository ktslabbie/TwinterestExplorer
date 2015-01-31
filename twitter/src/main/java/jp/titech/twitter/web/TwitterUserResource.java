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
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

@Path("/api/get-twitter-user")
@Produces(MediaType.APPLICATION_JSON)
public class TwitterUserResource {

	private final String defaultName;

	public TwitterUserResource(String defaultName) {
		this.defaultName = defaultName;
	}

	@GET
	@Timed
	public TwitterUser getUser(@QueryParam("id") Optional<Long> id, @QueryParam("screenName") Optional<String> screenName, 
								@QueryParam("concatenation") Optional<Integer> concatenation, 
								@QueryParam("confidence") Optional<Float> confidence, @QueryParam("support") Optional<Integer> support) {

		String name = screenName.or(defaultName);
		TwitterConnector connector = new TwitterConnector(-1);
		TwitterUser targetUser = (id.isPresent()) ? connector.getTwitterUserWithID(id.get()) : connector.getTwitterUserWithScreenName(name);
		
		// If the user has tweets he may have an ontology as well. Get if so.
    	if(targetUser != null && !targetUser.getTweets().isEmpty()) {
    		
        	// Generate the ontology key.
        	float c = confidence.or(Vars.SPOTLIGHT_CONFIDENCE);
        	int s = support.or(Vars.SPOTLIGHT_SUPPORT);
        	int concat = concatenation.or(Vars.CONCATENATION_WINDOW);
        	String ontologyKey = Joiner.on(":").join(name.toLowerCase(), c, s, concat);
        	
        	Log.getLogger().info("Ontology key: " + ontologyKey);
        	
        	// Get  the user ontology if present.
			OntologyController ontologyController = new OntologyController(ontologyKey);
			ontologyController.getUserOntology(targetUser);
    	}

		return targetUser;
	}
}
