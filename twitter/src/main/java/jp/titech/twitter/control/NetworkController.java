package jp.titech.twitter.control;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBaseUtil;
import jp.titech.twitter.network.NetworkBuilder;
import jp.titech.twitter.util.Log;

import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;

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
		//JGraphModelAdapter<String, WeightedEdge> model = new JGraphModelAdapter<String, WeightedEdge>(dgraph);

		//control.startSearchMining(screenName, Vars.TIMELINE_TWEET_COUNT);
		//Log.getLogger().info("Creating ontology...");
		//control.createUserOntology(screenName, 500);

		// Construct Model and Graph
		JGraph graph = new JGraph(model);
		// Control-drag should clone selection
		graph.setCloneable(true);

		// When over a cell, jump to its default port (we only have one, anyway)
		graph.setJumpToDefaultPort(true);

		// Set Arrow Style for edge
		//int arrow = GraphConstants.ARROW_CLASSIC;
		//GraphConstants.setLineEnd(edge.getAttributes(), arrow);
		//GraphConstants.setEndFill(edge.getAttributes(), true);

		// Insert the cells via the cache, so they get selected
		//graph.getGraphLayoutCache().insert(cells);

		// Show in Frame
		JFrame frame = new JFrame();

		frame.setSize(1600,900);
		frame.setLocation(100,100);

		frame.getContentPane().add(new JScrollPane(graph));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.pack();
		frame.setVisible(true);
	}

	public void drawFollowGraph(Graph<TwitterUser, DefaultWeightedEdge> dgraph) {

		JGraphModelAdapter<TwitterUser, DefaultWeightedEdge> model = new JGraphModelAdapter<TwitterUser, DefaultWeightedEdge>(dgraph);
		//JGraphModelAdapter<String, WeightedEdge> model = new JGraphModelAdapter<String, WeightedEdge>(dgraph);

		//control.startSearchMining(screenName, Vars.TIMELINE_TWEET_COUNT);
		//Log.getLogger().info("Creating ontology...");
		//control.createUserOntology(screenName, 500);

		// Construct Model and Graph
		JGraph graph = new JGraph(model);
		// Control-drag should clone selection
		graph.setCloneable(true);

		// When over a cell, jump to its default port (we only have one, anyway)
		graph.setJumpToDefaultPort(true);

		// Set Arrow Style for edge
		//int arrow = GraphConstants.ARROW_CLASSIC;
		//GraphConstants.setLineEnd(edge.getAttributes(), arrow);
		//GraphConstants.setEndFill(edge.getAttributes(), true);

		// Insert the cells via the cache, so they get selected
		//graph.getGraphLayoutCache().insert(cells);

		// Show in Frame
		JFrame frame = new JFrame();

		frame.setSize(1600,900);
		frame.setLocation(100,100);

		frame.getContentPane().add(new JScrollPane(graph));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.pack();
		frame.setVisible(true);
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
