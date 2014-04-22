package jp.titech.twitter.ontology.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserSimilarity;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;

public class CosineSimilarity extends SimilarityFunction {
	
	public CosineSimilarity(Set<TwitterUser> users) {
		super(users);
	}
	
	/**
	 * Normalize the DF-IUF map of each user and compare cosine similarities between each user.
	 * Complexity: O(3n)
	 */
	public SortedSet<UserSimilarity> calculate() {
		Log.getLogger().info("Calculating cosine similarity.");
		
		userSimilaritySet = new TreeSet<UserSimilarity>();
		List<TwitterUser> userList = new ArrayList<TwitterUser>();
		
		for (TwitterUser user : users) {
			Map<YAGOType, Double> userTypeMap = user.getUserOntology().getYagoCFIUFMap();
			user.getUserOntology().setYagoCFIUFMap(this.normalize(userTypeMap));
			userList.add(user);
		}
		
		for(int i = 0; i < userList.size(); i++) {
			TwitterUser userA = userList.get(i);
			for(int j = i + 1; j < userList.size(); j++) {
				TwitterUser userB = userList.get(j);
				
				double cosineSimilarity = this.calculateCosineSimilarity(userA, userB);
				if(cosineSimilarity > 0.0) 
					userSimilaritySet.add(new UserSimilarity(userA, userB, cosineSimilarity));
			}
		}
		return userSimilaritySet;
	}

	/**
	 * Calculate cosine similarity between two users.
	 * Complexity: O(n)
	 * 
	 * @param userA
	 * @param userB
	 * @return
	 */
	private double calculateCosineSimilarity(TwitterUser userA, TwitterUser userB) {
		Map<YAGOType, Double> userAMap = userA.getUserOntology().getYagoCFIUFMap();
		Map<YAGOType, Double> userBMap = userB.getUserOntology().getYagoCFIUFMap();
		double similarity = 0.0;
		
		for(YAGOType type : userAMap.keySet()) {
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
	private Map<YAGOType, Double> normalize(Map<YAGOType, Double> userTypeMap) {
		Map<YAGOType, Double> normalizedUserTypeMap = new HashMap<YAGOType, Double>();
		int cfSum = 0;
		
		for(YAGOType type : userTypeMap.keySet())
			cfSum += Math.pow(userTypeMap.get(type), 2);
		
		double euclidLength = Math.sqrt(cfSum);
		
		for(YAGOType type : userTypeMap.keySet())
			normalizedUserTypeMap.put(type, (double) userTypeMap.get(type) / euclidLength);
		
		return normalizedUserTypeMap;
	}
}