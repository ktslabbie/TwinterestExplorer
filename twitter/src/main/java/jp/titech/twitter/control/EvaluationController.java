/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import java.io.File;
import java.util.SortedMap;

import jp.titech.twitter.ontology.evaluation.DCGEvaluation;
import jp.titech.twitter.ontology.evaluation.PRFEvaluation;
import jp.titech.twitter.ontology.similarity.SimilarityFunction;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * Class to control most of the functionality of the program.
 * 
 * Implemented as a singleton class.
 * 
 * @author Kristian
 *
 */
public class EvaluationController {

	private static EvaluationController controller;

	private EvaluationController() {}

	public void evaluate(SimilarityFunction similarityFunction, String targetUser) {
		
		similarityFunction.calculate();
		Util.writeToFile(similarityFunction.getUserSimilarityString(targetUser), 
							new File(Vars.EVALUATION_DIRECTORY + targetUser + "/similarity." + similarityFunction.getName() + "." + Vars.SPOTLIGHT_CONFIDENCE + ".txt"));
		
		SortedMap<String, Double> singleUserSimilarityMap = similarityFunction.getSingleUserSimilarityMap(targetUser);
		
		int[] topK = {3, 5, 10, 15, 20, 25};
		DCGEvaluation dcgEvaluation = new DCGEvaluation(targetUser, singleUserSimilarityMap, topK);
		dcgEvaluation.calculate();
		Util.writeToFile(dcgEvaluation.getEvaluationString(), 
							new File(Vars.EVALUATION_DIRECTORY + targetUser + "/dcg." + similarityFunction.getName() + "." + Vars.SPOTLIGHT_CONFIDENCE + ".txt"));
		
		PRFEvaluation prfEvaluation = new PRFEvaluation(targetUser, singleUserSimilarityMap);
		
		if(similarityFunction.getName().contains("TF-IDF")) {
			prfEvaluation.THRESH_START /= 10;
			prfEvaluation.THRESH_END /= 10;
			prfEvaluation.THRESH_STEP /= 10;
		}
		
		prfEvaluation.calculate();
		Util.writeToFile(prfEvaluation.getEvaluationString(), 
							new File(Vars.EVALUATION_DIRECTORY + targetUser + "/prf." + similarityFunction.getName() + "." + Vars.SPOTLIGHT_CONFIDENCE + ".txt"));
	}
	
	/**
	 * Retrieve the Controller singleton instance.
	 * 
	 * @return the controller singleton
	 */
	public static EvaluationController getInstance(){
		if(controller == null){ controller = new EvaluationController(); }
		return controller;
	}

}
