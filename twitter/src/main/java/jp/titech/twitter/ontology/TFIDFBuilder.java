package jp.titech.twitter.ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.dbpedia.spotlight.model.OntologyType;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.ontology.types.YAGOType;

public class TFIDFBuilder {

	private Map<String, Map<YAGOType, Double>> tfIdfMap;
	private Map<String, Map<YAGOType, Double>> nTFIDFMap;
	private List<TwitterUser> users;
	private double maxTFScore = 0.0;
	private double maxTFIDFScore = 0.0;

	public TFIDFBuilder(List<TwitterUser> users) {
		this.tfIdfMap = new HashMap<String, Map<YAGOType,Double>>();
		this.nTFIDFMap = new HashMap<String, Map<YAGOType,Double>>();
		this.users = users;
	}

	public Map<String, Map<YAGOType, Double>> calculateYAGOTFIDF() {

		Map<YAGOType, Integer> tfMap;
		Map<YAGOType, Double> tempTFIDFMap	= new HashMap<YAGOType, Double>();
		YAGOType currentYAGOType = null;
		int N = users.size();

		Map<YAGOType, Integer> dfMap = this.calculateDFMap();

		for (int i = 0; i < users.size(); i++) {
			TwitterUser user = users.get(i);
			tfMap = new HashMap<YAGOType, Integer>();
			Map<OntologyType, Integer> map = TweetBase.getInstance().getUserOntology(user.getUserID());


			for(OntologyType ontType : map.keySet()) {

				if(ontType instanceof YAGOType) {
					currentYAGOType = (YAGOType) ontType;
					tfMap.put(currentYAGOType, map.get(currentYAGOType));
				}
			}

			for (YAGOType tfType : tfMap.keySet()) {
				int tf = tfMap.get(tfType);
				if(tf > maxTFScore) {
					maxTFScore = tf;
				}
				
				double idf = Math.log(N / dfMap.get(tfType));
				double tfIdf = tf*idf;
				
				if(tfIdf > maxTFIDFScore) {
					maxTFIDFScore = tfIdf;
				}

				tempTFIDFMap.put(tfType, tfIdf);
			}

			tfIdfMap.put(user.getScreenName(), tempTFIDFMap );
		}

		return tfIdfMap;
	}
	
	public double getMaxTFScore() {
		return maxTFScore;
	}
	
	public double getMaxTFIDFScore() {
		return maxTFIDFScore;
	}

	private Map<YAGOType, Integer> calculateDFMap() {
		Map<YAGOType, Integer> dfMap = new HashMap<YAGOType, Integer>();

		for (int i = 0; i < users.size(); i++) {
			TwitterUser user = users.get(i);

			Map<OntologyType, Integer> map = TweetBase.getInstance().getUserOntology(user.getUserID());

			for(OntologyType ontType : map.keySet()) {
				if(ontType instanceof YAGOType) {
					YAGOType currentYAGOType = (YAGOType) ontType;

					if(dfMap.get(currentYAGOType) != null) {
						dfMap.put(currentYAGOType, dfMap.get(currentYAGOType)+1);
					} else {
						dfMap.put(currentYAGOType, 1);
					}
				}
			}
		}

		return dfMap;
	}

	/*public Map<String, Map<YAGOType, Double>> normalizeTFIDF() {
		
		for (String userName : tfIdfMap.keySet()) {
			
			
			
			for( YAGOType type : tfIdfMap.get(userName).keySet() ) {
				tfIdfMap.get(userName).get(type)
			}
		}
	}*/
}
