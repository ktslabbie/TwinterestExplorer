/*package jp.titech.twitter.ontology.similarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserSimilarity;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;

public class OccurrenceSimilarity extends SimilarityFunction {

	public OccurrenceSimilarity(WeightingScheme tWeigthingScheme) {
		super(tWeigthingScheme);
	}

	public SortedSet<UserSimilarity> calculate() {
		Log.getLogger().info("Calculating occurrence similarity.");

		userSimilaritySet = new TreeSet<UserSimilarity>();
		List<TwitterUser> userList = new ArrayList<TwitterUser>();

		for (TwitterUser user : weightingScheme.getUsers()) {
			userList.add(user);
		}

		for(int i = 0; i < userList.size(); i++) {
			TwitterUser userA = userList.get(i);
			for(int j = i + 1; j < userList.size(); j++) {
				TwitterUser userB = userList.get(j);

				double occurrenceSimilarity = this.calculateOccurrenceSimilarity(userA, userB);
				userSimilaritySet.add(new UserSimilarity(userA, userB, occurrenceSimilarity));
			}
		}

		return userSimilaritySet;
	}

	private double calculateOccurrenceSimilarity(TwitterUser userA, TwitterUser userB) {
		Map<YAGOType, Integer> userAMap = userA.getUserOntology().getYAGOTypes();
		Map<YAGOType, Integer> userBMap = userB.getUserOntology().getYAGOTypes();
		Map<YAGOType, Integer> biggestMap;
		Map<YAGOType, Integer> smallestMap;
		int totalSmallest;

		int totalOne = userA.getUserOntology().getYAGOTypeCount();
		int totalTwo = userB.getUserOntology().getYAGOTypeCount();

		if(totalOne > totalTwo) {
			biggestMap = userAMap;
			smallestMap = userBMap;
			totalSmallest = totalTwo;
		} else {
			biggestMap = userBMap;
			smallestMap = userAMap;
			totalSmallest = totalOne;
		}

		double similarity = 0.0;
		int included = totalSmallest;

		for (YAGOType type : smallestMap.keySet()) {

			Integer numberBig = biggestMap.get(type);
			int remaining = smallestMap.get(type);

			if(numberBig != null) {
				remaining -= numberBig;
				if(remaining < 0) remaining = 0;
			}
			included -= remaining;
		}

		similarity = ((double)included/(double)totalSmallest);

		if(Double.isNaN(similarity) || similarity == Double.NEGATIVE_INFINITY) {
			similarity = 0;
		}

		if(similarity >= confidence || Double.isNaN(similarity)) {
			Log.getLogger().info("Total included: " + included + "/" + totalSmallest + " class occurrences. Similarity: " + similarity + "\t\tClass included!");
		} else {
			Log.getLogger().info("Total included: " + included + "/" + totalSmallest + " class occurrences. Similarity: " + similarity + "\t\tClass excluded!");
		}

		return similarity;
	}
	
	public String getName() {
		return "occurrence";
	}
}
*/