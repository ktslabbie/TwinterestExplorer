/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.ontology.OntologyBuilder;

/**
 * Class to control most of the functionality of the program.
 * 
 * Implemented as a singleton class.
 * 
 * @author Kristian
 *
 */
public class OntologyController {

	private UserOntology userOntology;
	private final String ontologyKey;

	public OntologyController(String ontologyKey) {
		this.ontologyKey = ontologyKey;
	}
	
	/**
	 * Get the ontology belonging to user. Return an empty map if not yet in DB.
	 * 
	 * @param user The user
	 */
	public void getUserOntology(TwitterUser user) {
		userOntology = TweetBase.getInstance().getUserOntology(ontologyKey);
		user.setUserOntology(userOntology);
	}

	/**
	 * Create the ontology belonging to user by finding entities.
	 * 
	 * @param user The user
	 * @param spotlightUrl An instance of DBpedia Spotlight to connect to.
	 */
	public void getOrCreateUserOntology(TwitterUser user, String spotlightUrl, float c, int s, int concat) {
		userOntology = TweetBase.getInstance().getUserOntology(ontologyKey);
		
		if(userOntology.getOntology().isEmpty()) {
			OntologyBuilder ob = new OntologyBuilder(user, spotlightUrl, c, s, concat);
			ob.build();
			userOntology = ob.getUserOntology();
			if(!userOntology.getOntology().isEmpty()) {
				TweetBase.getInstance().addUserOntology(ontologyKey, userOntology);
			}
		}
		
		user.setUserOntology(userOntology);
	}
}
