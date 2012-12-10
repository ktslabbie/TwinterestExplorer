/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import jp.titech.twitter.mining.Miner;
import jp.titech.twitter.mining.SearchMiner;
import jp.titech.twitter.ontology.OntologyBuilder;

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
		miner = new SearchMiner();
		miner.mineUser(userID, count);
	}
	
	public void createOntology(int userID) {
		OntologyBuilder ob = new OntologyBuilder(userID);
		ob.build();
	}
}
