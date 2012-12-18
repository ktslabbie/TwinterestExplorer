/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import java.util.Map;

import org.dbpedia.spotlight.model.OntologyType;

import jp.titech.twitter.mining.Miner;
import jp.titech.twitter.mining.UserMiner;
import jp.titech.twitter.ontology.OntologyBuilder;
import jp.titech.twitter.ontology.pruning.HighGeneralityPruner;
import jp.titech.twitter.ontology.pruning.LowOccurrencePruner;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

public class Controller {

	private Miner miner;
	private static Controller controller;
	
	private Controller(){}
	
	public static Controller getInstance(){
		if(controller == null){
			controller = new Controller();
		}
		return controller;
	}

	/**
	 * 
	 * @param userID
	 * @param count Number of tweets to collect.
	 */
	public void startSearchMining(int userID, int count) {
		miner = new UserMiner();
		miner.mineUser(userID, count);
	}
	
	public void createOntology(int userID) {
		OntologyBuilder ob = new OntologyBuilder(userID);
		ob.build();
	}
	
	public void createOntology(int userID, int count) {
		OntologyBuilder ob = new OntologyBuilder(userID, count);
		ob.build();
		Map<OntologyType, Integer> ontology = ob.getOntology();
		LowOccurrencePruner lop = new LowOccurrencePruner(ontology);
		lop.prune();
		Log.getLogger().info("Low-occurrence pruned full ontology: " + lop.getPrunedFullOntology());
		
		HighGeneralityPruner hgp = new HighGeneralityPruner(lop.getPrunedFullOntology());
		hgp.prune();
		Log.getLogger().info("High-generality pruned full ontology: " + hgp.printMapCSV());
	}
}
