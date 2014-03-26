package jp.titech.twitter.control;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBaseUtil;
import jp.titech.twitter.network.NetworkBuilder;
import jp.titech.twitter.util.Log;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class NetworkController {
	
	private static NetworkController 	networkController;
	
	private NetworkController() {}
	
	/**
	 * Entry point to gather a network of users around some seed user.
	 * 
	 * @param userID
	 * @param depth
	 * @param maxCount
	 * @return a directed graph of users and edges
	 */
	public DirectedGraph<TwitterUser, DefaultWeightedEdge> createNetworkFromSeedUser(long userID, int depth, int maxCount) {
		
		TwitterUser seedUser = TweetBaseUtil.getTwitterUserWithID(userID);
		
		Log.getLogger().info("Constructing network from seed user @" + seedUser.getScreenName() + "...");
		
		NetworkBuilder networkBuilder = new NetworkBuilder(seedUser, depth, maxCount);
		networkBuilder.build();

		return networkBuilder.getGraph();
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
