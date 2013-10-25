/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		18 jun. 2013
 */
package jp.titech.twitter;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.Controller;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

import org.apache.log4j.PropertyConfigurator;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class UserRecommendApp {

	public static void main( String[] args ) {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting program.");
		Controller control = Controller.getInstance();

		String screenName = "Gabe138";

		control.startSearchMining(screenName, Vars.TIMELINE_TWEET_COUNT);
		
		Log.getLogger().info("Creating ontology...");
		control.createUserOntology(screenName, 3200);
	}
}
