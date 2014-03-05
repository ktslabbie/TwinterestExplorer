/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.dbpedia.spotlight.model.OntologyType;
import org.jgraph.graph.Edge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.WeightedEdge;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.mining.UserMiner;
import jp.titech.twitter.ontology.OntologyBuilder;
import jp.titech.twitter.ontology.pruning.HighGeneralityPruner;
import jp.titech.twitter.ontology.pruning.LowOccurrencePruner;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

public class Controller {

	private UserMiner userMiner;
	private static Controller controller;
	private Twitter twitter;
	private TweetBase tweetBase;

	private Controller(){
		twitter = new TwitterFactory().getInstance();
		tweetBase = TweetBase.getInstance();
	}

	public static Controller getInstance(){
		if(controller == null){
			controller = new Controller();
		}
		return controller;
	}
	
	public UserMiner getUserMiner(){
		return userMiner;
	}

	/**
	 * 
	 * @param userID
	 * @param count Number of tweets to collect.
	 */
	public void startSearchMining(int userID, int count) {
		userMiner = new UserMiner();
		userMiner.mineUser(userID, count);
	}

	/**
	 * 
	 * @param userID
	 * @param count Number of tweets to collect.
	 */
	public void startSearchMining(String tScreenName, int count) {
		userMiner = new UserMiner();
		userMiner.mineUser(tScreenName, count);
	}

	public void createOntology(long userID) {
		OntologyBuilder ob = new OntologyBuilder(userID);
		ob.build(Vars.CONCATENATION_WINDOW);
	}

	public void createOntology(long userID, String userName, int count, String topic, int rank) {
		OntologyBuilder ob = new OntologyBuilder(userID, count);
		ob.setStartDate(new Date(112, 11, 15));
		ob.setEndDate(new Date(112, 11, 19));
		ob.build(Vars.CONCATENATION_WINDOW);
		Map<OntologyType, Integer> ontology = ob.getOntology();
		LowOccurrencePruner lop = new LowOccurrencePruner(ontology);
		lop.prune();
		Log.getLogger().info("Low-occurrence pruned full ontology: " + lop.getPrunedFullOntology());

		HighGeneralityPruner hgp = new HighGeneralityPruner(lop.getPrunedFullOntology(), ontology);
		hgp.prune();
		Log.getLogger().info("High-generality pruned full ontology: " + hgp.printPrunedMapTSV("type_output_" + topic + "_" + rank + "_mine#" + userName + ".csv"));


		OntologyBuilder obOld = new OntologyBuilder(userID, count);
		obOld.setStartDate(new Date(112, 11, 20));
		obOld.setEndDate(new Date(113, 1, 9));
		obOld.build(Vars.CONCATENATION_WINDOW);
		Map<OntologyType, Integer> ontologyOld = obOld.getOntology();
		LowOccurrencePruner lopOld = new LowOccurrencePruner(ontologyOld);
		lopOld.prune();
		Log.getLogger().info("Low-occurrence pruned full ontology: " + lopOld.getPrunedFullOntology());

		HighGeneralityPruner hgpOld = new HighGeneralityPruner(lopOld.getPrunedFullOntology(), ontologyOld);
		hgpOld.prune();
		Log.getLogger().info("High-generality pruned full ontology: " + hgpOld.printPrunedMapTSV("type_output_" + topic + "_" + rank + "_nonmine#" + userName + ".csv"));
		hgpOld.getPrunedYAGOTypes();
	}

	/**
	 * @param screenName2
	 * @param i
	 */
	public void createUserOntology(String screenName, int tweetCount) {
		long userID = TweetBase.getInstance().getUserID(screenName);
		OntologyBuilder ob = new OntologyBuilder(userID, tweetCount);
		ob.build(Vars.CONCATENATION_WINDOW);
		Map<OntologyType, Integer> ontology = ob.getOntology();

		LowOccurrencePruner lop = new LowOccurrencePruner(ontology);

		lop.printFullMapTSV(screenName + "_full_ontology.txt");

		lop.prune();
		Log.getLogger().info("Low-occurrence pruned full ontology: " + lop.getPrunedFullOntology());

		HighGeneralityPruner hgp = new HighGeneralityPruner(lop.getPrunedFullOntology(), ontology);
		hgp.prune();
		Log.getLogger().info("High-generality pruned full ontology: " + hgp.printPrunedMapTSV("class_output_user#" + screenName + "-" + Vars.CONCATENATION_WINDOW + 
				"-window-c" + Vars.SPOTLIGHT_CONFIDENCE + "s" + Vars.SPOTLIGHT_SUPPORT + ".csv"));
	}

	public DirectedGraph<TwitterUser, DefaultWeightedEdge> gatherNetworkFromSeedUser(long userID, int depth, int maxCount) {
		Queue<TwitterUser> queue = new PriorityQueue<TwitterUser>();

		TwitterUser seedUser = getTwitterUser(userID);

		queue.add(seedUser);

		Log.getLogger().info("Constructing network from seed user " + seedUser.getScreenName() + "...");

		return gatherNetwork(queue, depth, maxCount);
	}

	/**
	 * Make a network with depth, starting from startingUser, with maximum node count maxCount.
	 * Consider all followers and followees.
	 * TODO: make depth work
	 * TODO: make followers/friends paging (cursor) work
	 * 
	 * @param startingUser
	 * @param depth
	 * @param maxCount
	 * 
	 * @return screenname
	 * @throws InterruptedException 
	 */
	public DirectedGraph<TwitterUser, DefaultWeightedEdge> gatherNetwork(Queue<TwitterUser> queue, int depth, int maxCount) {

		int currentDepth = 1;
		int count = 1;
		TwitterUser currentUser, follower;
		List<Long> ids;
		DirectedGraph<TwitterUser, DefaultWeightedEdge> graph = 
				new SimpleDirectedWeightedGraph<TwitterUser, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		
		Log.getLogger().info("Current depth: " + currentDepth);

		while(!queue.isEmpty()) {

			currentUser = queue.poll();

			if(currentUser == null) {
				//Log.getLogger().info("Follower points to a suspended account. Skip!");
				continue;
			}

			if(currentUser.getFollowersCount() >= Vars.MIN_FOLLOWERS && currentUser.getFollowersCount() <= Vars.MAX_FOLLOWERS &&
					currentUser.getFriendsCount() >= Vars.MIN_FRIENDS && currentUser.getFriendsCount() <= Vars.MAX_FRIENDS && 
					currentUser.getStatusesCount() >= Vars.MIN_TWEETS && currentUser.getStatusesCount() <= Vars.MAX_TWEETS) {

				Log.getLogger().info("User " + currentUser.getScreenName() + " fulfills min/max followers/friends/tweets conditions! Add!");

				if(!graph.containsVertex(currentUser)) {
					Log.getLogger().info("Adding new user #" + count + " with ID " + currentUser.getUserID() + " to graph!");
					graph.addVertex(currentUser);
				} else {
					Log.getLogger().info("User with ID " + currentUser.getUserID() + " already exists in graph!");
				}

				//Thread.sleep(2000);
				ids = getFollowersIDs(currentUser.getUserID(), -1);

				Log.getLogger().info("Followers IDs of " + currentUser.getName() + ": " + ids);

				for (long id : ids) {

					follower = getTwitterUser(id);
					if(follower == null) {
						//Log.getLogger().info("Follower points to a suspended account. Skip!");
						continue;
					}
					
					if(follower.isProtected()) {
						//Log.getLogger().info("Follower points to a protected account. Skip!");
						continue;
					}
					
					if(follower.equals(currentUser)) {
						//Log.getLogger().info("Follower equals current user. Skip!");
						continue;
					}

					if(follower.getFollowersCount() >= Vars.MIN_FOLLOWERS && follower.getFollowersCount() <= Vars.MAX_FOLLOWERS &&
							follower.getFriendsCount() >= Vars.MIN_FRIENDS && follower.getFriendsCount() <= Vars.MAX_FRIENDS && 
									follower.getStatusesCount() >= Vars.MIN_TWEETS && follower.getStatusesCount() <= Vars.MAX_TWEETS) {

						Log.getLogger().info("User " + follower.getScreenName() + " fulfills min/max followers/friends/tweets conditions! Add!");

						//Log.getLogger().info("Followers IDs of " + follower.getName() + ": " + getFollowersIDs(follower.getUserID(), -1));
						
						if(!graph.containsVertex(follower)) {
							Log.getLogger().info("Adding new user #" + count + " with ID " + id + " to graph!");
							queue.add(follower);
							graph.addVertex(follower);
							count++;
						} else {
							Log.getLogger().info("User #" + count + " with ID " + id + " already exists!");
						}
						
						if(graph.addEdge(follower, currentUser) != null)
							Log.getLogger().info("Adding new edge: (" + follower.getUserID() + ") -> (" + currentUser.getUserID() + ")!");
						else
							Log.getLogger().info("Edge (" + follower.getUserID() + ") -> (" + currentUser.getUserID() + ") already exists!");

						
						if(count > maxCount) return connectAllUsers(graph);
					} else {
						//Log.getLogger().info("User " + follower.getScreenName() + " has more than " + Vars.MAX_FOLLOWERS + " followers! Ignore!");
					}
				}
			}

			if(currentDepth > depth) return connectAllUsers(graph);
		}
		
		return connectAllUsers(graph);
	}
	
	/**
	 * Finish up the graph by connecting unconnected users.
	 * 
	 * @param graph
	 * @return the updated graph
	 */
	private DirectedGraph<TwitterUser, DefaultWeightedEdge> connectAllUsers(DirectedGraph<TwitterUser, DefaultWeightedEdge> graph) {
		
		Log.getLogger().info("");
		Log.getLogger().info("Finishing up the graph by connecting all users by follow relation...");
		
		Set<TwitterUser> users = graph.vertexSet();
		
		for (TwitterUser user : users) {
			List<Long> ids = getFollowersIDs(user.getUserID(), -1);
			for (long id : ids) {
				for (TwitterUser follower : users) {
					if(id == follower.getUserID() && !user.equals(follower)) {
						graph.addEdge(follower, user);
					}
				}
			}
		}
		
		return graph;
	}

	private TwitterUser getTwitterUser(long userID){

		if(!tweetBase.isUserContained(userID)) {
			User user = null;

			try {
				//Thread.sleep(1000);
				user = twitter.showUser(userID);

				if(user != null) {
					if(user.isProtected()) {
						Log.getLogger().info("User @" + user.getScreenName() + " is a protected account. Skip!");
					}

					tweetBase.addUser(user);
				}

			} catch (TwitterException e) {
				if(e.getErrorCode() == 63)	{
					Log.getLogger().error("Error getting user (suspended account). Skip this user!");
					return null;
				} else if(e.getErrorCode() == 88){
					//e.printStackTrace();
					int secondsUntil =  e.getRateLimitStatus().getSecondsUntilReset();
					Log.getLogger().error("Error getting user (rate limit exceeded). Retry in " + secondsUntil + " seconds...");
					try {
						Thread.sleep(secondsUntil*1000);
						return getTwitterUser(userID);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		} else {
			//Log.getLogger().info("User already exists in DB! Returning local version.");
		}

		return tweetBase.getUser(userID);
	}

	private List<Long> getFollowersIDs(long userID, int cursor) {
		IDs ids = null;
		
		if(!tweetBase.isUserFollowersContained(userID)) {
			
			try {
				ids = twitter.getFollowersIDs(userID, -1);
				for (long id : ids.getIDs()) {
					tweetBase.addUserFollower(userID, id);
				}
				
			} catch (TwitterException e) {
				if(e.getErrorCode() == 88){
					int secondsUntil =  e.getRateLimitStatus().getSecondsUntilReset();
					Log.getLogger().error("Error getting followers (rate limit exceeded). Retry in " + secondsUntil + " seconds...");
					try {
						Thread.sleep(secondsUntil*1000);
						return getFollowersIDs(userID, cursor);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		
		Log.getLogger().info("RETURNING FOLLOWERS FOR USER: " + userID);
		
		return tweetBase.getUserFollowers(userID);
	}

	public SimpleGraph<String, DefaultWeightedEdge> gatherNetworkFromCommunityFile(String filePath) {
		
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
}
