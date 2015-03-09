/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		25 okt. 2012
 */
package jp.titech.twitter.ontology.dbpedia;

import java.util.List;

import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.ontology.dbpedia.DBpediaResourceOccurrence;

/**
 * @author Kristian Slabbekoorn
 * 
 * Class for making queries to Redis, which contains hashmaps 
 * of all DBpedia resources with their YAGO types.
 */
public class RedisQuery {
	
	private final RedisClient redis;

	public RedisQuery() {
		redis = RedisClient.getInstance();
	}

	/**
	 * Collects all types from DBpedia Spotlight resource occurrences.
	 * This means both Spotlight native types (DBpedia, Schema, foaf)
	 * and YAGO types by querying Redis.
	 * 
	 * @param occs The Spotlight resource occurrences
	 * @param userOntology The user ontology to add them to
	 */
	public void collectAllTypes(List<DBpediaResourceOccurrence> occs, UserOntology userOntology) {
        for (DBpediaResourceOccurrence occ : occs) {
            String resourceURI = occ.getResource().getFullUri();
            
            // TODO: test if this is a good idea. Adding the actual resources as classes as well.
            userOntology.addClass("RES:" + resourceURI.split("dbpedia.org/resource/")[1]);

            for (String type : occ.getResource().getTypes()) {
                userOntology.addClass(type);
            }

            List<String> resultList = redis.query(resourceURI, "yago");

            for (String entry : resultList) {
                userOntology.addClass("YAGO:" + entry.split("class/yago/")[1]);
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
