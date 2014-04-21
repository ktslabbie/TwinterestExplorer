/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 dec. 2012
 */
package jp.titech.twitter.tools;

import jp.titech.twitter.ontology.dbpedia.DBpediaQuery;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class RDFTestQuery {

	public static void main( String[] args ) {
		DBpediaQuery.getInstance().testQuery(Util.readFile(Vars.SPARQL_SCRIPT_DIRECTORY + "test.sparql"));
	}
}
