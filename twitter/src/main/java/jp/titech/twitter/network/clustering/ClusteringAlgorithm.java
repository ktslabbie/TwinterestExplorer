package jp.titech.twitter.network.clustering;

import java.util.List;

import jp.titech.twitter.data.TwitterUser;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public abstract class ClusteringAlgorithm {

	UndirectedGraph<TwitterUser, DefaultWeightedEdge> similarityGraph;
	List<UndirectedGraph<TwitterUser, DefaultWeightedEdge>> clusters;

	public abstract void execute();
	
	/**
	 * @return the clusters
	 */
	public List<UndirectedGraph<TwitterUser, DefaultWeightedEdge>> getClusters() {
		return clusters;
	}
}
