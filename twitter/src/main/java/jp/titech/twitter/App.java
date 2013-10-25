/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.control.Controller;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

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
		// A CHANGE FROM MASTER TO TWITCOM
		//Obama: 813286
		//G4L_007: 199545800
		//robert_a_walker: 70674405
		//IMjustSD: 221374709
		//Anaris82: 3195651
		//String[] screenNames = {"shaanhaider", "alienvault", "LogicaSecurity", "Sec_Cyber", "Balmich", 
		/*String[] screenNames = {"Hachiro", "ThatArcher", "nukeroadie", "totterdell91", "tjshaw75",
								"rakingmuck", "susila55", "r_cherwink", "NucEnergy", "Aolenergy",
								"forumonenergy", "SoNuke", "nuclearnewsbot", "SKSMediaUAE", "EnergiewendeGER",
								"N_E_I", "nuclearcom", "KiitosMummo", "merijoyce", "japantimes", 
								"ikkakuboo", "ChristinaMac1", "kevinmeyerson", "EnvironmeEnergy", "Atomicrod",
								"NPImagazine", "RPHPresearch", "First_Power", "MyFukushima", "Rainer_Klute", 
								"CANARYorg", "seapillars", "FreyaFoust", "J_Lovering", "psephy",
								"GreenpeaceJO", "arclight", "freedomwv", "watermelon_man", "digital_comic12"};*/
		String[] screenNames = { "GreenpeaceJO"};
		//String[] screenNames = {"InfotechNews_"}; //Adriane_Guarno
		String topic = "nuclear";
		int count = 10;
		
		for (String screenName : screenNames) {
			count++;
			control.startSearchMining(screenName, Vars.TIMELINE_TWEET_COUNT);

			TweetBase tb = TweetBase.getInstance();
			long userID = tb.getUserID(screenName);
			
			Log.getLogger().info("Creating ontology...");
			control.createOntology(userID, screenName, 500, topic, count);
		}
	}
}
