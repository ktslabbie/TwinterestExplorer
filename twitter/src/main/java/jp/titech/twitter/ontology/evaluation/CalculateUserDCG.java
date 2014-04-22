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
public class CalculateUserDCG {

	public static final String USER = "bnmuller";
	//public static final String EVALUATION_PATH = "../twitter/data/Twitter ranking/evaluation/"; // rover.result.dup.turkeys.tweet_user
	public static final int[] TOP_K = {3, 5, 10, 15, 20, 25};

	public static void main( String[] args ) {

		String relevance = Util.readFile(Vars.DATA_DIRECTORY + USER + "_relevance.txt");
		String similarity = Util.readFile(Vars.DATA_DIRECTORY + USER + "_cosine_similarity_0.3.txt");
		//String similarity = Util.readFile(Vars.DATA_DIRECTORY + USER + "_occurrence_similarity_0.3.txt");
		
		Map<String, Integer> relevanceMap = stringToRelevanceMap(relevance);
		
		File outputFile = new File(Vars.DATA_DIRECTORY + USER + ".dcg.cosine.0.3.results.txt");
		//File outputFile = new File(Vars.DATA_DIRECTORY + USER + ".dcg.occurrence.0.3.results.txt");
		String log = "Top-k:\tDCG\tnDCG\n";
		
		for (int k : TOP_K) {
			
			String[] lines = similarity.split("\n");
			if(lines.length == 0) return;
			if(lines.length < k) return;

			double rel1 = relevanceMap.get(lines[0].split("\t")[0]);
			double reli = 0.0;
			double dcg = rel1;
			double idcg = 2;

			Log.getLogger().info("rel1: " + rel1);

			for (int i = 1; i < k; i++) {
				String line = lines[i];
				String[] parts = line.split("\t");
				reli = relevanceMap.get(parts[0]);
				
				dcg 	+= reli * 	Math.log(2) / Math.log(i+1);
				idcg 	+= 2 	* 	Math.log(2) / Math.log(i+1);

				Log.getLogger().info("Current DCG: " + dcg);
				Log.getLogger().info("Current iDCG: " + idcg);
			}
			
			double nDCG = dcg / idcg;
			Log.getLogger().info("nDCG: " + nDCG);
			
			log += "Top-" + k + ":\t" + dcg + "\t" + nDCG + "\n";

			//for (int i = 0; i < files.size(); i++) {
			//	file = files.get(i);
			//log += file.getName() + ":\t" + calculateDCG(Util.readFile(file), k) + "\n";
			//}
			Util.writeToFile(log, outputFile);
		}
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
}
