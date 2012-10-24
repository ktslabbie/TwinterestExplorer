/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.Controller;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.util.Log;

import org.apache.log4j.PropertyConfigurator;

/**
 * Main method.
 *
 */
public class App {
	
    public static void main( String[] args ) {
    	PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting program.");
		Controller control = Controller.getInstance();
		//control.startSearchMining(812286, 100);
		
		TweetBase tb = TweetBase.getInstance();
		
		/*for (Tweet tweet : tb.getTweets(813286)) {
			Log.getLogger().info(tweet.toString());
		}*/
		
		Log.getLogger().info("Running DBpedia Spotlight on tweet content...");
		control.createOntology(813286);
    }
}
