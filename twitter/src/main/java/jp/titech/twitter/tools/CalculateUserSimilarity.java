/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 */
package jp.titech.twitter.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jp.titech.twitter.ontology.dbpedia.DBpediaQuery;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class CalculateUserSimilarity {

	public static final String KEYWORD = "malware";

	public static final String EVALUATION_RESULT = "../twitter/data/Twitter ranking/evaluation/" + KEYWORD + "/" + KEYWORD + ".result.dup.turkeys.tweet_user.turkeys";
	public static final String EVALUATION_OUTPUT = "../twitter/data/Twitter ranking/evaluation/" + KEYWORD + "/" + KEYWORD + ".result.dup.turkeys.tweet_user.taxonomy.usercompare";

	public static void main( String[] args ) {

		ArrayList<File> files = new ArrayList<File>();
		File dir = new File(Vars.OUTPUT_DIRECTORY + KEYWORD + "_user_compare/");
		for (File file : dir.listFiles()) {
			files.add(file);
		}

		File outputFile = new File(EVALUATION_OUTPUT + ".user_compare");
		String log = "";
		String rankingString = "";
		Map<String, Double> rankMap = new HashMap<String, Double>();

		String turkeyRanking = Util.readFile(EVALUATION_RESULT);
		String[] rankingLines = turkeyRanking.split("\n");

		for (int i = 0; i < files.size(); i++) {
			File first = files.get(i);

			for(int j = i+1; j < files.size(); j++){
				File second = files.get(j);

				double similarity = Util.calculateYAGOOntologySimilarity(first, second, 0, 0, -1);
				String relevanceFirst = rankingLines[i].split("\t")[2];
				String relevanceSecond = rankingLines[j].split("\t")[2];
				
				rankMap.put(first.getName().split("[#.]")[1] + "(" + relevanceFirst + ") vs. " + second.getName().split("[#.]")[1] + "(" + relevanceSecond + "):\t", similarity);
			}
		}
		
		Map<String, Double> sortedRankMap = Util.sortByValue(rankMap);
		
		for (Iterator<String> iterator = sortedRankMap.keySet().iterator(); iterator.hasNext();) {
			String str = iterator.next();
			rankingString += str + sortedRankMap.get(str) + "\n";
		}

		Util.writeToFile(rankingString, outputFile);

	}
}
