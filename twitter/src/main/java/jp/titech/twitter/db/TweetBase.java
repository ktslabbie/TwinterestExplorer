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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Place;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

public class TweetBase {

	private static TweetBase tweetBase;
	private Connection dbConnection;

	private PreparedStatement preparedAddTweetStatement, preparedAddHashtagStatement, preparedAddLocationStatement,
	preparedGetSingleTweetStatement, preparedGetTweetsStatement, preparedGetHashtagsStatement, preparedGetLocationStatement;

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
			preparedGetTweetsStatement = dbConnection.prepareStatement(Util.readFile(Vars.SQL_SCRIPT_DIR + "select_user_tweets.sql"));
			preparedGetHashtagsStatement = dbConnection.prepareStatement("SELECT * FROM TweetBase.Hashtags WHERE tweet_id = ?");
			preparedGetLocationStatement = dbConnection.prepareStatement("SELECT * FROM TweetBase.Locations WHERE tweet_id = ?");

			preparedGetSingleTweetStatement = dbConnection.prepareStatement(Util.readFile(Vars.SQL_SCRIPT_DIR + "select_single_tweet.sql"));

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

	public void addTweet(long tweetID, long userID, String screenName, Date createdAt, String content, boolean isRetweet, Place place, GeoLocation geoLocation, HashtagEntity[] hashtagEntities) {

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
				Log.getLogger().info("Successfully added location to DB!");
			}

			/*if(geoLocation != null) {
				preparedAddTweetStatement.setDouble(7, geoLocation.getLatitude());
				preparedAddTweetStatement.setDouble(8, geoLocation.getLongitude());
			}*/

			if(hashtagEntities != null) {

				for (int i = 0; i < hashtagEntities.length; i++) {
					HashtagEntity hashtagEntity = hashtagEntities[i];
					preparedAddHashtagStatement.clearParameters();
					preparedAddHashtagStatement.setLong(1, tweetID);
					preparedAddHashtagStatement.setString(2, hashtagEntity.getText());
					preparedAddHashtagStatement.executeUpdate();
					Log.getLogger().info("Successfully added hashtag entity to DB!");
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

	public ArrayList<Tweet> getTweets(long userID) {

		ArrayList<Tweet> tweets = new ArrayList<Tweet>();

		try {
			preparedGetTweetsStatement.clearParameters();
			preparedGetTweetsStatement.setLong(1, userID);
			
			ResultSet resultSet = preparedGetTweetsStatement.executeQuery();

			while(resultSet.next()) {
				long tweetID = resultSet.getLong(1);
				Tweet tweet = new Tweet(tweetID, resultSet.getLong(2), resultSet.getString(3), resultSet.getDate(4), resultSet.getString(5), 
										resultSet.getBoolean(6), getHashtags(tweetID), getLocation(tweetID));
				tweets.add(tweet);
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

		return tweets;
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
