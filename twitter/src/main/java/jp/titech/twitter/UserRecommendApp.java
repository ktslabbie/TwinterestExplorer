/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		18 jun. 2013
 */
package jp.titech.twitter;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.MiningController;
import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBaseUtil;
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
		OntologyController ontologyController = OntologyController.getInstance();
		MiningController miningController = MiningController.getInstance();

		//String screenName = "richfroning";
		// "richfroning", "iLikeGirlsDaily", "KaittLouii", "AncientAliens"
		//, "Hownby", "mtn_dew", "BarbellShrugged", "Tsoukalos"
		//, "RRRawlings", "artpoptart", "Every_Day_Fit", "cborders" "trainHYLETE", "williebosshog", "EastwoodWhiskey", "GoogleEarthPics"
		//, "ThorMovies", "JordanAndre625", "mental_floss", "LiamHemsworth"
		/* "TipsForYouDaily", "ZaAnderson023", "CrossFitGames", "kelbell2587"
								, "CrossFitMayhem", "morggnichole", "AndreaDeLo1", "michaelfarragut"
								, "AshleyTaffer", "PROGENEX", "LoganSmelcer", "cebohanan"
								, "Rockyj05", "JaimeCheek", "B_Moore018", "PrincessBrittJ"
								, "Rjnowack", "jtsmith3552", "TylerLepore", "FHratliff"
								, "LexSchoenfield", "MIT", 
		*/
		
		
		String[] screenNames = {  "PopMech", "PopSci"
								, "Iron_Man", "GasMonkeyGarage", "cnnbrk", "TheScienceGuy"
								, "NASA"};
		
		//String[] screenNames = {"elonmusk"}; //Adriane_Guarno

		for (String screenName : screenNames) {
			
			TwitterUser user = TweetBaseUtil.getTwitterUserWithScreenName(screenName);

			miningController.mineUser(user);

			Log.getLogger().info("Creating ontology...");
			ontologyController.createUserOntology(user);
		}
	}
}
