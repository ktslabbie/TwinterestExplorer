package jp.titech.twitter.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.titech.twitter.data.TwitterUser;

public class TweetBaseUtil {
	
	static private TweetBase tweetBase = TweetBase.getInstance();

	public static List<TwitterUser> getTwitterUsersFromDirectory(File dir) {
		
		List<TwitterUser> userList = new ArrayList<TwitterUser>();
		
		for (String fileName : dir.list()) {
			String userName = fileName.split("#")[1].split("-")[0];
			userList.add(tweetBase.getUser(userName));
		}
		
		return userList;
	}
}
