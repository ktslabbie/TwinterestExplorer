/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import java.util.HashMap;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.ontology.OntologyBuilder;
import jp.titech.twitter.ontology.types.OntologyType;

/**
 * Class to control most of the functionality of the program.
 * 
 * Implemented as a singleton class.
 * 
 * @author Kristian
 *
 */
public class OntologyController {

	private static OntologyController controller;
	private UserOntology fullUserOntology, prunedUserOntology;

	private OntologyController() {}

	/**
	 * Create the ontology belonging to user by finding entities and doing some class pruning.
	 * 
	 * @param user
	 */
	public void createUserOntology(TwitterUser user, String spotlightUrl) {
		
		OntologyBuilder ob = new OntologyBuilder(user, spotlightUrl);
		ob.build();
		fullUserOntology = ob.getUserOntology();
		//fullUserOntology.setOntology(new HashMap<OntologyType, Integer>());
		user.setUserOntology(fullUserOntology);

		/*if(!Vars.PRUNING_MODE.equals("NONE")) {
			this.applyPruning(user);
		}*/
	}
	
	/*private void applyPruning(TwitterUser user) {
		
		Pruner pruner = null;
		prunedUserOntology = user.getUserOntology();
		
		if(Vars.PRUNING_MODE.equals("BOTH") || Vars.PRUNING_MODE.equals("LOW")) {
			Log.getLogger().info("Applying low-occurrence pruning.");
			
			pruner = new LowOccurrencePruner(fullUserOntology);
			pruner.prune();
			prunedUserOntology = pruner.getPrunedUserOntology();
		}
		
		if(Vars.PRUNING_MODE.equals("BOTH") || Vars.PRUNING_MODE.equals("HIGH")) {
			Log.getLogger().info("Applying high-generality pruning.");
			
			if(Vars.PRUNING_MODE.equals("BOTH")) {
				pruner = new HighGeneralityPruner(fullUserOntology, prunedUserOntology);
			} else if(Vars.PRUNING_MODE.equals("HIGH")) {
				pruner = new HighGeneralityPruner(fullUserOntology);
			}
			pruner.prune();
			prunedUserOntology = pruner.getPrunedUserOntology();
		}
		
		user.setUserOntology(prunedUserOntology);
 	}*/
	
	/**
	 * Retrieve the Controller singleton instance.
	 * 
	 * @return the controller singleton
	 */
	public static OntologyController getInstance(){
		if(controller == null){ controller = new OntologyController(); }
		return controller;
	}
}
