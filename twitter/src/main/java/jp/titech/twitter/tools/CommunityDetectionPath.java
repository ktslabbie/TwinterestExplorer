/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 */
package jp.titech.twitter.tools;

import java.io.File;
import java.util.ArrayList;

import jp.titech.twitter.ontology.dbpedia.DBpediaQuery;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class CommunityDetectionPath {

	public static double CONFIDENCE = 0.7;
	public static int SUPPORT = 3;
	public static int TOP_K = 100;

	public static final String EVALUATION_INPUT = "../twitter/data/users/mikegroner_25_tweet_window_0.0-0/";
	//public static final String EVALUATION_OUTPUT = "../twitter/data/output/community/result.community.similarity.25_tweet_window_0.0-0.txt";
	public static final String EVALUATION_OUTPUT = "../twitter/data/output/community/result.community.path.25_tweet_window_0.0-0.txt";

	public static void main( String[] args ) {

		ArrayList<File> files = new ArrayList<File>();
		File dir = new File(EVALUATION_INPUT);
		for (File file : dir.listFiles()) {
			files.add(file);
		}

		File outputFile = new File(EVALUATION_OUTPUT);
		String log = "";
		String ranking = "";

		for (int i = 0; i < files.size()-1; i++) {
			File first = files.get(i);

			for(int j = i+1; j < files.size(); j++) {
				File second = files.get(j);

				//log += first.getName() + "\t\t" + Util.calculateYAGOOntologySimilarity(first, second, outputFile, THRESHOLD) + "\n";
				//double similarity = Util.calculateYAGOOntologySimilarity(first, second, CONFIDENCE, SUPPORT, TOP_K);
				double distance = Util.calculateYAGOOntologyDistance(first, second);

				Log.getLogger().info("Average path length: " + distance);


				ranking += first.getName().split("#")[1] + "\t" + second.getName().split("#")[1] + "\t" + distance + "\t";

				//String relevance = rankingLines[i/2].split("\t")[2];
				ranking += "\n";


				/*if(similarity >= CONFIDENCE){
					ranking += first.getName().split("#")[1] + "\t" + second.getName().split("#")[1] + "\t" + similarity + "\t";

					//String relevance = rankingLines[i/2].split("\t")[2];
					ranking += "\n";
				}*/
			}
		}
		Util.writeToFile(ranking, outputFile);

	}
}
