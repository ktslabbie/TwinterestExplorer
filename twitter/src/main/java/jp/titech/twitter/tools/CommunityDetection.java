/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 *//*
package jp.titech.twitter.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dbpedia.spotlight.model.OntologyType;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.db.TweetBaseUtil;
import jp.titech.twitter.ontology.dbpedia.DBpediaQuery;
import jp.titech.twitter.ontology.similarity.CFIUF;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

*//**
 * @author Kristian Slabbekoorn
 *
 *//*
public class CommunityDetection {

	public static double CONFIDENCE = 0.7;
	public static int SUPPORT = 3;
	public static int TOP_K = 100;

	public static final String EVALUATION_INPUT = "../twitter/data/users/mikegroner_10_tweet_window_0.2-0/";
	public static final String EVALUATION_OUTPUT = "../twitter/data/output/community/result.community.similarity.10_tweet_window_0.2-0.txt";
	//public static final String EVALUATION_OUTPUT = "../twitter/data/output/community/result.community.path.25_tweet_window_0.0-0.txt";

	public static void main( String[] args ) {

		List<TwitterUser> users = TweetBaseUtil.getTwitterUsersFromDirectory(new File(EVALUATION_INPUT));

		File outputFile = new File(EVALUATION_OUTPUT);
		String log = "";
		String ranking = "";

		CFIUF builder = new CFIUF(users);

		Map<String, Map<YAGOType, Double>> tfIdfMap = builder.calculate();
		double max = builder.getMaxCFIUFScore();
		
		Log.getLogger().info("Max TF-IDF: " + max);
		
		for (String userName : tfIdfMap.keySet()) {

			Log.getLogger().info("");
			Log.getLogger().info("USER: " + userName);

			Map<YAGOType, Double> userTFIDFMap = tfIdfMap.get(userName);

			for (YAGOType type : userTFIDFMap.keySet()) {
				Log.getLogger().info("\tTF-IDF: " + Util.format(2, userTFIDFMap.get(type)/max) + "\tType: " + type.typeID());
			}
		}

		Log.getLogger().info("Processed " + users.size() + " users.");

		for (int i = 0; i < users.size()-1; i++) {
			TwitterUser first = users.get(i);

			for(int j = i+1; j < users.size(); j++) {
				TwitterUser second = users.get(j);
				
				
				//log += first.getName() + "\t\t" + Util.calculateYAGOOntologySimilarity(first, second, outputFile, THRESHOLD) + "\n";
				
				//TODO: file-based to user-based !
				//double similarity = Util.calculateYAGOOntologySimilarity(first, second, CONFIDENCE, SUPPORT, TOP_K);
				
				
				//double distance = Util.calculateYAGOOntologyDistance(first, second);

				//Log.getLogger().info("Average path length: " + distance);

				//ranking += first.getName().split("#")[1] + "\t" + second.getName().split("#")[1] + "\t" + distance + "\t";

				//String relevance = rankingLines[i/2].split("\t")[2];
				//ranking += "\n";

				if(similarity >= CONFIDENCE){
					ranking += first.getName().split("#")[1] + "\t" + second.getName().split("#")[1] + "\t" + similarity + "\t";

					//String relevance = rankingLines[i/2].split("\t")[2];
					ranking += "\n";
				}
			}
		}
		
		Util.writeToFile(ranking, outputFile);
	}
}
*/