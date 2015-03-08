/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 */
package jp.titech.twitter.ontology.evaluation;

import java.util.Map;
import java.util.SortedMap;

import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class PRFEvaluation {

	private final String targetUser;
	private final SortedMap<String, Double> userSimilarityMap;
	private String evaluationString = "Threshold\tPrecision\tRecall\tF-score\n";
	
	public double THRESH_START = 0.01;
	public double THRESH_END = 0.801;
	public double THRESH_STEP = 0.001;

	public PRFEvaluation(String targetUser, SortedMap<String, Double> userSimilarityMap) {
		this.targetUser = targetUser;
		this.userSimilarityMap = userSimilarityMap;
	}
	
	public void calculate() {
		Map<String, Integer> relevanceMap = Util.stringToRelevanceMap(Util.readFile(Vars.EVALUATION_DIRECTORY + targetUser + "/dcg.relevance.txt"));

		for (double thresh = THRESH_START; thresh < THRESH_END; thresh += THRESH_STEP) {
			double tp = 0, fp = 0, fn = 0;
			
			for (String relUser : relevanceMap.keySet()) {
				if(userSimilarityMap.get(relUser) == null || userSimilarityMap.get(relUser) < thresh) {
					if(relevanceMap.get(relUser) > 0) {
						fn++;
					}
				} else if(userSimilarityMap.get(relUser) >= thresh) {
					if(relevanceMap.get(relUser) > 0) {
						tp++;
					} else {
						fp++;
					}
				}
			}
			
			double precision = tp / (tp+fp);
			double recall = tp / (tp + fn);
			double fScore = 2*((precision*recall)/(precision+recall));
			
			evaluationString += Util.round(3, thresh) + "\t" + precision + "\t" + recall + "\t" + fScore + "\n";
		}
	}
	
	public String getEvaluationString() {
		return evaluationString;
	}
}
