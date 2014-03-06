package jp.titech.twitter.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.titech.twitter.data.TwitterUser;

/**
 * Helper class with convenience functions for dealing with the database.
 * 
 * @author Kristian
 *
 */
public class TweetBaseUtil {
	
	/**
	 * Get a list of TwitterUsers from a directory with files in the format {identifier}#{username}-{parameters}.
	 * 
	 * @param dir The directory to process
	 * @return a List of TwitterUsers
	 */
	public static List<TwitterUser> getTwitterUsersFromDirectory(File dir) {
		List<TwitterUser> userList = new ArrayList<TwitterUser>();
		TweetBase tb = TweetBase.getInstance();
		
		for (String fileName : dir.list()) {
			String userName = fileName.split("#")[1].split("-")[0];
			userList.add(tb.getUser(userName));
		}
		
		return userList;
	}
}
