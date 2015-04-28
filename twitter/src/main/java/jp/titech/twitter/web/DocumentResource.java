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
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.TwitterUser.Properties;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.mining.UserMiner;
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

@Path("/api/get-document")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentResource {

    private final String defaultName;

    public DocumentResource(String defaultName) {
        this.defaultName = defaultName;
    }

    @GET
    @Timed
    public TwitterUser getDocument(@QueryParam("n") Optional<String> screenName, @QueryParam("t") Optional<String> text,
                                   @QueryParam("c") Optional<Float> confidence, @QueryParam("s") Optional<Integer> support) {

        String name = screenName.or(defaultName);
        //TwitterConnector connector = new TwitterConnector(-1);
        //TwitterUser targetUser = (id.isPresent()) ? connector.getTwitterUserWithID(id.get()) : connector.getTwitterUserWithScreenName(name);

        // Return null if not found.
        //if(targetUser == null) return null;

        TwitterUser targetUser = TweetBase.getInstance().getUser(name);
        if(targetUser == null) {
            long id = System.currentTimeMillis();
            targetUser = new TwitterUser(id, name, new Properties(name, "", "", 0, 0, 0, id-1, false, ""), 1.0f);
            Tweet doc = new Tweet(id+1, id, id-1);
            doc.setContent(text.or(""));
            Log.getLogger().info("Text set: " + text);
            targetUser.addTweet(doc);
            //TweetBase.getInstance().addUser(targetUser);
        }

        // If the doc is already contained...

        // Choose a DBpedia Spotlight instance based on the current user index (to assure even distribution).
		/*int pi = index+1;
    	int portIndex = index;
    	if(index % Vars.SPOTLIGHT_PORTS.length == 0) index = -1;*/
        String spotlightUrl = Vars.SPOTLIGHT_BASE_URL + ":" + Vars.SPOTLIGHT_PORTS[0];

        // Generate the ontology key.
        float c = confidence.or(Vars.SPOTLIGHT_CONFIDENCE);
        int s = support.or(Vars.SPOTLIGHT_SUPPORT);
        String[] parts = name.split("\\.");
        String docID = parts[parts.length-2];

        String ontologyKey = Joiner.on(":").join("doc", docID, c, s, 1);

        // Get or create the user ontology.
        OntologyController ontologyController = new OntologyController(ontologyKey);
        ontologyController.getOrCreateUserOntology(targetUser, spotlightUrl, c, s, 1);


        return targetUser;
    }
}