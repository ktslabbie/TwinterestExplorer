/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 */
package jp.titech.twitter.tools;

import java.io.File;
import java.util.ArrayList;

import jp.titech.twitter.ontology.DBpediaQuery;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class CalculateDCG {

	public static final String KEYWORD = "malware"; 
	public static final String EVALUATION_PATH = "../twitter/data/Twitter ranking/evaluation/"; // rover.result.dup.turkeys.tweet_user
	public static final int[] TOP_K = {5, 10, 20};

	public static void main( String[] args ) {

		for (int k : TOP_K) {
			ArrayList<File> files = new ArrayList<File>();
			File dir = new File(EVALUATION_PATH + KEYWORD);
			for (File file : dir.listFiles()) {
				files.add(file);
			}

			File outputFile = new File(EVALUATION_PATH + KEYWORD + ".results/" + KEYWORD + ".top" + k + ".dcg.results");
			String log = "Filename:\tDCG (top-" + k + ")\tnDCG (top-" + k + ")\n";

			for (int i = 0; i < files.size(); i++) {
				File file = files.get(i);
				log += file.getName() + ":\t" + calculateDCG(Util.readFile(file), k) + "\n";
			}
			Util.writeToFile(log, outputFile);
		}
	}

	public static String calculateDCG(String ranking, int k){

		String output = "";

		String[] lines = ranking.split("\n");
		if(lines.length == 0) return "Empty ranking!";
		if(lines.length < k) return "";

		for (int i = 0; i < k; i++) {
			String line = lines[i];
			String[] parts = line.split("\t");
			if(parts.length != 3) return "No relevance score defined for user at rank " + (i+1) + "!";
		}

		double rel1 = Integer.parseInt(lines[0].split("\t")[2]);
		double reli = 0.0;
		double dcg = rel1;
		double idcg = 2;

		Log.getLogger().info("rel1: " + rel1);

		for (int i = 1; i < k; i++) {
			String line = lines[i];
			String[] parts = line.split("\t");
			reli = Double.parseDouble(parts[2]);
			
			dcg 	+= reli * 	Math.log(2) / Math.log(i+1);
			idcg 	+= 2 	* 	Math.log(2) / Math.log(i+1);

			Log.getLogger().info("Current DCG: " + dcg);
			Log.getLogger().info("Current iDCG: " + idcg);
		}
		
		double nDCG = dcg / idcg;
		Log.getLogger().info("nDCG: " + nDCG);
		output += dcg + "\t" + nDCG;
		
		return output;
	}
}
