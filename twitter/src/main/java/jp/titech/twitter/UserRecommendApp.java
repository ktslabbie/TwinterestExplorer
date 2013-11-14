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

		//String screenName = "richfroning";

		/*String[] screenNames = {  "richfroning", "iLikeGirlsDaily", "KaittLouii", "AncientAliens"
								, "Hownby", "mtn_dew", "BarbellShrugged", "Tsoukalos"
								, "RRRawlings", "artpoptart", "Every_Day_Fit", "cborders"
								, "trainHYLETE", "williebosshog", "EastwoodWhiskey", "GoogleEarthPics"
								, "ThorMovies", "JordanAndre625", "mental_floss", "LiamHemsworth"
								, "TipsForYouDaily", "ZaAnderson023", "CrossFitGames", "kelbell2587"
								, "CrossFitMayhem", "morggnichole", "AndreaDeLo1", "michaelfarragut"
								, "AshleyTaffer", "PROGENEX", "LoganSmelcer", "cebohanan"
								, "Rockyj05", "JaimeCheek", "B_Moore018", "PrincessBrittJ"
								, "Rjnowack", "jtsmith3552", "TylerLepore", "FHratliff"
								, "LexSchoenfield", "MIT", "PopMech", "PopSci"
								, "Iron_Man", "GasMonkeyGarage", "cnnbrk", "TheScienceGuy"
								, "NASA"};*/
		
		String[] screenNames = {"MichaelAnke"}; //Adriane_Guarno

		for (String screenName : screenNames) {

			control.startSearchMining(screenName, Vars.TIMELINE_TWEET_COUNT);

			Log.getLogger().info("Creating ontology...");
			control.createUserOntology(screenName, 250);
		}
	}
}
