/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		18 jun. 2013
 */
package jp.titech.twitter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.control.NetworkController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.WeightedEdge;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

import org.apache.log4j.PropertyConfigurator;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.tree.OrganizationalChart;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class GetUserNetworkApp {

	public static MiningController miningController 	= MiningController.getInstance();
	public static OntologyController ontologyController = OntologyController.getInstance();
	public static NetworkController networkController 	= NetworkController.getInstance();

	public static void main( String[] args ) throws InterruptedException {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting program.");

		final long SEED_USER_ID = 55569628; //@mikegroner: 55569628

		DirectedGraph<TwitterUser, DefaultWeightedEdge> dgraph;
		//SimpleGraph<String, DefaultWeightedEdge> dgraph;

		dgraph = networkController.createNetworkFromSeedUser(SEED_USER_ID, 3, 500);
		//dgraph = networkController.buildNetworkFromCommunityFile("../twitter/data/output/community/result.community.10-window.txt");

		Log.getLogger().info("");
		Log.getLogger().info("Graph: " + dgraph.toString());

		for (TwitterUser user : dgraph.vertexSet()) {
			
			miningController.mineUser(user);
			double englishRate = miningController.getUserMiner().getEnglishRate();
			Log.getLogger().info("Rate of English tweets of user @" + user.getScreenName() + ": " + englishRate);
			
			if(miningController.getUserMiner().getEnglishRate() > 0.9) {
				Log.getLogger().info("Enough English tweets, so create ontology...");
				ontologyController.createUserOntology(user);
			}
		}

		//drawSimilarityGraph(dgraph);
		//drawFollowGraph(dgraph);
	}

	private static void drawSimilarityGraph(Graph<TwitterUser, DefaultWeightedEdge> dgraph) {

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

	private static void drawFollowGraph(Graph<TwitterUser, DefaultWeightedEdge> dgraph) {

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
}
