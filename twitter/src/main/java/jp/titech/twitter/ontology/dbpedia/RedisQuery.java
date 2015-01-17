/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		25 okt. 2012
 */
package jp.titech.twitter.ontology.dbpedia;

import java.util.Iterator;
import java.util.List;

import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.ontology.types.OntologyType;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.ontology.dbpedia.DBpediaResourceOccurrence;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class RedisQuery {
	
	private RedisClient redis;

	public RedisQuery() {
		redis = RedisClient.getInstance();
	}

	public void collectAllTypes(List<DBpediaResourceOccurrence> occs, UserOntology userOntology) {
		for(Iterator<DBpediaResourceOccurrence> occIt = occs.iterator(); occIt.hasNext();) {
			DBpediaResourceOccurrence occ = occIt.next();
			String resourceURI = occ.getResource().getFullUri();
			
			for(OntologyType type : occ.getResource().getTypes()) {
				userOntology.addClass(type);
			}
			
			List<String> resultList = redis.query(resourceURI, "yago");
			
			for (String entry : resultList) {
				userOntology.addClass(new YAGOType(entry.split("class/yago/")[1]));
			}
		}
	}

	/**
	 * Execute a single, manually input test query.
	 * 
	 * @param string
	 */
	public void testQuery(String uri, String type){

		List<String> list = redis.query(uri, type);

		for (String entry : list) {
			Log.getLogger().info("Result: " + entry);
		}
	}
}
