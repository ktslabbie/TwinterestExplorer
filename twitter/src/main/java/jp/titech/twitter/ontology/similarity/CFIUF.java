/*package jp.titech.twitter.ontology.similarity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Vars;

public class CFIUF extends WeightingScheme {

	public CFIUF(Set<TwitterUser> users) {
		super(users, "CF-IUF");
	}

	public void calculate() {
		int N = users.size();

		Map<YAGOType, Integer> dfMap = this.calculateDFMap();

		for (TwitterUser user : users) {
			Map<YAGOType, Double> userCFIDFMap	= new HashMap<YAGOType, Double>();
			Map<YAGOType, Integer> cfMap = user.getUserOntology().getYAGOTypes();

			for (YAGOType yagoType : cfMap.keySet()) {
				double cf = cfMap.get(yagoType);

				double iuf = Math.log((double)N / (double)dfMap.get(yagoType));
				double cfIuf = (Math.pow(cf, 1 + Vars.GENERALITY_BIAS))*(Math.pow(iuf, 1 - Vars.GENERALITY_BIAS));

				userCFIDFMap.put(yagoType, cfIuf);
			}
			
			user.getUserOntology().setYagoCFIUFMap(userCFIDFMap);
			this.userWeightingMaps.put(user, userCFIDFMap);
		}
	}

	private Map<YAGOType, Integer> calculateDFMap() {
		Map<YAGOType, Integer> dfMap = new HashMap<YAGOType, Integer>();

		for (TwitterUser user : users) {
			Map<YAGOType, Integer> map = user.getUserOntology().getYAGOTypes();

			for(YAGOType yagoType : map.keySet()) {
				if(dfMap.get(yagoType) != null) dfMap.put(yagoType, dfMap.get(yagoType)+1);
				else dfMap.put(yagoType, 1);
			}
		}
		
		return dfMap;
	}
}
*/