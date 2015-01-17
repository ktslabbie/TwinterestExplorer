/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		18 jun. 2013
 *//*
package jp.titech.twitter;

import java.util.HashSet;
import java.util.Set;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.EvaluationController;
import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.control.NetworkController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.mining.api.TwitterConnector;
import jp.titech.twitter.network.NetworkClusterer;
import jp.titech.twitter.network.clustering.HCS;
import jp.titech.twitter.ontology.similarity.CFIUF;
import jp.titech.twitter.ontology.similarity.CosineSimilarity;
import jp.titech.twitter.ontology.similarity.OccurrenceSimilarity;
import jp.titech.twitter.ontology.similarity.SimilarityFunction;
import jp.titech.twitter.ontology.similarity.TFIDF;
import jp.titech.twitter.ontology.similarity.WeightingScheme;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

import org.apache.log4j.PropertyConfigurator;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

*//**
 * @author Kristian Slabbekoorn
 *
 *//*
public class TestUserNetworkApp {

	final static long SEED_USER_ID = 50407038; //@mikegroner: 55569628 @bnmuller: 55569628   @BBC_TopGear 52344859
	//final static String TARGET_USER_SCREEN_NAME = "bnmuller";
	final static String TARGET_USER_SCREEN_NAME = "CCCManhattan";

	public static void main( String[] args ) throws InterruptedException {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting program.");
		
		NetworkController networkController 		= NetworkController.getInstance();
		MiningController miningController 			= MiningController.getInstance();
		OntologyController ontologyController 		= OntologyController.getInstance();
		EvaluationController evaluationController 	= EvaluationController.getInstance();
		

		DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph = networkController.createNetworkFromSeedUser(TARGET_USER_SCREEN_NAME, 105);

		Set<TwitterUser> userSet = twitterUserGraph.vertexSet();
		Set<TwitterUser> englishUserSet = new HashSet<TwitterUser>();
		TwitterConnector connector = new TwitterConnector();
		String users = "\n";

		for (TwitterUser user : userSet) {
			//users += user.getScreenName();
			//users += "\n";
			miningController.mineUser(user, connector);
			
			if(user.getEnglishRate() > Vars.MIN_ENGLISH_RATE) {
				Log.getLogger().info("Enough English tweets for user @" + user.getScreenName() + " (" + Util.round(2, user.getEnglishRate()) + "), so create ontology...");
				ontologyController.createUserOntology(user, Vars.SPOTLIGHT_DEFAULT_URL);
				englishUserSet.add(user);
			} else {
				Log.getLogger().info("Not enough English tweets for user @" + user.getScreenName() + " (" + Util.round(2, user.getEnglishRate()) + ")! Skipping.");
			}
			
			
		}
		
		//System.out.println(users);
		
		Log.getLogger().info("Calculating the TF-IDF and CF-IUF weights for the terms and classes. We have " + englishUserSet.size() + " English users in our list.");
		
		WeightingScheme tfidf = new TFIDF(englishUserSet);
		tfidf.calculate();
		
		WeightingScheme cfiuf = new CFIUF(englishUserSet);
		cfiuf.calculate();
		
		for (TwitterUser user : englishUserSet) {
			Util.writeUserTFIDFMap(user);
			Util.writeUserOntology(user);
			
			Log.getLogger().info("User: " + user.getScreenName());
			Log.getLogger().info("Empty YAGO Ontology?");
			if(user.getUserOntology().getYagoCFIUFMap().isEmpty()) {
				Log.getLogger().info("Empty!");
			}
			//Log.getLogger().info(user.getUserOntology().toString());
		}
		
		SimilarityFunction occurrenceSimilarity = new OccurrenceSimilarity(cfiuf);
		SimilarityFunction tfidfCosineSimilarity = new CosineSimilarity(tfidf);
		SimilarityFunction cfiufCosineSimilarity = new CosineSimilarity(cfiuf);
		
		evaluationController.evaluateUserSimilarity(occurrenceSimilarity, TARGET_USER_SCREEN_NAME);
		evaluationController.evaluateUserSimilarity(tfidfCosineSimilarity, TARGET_USER_SCREEN_NAME);
		evaluationController.evaluateUserSimilarity(cfiufCosineSimilarity, TARGET_USER_SCREEN_NAME);
		
		Log.getLogger().info("Creating similarity graph.");
		UndirectedGraph<TwitterUser, DefaultWeightedEdge> occurrenceSimilarityGraph = networkController.createSimilarityGraph(occurrenceSimilarity, 0.7);
		UndirectedGraph<TwitterUser, DefaultWeightedEdge> tfidfSimilarityGraph = networkController.createSimilarityGraph(tfidfCosineSimilarity, 0.07);
		UndirectedGraph<TwitterUser, DefaultWeightedEdge> cfiufSimilarityGraph = networkController.createSimilarityGraph(cfiufCosineSimilarity, 0.2);
		
		//Log.getLogger().info("Similarity graph: " + cfiufSimilarityGraph);
		
		Log.getLogger().info("Clustering similarity graph occurrence style.");
		NetworkClusterer occurrenceClusterer = new NetworkClusterer(new HCS(occurrenceSimilarityGraph));
		occurrenceClusterer.cluster();
		
		Log.getLogger().info("Clustering similarity graph TF-IDF style.");
		NetworkClusterer tfidfClusterer = new NetworkClusterer(new HCS(tfidfSimilarityGraph));
		tfidfClusterer.cluster();
		
		Log.getLogger().info("Clustering similarity graph CF-IUF style.");
		NetworkClusterer cfiufClusterer = new NetworkClusterer(new HCS(cfiufSimilarityGraph));
		cfiufClusterer.cluster();
		
		Log.getLogger().info("Printing clusters for Occurrence weighting.");
		Log.getLogger().info(occurrenceClusterer.printClusters());
		
		Log.getLogger().info("Printing clusters for TF-IDF weighting.");
		Log.getLogger().info(tfidfClusterer.printClusters());
		
		Log.getLogger().info("Printing clusters for CF-IUF weighting.");
		Log.getLogger().info(cfiufClusterer.printClusters());
		
	}
}
*/