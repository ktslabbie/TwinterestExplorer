/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 */
package jp.titech.twitter.ontology.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.titech.twitter.ontology.dbpedia.DBpediaQuery;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class CalculateUserPRF {

	public static final String USER = "bnmuller";
	//public static final String EVALUATION_PATH = "../twitter/data/Twitter ranking/evaluation/"; // rover.result.dup.turkeys.tweet_user
	public double threshold = 0.01;

	public static void main( String[] args ) {

		String relevance = Util.readFile(Vars.DATA_DIRECTORY + USER + "_relevance.txt");
		String similarity = Util.readFile(Vars.DATA_DIRECTORY + USER + "_cosine_similarity_0.3.txt");
		//String similarity = Util.readFile(Vars.DATA_DIRECTORY + USER + "_occurrence_similarity_0.3.txt");

		Map<String, Integer> relevanceMap = stringToRelevanceMap(relevance);
		Map<String, Double> similarityMap = stringToSimilarityMap(similarity);

		File outputFile = new File(Vars.DATA_DIRECTORY + USER + ".prf.cosine.0.3.results.txt");
		//File outputFile = new File(Vars.DATA_DIRECTORY + USER + ".prf.occurrence.0.3.results.txt");
		String log = "Threshold:\tPrecision\tRecall\tF-score\n";

		for (double thresh = 0.01; thresh < 0.801; thresh+=0.01) {
			
			double tp = 0, tn = 0, fp = 0, fn = 0;
			
			for (String relUser : relevanceMap.keySet()) {
				if(similarityMap.get(relUser) == null || similarityMap.get(relUser) < thresh) {
					if(relevanceMap.get(relUser) > 0) {
						fn++;
					} else {
						tn++;
					}
				} else if(similarityMap.get(relUser) >= thresh) {
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
			
			log += Util.round(2, thresh) + "\t" + precision + "\t" + recall + "\t" + fScore + "\n";
		}

		//for (int i = 0; i < files.size(); i++) {
		//	file = files.get(i);
		//log += file.getName() + ":\t" + calculateDCG(Util.readFile(file), k) + "\n";
		//}
		Util.writeToFile(log, outputFile);

	}

	private static Map<String, Integer> stringToRelevanceMap(String text) {

		Map<String, Integer> map = new HashMap<String, Integer>();
		String lines[] = text.split("\n");

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] parts = line.split("\t");
			map.put(parts[0], Integer.parseInt(parts[1]));
		}

		return map;
	}
	
	private static Map<String, Double> stringToSimilarityMap(String text) {

		Map<String, Double> map = new HashMap<String, Double>();
		String lines[] = text.split("\n");

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] parts = line.split("\t");
			map.put(parts[0], Double.parseDouble(parts[1]));
		}

		return map;
	}
}
