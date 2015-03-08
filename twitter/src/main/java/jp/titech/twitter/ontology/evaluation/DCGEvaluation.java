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

	private final String targetUser;
	private final SortedMap<String, Double> userSimilarityMap;
	private final int[] topK;
	private String evaluationString = "Top-k\tnDCG\tBinDCG\n";
	
	public DCGEvaluation(String targetUser, SortedMap<String, Double> userSimilarityMap, int[] topK) {
		this.targetUser = targetUser;
		this.userSimilarityMap = userSimilarityMap;
		this.topK = topK;
	}
	
	public void calculate() {
		Map<String, Integer> relevanceMap = Util.stringToRelevanceMap(Util.readFile(Vars.EVALUATION_DIRECTORY + targetUser + "/dcg.relevance.txt"));
		String currentUser;
		
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

			double reli;
			double binreli;
			double dcg = relevanceMap.get(currentUser);
			double binDCG = (relevanceMap.get(currentUser) > 0) ? 2 : 0;
			double idcg = 2;
			int i = 1;

			Log.getLogger().info("rel1: " + dcg);
			
			while (twitterUserIt.hasNext() && i < k) {
				currentUser = twitterUserIt.next().getKey();
				Log.getLogger().info("currentUser: " + currentUser);
				Log.getLogger().info("currentSimilarity: " + userSimilarityMap.get(currentUser));
				reli = relevanceMap.get(currentUser);
				binreli = (relevanceMap.get(currentUser) > 0) ? 2 : 0;
				
				dcg 	+= reli * 	Math.log(2) / Math.log(i+1);
				binDCG 	+= binreli * 	Math.log(2) / Math.log(i+1);
				idcg 	+= 2 	* 	Math.log(2) / Math.log(i+1);
				i++;

				Log.getLogger().info("Current DCG: " + dcg);
				Log.getLogger().info("Current binDCG: " + binDCG);
				Log.getLogger().info("Current iDCG: " + idcg);
			}
			
			double nDCG = dcg / idcg;
			double binnDCG = binDCG / idcg;
			Log.getLogger().info("nDCG: " + nDCG);
			Log.getLogger().info("bin nDCG: " + binnDCG);
			
			evaluationString += "Top-" + k + "\t" + nDCG + "\t" + binnDCG + "\n";
		}
	}

	public String getEvaluationString() {
		return evaluationString;
	}
}
