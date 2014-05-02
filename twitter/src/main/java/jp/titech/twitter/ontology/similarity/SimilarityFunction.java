package jp.titech.twitter.ontology.similarity;

import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserSimilarity;

public abstract class SimilarityFunction {

	protected SortedSet<UserSimilarity> userSimilaritySet;
	protected WeightingScheme weightingScheme;
	
	SimilarityFunction(WeightingScheme weightingScheme) {
		this.weightingScheme = weightingScheme;
	};
	
	public abstract SortedSet<UserSimilarity> calculate();
	public abstract String getName();
	
	public String getUserSimilarityString(String screenName) {
		String ret = "";
		
		for(UserSimilarity sim : userSimilaritySet) {
			if(sim.getUserA().getScreenName().equals(screenName)) {
				ret += sim.getUserB().getScreenName() + "\t" + sim.getSimilarity() + "\n";
			} else if (sim.getUserB().getScreenName().equals(screenName)) {
				ret += sim.getUserA().getScreenName() + "\t" + sim.getSimilarity() + "\n";
			}
		}
		
		return ret;
	}

	public SortedMap<String, Double> getSingleUserSimilarityMap(String screenName) {
		SortedMap<String, Double> singleUserSimilarityMap = new TreeMap<String, Double>();
		
		for(UserSimilarity sim : userSimilaritySet) {
			if(sim.getUserA().getScreenName().equals(screenName)) {
				singleUserSimilarityMap.put(sim.getUserB().getScreenName(), sim.getSimilarity());
			} else if (sim.getUserB().getScreenName().equals(screenName)) {
				singleUserSimilarityMap.put(sim.getUserA().getScreenName(), sim.getSimilarity());
			}
		}
		
		return singleUserSimilarityMap;
	}
	
	/**
	 * @return the userSimilaritySet
	 */
	public SortedSet<UserSimilarity> getUserSimilaritySet() {
		return userSimilaritySet;
	}
}
