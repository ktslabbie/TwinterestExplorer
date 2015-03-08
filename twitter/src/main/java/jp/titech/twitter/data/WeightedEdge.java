package jp.titech.twitter.data;

import jp.titech.twitter.util.Util;

import org.jgrapht.graph.DefaultWeightedEdge;

class WeightedEdge extends DefaultWeightedEdge{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return Util.format(2, getWeight());
	}
}

