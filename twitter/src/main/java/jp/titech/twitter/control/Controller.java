/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import java.util.Date;
import java.util.Map;

import org.dbpedia.spotlight.model.OntologyType;

import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.mining.UserMiner;
import jp.titech.twitter.ontology.OntologyBuilder;
import jp.titech.twitter.ontology.pruning.HighGeneralityPruner;
import jp.titech.twitter.ontology.pruning.LowOccurrencePruner;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

public class Controller {

	private UserMiner userMiner;
	private static Controller controller;
	private String screenName = "";
	
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
		userMiner = new UserMiner();
		userMiner.mineUser(userID, count);
	}
	
	/**
	 * 
	 * @param userID
	 * @param count Number of tweets to collect.
	 */
	public void startSearchMining(String tScreenName, int count) {
		userMiner = new UserMiner();
		userMiner.mineUser(tScreenName, count);
		screenName = tScreenName;
	}
	
	public void createOntology(long userID) {
		OntologyBuilder ob = new OntologyBuilder(userID);
		ob.build();
	}
	
	public void createOntology(long userID, String userName, int count, String topic, int rank) {
		OntologyBuilder ob = new OntologyBuilder(userID, count);
		ob.setStartDate(new Date(112, 11, 15));
		ob.setEndDate(new Date(112, 11, 19));
		ob.build();
		Map<OntologyType, Integer> ontology = ob.getOntology();
		LowOccurrencePruner lop = new LowOccurrencePruner(ontology);
		lop.prune();
		Log.getLogger().info("Low-occurrence pruned full ontology: " + lop.getPrunedFullOntology());
		
		HighGeneralityPruner hgp = new HighGeneralityPruner(lop.getPrunedFullOntology(), ontology);
		hgp.prune();
		Log.getLogger().info("High-generality pruned full ontology: " + hgp.printMapTSV("type_output_" + topic + "_" + rank + "_mine#" + userName + ".csv"));
		
		
		OntologyBuilder obOld = new OntologyBuilder(userID, count);
		obOld.setStartDate(new Date(112, 11, 20));
		obOld.setEndDate(new Date(113, 1, 9));
		obOld.build();
		Map<OntologyType, Integer> ontologyOld = obOld.getOntology();
		LowOccurrencePruner lopOld = new LowOccurrencePruner(ontologyOld);
		lopOld.prune();
		Log.getLogger().info("Low-occurrence pruned full ontology: " + lopOld.getPrunedFullOntology());
		
		HighGeneralityPruner hgpOld = new HighGeneralityPruner(lopOld.getPrunedFullOntology(), ontologyOld);
		hgpOld.prune();
		Log.getLogger().info("High-generality pruned full ontology: " + hgpOld.printMapTSV("type_output_" + topic + "_" + rank + "_nonmine#" + userName + ".csv"));
		hgpOld.getPrunedYAGOTypes();
	}

	/**
	 * @param screenName2
	 * @param i
	 */
	public void createUserOntology(String screenName, int tweetCount) {
		long userID = TweetBase.getInstance().getUserID(screenName);
		OntologyBuilder ob = new OntologyBuilder(userID, tweetCount);
		ob.build();
		Map<OntologyType, Integer> ontology = ob.getOntology();
		LowOccurrencePruner lop = new LowOccurrencePruner(ontology);
		lop.prune();
		Log.getLogger().info("Low-occurrence pruned full ontology: " + lop.getPrunedFullOntology());
		
		HighGeneralityPruner hgp = new HighGeneralityPruner(lop.getPrunedFullOntology(), ontology);
		hgp.prune();
		Log.getLogger().info("High-generality pruned full ontology: " + hgp.printMapTSV("class_output_user#" + screenName + ".csv"));
	}
}
