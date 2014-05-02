package jp.titech.twitter.ontology.similarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.titech.twitter.data.TwitterUser;

public abstract class WeightingScheme {

	protected Set<TwitterUser> users;
	protected Map<TwitterUser, Map<?, Double>>	userWeightingMaps;
	private String weightingName;

	WeightingScheme(Set<TwitterUser> users, String weightingName) {
		this.users = users;
		this.userWeightingMaps = new HashMap<TwitterUser, Map<?, Double>>();
		this.weightingName = weightingName;
	}
	
	public abstract void calculate();
	
	public Map<TwitterUser, Map<?, Double>>	getUserWeightingMaps() {
		return this.userWeightingMaps;
	}
	
	public Map<?, Double> getWeightMapByUser(TwitterUser user) {
		return userWeightingMaps.get(user);
	}
	
	public Set<TwitterUser> getUsers() {
		return this.users;
	}
	
	public String getWeightingName() {
		return this.weightingName;
	}
}
