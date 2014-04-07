package jp.titech.twitter.network;

import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.WeightedEdge;
import jp.titech.twitter.db.TweetBaseUtil;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * Class to build graphs of Twitter users.
 * 
 * @author Kristian
 *
 */
public class NetworkBuilder {

	TwitterUser 		seedUser;		// The seed user to start out with
	Queue<TwitterUser> 	processQueue;
	int					maxUsers;		// Maximum number of users to collect
	Set<Long>			processed;		// Set of IDs already processed (to speed things up)

	DirectedGraph<TwitterUser, DefaultWeightedEdge> graph;

	public NetworkBuilder(TwitterUser seedUser) {
		graph = new SimpleDirectedWeightedGraph<TwitterUser, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		this.seedUser = seedUser;
		processQueue = new PriorityQueue<TwitterUser>();
		processQueue.add(this.seedUser);
		processed = new HashSet<Long>();
	}

	public NetworkBuilder(TwitterUser seedUser, int maxSize) {
		graph = new SimpleDirectedWeightedGraph<TwitterUser, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		this.seedUser = seedUser;
		processQueue = new PriorityQueue<TwitterUser>();
		processQueue.add(this.seedUser);
		this.maxUsers = maxSize;
		processed = new HashSet<Long>();
	}

	public void build() {

		int userCount = 1;
		TwitterUser currentUser, follower;
		List<Long> ids;

		while(!processQueue.isEmpty()) {

			currentUser = processQueue.poll();

			if(isValidUser(currentUser)) {
				
				if(!processed.contains(currentUser.getUserID())) {
					Log.getLogger().info("Adding user # " + userCount + " (@" + currentUser.getScreenName() + ") to graph.");
					graph.addVertex(currentUser);
					userCount++;
				}

				if(userCount > maxUsers) break;

				Log.getLogger().info("Getting followers of @" + currentUser.getScreenName() + "...");

				ids = TweetBaseUtil.getFollowersIDs(currentUser.getUserID(), -1);

				for (long id : ids) {
					if(!processed.contains(id)) {
						follower = TweetBaseUtil.getTwitterUserWithID(id);

						if(isValidUser(follower) && !follower.equals(currentUser)) {
							Log.getLogger().info("Adding user # " + userCount + " (@" + follower.getScreenName() + ") to graph.");
							processQueue.add(follower);
							graph.addVertex(follower);
							userCount++;

							if(graph.addEdge(follower, currentUser) != null)
								Log.getLogger().info("Adding new edge: (" + follower.getScreenName() + ") -> (" + currentUser.getScreenName() + ").");

							if(userCount > maxUsers) break;
						}
						processed.add(id);
					}
				}
			}
		}

		connectAllUsers();
	}

	/**
	 * Finish up the graph by connecting unconnected users.
	 * 
	 * @param graph
	 * @return the updated graph
	 */
	private void connectAllUsers() {

		Log.getLogger().info("Finishing up the graph by connecting all users by follow relation...");

		Set<TwitterUser> users = graph.vertexSet();

		for (TwitterUser user : users) {
			List<Long> ids = TweetBaseUtil.getFollowersIDs(user.getUserID(), -1);
			for (long id : ids)
				for (TwitterUser follower : users)
					if(id == follower.getUserID() && !user.equals(follower))
						graph.addEdge(follower, user);
		}
	}

	public SimpleGraph<String, DefaultWeightedEdge> buildNetworkFromCommunityFile(String filePath) {

		SimpleGraph<String, DefaultWeightedEdge> graph = 
				new SimpleWeightedGraph<String, DefaultWeightedEdge>(WeightedEdge.class);

		String content = Util.readFile(filePath);
		String[] lines = content.split("\n");

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] parts = line.split("\t");
			String from = "@" + parts[0].split("-")[0];
			String to = "@" + parts[1].split("-")[0];

			graph.addVertex(from);
			graph.addVertex(to);
			DefaultWeightedEdge edge = graph.addEdge(from, to);
			graph.setEdgeWeight(edge, Double.parseDouble(parts[2]));

		}

		return graph;
	}

	/**
	 * Check for user validity based on the constraints imposed in the properties file.
	 * 
	 * @param currentUser
	 * @return true if valid
	 */
	private boolean isValidUser(TwitterUser currentUser) {
		return  currentUser != null &&
				currentUser.getFollowersCount()	>= Vars.MIN_FOLLOWERS &&
				currentUser.getFollowersCount()	<= Vars.MAX_FOLLOWERS &&
				currentUser.getFriendsCount() 	>= Vars.MIN_FRIENDS &&
				currentUser.getFriendsCount() 	<= Vars.MAX_FRIENDS && 
				currentUser.getStatusesCount() 	>= Vars.MIN_TWEETS &&
				currentUser.getStatusesCount() 	<= Vars.MAX_TWEETS && 
				!currentUser.isProtected();
	}

	/**
	 * @return the seedUser
	 */
	public TwitterUser getSeedUser() {
		return seedUser;
	}

	/**
	 * @param seedUser the seedUser to set
	 */
	public void setSeedUser(TwitterUser seedUser) {
		this.seedUser = seedUser;
	}

	/**
	 * @return the processQueue
	 */
	public Queue<TwitterUser> getProcessQueue() {
		return processQueue;
	}

	/**
	 * @param processQueue the processQueue to set
	 */
	public void setProcessQueue(Queue<TwitterUser> processQueue) {
		this.processQueue = processQueue;
	}

	/**
	 * @return the maxUsers
	 */
	public int getMaxUsers() {
		return maxUsers;
	}

	/**
	 * @param maxUsers the maxUsers to set
	 */
	public void setMaxUsers(int maxSize) {
		this.maxUsers = maxSize;
	}

	/**
	 * @return the graph
	 */
	public DirectedGraph<TwitterUser, DefaultWeightedEdge> getGraph() {
		return graph;
	}

	/**
	 * @param graph the graph to set
	 */
	public void setGraph(DirectedGraph<TwitterUser, DefaultWeightedEdge> graph) {
		this.graph = graph;
	}
}
