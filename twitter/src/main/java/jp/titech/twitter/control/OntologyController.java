/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import java.io.File;
import java.util.Map;

import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.OntologyType;
import org.dbpedia.spotlight.model.SchemaOrgType;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.ontology.OntologyBuilder;
import jp.titech.twitter.ontology.pruning.HighGeneralityPruner;
import jp.titech.twitter.ontology.pruning.LowOccurrencePruner;
import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.FreebaseType;
import jp.titech.twitter.ontology.types.YAGOType;
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

	private static OntologyController 	controller;

	private OntologyController() {}

	/*public void createOntology(TwitterUser user, int count, String topic, int rank) {
		
		UserOntology userOntology = user.getUserOntology();
		
		if(userOntology.isEmpty()) {
			OntologyBuilder ob = new OntologyBuilder(user);
			
			ob.setStartDate(Util.getDate(2012, 12, 15));
			ob.setEndDate(Util.getDate(2012, 12, 19));
			ob.build();
			
			userOntology.setOntology(ob.getOntology());
		}
		
		LowOccurrencePruner lop = new LowOccurrencePruner(userOntology.getOntology());
		lop.prune();
		Log.getLogger().info("Low-occurrence pruned full ontology: " + lop.getPrunedFullOntology());

		HighGeneralityPruner hgp = new HighGeneralityPruner(lop.getPrunedFullOntology(), userOntology.getOntology());
		hgp.prune();
		Log.getLogger().info("High-generality pruned full ontology: " + hgp.printPrunedMapTSV("type_output_" + topic + "_" + rank + "_mine#" + user.getScreenName() + ".csv"));

		OntologyBuilder obOld = new OntologyBuilder(user);
		obOld.setStartDate(Util.getDate(2012, 12, 20));
		obOld.setEndDate(Util.getDate(2013, 2, 9));
		obOld.build();
		Map<OntologyType, Integer> ontologyOld = obOld.getOntology();
		LowOccurrencePruner lopOld = new LowOccurrencePruner(ontologyOld);
		lopOld.prune();
		Log.getLogger().info("Low-occurrence pruned full ontology: " + lopOld.getPrunedFullOntology());

		HighGeneralityPruner hgpOld = new HighGeneralityPruner(lopOld.getPrunedFullOntology(), ontologyOld);
		hgpOld.prune();
		Log.getLogger().info("High-generality pruned full ontology: " + hgpOld.printPrunedMapTSV("type_output_" + topic + "_" + rank + "_nonmine#" + user.getScreenName() + ".csv"));
		hgpOld.getPrunedYAGOTypes();
	}*/

	/**
	 * Create the ontology belonging to user by finding entities and doing some class pruning.
	 * 
	 * @param user
	 */
	public void createUserOntology(TwitterUser user) {
		
		OntologyBuilder ob = new OntologyBuilder(user);
		ob.build();
		UserOntology ontology = ob.getUserOntology();

		Util.writeToFile(ontology.toString(), new File(Vars.USER_DIRECTORY + user.getScreenName() + "/ontology.txt"));

		LowOccurrencePruner lop = new LowOccurrencePruner(ontology);
		lop.prune();
		UserOntology prunedOntology = lop.getPrunedUserOntology();
		
		Util.writeToFile(prunedOntology.toString(), new File(Vars.USER_DIRECTORY + user.getScreenName() + "/LOP_pruned_ontology.txt"));

		HighGeneralityPruner hgp = new HighGeneralityPruner(ontology, prunedOntology);
		hgp.prune();
		UserOntology hgpPrunedOntology = hgp.getPrunedUserOntology();
		
		Util.writeToFile(hgpPrunedOntology.toString(), new File(Vars.USER_DIRECTORY + user.getScreenName() + "/HGP_pruned_ontology.txt"));
		/*Log.getLogger().info("High-generality pruned full ontology: " + hgp.printPrunedMapTSV("class_output_user#" + user.getScreenName() + "-" + Vars.CONCATENATION_WINDOW + 
				"-window-c" + Vars.SPOTLIGHT_CONFIDENCE + "s" + Vars.SPOTLIGHT_SUPPORT + ".csv"));*/
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
