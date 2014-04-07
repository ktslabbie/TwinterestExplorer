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
	

	private OntologyController() {}

	/**
	 * Create the ontology belonging to user by finding entities and doing some class pruning.
	 * 
	 * @param user
	 */
	public void createUserOntology(TwitterUser user) {
		
		OntologyBuilder ob = new OntologyBuilder(user);
		ob.build();
		UserOntology ontology = ob.getUserOntology();
		user.setUserOntology(ontology);
		Util.writeToFile(ontology.toString(), new File(Vars.USER_DIRECTORY + Vars.PARAMETER_STRING + "/" + user.getScreenName() + "/ontology.txt"));

		this.applyPruning(user);
	}
	
	private void applyPruning(TwitterUser user) {
		
		UserOntology prunedOntology = user.getUserOntology();
		Pruner pruner = null;
		
		if(!Vars.PRUNING_MODE.equals("NONE")) {
			if(Vars.PRUNING_MODE.equals("BOTH") || Vars.PRUNING_MODE.equals("LOW")) {
				Log.getLogger().info("Applying low-occurrence pruning.");
				
				pruner = new LowOccurrencePruner(user.getUserOntology());
				pruner.prune();
				prunedOntology = pruner.getPrunedUserOntology();
				Util.writeToFile(prunedOntology.toString(), new File(Vars.USER_DIRECTORY + Vars.PARAMETER_STRING + "/" + user.getScreenName() + "/LOP_pruned_ontology.txt"));
			}
			
			if(Vars.PRUNING_MODE.equals("BOTH") || Vars.PRUNING_MODE.equals("HIGH")) {
				Log.getLogger().info("Applying high-generality pruning.");
				
				if(Vars.PRUNING_MODE.equals("BOTH")) {
					pruner = new HighGeneralityPruner(user.getUserOntology(), prunedOntology);
				} else if(Vars.PRUNING_MODE.equals("HIGH")) {
					pruner = new HighGeneralityPruner(user.getUserOntology());
				}
				pruner.prune();
				prunedOntology = pruner.getPrunedUserOntology();
				Util.writeToFile(prunedOntology.toString(), new File(Vars.USER_DIRECTORY + Vars.PARAMETER_STRING + "/" + user.getScreenName() + "/HGP_pruned_ontology.txt"));
			}
		}
		
		user.setUserOntology(prunedOntology);
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
