/*package jp.titech.twitter.ontology.similarity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.titech.twitter.data.TwitterUser;

public class TFIDF extends WeightingScheme {

	public TFIDF(Set<TwitterUser> users) {
		super(users, "TF-IDF");
	}

	public void calculate() {
		int N = users.size();

		Map<String, Integer> dfMap = this.calculateDFMap();

		for (TwitterUser user : users) {
			Map<String, Double> userTFIDFMap	= new HashMap<String, Double>();
			Map<String, Integer> tfMap			= user.computeAndGetTermFrequencyMap();

			for (String term : tfMap.keySet()) {
				double tf = tfMap.get(term);

				double idf = Math.log((double) N / (double) dfMap.get(term));
				double tfIdf = (Math.pow(tf, 1)) * (Math.pow(idf, 1));

				userTFIDFMap.put(term, tfIdf);
			}

			user.setTFIDFMap(userTFIDFMap);
			userWeightingMaps.put(user, userTFIDFMap);
		}
	}

	private Map<String, Integer> calculateDFMap() {
		Map<String, Integer> dfMap = new HashMap<String, Integer>();
		for (TwitterUser user : users) {
			Map<String, Integer> userTFMap = user.getTermFrequencyMap();
			
			for (String term : userTFMap.keySet()) {
				if(dfMap.get(term) != null) dfMap.put(term, dfMap.get(term)+1);
				else dfMap.put(term, 1);
			}
		}
		return dfMap;
	}
}
*/