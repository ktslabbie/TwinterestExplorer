/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter;

import java.io.File;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.Controller;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

import org.apache.log4j.PropertyConfigurator;
import org.openrdf.rio.RDFFormat;

/**
 * Main method.
 *
 */
public class App {

	public static void main( String[] args ) {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting program.");
		Controller control = Controller.getInstance();
		
		//Obama: 813286
		//G4L_007: 199545800
		//robert_a_walker: 70674405
		
		//control.startSearchMining(70674405, 3000);

		TweetBase tb = TweetBase.getInstance();

		/*for (Tweet tweet : tb.getTweets(813286)) {
			Log.getLogger().info(tweet.toString());
		}*/

		Log.getLogger().info("Creating ontology...");
		control.createOntology(70674405, 3000);
	}
}
