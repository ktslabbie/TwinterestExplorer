/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		18 jun. 2013
 */
package jp.titech.twitter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.control.NetworkController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.ontology.similarity.CFIUF;
import jp.titech.twitter.ontology.similarity.CosineSimilarity;
import jp.titech.twitter.ontology.similarity.OccurrenceSimilarity;
import jp.titech.twitter.ontology.similarity.SimilarityFunction;
import jp.titech.twitter.ontology.similarity.UserSimilarity;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

import org.apache.log4j.PropertyConfigurator;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

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

		DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph = networkController.createNetworkFromSeedUser(SEED_USER_ID, 107);

		Set<TwitterUser> userSet = twitterUserGraph.vertexSet();

		for (TwitterUser user : userSet) {
			miningController.mineUser(user);
			
			if(user.getEnglishRate() > Vars.MIN_ENGLISH_RATE) {
				Log.getLogger().info("Enough English tweets for user @" + user.getScreenName() + " (" + Util.round(2, user.getEnglishRate()) + "), so create ontology...");
				ontologyController.createUserOntology(user);
			} else {
				Log.getLogger().info("Not enough English tweets for user @" + user.getScreenName() + " (" + Util.round(2, user.getEnglishRate()) + ")! Skipping.");
			}
		}
		
		Log.getLogger().info("Calculating the CF-IUF scores for the ontologies. We have " + userSet.size() + " users in our list.");
		
		CFIUF cfiuf = new CFIUF(userSet);
		cfiuf.calculate();
		
		for (TwitterUser user : userSet)
			ontologyController.writeUserOntology(user);
		
		SimilarityFunction cs = new CosineSimilarity(userSet);
		cs.calculate();
		Util.writeToFile(cs.userSimilarityString("bnmuller"), new File(Vars.DATA_DIRECTORY + "bnmuller_cosine_similarity.txt"));
		
		SimilarityFunction os = new OccurrenceSimilarity(userSet);
		os.calculate();
		Util.writeToFile(os.userSimilarityString("bnmuller"), new File(Vars.DATA_DIRECTORY + "bnmuller_occurrence_similarity.txt"));
		
		SimpleGraph<TwitterUser, DefaultWeightedEdge> twitterUserSimilarityGraph  = networkController.getSimilarityGraph(cs, 0.3);
		networkController.drawSimilarityGraph(twitterUserSimilarityGraph);
	}
}
