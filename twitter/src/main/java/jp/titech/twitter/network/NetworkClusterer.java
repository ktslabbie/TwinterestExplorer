package jp.titech.twitter.network;

import java.util.List;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.network.clustering.ClusteringAlgorithm;

public class NetworkClusterer {

	private ClusteringAlgorithm algorithm;
	
	public NetworkClusterer(ClusteringAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public void cluster() {
		algorithm.execute();
	}
	
	public List<UndirectedGraph<TwitterUser, DefaultWeightedEdge>> getClusters() {
		return algorithm.getClusters();
	}

	public String printClusters() {
		String out = "";
		
		for(UndirectedGraph<TwitterUser, DefaultWeightedEdge> cluster : algorithm.getClusters()) {
			out += "\n\nCluster:\n";
			for(TwitterUser user : cluster.vertexSet()) {
				out += user.getScreenName() + "\n";
			}
		}
		
		return out;
	}
}
