/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		18 jun. 2013
 */
package jp.titech.twitter;

import java.util.ArrayList;
import java.util.List;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.control.NetworkController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.ontology.TFIDFBuilder;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

import org.apache.log4j.PropertyConfigurator;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class GetUserNetworkApp {

	final static long SEED_USER_ID = 55569628; //@mikegroner: 55569628

	public static void main( String[] args ) throws InterruptedException {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting program.");
		
		NetworkController networkController 	= NetworkController.getInstance();
		MiningController miningController 		= MiningController.getInstance();
		OntologyController ontologyController 	= OntologyController.getInstance();

		DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph = networkController.createNetworkFromSeedUser(SEED_USER_ID, 500);

		Log.getLogger().info("");
		Log.getLogger().info("Graph: " + twitterUserGraph.toString());
		
		List<TwitterUser> userList = new ArrayList<TwitterUser>();

		for (TwitterUser user : twitterUserGraph.vertexSet()) {
			
			miningController.mineUser(user);
			double englishRate = miningController.getUserMiner().getEnglishRate();
			Log.getLogger().info("Rate of English tweets of user @" + user.getScreenName() + ": " + englishRate);
			
			if(englishRate > Vars.MIN_ENGLISH_RATE) {
				Log.getLogger().info("Enough English tweets, so create ontology...");
				ontologyController.createUserOntology(user);
				userList.add(user);
			}
		}
		
		String list = "";
		
		for (TwitterUser twitterUser : userList) {
			list += twitterUser.getUserOntology().toString() + "\n";
		}
		
		Log.getLogger().info("List of classes found:\n" + list);
		
		Log.getLogger().info("Calculating the TF-IDF scores for the ontologies...");
		
		TFIDFBuilder tb = new TFIDFBuilder(userList);
		tb.calculateYAGOTFIDF();
		
		Log.getLogger().info("Max TF score: " + tb.getMaxTFScore());
		Log.getLogger().info("Max TF-IDF score: " + tb.getMaxTFIDFScore());
		
		
	}
}
