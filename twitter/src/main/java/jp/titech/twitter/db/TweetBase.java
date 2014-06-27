/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbpedia.spotlight.model.OntologyType;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import jp.titech.twitter.control.OntologyController;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * Singleton class handling SQL interactions with the (Apache Derby) database.
 * 
 * @author Kristian
 *
 */
public class TweetBase {

	private static TweetBase 	tweetBase; // TweetBase is a singleton class

	private Connection			dbConnection;

	private String				sqlSelectDir = Vars.SQL_SCRIPT_DIRECTORY + "select/";
	private String				sqlInsertDir = Vars.SQL_SCRIPT_DIRECTORY + "insert/";
	private String				sqlUpdateDir = Vars.SQL_SCRIPT_DIRECTORY + "update/";

	private PreparedStatement 	addTweetStatement, addHashtagStatement, addURLStatement, addUserMentionStatement, 
	addMediaStatement, addLocationStatement, addOntologyStatement, addUserStatement, updateUserEnglishRateStatement, addUserFollowerStatement;

	private PreparedStatement 	getSingleTweetStatement, getTweetsStatement, getHashtagsStatement, getLocationStatement, getURLStatement,
	getUserMentionsStatement, getMediaStatement, getUserOntologyStatement, getUserByIDStatement,
	getUserByNameStatement, getUserFollowersStatement;

	/**
	 * Default constructor.
	 */
	private TweetBase(){
		initDB();
		prepareStatements();
	}

	/**
	 * Initialize the database.
	 */
	private void initDB(){
		try {
			String strUrl = "jdbc:derby:TweetBase";
			dbConnection = DriverManager.getConnection(strUrl);
			Log.getLogger().info("Connected to Database: " + dbConnection.toString());
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	/**
	 * Prepare SQL statements in memory for quick access.
	 */
	private void prepareStatements() {
		try {
			addTweetStatement 			= dbConnection.prepareStatement(Util.readFile(this.sqlInsertDir + "add_tweet.sql"));
			addHashtagStatement 		= dbConnection.prepareStatement(Util.readFile(this.sqlInsertDir + "add_hashtag.sql"));
			addLocationStatement 		= dbConnection.prepareStatement(Util.readFile(this.sqlInsertDir + "add_location.sql"));
			addURLStatement 			= dbConnection.prepareStatement(Util.readFile(this.sqlInsertDir + "add_url.sql"));
			addUserMentionStatement 	= dbConnection.prepareStatement(Util.readFile(this.sqlInsertDir + "add_user_mention.sql"));
			addMediaStatement 			= dbConnection.prepareStatement(Util.readFile(this.sqlInsertDir + "add_media.sql"));
			addOntologyStatement 		= dbConnection.prepareStatement(Util.readFile(this.sqlInsertDir + "add_ontology.sql"));
			addUserStatement 			= dbConnection.prepareStatement(Util.readFile(this.sqlInsertDir + "add_user.sql"));
			addUserFollowerStatement	= dbConnection.prepareStatement(Util.readFile(this.sqlInsertDir + "add_user_follower.sql"));

			getTweetsStatement 			= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_user_tweets.sql"));
			getSingleTweetStatement 	= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_single_tweet.sql"));
			getUserMentionsStatement 	= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_user_mentions.sql"));
			getHashtagsStatement 		= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_hashtags.sql"));
			getURLStatement 			= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_urls.sql"));
			getMediaStatement 			= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_media.sql"));
			getLocationStatement 		= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_location.sql"));
			getUserOntologyStatement 	= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_user_ontology.sql"));
			getUserByIDStatement 		= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_user_by_id.sql"));
			getUserByNameStatement 		= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_user_by_screen_name.sql"));
			getUserFollowersStatement	= dbConnection.prepareStatement(Util.readFile(this.sqlSelectDir + "select_user_followers.sql"));
			
			updateUserEnglishRateStatement	= dbConnection.prepareStatement(Util.readFile(this.sqlUpdateDir + "update_user_english_rate.sql"));

			Log.getLogger().info("SQL statements prepared.");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	/**
	 * Add a tweet with the given parameters to the database (if not yet contained).
	 * 
	 * @param tweet the tweet to add
	 */
	public void addTweet(Tweet tweet) {

		long tweetID = tweet.getTweetID();

		if(this.isContained(tweetID)) {
			Log.getLogger().warn("Tweet from user @" + tweet.getScreenName() + " with ID " + tweetID + " already contained in DB! Skipping.");
			return;
		}

		try {
			addTweetStatement.clearParameters();
			addTweetStatement.setLong(1, tweetID);
			addTweetStatement.setLong(2, tweet.getUserID());
			addTweetStatement.setString(3, tweet.getScreenName());
			addTweetStatement.setDate(4, new java.sql.Date(tweet.getCreatedAt().getTime()));
			addTweetStatement.setString(5, tweet.getContent());
			addTweetStatement.setBoolean(6, tweet.isRetweet());
			addTweetStatement.setString(7, tweet.getLanguage());
			addTweetStatement.executeUpdate();

			Log.getLogger().info("Successfully added tweet to DB!");

			if(tweet.getLocationName() != null) {
				addLocationStatement.clearParameters();
				addLocationStatement.setLong(1, tweetID);
				addLocationStatement.setString(2, tweet.getLocationName());
				addLocationStatement.executeUpdate();
				Log.getLogger().info("Successfully added location " + tweet.getLocationName() + " to DB!");
			}

			for (String entity : tweet.getUserMentions()) {
				addUserMentionStatement.clearParameters();
				addUserMentionStatement.setLong(1, tweetID);
				addUserMentionStatement.setString(2, entity);
				addUserMentionStatement.executeUpdate();
				Log.getLogger().info("Successfully added user mention " + entity + " to DB!");
			}

			for (String entity : tweet.getHashtags()) {
				addHashtagStatement.clearParameters();
				addHashtagStatement.setLong(1, tweetID);
				addHashtagStatement.setString(2, entity);
				addHashtagStatement.executeUpdate();
				Log.getLogger().info("Successfully added hashtag " + entity + " to DB!");
			}	

			for (String entity : tweet.getURLs()) {
				addURLStatement.clearParameters();
				addURLStatement.setLong(1, tweetID);
				addURLStatement.setString(2, entity);
				addURLStatement.executeUpdate();
				Log.getLogger().info("Successfully added URL " + entity + " to DB!");
			}

			for (String entity : tweet.getMedia()) {
				addMediaStatement.clearParameters();
				addMediaStatement.setLong(1, tweetID);
				addMediaStatement.setString(2, entity);
				addMediaStatement.executeUpdate();
				Log.getLogger().info("Successfully added media " + entity + " to DB!");
			}	

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	/**
	 * Add a user (in twitter4j format) to the database.
	 * 
	 * @param user
	 */
	public TwitterUser addUser(User user) {
		return addUser(user.getId(), user.getScreenName(), user.getName(), user.getDescription(), user.getLocation(),
				user.getFollowersCount(), user.getFriendsCount(), user.getStatusesCount(), user.getCreatedAt(), user.isProtected(), -1.0);
	}

	/**
	 * Add a user with the given parameters to the database (if not yet contained).
	 * 
	 * @param userID
	 * @param screenName
	 * @param name
	 * @param description
	 * @param location
	 * @param followersCount
	 * @param friendsCount
	 * @param statusesCount
	 * @param createdAt
	 * @param isProtected
	 * 
	 * @return the user just added in TwitterUser object form
	 */
	public TwitterUser addUser(long userID, String screenName, String name, String description, String location, 
			int followersCount, int friendsCount, int statusesCount, Date createdAt, boolean isProtected, double englishRate) {

		if(this.isContained(userID)) {
			Log.getLogger().warn("User " + screenName + " with ID " + userID + " already contained in DB! Skipping.");
			return new TwitterUser(userID, screenName, name, description, location, followersCount, friendsCount, statusesCount, createdAt, isProtected, englishRate);
		}

		try {
			addUserStatement.clearParameters();
			addUserStatement.setLong(1, userID);
			addUserStatement.setString(2, screenName);
			addUserStatement.setString(3, name);
			addUserStatement.setString(4, description);
			addUserStatement.setString(5, location);
			addUserStatement.setInt(6, followersCount);
			addUserStatement.setInt(7, friendsCount);
			addUserStatement.setInt(8, statusesCount);
			addUserStatement.setDate(9, new java.sql.Date(createdAt.getTime()));
			addUserStatement.setBoolean(10, isProtected);
			addUserStatement.setDouble(11, englishRate);
			addUserStatement.executeUpdate();
			Log.getLogger().info("Successfully added user to DB!");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return new TwitterUser(userID, screenName, name, description, location, followersCount, friendsCount, statusesCount, createdAt, isProtected, englishRate);
	}
	
	public void updateUserEnglishRate(long userID, double englishRate) {
		try {
			updateUserEnglishRateStatement.clearParameters();
			updateUserEnglishRateStatement.setDouble(1, englishRate);
			updateUserEnglishRateStatement.setLong(2, userID);
			updateUserEnglishRateStatement.executeUpdate();
			Log.getLogger().info("Successfully updated user English rate in DB!");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		
	}

	/**
	 * Add a user follower to the database.
	 * 
	 * @param userID
	 * @param followerID
	 */
	public void addUserFollower(long userID, long followerID) {
		try {
			addUserFollowerStatement.clearParameters();
			addUserFollowerStatement.setLong(1, userID);
			addUserFollowerStatement.setLong(2, followerID);
			addUserFollowerStatement.executeUpdate();
		} catch (SQLException sqle) { sqle.printStackTrace(); }
	}

	/**
	 * Check whether a user is contained in the database or not.
	 * 
	 * @param userID
	 * @return true if contained, false otherwise
	 */
	public boolean isUserContained(long userID) {
		try {
			getUserByIDStatement.clearParameters();
			getUserByIDStatement.setLong(1, userID);
			ResultSet resultSet = getUserByIDStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) { sqle.printStackTrace(); }
		return false;
	}

	/**
	 * Check if a user's followers are already in the database.
	 * 
	 * @param userID
	 * @return true if contained, false otherwise
	 */
	public boolean isUserFollowersContained(long userID) {
		try {
			getUserFollowersStatement.clearParameters();
			getUserFollowersStatement.setLong(1, userID);
			ResultSet resultSet = getUserFollowersStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) { sqle.printStackTrace(); }
		return false;
	}

	/**
	 * Check if a tweet is already contained in the database.
	 * 
	 * @param tweetID
	 * @return true if contained, false otherwise
	 */
	public boolean isContained(long tweetID) {
		try {
			getSingleTweetStatement.clearParameters();
			getSingleTweetStatement.setLong(1, tweetID);
			ResultSet resultSet = getSingleTweetStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) { sqle.printStackTrace(); }
		return false;
	}

	/**
	 * Check containment of a user ontology in the database.
	 * 
	 * @param userID
	 * @return true if contained, false otherwise
	 */
	public boolean isOntologyContained(long userID) {
		try {
			getUserOntologyStatement.clearParameters();
			getUserOntologyStatement.setLong(1, userID);
			getUserOntologyStatement.setInt(2, Vars.CONCATENATION_WINDOW);
			getUserOntologyStatement.setDouble(3, Vars.SPOTLIGHT_CONFIDENCE);
			getUserOntologyStatement.setInt(4, Vars.SPOTLIGHT_SUPPORT);
			ResultSet resultSet = getUserOntologyStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) { sqle.printStackTrace(); }
		return false;
	}

	/**
	 * Add a user ontology to the database.
	 * 
	 * @param userID
	 * @param fullMap
	 */
	public void addUserOntology(long userID, UserOntology userOntology) {

		for (OntologyType type : userOntology.getOntology().keySet()) {
			try {
				addOntologyStatement.clearParameters();
				addOntologyStatement.setLong(1, userID);
				addOntologyStatement.setString(2, type.getFullUri());
				addOntologyStatement.setInt(3, userOntology.getOntology().get(type));
				addOntologyStatement.setInt(4, Vars.CONCATENATION_WINDOW);
				addOntologyStatement.setDouble(5, Vars.SPOTLIGHT_CONFIDENCE);
				addOntologyStatement.setInt(6, Vars.SPOTLIGHT_SUPPORT);
				addOntologyStatement.executeUpdate();
			} catch (SQLIntegrityConstraintViolationException sqlicve) {
				Log.getLogger().error("Row already exists in ONTOLOGY database! Delete ontology of user " + userID + " first. Aborting entire procedure.");
				break;
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
		Log.getLogger().info("Added user ontology to DB!");
	}

	/**
	 * Get all tweets of a user from the database.
	 * 
	 * @param userID
	 * @return a List of Tweets
	 */
	public List<Tweet> getTweets(long userID) {

		List<Tweet> tweets = new ArrayList<Tweet>();

		try {
			getTweetsStatement.clearParameters();
			getTweetsStatement.setLong(1, userID);
			ResultSet resultSet = getTweetsStatement.executeQuery();

			while(resultSet.next()) {
				long tweetID = resultSet.getLong(1);
				Tweet tweet = new Tweet(tweetID, resultSet.getLong(2), resultSet.getString(3), resultSet.getDate(4), 
						resultSet.getString(5), resultSet.getBoolean(6), getLocation(tweetID), resultSet.getString(7), 
						getUserMentions(tweetID), getHashtags(tweetID), getURLs(tweetID), getMedia(tweetID));
				tweets.add(tweet);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return tweets;
	}

	/**
	 * Fetch a user from the database based on user name.
	 * 
	 * @param userName
	 * @return TwitterUser if exists, otherwise null
	 */
	public TwitterUser getUser(String userName) {

		TwitterUser user = null;

		try {
			getUserByNameStatement.clearParameters();
			getUserByNameStatement.setString(1, userName);
			ResultSet resultSet = getUserByNameStatement.executeQuery();

			if(resultSet.next()) {
				user = new TwitterUser(resultSet.getLong(1), userName, resultSet.getString(3), resultSet.getString(4), resultSet.getString(5), 
						resultSet.getInt(6), resultSet.getInt(7), resultSet.getInt(8), resultSet.getDate(9), resultSet.getBoolean(10), resultSet.getDouble(11));

				//user.setTweets(getTweets(resultSet.getLong(1)));
				//user.setUserOntology(getUserOntology(resultSet.getLong(1)));
			}
		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return user;
	}

	/**
	 * Fetch a user and their ontology (if exists) from the database based on user ID.
	 * 
	 * @param userID
	 * @return TwitterUser if exists, otherwise null
	 */
	public TwitterUser getUser(long userID) {

		TwitterUser user = null;

		try {
			getUserByIDStatement.clearParameters();
			getUserByIDStatement.setLong(1, userID);
			ResultSet resultSet = getUserByIDStatement.executeQuery();

			if(resultSet.next())
				user = new TwitterUser(userID, resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getString(5), 
						resultSet.getInt(6), resultSet.getInt(7), resultSet.getInt(8), resultSet.getDate(9), resultSet.getBoolean(10), resultSet.getDouble(11));

		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return user;
	}

	/**
	 * Get a user ontology from the database.
	 * 
	 * @param userID
	 * @return a map with OntologyTypes with occurrence numbers
	 */
	public UserOntology getUserOntology(long userID) {

		UserOntology userOntology = new UserOntology();

		try {
			getUserOntologyStatement.clearParameters();
			getUserOntologyStatement.setLong(1, userID);
			getUserOntologyStatement.setInt(2, Vars.CONCATENATION_WINDOW);
			getUserOntologyStatement.setDouble(3, Vars.SPOTLIGHT_CONFIDENCE);
			getUserOntologyStatement.setInt(4, Vars.SPOTLIGHT_SUPPORT);
			ResultSet resultSet = getUserOntologyStatement.executeQuery();

			while(resultSet.next()) {
				String uri = resultSet.getString(2);
				int cardinality = resultSet.getInt(3);
				OntologyType ontologyType = Util.determineOntologyType(uri);
				userOntology.addClass(ontologyType, cardinality);
			}
		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return userOntology;
	}

	/**
	 * Get a tweet's associated hashtags from the database.
	 * 
	 * @param tweetID
	 * @return A list of hashtags in String format (empty if none are found)
	 */
	public List<String> getHashtags(long tweetID) {
		List<String> hashtags = new ArrayList<String>();
		try {
			getHashtagsStatement.clearParameters();
			getHashtagsStatement.setLong(1, tweetID);
			ResultSet resultSet = getHashtagsStatement.executeQuery();
			while(resultSet.next()) { hashtags.add(resultSet.getString(2)); }
		} catch (SQLException sqle) { sqle.printStackTrace(); }
		return hashtags;
	}

	/**
	 * Get a tweet's associated URLs from the database.
	 * 
	 * @param tweetID
	 * @return A list of URLs in String format (empty if none are found)
	 */
	public List<String> getURLs(long tweetID) {
		List<String> URLs = new ArrayList<String>();
		try {
			getURLStatement.clearParameters();
			getURLStatement.setLong(1, tweetID);
			ResultSet resultSet = getURLStatement.executeQuery();
			while(resultSet.next()) { URLs.add(resultSet.getString(2)); }
		} catch (SQLException sqle) { sqle.printStackTrace(); }
		return URLs;
	}

	/**
	 * Get a tweet's associated user mentions from the database.
	 * 
	 * @param tweetID
	 * @return A list of user mentions in String format (empty if none are found)
	 */
	public ArrayList<String> getUserMentions(long tweetID) {
		ArrayList<String> userMentions = new ArrayList<String>();
		try {
			getUserMentionsStatement.clearParameters();
			getUserMentionsStatement.setLong(1, tweetID);
			ResultSet resultSet = getUserMentionsStatement.executeQuery();
			while(resultSet.next()) { userMentions.add(resultSet.getString(2)); }
		} catch (SQLException sqle) { sqle.printStackTrace(); }
		return userMentions;
	}

	/**
	 * Get a tweet's associated media from the database.
	 * 
	 * @param tweetID
	 * @return A list of media in String format (empty if none are found)
	 */
	public List<String> getMedia(long tweetID) {
		List<String> media = new ArrayList<String>();
		try {
			getMediaStatement.clearParameters();
			getMediaStatement.setLong(1, tweetID);
			ResultSet resultSet = getMediaStatement.executeQuery();
			while(resultSet.next()) { media.add(resultSet.getString(2)); }
		} catch (SQLException sqle) { sqle.printStackTrace(); }
		return media;
	}

	/**
	 * Get a tweet's associated location from the database.
	 * 
	 * @param tweetID
	 * @return A location in String format (empty String if none is found)
	 */
	public String getLocation(long tweetID) {
		String location = "";
		try {
			getLocationStatement.clearParameters();
			getLocationStatement.setLong(1, tweetID);
			ResultSet resultSet = getLocationStatement.executeQuery();
			while(resultSet.next()) { location = resultSet.getString(2); }
		} catch (SQLException sqle) { sqle.printStackTrace(); }
		return location;
	}

	/**
	 * Get a user's followers from the database.
	 * 
	 * @param userID
	 * @return a List of follower user IDs
	 */
	public List<Long> getUserFollowers(long userID) {

		List<Long> followers = new ArrayList<Long>();

		try {
			getUserFollowersStatement.clearParameters();
			getUserFollowersStatement.setLong(1, userID);
			ResultSet resultSet = getUserFollowersStatement.executeQuery();

			while(resultSet.next()) {
				followers.add(resultSet.getLong(2));
			}
		} catch (SQLException sqle) { sqle.printStackTrace(); }

		return followers;
	}

	/**
	 * Get the singleton instance of the database object.
	 * @return TweetBase the database
	 */
	public static TweetBase getInstance(){
		if(tweetBase == null){ tweetBase = new TweetBase(); }
		return tweetBase;
	}
}
