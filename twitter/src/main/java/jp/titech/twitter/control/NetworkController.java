package jp.titech.twitter.control;

import java.util.Iterator;
import java.util.SortedSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.WeightedEdge;
import jp.titech.twitter.db.TweetBaseUtil;
import jp.titech.twitter.network.NetworkBuilder;
import jp.titech.twitter.ontology.similarity.SimilarityFunction;
import jp.titech.twitter.ontology.similarity.UserSimilarity;
import jp.titech.twitter.util.Log;

import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

public class NetworkController {

	private static NetworkController 	networkController;
	private DirectedGraph<TwitterUser, DefaultWeightedEdge> twitterUserGraph;

	private NetworkController() {}

	/**
	 * Entry point to gather a network of users around some seed user.
	 * 
	 * @param userID
	 * @param depth
	 * @param maxCount
	 * @return a directed graph of users and edges
	 */
	public DirectedGraph<TwitterUser, DefaultWeightedEdge> createNetworkFromSeedUser(long userID, int maxCount) {

		TwitterUser seedUser = TweetBaseUtil.getTwitterUserWithID(userID);

		Log.getLogger().info("Constructing network from seed user @" + seedUser.getScreenName() + "...");

		NetworkBuilder networkBuilder = new NetworkBuilder(seedUser, maxCount);
		networkBuilder.build();
		twitterUserGraph = networkBuilder.getGraph();

		return twitterUserGraph;
	}

	public void drawSimilarityGraph(Graph<TwitterUser, DefaultWeightedEdge> dgraph) {

		JGraphModelAdapter<TwitterUser, DefaultWeightedEdge> model = new JGraphModelAdapter<TwitterUser, DefaultWeightedEdge>(dgraph);
		
		// Construct Model and Graph
		JGraph graph = new JGraph(model);
		
		// Control-drag should clone selection
		graph.setCloneable(true);

		// When over a cell, jump to its default port (we only have one, anyway)
		graph.setJumpToDefaultPort(true);

		// Show in Frame
		JFrame frame = new JFrame();

		frame.setSize(1600,900);
		frame.setLocation(100,100);
		frame.getContentPane().add(new JScrollPane(graph));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public SimpleGraph<TwitterUser, DefaultWeightedEdge> getSimilarityGraph(SimilarityFunction similarityFunction, double threshold) {
		
		SimpleGraph<TwitterUser, DefaultWeightedEdge> similarityGraph = new SimpleWeightedGraph<TwitterUser, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		SortedSet<UserSimilarity> similaritySet = similarityFunction.getUserSimilaritySet();
		
		for (Iterator<UserSimilarity> iterator = similaritySet.iterator(); iterator.hasNext();) {
			UserSimilarity userSimilarity = iterator.next();
			if(userSimilarity.getSimilarity() < threshold) {
				return similarityGraph;
			} else {
				similarityGraph.addVertex(userSimilarity.getUserA());
				similarityGraph.addVertex(userSimilarity.getUserB());
				DefaultWeightedEdge edge = similarityGraph.addEdge(userSimilarity.getUserA(), userSimilarity.getUserB());
				similarityGraph.setEdgeWeight(edge, userSimilarity.getSimilarity());
			}
		}
		return similarityGraph;
	}

	/**
	 * Retrieve the NetworkController singleton instance.
	 * 
	 * @return the network controller singleton
	 */
	public static NetworkController getInstance(){
		if(networkController == null){ networkController = new NetworkController(); }
		return networkController;
	}
}
