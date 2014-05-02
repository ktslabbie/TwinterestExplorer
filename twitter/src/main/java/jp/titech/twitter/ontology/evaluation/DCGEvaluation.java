/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 */
package jp.titech.twitter.ontology.evaluation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class DCGEvaluation {

	private String targetUser;
	private SortedMap<String, Double> userSimilarityMap;
	private int[] topK;
	private String evaluationString = "Top-k\tnDCG\n";
	
	public DCGEvaluation(String targetUser, SortedMap<String, Double> userSimilarityMap, int[] topK) {
		this.targetUser = targetUser;
		this.userSimilarityMap = userSimilarityMap;
		this.topK = topK;
	}
	
	public void calculate() {
		Map<String, Integer> relevanceMap = Util.stringToRelevanceMap(Util.readFile(Vars.EVALUATION_DIRECTORY + targetUser + "/dcg.relevance.txt"));
		String currentUser = null;
		
		List<Entry<String, Double>> sortedEntries = Util.sortSimilarityMapByValue(userSimilarityMap);
		
		for (int k : topK) {
			if(userSimilarityMap.size() == 0) return;
			if(userSimilarityMap.size() < k) return;
			
			Iterator<Entry<String, Double>> twitterUserIt = sortedEntries.iterator();
			
			if(twitterUserIt.hasNext()) {
				currentUser = twitterUserIt.next().getKey();
			} else {
				return;
			}

			double reli = 0.0;
			double dcg = relevanceMap.get(currentUser);
			double idcg = 2;
			int i = 1;

			Log.getLogger().info("rel1: " + dcg);
			
			while (twitterUserIt.hasNext() && i < k) {
				currentUser = twitterUserIt.next().getKey();
				Log.getLogger().info("currentUser: " + currentUser);
				Log.getLogger().info("currentSimilarity: " + userSimilarityMap.get(currentUser));
				reli = relevanceMap.get(currentUser);
				
				dcg 	+= reli * 	Math.log(2) / Math.log(i+1);
				idcg 	+= 2 	* 	Math.log(2) / Math.log(i+1);
				i++;

				Log.getLogger().info("Current DCG: " + dcg);
				Log.getLogger().info("Current iDCG: " + idcg);
			}
			
			double nDCG = dcg / idcg;
			Log.getLogger().info("nDCG: " + nDCG);
			
			evaluationString += "Top-" + k + "\t" + nDCG + "\n";
		}
	}

	public String getEvaluationString() {
		return evaluationString;
	}
}
