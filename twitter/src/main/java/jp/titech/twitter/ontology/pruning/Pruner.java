/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		17 dec. 2012
 */
package jp.titech.twitter.ontology.pruning;

import jp.titech.twitter.data.UserOntology;

/**
 * @author Kristian Slabbekoorn
 *
 */
public interface Pruner {
	
	public void prune();

	public UserOntology getPrunedUserOntology();
}
