/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.control;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.mining.UserMiner;
import jp.titech.twitter.util.Vars;

/**
 * Class to control most of the functionality of the program.
 * 
 * Implemented as a singleton class.
 * 
 * @author Kristian
 *
 */
public class MiningController {

	private UserMiner 					userMiner;
	private static MiningController 	controller;

	private MiningController() {}

	/**
	 * Mine the last count number of tweets of user user and store in the database. 
	 * 
	 * @param user
	 */
	public void mineUser(TwitterUser user) {
		
		if(Vars.MINING_MODE.equals("NONE")) {
			userMiner = new UserMiner(user, UserMiner.MINE_NONE);
		} else if(Vars.MINING_MODE.equals("NEW")) {
			userMiner = new UserMiner(user, UserMiner.MINE_NEW);
		} else if (Vars.MINING_MODE.equals("ALL")) {
			userMiner = new UserMiner(user, UserMiner.MINE_ALL);
		}
		
		userMiner.mineUser();
	}
	
	public UserMiner getUserMiner(){
		return userMiner;
	}
	
	/**
	 * Retrieve the Controller singleton instance.
	 * 
	 * @return the controller singleton
	 */
	public static MiningController getInstance(){
		if(controller == null){ controller = new MiningController(); }
		return controller;
	}
}
