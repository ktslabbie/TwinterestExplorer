package jp.titech.twitter.network.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.util.Log;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class HCS extends ClusteringAlgorithm {
	
	

	public HCS(UndirectedGraph<TwitterUser, DefaultWeightedEdge> similarityGraph) {
		this.similarityGraph = similarityGraph;
		this.clusters = new ArrayList<UndirectedGraph<TwitterUser, DefaultWeightedEdge>>();
	}

	public void execute() {
		Log.getLogger().info("Starting HCS execution.");
		
		//UndirectedGraph<TwitterUser, DefaultWeightedEdge> clusteredGraph = new SimpleWeightedGraph<TwitterUser, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		this.clusters = new ArrayList<UndirectedGraph<TwitterUser, DefaultWeightedEdge>>();
		Set<TwitterUser> singletons = new HashSet<TwitterUser>();
		
		for(TwitterUser user : this.similarityGraph.vertexSet()) {
			if(this.similarityGraph.degreeOf(user) < 1) {
				singletons.add(user);
				//clusteredGraph.addVertex(user);
			}
		}
		
		//Log.getLogger().info("Singletons: " + singletons);
		

		// Add edges
		//for(DefaultWeightedEdge edge : this.similarityGraph.edgeSet()) {
			//TwitterUser source = this.similarityGraph.getEdgeSource(edge);
			//TwitterUser target = this.similarityGraph.getEdgeTarget(edge);
			//clusteredGraph.addEdge(source, target, edge);
		//}
		
		this.similarityGraph.removeAllVertices(singletons);
		
		//Log.getLogger().info("Graph with singletons removed: " + this.similarityGraph);
		
		ConnectivityInspector<TwitterUser, DefaultWeightedEdge> inspector = new ConnectivityInspector<TwitterUser, DefaultWeightedEdge>(this.similarityGraph);
		
		List<Set<TwitterUser>> userSets = inspector.connectedSets();
		
		//Log.getLogger().info("Connected user sets: " + userSets);
		
		for (Set<TwitterUser> userSet : userSets) {
			UndirectedGraph<TwitterUser, DefaultWeightedEdge> cluster = new SimpleWeightedGraph<TwitterUser, DefaultWeightedEdge>(DefaultWeightedEdge.class);

			for(TwitterUser user : userSet) {
				cluster.addVertex(user);
			}

			for(DefaultWeightedEdge edge : this.similarityGraph.edgeSet()) {
				TwitterUser source = this.similarityGraph.getEdgeSource(edge);
				TwitterUser target = this.similarityGraph.getEdgeTarget(edge);
				if(cluster.containsVertex(source) && cluster.containsVertex(target)) {
					cluster.addEdge(source, target, edge);
				}
			}
			
			if(isHighlyConnected(cluster)) {
				this.clusters.add(cluster);
			} else {
				hcs(cluster);
			}
			
		}
	}

	private void hcs(UndirectedGraph<TwitterUser, DefaultWeightedEdge> graph) {
		
		if(graph.vertexSet().size() <= 2) {
			//Log.getLogger().info("Graph too small. Removing this graph: " + graph);
			return;
		}
		
		//Log.getLogger().info("Apply Stoer-Wagner Min-Cut Algorithm.");
		
		StoerWagnerMinimumCut<TwitterUser, DefaultWeightedEdge> minimumCut = new StoerWagnerMinimumCut<TwitterUser, DefaultWeightedEdge>(graph);

		UndirectedGraph<TwitterUser, DefaultWeightedEdge> cutA = new SimpleWeightedGraph<TwitterUser, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		UndirectedGraph<TwitterUser, DefaultWeightedEdge> cutB = new SimpleWeightedGraph<TwitterUser, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		Set<TwitterUser> minCut = minimumCut.minCut();
		//Log.getLogger().info("minCut: " + minCut);
		//Log.getLogger().info("minCutWeight: " + minimumCut.minCutWeight());

		// Add vertices
		for(TwitterUser user : graph.vertexSet()) {
			if(minCut.contains(user)) {
				cutA.addVertex(user);
			} else {
				cutB.addVertex(user);
			}
		}
		
		//Log.getLogger().info("Cut A vertices: " + cutA);
		//Log.getLogger().info("Cut B vertices: " + cutB);

		// Add edges
		for(DefaultWeightedEdge edge : graph.edgeSet()) {
			TwitterUser source = graph.getEdgeSource(edge);
			TwitterUser target = graph.getEdgeTarget(edge);

			if(cutA.containsVertex(source) && cutA.containsVertex(target)) {
				cutA.addEdge(source, target, edge);
			} else if (cutB.containsVertex(source) && cutB.containsVertex(target)) {
				cutB.addEdge(source, target, edge);
			}
		}
		
		//Log.getLogger().info("Cut A with edges: " + cutA);

		if(isHighlyConnected(cutA)) {
			//Log.getLogger().info("Highly connected cluster found: " + cutA);
			this.clusters.add(cutA);
		} else {
			hcs(cutA);
		}
		
		if(isHighlyConnected(cutB)) {
			//Log.getLogger().info("Highly connected cluster found: " + cutB);
			this.clusters.add(cutB);
		} else {
			hcs(cutB);
		}
	}

	private boolean isHighlyConnected(UndirectedGraph<TwitterUser, DefaultWeightedEdge> graph) {
		
		//Log.getLogger().info("Checking for highly connectedness.");
		
		Set<TwitterUser> vertices = graph.vertexSet();
		int k = Integer.MAX_VALUE;
		int n = vertices.size();
		if(n <= 2) return false; // cluster must have at least 3 vertices (according to HCS algo, at least?)
		
		//Log.getLogger().info("n = " + n);
		
		for (TwitterUser user : vertices) {
			int degree = graph.degreeOf(user);
			if(degree < k) k = degree;
		}

        return (k > n/4);
	}	
}
