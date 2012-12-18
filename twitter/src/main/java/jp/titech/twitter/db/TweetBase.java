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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dbpedia.spotlight.model.OntologyType;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.ner.spotlight.SpotlightUtil;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

public class TweetBase {

	private static TweetBase tweetBase;
	private Connection dbConnection;

	private PreparedStatement preparedAddTweetStatement, preparedAddHashtagStatement, preparedAddURLStatement, preparedAddUserMentionStatement, 
	preparedAddMediaStatement, preparedAddLocationStatement, preparedAddOntologyStatement;
	private PreparedStatement preparedGetSingleTweetStatement, preparedGetTweetsStatement, preparedGetHashtagsStatement, preparedGetLocationStatement, preparedGetURLStatement,
	preparedGetUsermentionStatement, preparedGetMediaStatement, preparedGetUserOntologyStatement;

	private TweetBase(){
		initDB();
		prepareStatements();
	}

	private void prepareStatements() {
		try {
			preparedAddTweetStatement = dbConnection.prepareStatement(
					"INSERT INTO TweetBase.Tweets " +
							"(tweet_id, user_id, screen_name, created_at, content, isretweet)" +
							" VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

			preparedAddHashtagStatement = dbConnection.prepareStatement(
					"INSERT INTO TweetBase.Hashtags (tweet_id, hashtag) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);

			preparedAddLocationStatement = dbConnection.prepareStatement(
					"INSERT INTO TweetBase.Locations (tweet_id, full_name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedAddURLStatement = dbConnection.prepareStatement(
					"INSERT INTO TweetBase.URLs (tweet_id, url) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedAddUserMentionStatement = dbConnection.prepareStatement(
					"INSERT INTO TweetBase.Usermentions (tweet_id, user_mention) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedAddMediaStatement = dbConnection.prepareStatement(
					"INSERT INTO TweetBase.Media (tweet_id, media_url) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedAddOntologyStatement = dbConnection.prepareStatement(
					"INSERT INTO TweetBase.Ontology (user_id, ontology_type, cardinality, confidence, support) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			preparedGetTweetsStatement = dbConnection.prepareStatement(Util.readFile(Vars.SQL_SCRIPT_DIRECTORY + "select_user_tweets.sql"));
			preparedGetUsermentionStatement = dbConnection.prepareStatement("SELECT * FROM TweetBase.Usermentions WHERE tweet_id = ?");
			preparedGetHashtagsStatement = dbConnection.prepareStatement("SELECT * FROM TweetBase.Hashtags WHERE tweet_id = ?");
			preparedGetURLStatement = dbConnection.prepareStatement("SELECT * FROM TweetBase.URLs WHERE tweet_id = ?");
			preparedGetMediaStatement = dbConnection.prepareStatement("SELECT * FROM TweetBase.Media WHERE tweet_id = ?");
			preparedGetLocationStatement = dbConnection.prepareStatement("SELECT * FROM TweetBase.Locations WHERE tweet_id = ?");
			preparedGetUserOntologyStatement = dbConnection.prepareStatement(Util.readFile(Vars.SQL_SCRIPT_DIRECTORY + "select_user_ontology.sql"));

			preparedGetSingleTweetStatement = dbConnection.prepareStatement(Util.readFile(Vars.SQL_SCRIPT_DIRECTORY + "select_single_tweet.sql"));

			Log.getLogger().info("SQL statements prepared.");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	private void initDB(){
		try {
			String strUrl = "jdbc:derby:TweetBase";
			dbConnection = DriverManager.getConnection(strUrl);
			Log.getLogger().info("Connected to Database: " + dbConnection.toString());
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	public void addTweet(long tweetID, long userID, String screenName, Date createdAt, String content, boolean isRetweet, Place place, GeoLocation geoLocation, 
			UserMentionEntity[] userMentionEntities, HashtagEntity[] hashtagEntities, URLEntity[] urlEntities, MediaEntity[] mediaEntities) {

		if(this.isContained(tweetID)) {
			Log.getLogger().warn("Tweet from user " + screenName + " with ID " + tweetID + " already contained in DB! Skipping.");
			return;
		}
		
		try {
			preparedAddTweetStatement.clearParameters();
			preparedAddTweetStatement.setLong(1, tweetID);
			preparedAddTweetStatement.setLong(2, userID);
			preparedAddTweetStatement.setString(3, screenName);
			preparedAddTweetStatement.setDate(4, new java.sql.Date(createdAt.getTime()));
			preparedAddTweetStatement.setString(5, content);
			preparedAddTweetStatement.setBoolean(6, isRetweet);
			preparedAddTweetStatement.executeUpdate();
			Log.getLogger().info("Successfully added tweet to DB!");

			if(place != null) {
				preparedAddLocationStatement.clearParameters();
				preparedAddLocationStatement.setLong(1, tweetID);
				preparedAddLocationStatement.setString(2, place.getFullName());
				preparedAddLocationStatement.executeUpdate();
				Log.getLogger().info("Successfully added location " + place.getFullName() + " to DB!");
			}

			/*if(geoLocation != null) {
				preparedAddTweetStatement.setDouble(7, geoLocation.getLatitude());
				preparedAddTweetStatement.setDouble(8, geoLocation.getLongitude());
			}*/

			if(userMentionEntities != null) {

				for (int i = 0; i < userMentionEntities.length; i++) {
					twitter4j.
					UserMentionEntity userMentionEntity = userMentionEntities[i];
					preparedAddUserMentionStatement.clearParameters();
					preparedAddUserMentionStatement.setLong(1, tweetID);
					preparedAddUserMentionStatement.setString(2, userMentionEntity.getScreenName());
					preparedAddUserMentionStatement.executeUpdate();
					Log.getLogger().info("Successfully added user mention " + userMentionEntity.getScreenName() + " to DB!");
				}
			}
			
			if(hashtagEntities != null) {

				for (int i = 0; i < hashtagEntities.length; i++) {
					HashtagEntity hashtagEntity = hashtagEntities[i];
					preparedAddHashtagStatement.clearParameters();
					preparedAddHashtagStatement.setLong(1, tweetID);
					preparedAddHashtagStatement.setString(2, hashtagEntity.getText());
					preparedAddHashtagStatement.executeUpdate();
					Log.getLogger().info("Successfully added hashtag " + hashtagEntity.getText() + " to DB!");
				}	
			}
			
			if(urlEntities != null) {

				for (int i = 0; i < urlEntities.length; i++) {
					URLEntity urlEntity = urlEntities[i];
					preparedAddURLStatement.clearParameters();
					preparedAddURLStatement.setLong(1, tweetID);
					preparedAddURLStatement.setString(2, urlEntity.getURL());
					preparedAddURLStatement.executeUpdate();
					Log.getLogger().info("Successfully added URL " + urlEntity.getURL() + " to DB!");
				}	
			}
			
			if(mediaEntities != null) {

				for (int i = 0; i < mediaEntities.length; i++) {
					MediaEntity mediaEntity = mediaEntities[i];
					preparedAddMediaStatement.clearParameters();
					preparedAddMediaStatement.setLong(1, tweetID);
					preparedAddMediaStatement.setString(2, mediaEntity.getURL());
					preparedAddMediaStatement.executeUpdate();
					Log.getLogger().info("Successfully added media " + mediaEntity.getURL() + " to DB!");
				}	
			}
			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	public boolean isContained(long tweetID) {
		try {
			preparedGetSingleTweetStatement.clearParameters();
			preparedGetSingleTweetStatement.setLong(1, tweetID);
			ResultSet resultSet = preparedGetSingleTweetStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Check containment on Ontology DB.
	 * 
	 * @param userID
	 * @param fullUri
	 * @param confidence
	 * @param support
	 * @return
	 */
	public boolean isContained(int userID, double confidence, int support) {
		try {
			preparedGetUserOntologyStatement.clearParameters();
			preparedGetUserOntologyStatement.setLong(1, userID);
			preparedGetUserOntologyStatement.setDouble(2, Vars.SPOTLIGHT_CONFIDENCE);
			preparedGetUserOntologyStatement.setInt(3, Vars.SPOTLIGHT_SUPPORT);
			ResultSet resultSet = preparedGetUserOntologyStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return false;
	}
	
	/**
	 * @param userID
	 * @param fullMap
	 */
	public void addOntology(int userID, Map<OntologyType, Integer> fullMap) {
		
		for (OntologyType type : fullMap.keySet()) {
			
			/*if(this.isContained(userID, type.getFullUri(), Vars.SPOTLIGHT_CONFIDENCE, Vars.SPOTLIGHT_SUPPORT)) {
				Log.getLogger().warn("Tweet from user " + screenName + " with ID " + tweetID + " already contained in DB! Skipping.");
				return;
			}*/
			
			try {
				preparedAddOntologyStatement.clearParameters();
				preparedAddOntologyStatement.setLong(1, userID);
				preparedAddOntologyStatement.setString(2, type.getFullUri());
				preparedAddOntologyStatement.setInt(3, fullMap.get(type));
				preparedAddOntologyStatement.setDouble(4, Vars.SPOTLIGHT_CONFIDENCE);
				preparedAddOntologyStatement.setInt(5, Vars.SPOTLIGHT_SUPPORT);
				preparedAddOntologyStatement.executeUpdate();
			} catch (SQLIntegrityConstraintViolationException sqlicve) {
				Log.getLogger().info("Row already exists in ONTOLOGY database! Delete ontology of user " + userID + " first. Aborting entire procedure.");
				break;
			} catch (SQLException sqle) {
				sqle.printStackTrace();
			}
		}
		Log.getLogger().info("Finished adding ontology to DB!");
	}

	public ArrayList<Tweet> getTweets(long userID) {

		ArrayList<Tweet> tweets = new ArrayList<Tweet>();

		try {
			preparedGetTweetsStatement.clearParameters();
			preparedGetTweetsStatement.setLong(1, userID);
			
			ResultSet resultSet = preparedGetTweetsStatement.executeQuery();

			while(resultSet.next()) {
				long tweetID = resultSet.getLong(1);
				Tweet tweet = new Tweet(tweetID, resultSet.getLong(2), resultSet.getString(3), resultSet.getDate(4), resultSet.getString(5), 
										resultSet.getBoolean(6), getUserMentions(tweetID), getHashtags(tweetID), getURLs(tweetID), getMedia(tweetID), getLocation(tweetID));
				tweets.add(tweet);
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return tweets;
	}
	
	public Map<OntologyType, Integer> getOntology(long userID) {

		Map<OntologyType, Integer> ontology = new HashMap<OntologyType, Integer>();

		try {
			preparedGetUserOntologyStatement.clearParameters();
			preparedGetUserOntologyStatement.setLong(1, userID);
			preparedGetUserOntologyStatement.setDouble(2, Vars.SPOTLIGHT_CONFIDENCE);
			preparedGetUserOntologyStatement.setInt(3, Vars.SPOTLIGHT_SUPPORT);
			ResultSet resultSet = preparedGetUserOntologyStatement.executeQuery();

			while(resultSet.next()) {
				String uri = resultSet.getString(2);
				int cardinality = resultSet.getInt(3);
				OntologyType ontologyType = Util.determineOntologyType(uri);
				ontology.put(ontologyType, cardinality);
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return ontology;
	}

	public ArrayList<String> getHashtags(long tweetID) {

		ArrayList<String> hashtags = new ArrayList<String>();

		try {
			preparedGetHashtagsStatement.clearParameters();
			preparedGetHashtagsStatement.setLong(1, tweetID);
			ResultSet resultSet = preparedGetHashtagsStatement.executeQuery();

			while(resultSet.next()) {
				hashtags.add(resultSet.getString(2));
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return hashtags;
	}
	
	public ArrayList<String> getURLs(long tweetID) {

		ArrayList<String> URLs = new ArrayList<String>();

		try {
			preparedGetURLStatement.clearParameters();
			preparedGetURLStatement.setLong(1, tweetID);
			ResultSet resultSet = preparedGetURLStatement.executeQuery();

			while(resultSet.next()) {
				URLs.add(resultSet.getString(2));
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return URLs;
	}
	
	public ArrayList<String> getUserMentions(long tweetID) {

		ArrayList<String> userMentions = new ArrayList<String>();

		try {
			preparedGetUsermentionStatement.clearParameters();
			preparedGetUsermentionStatement.setLong(1, tweetID);
			ResultSet resultSet = preparedGetUsermentionStatement.executeQuery();

			while(resultSet.next()) {
				userMentions.add(resultSet.getString(2));
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return userMentions;
	}
	
	public ArrayList<String> getMedia(long tweetID) {

		ArrayList<String> media = new ArrayList<String>();

		try {
			preparedGetMediaStatement.clearParameters();
			preparedGetMediaStatement.setLong(1, tweetID);
			ResultSet resultSet = preparedGetMediaStatement.executeQuery();

			while(resultSet.next()) {
				media.add(resultSet.getString(2));
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return media;
	}


	public String getLocation(long tweetID) {

		String location = "";

		try {
			preparedGetLocationStatement.clearParameters();
			preparedGetLocationStatement.setLong(1, tweetID);
			ResultSet resultSet = preparedGetLocationStatement.executeQuery();

			while(resultSet.next()) {
				location = resultSet.getString(2);
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return location;
	}

	public static TweetBase getInstance(){
		if(tweetBase == null){
			tweetBase = new TweetBase();
		}
		return tweetBase;
	}
}
