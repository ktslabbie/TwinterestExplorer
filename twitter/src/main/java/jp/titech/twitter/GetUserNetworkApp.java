/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		18 jun. 2013
 */
package jp.titech.twitter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.EvaluationController;
import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.control.NetworkController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBaseUtil;
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
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.GmlExporter;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringEdgeNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.xml.sax.SAXException;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class GetUserNetworkApp {

	final static long SEED_USER_ID = 55569628; //@mikegroner: 55569628 @bnmuller: 55569628   @BBC_TopGear 52344859 @CCCManhattan
	//final static String TARGET_USER_SCREEN_NAME = "CCCManhattan";
	final static String TARGET_USER_SCREEN_NAME = "bnmuller";

	public static void main( String[] args ) throws InterruptedException {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting program.");
		
		
		
		NetworkController networkController 		= NetworkController.getInstance();
		MiningController miningController 			= MiningController.getInstance();
		OntologyController ontologyController 		= OntologyController.getInstance();
		EvaluationController evaluationController 	= EvaluationController.getInstance();

		DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph = 
				networkController.createNetworkFromTargetUser(TARGET_USER_SCREEN_NAME, new File(Vars.EVALUATION_DIRECTORY + TARGET_USER_SCREEN_NAME + "/dcg.relevance.txt"));

		Set<TwitterUser> userSet = twitterUserGraph.vertexSet();
		
		Set<TwitterUser> englishUserSet = new HashSet<TwitterUser>();
		
		TwitterUser targetUser = TweetBaseUtil.getTwitterUserWithScreenName(TARGET_USER_SCREEN_NAME);
		ontologyController.createUserOntology(targetUser);
		englishUserSet.add(targetUser);
		
		String users = "\n";

		for (TwitterUser user : userSet) {
			//users += user.getScreenName();
			//users += "\n";
			miningController.mineUser(user);
			
			if(user.getEnglishRate() > Vars.MIN_ENGLISH_RATE) {
				Log.getLogger().info("Enough English tweets for user @" + user.getScreenName() + " (" + Util.round(2, user.getEnglishRate()) + "), so create ontology...");
				ontologyController.createUserOntology(user);
				englishUserSet.add(user);
			} else {
				Log.getLogger().info("Not enough English tweets for user @" + user.getScreenName() + " (" + Util.round(2, user.getEnglishRate()) + ")! Skipping.");
			}
		}
		
		//System.out.println(users);
		
		Log.getLogger().info("Calculating the TF-IDF and CF-IUF weights for the terms and classes. We have " + englishUserSet.size() + " users in our list.");
		
		WeightingScheme tfidf = new TFIDF(englishUserSet);
		tfidf.calculate();
		
		WeightingScheme cfiuf = new CFIUF(englishUserSet);
		cfiuf.calculate();
		
		for (TwitterUser user : englishUserSet) {
			Util.writeUserTFIDFMap(user);
			Util.writeUserOntology(user);
		}
		
		SimilarityFunction occurrenceSimilarity = new OccurrenceSimilarity(cfiuf);
		SimilarityFunction tfidfCosineSimilarity = new CosineSimilarity(tfidf);
		SimilarityFunction cfiufCosineSimilarity = new CosineSimilarity(cfiuf);
		
		evaluationController.evaluateUserSimilarity(occurrenceSimilarity, TARGET_USER_SCREEN_NAME);
		evaluationController.evaluateUserSimilarity(tfidfCosineSimilarity, TARGET_USER_SCREEN_NAME);
		evaluationController.evaluateUserSimilarity(cfiufCosineSimilarity, TARGET_USER_SCREEN_NAME);
		
		Log.getLogger().info("Creating similarity graphs.");
		final UndirectedGraph<TwitterUser, DefaultWeightedEdge> occurrenceSimilarityGraph = networkController.createSimilarityGraph(occurrenceSimilarity, 0.707);
		final UndirectedGraph<TwitterUser, DefaultWeightedEdge> tfidfSimilarityGraph = networkController.createSimilarityGraph(tfidfCosineSimilarity, 0.0502);
		final UndirectedGraph<TwitterUser, DefaultWeightedEdge> cfiufSimilarityGraph = networkController.createSimilarityGraph(cfiufCosineSimilarity, 0.105);
		
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
		
		Log.getLogger().info("Exporting to GML files...");
		
		VertexNameProvider<TwitterUser> vertexIDProvider = new IntegerNameProvider<TwitterUser>();
		VertexNameProvider<TwitterUser> vertexLabelProvider = new StringNameProvider<TwitterUser>();
		EdgeNameProvider<DefaultWeightedEdge> edgeIDProvider = new IntegerEdgeNameProvider<DefaultWeightedEdge>();
		EdgeNameProvider<DefaultWeightedEdge> edgeLabelProvider = new StringEdgeNameProvider<DefaultWeightedEdge>() {
			
			@Override
			public String getEdgeName(DefaultWeightedEdge edge) {
				// TODO Auto-generated method stub
				return ""+cfiufSimilarityGraph.getEdgeWeight(edge);
			}
		};
		
		GraphMLExporter<TwitterUser, DefaultWeightedEdge> exp = 
				new GraphMLExporter<TwitterUser, DefaultWeightedEdge>(vertexIDProvider, vertexLabelProvider, edgeIDProvider, edgeLabelProvider);
		
		try {
			exp.export(new FileWriter(new File(Vars.DATA_DIRECTORY + TARGET_USER_SCREEN_NAME + ".cf-iuf.graphml")), cfiufSimilarityGraph);
			exp.export(new FileWriter(new File(Vars.DATA_DIRECTORY + TARGET_USER_SCREEN_NAME + ".tf-idf.graphml")), tfidfSimilarityGraph);
			exp.export(new FileWriter(new File(Vars.DATA_DIRECTORY + TARGET_USER_SCREEN_NAME + ".occurrence.graphml")), occurrenceSimilarityGraph);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


