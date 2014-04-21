/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import java.io.File;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.ontology.OntologyBuilder;
import jp.titech.twitter.ontology.pruning.HighGeneralityPruner;
import jp.titech.twitter.ontology.pruning.LowOccurrencePruner;
import jp.titech.twitter.ontology.pruning.Pruner;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

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
	public void createUserOntology(TwitterUser user) {
		
		OntologyBuilder ob = new OntologyBuilder(user);
		ob.build();
		fullUserOntology = ob.getUserOntology();
		user.setUserOntology(fullUserOntology);

		this.applyPruning(user);
	}
	
	private void applyPruning(TwitterUser user) {
		
		Pruner pruner = null;
		prunedUserOntology = user.getUserOntology();
		
		if(!Vars.PRUNING_MODE.equals("NONE")) {
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
		}
		
		user.setUserOntology(prunedUserOntology);
 	}
	
	public void writeUserOntology(TwitterUser user) {
		Util.writeToFile(user.getUserOntology().toString(), new File(Vars.USER_DIRECTORY + Vars.PARAMETER_STRING + "/" + user.getScreenName() + "/ontology.txt"));
		if(!Vars.PRUNING_MODE.equals("NONE")) {
			Util.writeToFile(user.getUserOntology().toString(), 
					new File(Vars.USER_DIRECTORY + Vars.PARAMETER_STRING + "/" + user.getScreenName() + "/" + Vars.PRUNING_MODE + "/ontology.txt"));
		}
	}
	
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
