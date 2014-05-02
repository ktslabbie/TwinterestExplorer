/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 *//*
package jp.titech.twitter.tools;

import java.io.File;
import java.util.ArrayList;

import jp.titech.twitter.ontology.dbpedia.DBpediaQuery;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

*//**
 * @author Kristian Slabbekoorn
 *
 *//*
public class CalculateSimilarity {
	
	public static final String KEYWORD = "malware";
	public static double CONFIDENCE = 0.1;
	public static int SUPPORT = 0;
	
	public static final String EVALUATION_RESULT = "../twitter/data/Twitter ranking/evaluation/" + KEYWORD + "/" + KEYWORD + ".result.dup.turkeys.tweet_user.turkeys";
	public static final String EVALUATION_OUTPUT = "../twitter/data/Twitter ranking/evaluation/" + KEYWORD + "/" + KEYWORD + ".result.dup.turkeys.tweet_user.taxonomy";

	public static void main( String[] args ) {
		
		for(CONFIDENCE = 0.1; CONFIDENCE <= 0.91; CONFIDENCE += 0.1) {
			ArrayList<File> files = new ArrayList<File>();
			File dir = new File(Vars.OUTPUT_DIRECTORY + KEYWORD + "/");
			for (File file : dir.listFiles()) {
				files.add(file);
			}
			
			File outputFile = new File(EVALUATION_OUTPUT + "." + CONFIDENCE);
			String log = "";
			String ranking = "";
			
			String turkeyRanking = Util.readFile(EVALUATION_RESULT);
			String[] rankingLines = turkeyRanking.split("\n");
			
			for (int i = 0; i < files.size(); i++) {
				File first = files.get(i);
				File second = files.get(i+1);
				
				//log += first.getName() + "\t\t" + Util.calculateYAGOOntologySimilarity(first, second, outputFile, THRESHOLD) + "\n";
				double similarity = Util.calculateYAGOOntologySimilarity(first, second, CONFIDENCE, SUPPORT, -1);
				
				if(similarity >= CONFIDENCE){
					ranking += first.getName().split("#")[1] + "\t" + similarity + "\t";
					String relevance = rankingLines[i/2].split("\t")[2];
					ranking += relevance + "\n";
				}
				
				i++;
			}
			
			Util.writeToFile(ranking, outputFile);
		}
	}
}
*/