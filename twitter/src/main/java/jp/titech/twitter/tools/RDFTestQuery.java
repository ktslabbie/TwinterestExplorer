/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 */
package jp.titech.twitter.tools;

import jp.titech.twitter.ontology.dbpedia.RedisQuery;

/**
 * @author Kristian Slabbekoorn
 *
 */
class RDFTestQuery {

	public static void main( String[] args ) {
		RedisQuery rq = new RedisQuery();
		rq.testQuery("http://dbpedia.org/resource/!!!_(album)", "yago");
	}
}
