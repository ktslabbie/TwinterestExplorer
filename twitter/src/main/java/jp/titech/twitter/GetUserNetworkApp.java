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
import java.util.SortedMap;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.EvaluationController;
import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.control.NetworkController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserSimilarity;
import jp.titech.twitter.ontology.evaluation.DCGEvaluation;
import jp.titech.twitter.ontology.similarity.CFIUF;
import jp.titech.twitter.ontology.similarity.CosineSimilarity;
import jp.titech.twitter.ontology.similarity.OccurrenceSimilarity;
import jp.titech.twitter.ontology.similarity.SimilarityFunction;
import jp.titech.twitter.ontology.similarity.TFIDF;
import jp.titech.twitter.ontology.similarity.WeightingScheme;
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

	final static long SEED_USER_ID = 55569628; //@mikegroner: 55569628 @bnmuller: 55569628   @BBC_TopGear 52344859
	//final static String TARGET_USER_SCREEN_NAME = "bnmuller";
	final static String TARGET_USER_SCREEN_NAME = "bnmuller";

	public static void main( String[] args ) throws InterruptedException {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting program.");
		
		NetworkController networkController 		= NetworkController.getInstance();
		MiningController miningController 			= MiningController.getInstance();
		OntologyController ontologyController 		= OntologyController.getInstance();
		EvaluationController evaluationController 	= EvaluationController.getInstance();

		DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph = networkController.createNetworkFromSeedUser(SEED_USER_ID, 105);

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
		
		Log.getLogger().info("Calculating the TF-IDF and CF-IUF weights for the terms and classes. We have " + userSet.size() + " users in our list.");
		
		WeightingScheme tfidf = new TFIDF(userSet);
		tfidf.calculate();
		
		WeightingScheme cfiuf = new CFIUF(userSet);
		cfiuf.calculate();
		
		for (TwitterUser user : userSet) {
			Util.writeUserTFIDFMap(user);
			Util.writeUserOntology(user);
		}
		
		evaluationController.evaluate(new CosineSimilarity(tfidf), TARGET_USER_SCREEN_NAME);
		evaluationController.evaluate(new CosineSimilarity(cfiuf), TARGET_USER_SCREEN_NAME);
		evaluationController.evaluate(new OccurrenceSimilarity(cfiuf), TARGET_USER_SCREEN_NAME);
		
		//SimpleGraph<TwitterUser, DefaultWeightedEdge> twitterUserSimilarityGraph  = networkController.getSimilarityGraph(cosineSimilarity, 0.3);
		//networkController.drawSimilarityGraph(twitterUserSimilarityGraph);
		
		
	}
}
