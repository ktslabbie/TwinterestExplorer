package jp.titech.twitter.ontology.similarity;

import java.util.Set;
import java.util.SortedSet;

import jp.titech.twitter.data.TwitterUser;

public abstract class SimilarityFunction {

	protected Set<TwitterUser> users;
	protected SortedSet<UserSimilarity> userSimilaritySet;
	
	SimilarityFunction(Set<TwitterUser> users) {
		this.users = users;
	};
	
	public abstract SortedSet<UserSimilarity> calculate();
	
	public String userSimilarityString(String screenName) {
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
	
	/**
	 * @return the userSimilaritySet
	 */
	public SortedSet<UserSimilarity> getUserSimilaritySet() {
		return userSimilaritySet;
	}
}
