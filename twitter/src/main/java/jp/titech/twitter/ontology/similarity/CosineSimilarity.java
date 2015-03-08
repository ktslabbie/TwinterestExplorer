package jp.titech.twitter.ontology.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserSimilarity;
import jp.titech.twitter.util.Log;

public class CosineSimilarity extends SimilarityFunction {
	
	public CosineSimilarity(WeightingScheme tWeigthingScheme) {
		super(tWeigthingScheme);
	}
	
	/**
	 * Normalize the CF-IUF map of each user and compare cosine similarities between each user.
	 * Complexity: O(3n)
	 */
	public void calculate() {
		Log.getLogger().info("Calculating cosine similarity.");
		
		userSimilaritySet = new TreeSet<UserSimilarity>();
		List<TwitterUser> userList = new ArrayList<TwitterUser>();
		
		for (TwitterUser user : weightingScheme.getUsers()) {
			Map<?, Double> weightMap = this.normalize(weightingScheme.getWeightMapByUser(user));
			weightingScheme.getUserWeightingMaps().put(user, weightMap);
			userList.add(user);
			
		}
		
		for(int i = 0; i < userList.size(); i++) {
			TwitterUser userA = userList.get(i);
			for(int j = i + 1; j < userList.size(); j++) {
				TwitterUser userB = userList.get(j);
				
				double cosineSimilarity = this.calculateCosineSimilarity(weightingScheme.getWeightMapByUser(userA), weightingScheme.getWeightMapByUser(userB));
				//Log.getLogger().info("Cosine similarity between user " + userA.getScreenName() + " and user " + userB.getScreenName() + ": " + cosineSimilarity);
				
				if(cosineSimilarity > 0.0) {
					userSimilaritySet.add(new UserSimilarity(userA, userB, cosineSimilarity));
				}
			}
		}
	}

	/**
	 * Calculate cosine similarity between two users.
	 * Complexity: O(n)
	 * 
	 * @param map
	 * @param map2
	 * @return
	 */
	private double calculateCosineSimilarity(Map<?, Double> userAMap, Map<?, Double> userBMap) {
		double similarity = 0.0;
		
		for(Object type : userAMap.keySet()) {
			Double bValue = userBMap.get(type);
			if(bValue != null) similarity += userAMap.get(type) * bValue;
		}
		
		return similarity;
	}

	/**
	 * Normalize the CF map for one user.
	 * Complexity: O(2n)
	 * 
	 * @param userTypeMap
	 * @return
	 */
	private Map<Object, Double> normalize(Map<?, Double> userTypeMap) {
		Map<Object, Double> normalizedUserTypeMap = new HashMap<Object, Double>();
		int cfSum = 0;
		
		for(Object type : userTypeMap.keySet())
			cfSum += Math.pow(userTypeMap.get(type), 2);
		
		double euclidLength = Math.sqrt(cfSum);
		
		for(Object type : userTypeMap.keySet())
			normalizedUserTypeMap.put(type, userTypeMap.get(type) / euclidLength);
		
		return normalizedUserTypeMap;
	}
	
	public String getName() {
		return weightingScheme.getWeightingName() + ".cosine";
	}
}