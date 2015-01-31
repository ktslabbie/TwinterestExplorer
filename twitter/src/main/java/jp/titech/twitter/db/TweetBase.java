/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.util.PGobject;
import org.postgresql.util.PSQLException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.data.TwitterUser.Properties;
import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

/**
 * Singleton class handling SQL interactions with the (PostgreSQL) database.
 * 
 * @author Kristian
 *
 */
public class TweetBase {

	private static TweetBase 	tweetBase;	// TweetBase is a singleton class.
	private PGPoolingDataSource pool;		// Postgres connection pool.
	private ObjectMapper 		mapper;		// JSON (de)serialization.

	private String				sqlSelectDir = Vars.SQL_SCRIPT_DIRECTORY + "select/";
	private String				sqlInsertDir = Vars.SQL_SCRIPT_DIRECTORY + "insert/";
	private String				sqlUpdateDir = Vars.SQL_SCRIPT_DIRECTORY + "update/";

	private String 			addTweetSQL, addHashtagSQL, addUserMentionSQL, 
						addOntologySQL, addUserSQL, updateUserEnglishRateSQL, addUserFollowerSQL;

	private String 	 	getSingleTweetSQL,
						getUserOntologySQL, getUserByIDSQL, getSingleUserOntologySQL,
							getUserByNameSQL, getUserFollowersSQL, updateUserTweetsSQL;

	/**
	 * Default constructor.
	 */
	private TweetBase() {
		mapper = new ObjectMapper();
		initDB();
		prepareSQLStrings();
	}

	/**
	 * Initialize the database.
	 */
	private void initDB(){
		try {
			pool = new PGPoolingDataSource();
			pool.setDataSourceName("TweetBase Data Source");
			pool.setServerName("localhost");
			pool.setDatabaseName("TweetBase");
			pool.setUser("postgres");
			pool.setPassword("9564");
			pool.setMaxConnections(10);
			
			Log.getLogger().info("Initialized connection pool: " + pool.toString());
		} catch (Exception sqle) {
			sqle.printStackTrace();
		}
	}

	/**
	 * Prepare SQL statements in memory for quick access.
	 */
	private void prepareSQLStrings() {
		addTweetSQL 			= Util.readFile(this.sqlInsertDir + "add_tweet.sql");
		addHashtagSQL 		= Util.readFile(this.sqlInsertDir + "add_hashtag.sql");
		//addLocationSQL 		= Util.readFile(this.sqlInsertDir + "add_location.sql");
		//addURLSQL 			= Util.readFile(this.sqlInsertDir + "add_url.sql");
		addUserMentionSQL 	= Util.readFile(this.sqlInsertDir + "add_user_mention.sql");
		//addMediaSQL 			= Util.readFile(this.sqlInsertDir + "add_media.sql");
		addOntologySQL 		= Util.readFile(this.sqlInsertDir + "add_ontology.sql");
		addUserSQL 			= Util.readFile(this.sqlInsertDir + "add_user.sql");
		addUserFollowerSQL	= Util.readFile(this.sqlInsertDir + "add_user_follower.sql");

		//getTweetsSQL 			= Util.readFile(this.sqlSelectDir + "select_user_tweets.sql");
		getSingleTweetSQL 	= Util.readFile(this.sqlSelectDir + "select_single_tweet.sql");
		//getUserMentionsSQL 	= Util.readFile(this.sqlSelectDir + "select_user_mentions.sql");
		//getHashtagsSQL 		= Util.readFile(this.sqlSelectDir + "select_hashtags.sql");
		//getURLSQL 			= Util.readFile(this.sqlSelectDir + "select_urls.sql");
		//getMediaSQL 			= Util.readFile(this.sqlSelectDir + "select_media.sql");
		//getLocationSQL 		= Util.readFile(this.sqlSelectDir + "select_location.sql");
		getUserOntologySQL 		= Util.readFile(this.sqlSelectDir + "select_user_ontology.sql");
		getSingleUserOntologySQL = Util.readFile(this.sqlSelectDir + "select_single_ontology.sql");
		getUserByIDSQL 		= Util.readFile(this.sqlSelectDir + "select_user_by_id.sql");
		getUserByNameSQL 		= Util.readFile(this.sqlSelectDir + "select_user_by_screen_name.sql");
		//getAllUsersSQL 		= Util.readFile(this.sqlSelectDir + "select_all_users.sql");
		getUserFollowersSQL	= Util.readFile(this.sqlSelectDir + "select_user_followers.sql");
		
		//updateSingleTweetSQL 		= Util.readFile(this.sqlUpdateDir + "update_single_tweet.sql");
		updateUserTweetsSQL 		= Util.readFile(this.sqlUpdateDir + "update_user_tweets.sql");
		updateUserEnglishRateSQL	= Util.readFile(this.sqlUpdateDir + "update_user_english_rate.sql");

		Log.getLogger().info("SQL strings prepared.");
	}
	
	
	/**
	 * Add a tweet with the given parameters to the database (if not yet contained).
	 * 
	 * @param tweet the tweet to add
	 */
	public void addTweet(Tweet tweet) {
		
		Connection conn = null;
		PreparedStatement addTweetStatement = null, addUserMentionStatement = null, addHashtagStatement = null;
		
		long tweetID = tweet.getTweetID();

		if(this.isContained(tweetID)) {
			//Log.getLogger().warn("Tweet with ID " + tweetID + " already contained in DB! Skipping.");
			return;
		}
		
		try {
		    conn = pool.getConnection();
		    addTweetStatement = conn.prepareStatement(addTweetSQL);
		    
			addTweetStatement.setLong(1, tweetID);
			addTweetStatement.setLong(2, tweet.getUserID());
			//addTweetStatement.setString(3, tweet.getScreenName().toLowerCase());
			addTweetStatement.setLong(3, tweet.getCreatedAt());
			addTweetStatement.setString(4, tweet.getContent());
			addTweetStatement.setBoolean(5, tweet.isRetweet());
			addTweetStatement.setString(6, tweet.getLanguage());
			addTweetStatement.executeUpdate();

			/*if(tweet.getLocationName() != null) {
				addLocationStatement.clearParameters();
				addLocationStatement.setLong(1, tweetID);
				addLocationStatement.setString(2, tweet.getLocationName());
				addLocationStatement.executeUpdate();
				//Log.getLogger().info("Successfully added location " + tweet.getLocationName() + " to DB!");
			}*/

			addUserMentionStatement = conn.prepareStatement(addUserMentionSQL);
			
			for (String entity : tweet.getUserMentions()) {
				addUserMentionStatement.clearParameters();
				addUserMentionStatement.setLong(1, tweetID);
				addUserMentionStatement.setString(2, entity);
				addUserMentionStatement.executeUpdate();
				//Log.getLogger().info("Successfully added user mention " + entity + " to DB!");
			}

			addHashtagStatement = conn.prepareStatement(addHashtagSQL);
			
			for (String entity : tweet.getHashtags()) {
				addHashtagStatement.clearParameters();
				addHashtagStatement.setLong(1, tweetID);
				addHashtagStatement.setString(2, entity);
				addHashtagStatement.executeUpdate();
				//Log.getLogger().info("Successfully added hashtag " + entity + " to DB!");
			}	

			/*for (String entity : tweet.getURLs()) {
				addURLStatement.clearParameters();
				addURLStatement.setLong(1, tweetID);
				addURLStatement.setString(2, entity);
				addURLStatement.executeUpdate();
				//Log.getLogger().info("Successfully added URL " + entity + " to DB!");
			}*/

			/*for (String entity : tweet.getMedia()) {
				addMediaStatement.clearParameters();
				addMediaStatement.setLong(1, tweetID);
				addMediaStatement.setString(2, entity);
				addMediaStatement.executeUpdate();
				//Log.getLogger().info("Successfully added media " + entity + " to DB!");
			}*/
			
			//Log.getLogger().info("Successfully added tweet to DB!");
		    
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			if (addTweetStatement != null) {
		        try { addTweetStatement.close(); } catch (SQLException e) {}
		    }
			if (addUserMentionStatement != null) {
		        try { addUserMentionStatement.close(); } catch (SQLException e) {}
		    }
			if (addHashtagStatement != null) {
		        try { addHashtagStatement.close(); } catch (SQLException e) {}
		    }
		    if (conn != null) {
		        try { conn.close(); } catch (SQLException e) {}
		    }
		}
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
	public TwitterUser addUser(TwitterUser user) {

		if(this.isUserContained(user.getUserID())) {
			//Log.getLogger().warn("User " + screenName + " with ID " + userID + " already contained in DB! Skipping.");
			return user;
		}
		
		Connection conn = null;
		PreparedStatement addUserStatement = null;

		try {
			Properties props = user.getProperties();
			String jsonString = mapper.writeValueAsString(props);
			
			conn = pool.getConnection();
		    addUserStatement = conn.prepareStatement(addUserSQL);
			addUserStatement.setLong(1, user.getUserID());
			addUserStatement.setString(2, user.getScreenName().toLowerCase());
			
			PGobject jsonObject = new PGobject();
			jsonObject.setType("json");
			jsonObject.setValue(jsonString);
			addUserStatement.setObject(3, jsonObject);
			addUserStatement.setDouble(4, user.getEnglishRate());

			addUserStatement.executeUpdate();

			Log.getLogger().info("Successfully added user to DB!");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			if (addUserStatement != null) {
		        try { addUserStatement.close(); } catch (SQLException e) {}
		    }
		    if (conn != null) {
		        try { conn.close(); } catch (SQLException e) {}
		    }
		}
		
		return user;
	}
	
	public void updateUserEnglishRate(long userID, double englishRate) {
		
		Connection conn = null;
		PreparedStatement updateUserEnglishRateStatement = null;
		
		try {
			conn = pool.getConnection();
		    updateUserEnglishRateStatement = conn.prepareStatement(updateUserEnglishRateSQL);
			updateUserEnglishRateStatement.setDouble(1, englishRate);
			updateUserEnglishRateStatement.setLong(2, userID);
			updateUserEnglishRateStatement.executeUpdate();
			Log.getLogger().info("Successfully updated user English rate in DB!");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			if (updateUserEnglishRateStatement != null) {
		        try { updateUserEnglishRateStatement.close(); } catch (SQLException e) {}
		    }
		    if (conn != null) {
		        try { conn.close(); } catch (SQLException e) {}
		    }
		}
		
	}

	/**
	 * Add a user follower to the database.
	 * 
	 * @param userID
	 * @param followerID
	 */
	public void addUserFollower(long userID, long followerID) {
		
		Connection conn = null;
		PreparedStatement addUserFollowerStatement = null;
		
		try {
			conn = pool.getConnection();
		    addUserFollowerStatement = conn.prepareStatement(addUserFollowerSQL);
			addUserFollowerStatement.setLong(1, userID);
			addUserFollowerStatement.setLong(2, followerID);
			addUserFollowerStatement.executeUpdate();
		} catch (PSQLException psql) { 
			//Log.getLogger().info("Follower with ID " + followerID + " already contained. Skipping!");
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (addUserFollowerStatement != null) {
		        try { addUserFollowerStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}

	/**
	 * Check whether a user is contained in the database or not.
	 * 
	 * @param userID
	 * @return true if contained, false otherwise
	 */
	public boolean isUserContained(long userID) {
		
		Connection conn = null;
		PreparedStatement getUserByIDStatement = null;
		
		try {
			conn = pool.getConnection();
		    getUserByIDStatement = conn.prepareStatement(getUserByIDSQL);
			getUserByIDStatement.setLong(1, userID);
			ResultSet resultSet = getUserByIDStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getUserByIDStatement != null) {
		        try { getUserByIDStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return false;
	}

	/**
	 * Check if a user's followers are already in the database.
	 * 
	 * @param userID
	 * @return true if contained, false otherwise
	 */
	public boolean isUserFollowersContained(long userID) {
		
		Connection conn = null;
		PreparedStatement getUserFollowersStatement = null;
		
		try {
			conn = pool.getConnection();
		    getUserFollowersStatement = conn.prepareStatement(getUserFollowersSQL);
			getUserFollowersStatement.setLong(1, userID);
			ResultSet resultSet = getUserFollowersStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getUserFollowersStatement != null) {
		        try { getUserFollowersStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return false;
	}
	
	/**
	 * Check if a user's followers are already in the database.
	 * 
	 * @param userID
	 * @return true if contained, false otherwise
	 */
	public boolean enoughUserFollowersContained(long userID, int size) {
		
		Connection conn = null;
		Statement stmt3 = null;
		
		try {
			conn = pool.getConnection();
			stmt3 = conn.createStatement();
			ResultSet rs3 = stmt3.executeQuery("SELECT COUNT(*) AS count FROM TweetBase.Followers WHERE user_id = " + userID + ";");
			
			int count = rs3.getInt("count");
			if(count >= size) return true;
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (stmt3 != null) {
				try { stmt3.close(); } catch (SQLException e) {}
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return false;
	}

	/**
	 * Check if a tweet is already contained in the database.
	 * 
	 * @param tweetID
	 * @return true if contained, false otherwise
	 */
	public boolean isContained(long tweetID) {
		
		Connection conn = null;
		PreparedStatement getSingleTweetStatement = null;
		
		try {
			conn = pool.getConnection();
		    getSingleTweetStatement = conn.prepareStatement(getSingleTweetSQL);
			getSingleTweetStatement.setLong(1, tweetID);
			ResultSet resultSet = getSingleTweetStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			if (getSingleTweetStatement != null) {
				try { getSingleTweetStatement.close(); } catch (SQLException e) {}
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return false;
	}

	/**
	 * Check containment of a user ontology in the database.
	 * 
	 * @param userID
	 * @return true if contained, false otherwise
	 */
	public boolean isOntologyContained(String ontologyKey) {
		
		Connection conn = null;
		PreparedStatement getSingleUserOntologyStatement = null;
		
		try {
			conn = pool.getConnection();
		    getSingleUserOntologyStatement = conn.prepareStatement(getSingleUserOntologySQL);
			getSingleUserOntologyStatement.setString(1, ontologyKey);
			ResultSet resultSet = getSingleUserOntologyStatement.executeQuery();
			if(resultSet.next()) return true;
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getSingleUserOntologyStatement != null) {
		        try { getSingleUserOntologyStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return false;
	}

/*	*//**
	 * Add a user ontology to the database.
	 * 
	 * @param userID
	 * @param fullMap
	 *//*
	public void addUserOntology(long userID, UserOntology userOntology) {
		
		Connection conn = null;
		PreparedStatement addOntologyStatement = null;

	    try {
	    	conn = pool.getConnection();
		    addOntologyStatement = conn.prepareStatement(addOntologySQL);
		    
		    Log.getLogger().info("Adding user ontology to DB...");
	    	
			for (OntologyType type : userOntology.getOntology().keySet()) {
				addOntologyStatement.clearParameters();
				addOntologyStatement.setLong(1, userID);
				addOntologyStatement.setString(2, type.getType());
				addOntologyStatement.setInt(3, userOntology.getOntology().get(type));
				addOntologyStatement.setInt(4, Vars.CONCATENATION_WINDOW);
				addOntologyStatement.setDouble(5, Vars.SPOTLIGHT_CONFIDENCE);
				addOntologyStatement.setInt(6, Vars.SPOTLIGHT_SUPPORT);
				addOntologyStatement.executeUpdate();
			}
			
			Log.getLogger().info("Added user ontology to DB!");
		
	    } catch (SQLIntegrityConstraintViolationException sqlicve) {
			Log.getLogger().error("Row already exists in ONTOLOGY database! Delete ontology of user " + userID + " first. Aborting entire procedure.");
		} catch (PSQLException dup) {
			Log.getLogger().error("Row already exists in ONTOLOGY table! Delete ontology of user " + userID + " first, or fix parallel processing. Aborting entire procedure.");
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			if (addOntologyStatement != null) {
		        try { addOntologyStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}*/
	
	public void addUserOntology(String ontologyKey, UserOntology userOntology) {
		
		Connection conn = null;
		PreparedStatement addOntologyStatement = null;
		
		//if(isOntologyContained(ontologyKey)) return;

	    try {
	    	conn = pool.getConnection();
		    addOntologyStatement = conn.prepareStatement(addOntologySQL);
		    
		    Log.getLogger().info("Adding user ontology to DB...");
		    
		    String ontologyJSON = mapper.writeValueAsString(userOntology);
			PGobject jsonObject = new PGobject();
			jsonObject.setType("json");
			jsonObject.setValue(ontologyJSON);
		    
			addOntologyStatement.clearParameters();
			addOntologyStatement.setString(1, ontologyKey);
			addOntologyStatement.setObject(2, jsonObject);
			addOntologyStatement.executeUpdate();
			
			Log.getLogger().info("Added user ontology to DB!");
		
	    } catch (SQLIntegrityConstraintViolationException sqlicve) {
	    	Log.getLogger().error("Row already exists in ONTOLOGY database? Delete ontology with key " + ontologyKey + " first. Aborting.");
	    	sqlicve.printStackTrace();
		} catch (PSQLException pe) {
			pe.printStackTrace();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			if (addOntologyStatement != null) {
		        try { addOntologyStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}

/*	*//**
	 * Get all tweets of a user from the database.
	 * 
	 * @param userID
	 * @return a List of Tweets
	 *//*
	public List<Tweet> getTweets(long userID) {
		
		Connection conn = null;
		PreparedStatement getTweetsStatement = null;

		List<Tweet> tweets = new ArrayList<Tweet>();

		try {
			conn = pool.getConnection();
		    getTweetsStatement = conn.prepareStatement(getTweetsSQL);
			getTweetsStatement.setLong(1, userID);
			ResultSet resultSet = getTweetsStatement.executeQuery();

			while(resultSet.next()) {
				long tweetID = resultSet.getLong(1);
				Tweet tweet = new Tweet(tweetID, resultSet.getLong(2), resultSet.getString(3), resultSet.getDate(4), 
						resultSet.getString(5), resultSet.getBoolean(6), getLocation(tweetID), resultSet.getString(7), 
						getUserMentions(tweetID), getHashtags(tweetID), getURLs(tweetID), getMedia(tweetID));
				Tweet tweet = new Tweet(tweetID, resultSet.getLong(2), resultSet.getLong(3), 
						resultSet.getString(4), resultSet.getBoolean(5), "", resultSet.getString(6), 
						null, null, null, null);
				tweets.add(tweet);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			if (getTweetsStatement != null) {
		        try { getTweetsStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}

		return tweets;
	}*/
/*
	*//**
	 * Fetch a user from the database based on user name.
	 * 
	 * @param userName
	 * @return TwitterUser if exists, otherwise null
	 *//*
	public TwitterUser getUser(String userName) {
		
		Connection conn = null;
		PreparedStatement getUserByNameStatement = null;

		TwitterUser user = null;

		try {
			conn = pool.getConnection();
		    getUserByNameStatement = conn.prepareStatement(getUserByNameSQL);
			getUserByNameStatement.setString(1, userName.toLowerCase());
			ResultSet resultSet = getUserByNameStatement.executeQuery();

			if(resultSet.next()) {
				Properties props = mapper.readValue(resultSet.getString(3), Properties.class);
				
				
				user = new TwitterUser(resultSet.getLong(1), userName, props, resultSet.getFloat(5));
				
				//user.setTweets(getTweets(resultSet.getLong(1)));
				user.setUserOntology(getUserOntology(resultSet.getLong(1)));
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (getUserByNameStatement != null) {
		        try { getUserByNameStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}

		return user;
	}*/
	
	/**
	 * Fetch a user from the database based on screenName.
	 * 
	 * @param userID
	 * @return TwitterUser if exists, otherwise null
	 */
	public TwitterUser getUser(String screenName) {
		
		Connection conn = null;
		PreparedStatement getUserByNameStatement = null;

		TwitterUser user = null;

		try {
			conn = pool.getConnection();
			getUserByNameStatement = conn.prepareStatement(getUserByNameSQL);
			getUserByNameStatement.setString(1, screenName.toLowerCase());
			ResultSet resultSet = getUserByNameStatement.executeQuery();
			
			if(resultSet.next()) {
				Properties props = mapper.readValue(resultSet.getString(3), Properties.class);
				user = new TwitterUser(resultSet.getLong(1), resultSet.getString(2), props, resultSet.getFloat(5));
				
				if(resultSet.getString(4) != null) {
					List<Tweet> tweets = mapper.readValue(resultSet.getString(4), new TypeReference<List<Tweet>>(){});
					user.setTweets(tweets);
				}
			}
			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (getUserByNameStatement != null) {
		        try { getUserByNameStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}

		return user;
	}

	/**
	 * Fetch a user and their ontology (if exists) from the database based on user ID.
	 * 
	 * @param userID
	 * @return TwitterUser if exists, otherwise null
	 */
	public TwitterUser getUser(long userID) {
		
		Connection conn = null;
		PreparedStatement getUserByIDStatement = null;

		TwitterUser user = null;

		try {
			conn = pool.getConnection();
		    getUserByIDStatement = conn.prepareStatement(getUserByIDSQL);
			getUserByIDStatement.setLong(1, userID);
			ResultSet resultSet = getUserByIDStatement.executeQuery();
			
			if(resultSet.next()) {
				Properties props = mapper.readValue(resultSet.getString(3), Properties.class);
				
				Log.getLogger().info("Props: " + props);
				
				List<Tweet> tweets = mapper.readValue(resultSet.getString(4), new TypeReference<List<Tweet>>(){});
				
				Log.getLogger().info("Tweets: " + tweets);
				
				user = new TwitterUser(resultSet.getLong(1), resultSet.getString(2), props, resultSet.getFloat(5));
				user.setTweets(tweets);
				//user.setUserOntology(getUserOntology(userID));
			}
			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (getUserByIDStatement != null) {
		        try { getUserByIDStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}

		return user;
	}
	
/*	public void stripAllTweets() {
		
		Connection conn = null;
		PreparedStatement getAllUsersStatement = null;

		try {
			conn = pool.getConnection();
		    getAllUsersStatement = conn.prepareStatement(getAllUsersSQL);
			ResultSet resultSet = getAllUsersStatement.executeQuery();

			while(resultSet.next()) {
				List<Tweet> tweets = getTweets(resultSet.getLong(1));
				
				for(Tweet tweet : tweets) {
					tweet.stripElements();
					updateTweet(tweet);
				}
			}

		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getAllUsersStatement != null) {
		        try { getAllUsersStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}*/
	
	public void updateUserTweets(TwitterUser twitterUser) {
		Connection conn = null;
		PreparedStatement updateUserTweetsStatement = null;
		
		try {
			String tweetsJSON = mapper.writeValueAsString(twitterUser.getTweets());
			PGobject jsonObject = new PGobject();
			jsonObject.setType("json");
			jsonObject.setValue(tweetsJSON);
			
			conn = pool.getConnection();
			updateUserTweetsStatement = conn.prepareStatement(updateUserTweetsSQL);
			updateUserTweetsStatement.setObject(1, jsonObject);
			updateUserTweetsStatement.setFloat(2, twitterUser.getEnglishRate());
			updateUserTweetsStatement.setLong(3, twitterUser.getUserID());
			updateUserTweetsStatement.executeUpdate();
			Log.getLogger().info("Successfully updated user tweets in DB!");
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			if (updateUserTweetsStatement != null) {
		        try { updateUserTweetsStatement.close(); } catch (SQLException e) {}
		    }
		    if (conn != null) {
		        try { conn.close(); } catch (SQLException e) {}
		    }
		}
	}

	/*private void updateTweet(Tweet tweet) {
		
		Connection conn = null;
		PreparedStatement updateSingleTweetStatement = null;
		
		long tweetID = tweet.getTweetID();
		
		try {
			conn = pool.getConnection();
		    updateSingleTweetStatement = conn.prepareStatement(updateSingleTweetSQL);
			updateSingleTweetStatement.setString(1, tweet.getContent());
			updateSingleTweetStatement.setLong(2, tweetID);
			updateSingleTweetStatement.executeUpdate();
			Log.getLogger().info("Successfully (?) updated tweet! New content: " + tweet.getContent());
			
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (updateSingleTweetStatement != null) {
		        try { updateSingleTweetStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}*/

/*	*//**
	 * Get a user ontology from the database.
	 * 
	 * @param userID
	 * @return a map with OntologyTypes with occurrence numbers
	 *//*
	public UserOntology getUserOntology(long userID) {
		
		Connection conn = null;
		PreparedStatement getUserOntologyStatement = null;

		UserOntology userOntology = new UserOntology();

		try {
			conn = pool.getConnection();
		    getUserOntologyStatement = conn.prepareStatement(getUserOntologySQL);
			getUserOntologyStatement.setLong(1, userID);
			getUserOntologyStatement.setInt(2, Vars.CONCATENATION_WINDOW);
			getUserOntologyStatement.setDouble(3, Vars.SPOTLIGHT_CONFIDENCE);
			getUserOntologyStatement.setInt(4, Vars.SPOTLIGHT_SUPPORT);
			ResultSet resultSet = getUserOntologyStatement.executeQuery();

			while(resultSet.next()) {
				String type = resultSet.getString(2);
				int cardinality = resultSet.getInt(3);
				
				// Hardcode YAGO types in
				if(uri.contains("dbpedia.org/class/yago/")) {
					userOntology.addYAGOType(new YAGOType(uri.split("dbpedia.org/class/yago/")[1]), cardinality);					
				}
				
				
				OntologyType ontologyType = new OntologyType(type);
				userOntology.addClass(ontologyType, cardinality);
			}
			
			//Log.getLogger().info("USER ONTOLOGY: " + userOntology);
			
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getUserOntologyStatement != null) {
		        try { getUserOntologyStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}

		return userOntology;
	}*/
	
	/**
	 * Get a user ontology from the database.
	 * 
	 * @param userID
	 * @return a map with OntologyTypes with occurrence numbers
	 */
	public UserOntology getUserOntology(String ontologyKey) {
		
		Connection conn = null;
		PreparedStatement getUserOntologyStatement = null;
		UserOntology userOntology = new UserOntology();

		try {
			conn = pool.getConnection();
		    getUserOntologyStatement = conn.prepareStatement(getUserOntologySQL);
		    getUserOntologyStatement.setString(1, ontologyKey);
			ResultSet resultSet = getUserOntologyStatement.executeQuery();

			if(resultSet.next())
				userOntology = mapper.readValue(resultSet.getString(2), UserOntology.class);
			
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (getUserOntologyStatement != null) {
		        try { getUserOntologyStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}

		return userOntology;
	}

/*	*//**
	 * Get a tweet's associated hashtags from the database.
	 * 
	 * @param tweetID
	 * @return A list of hashtags in String format (empty if none are found)
	 *//*
	public List<String> getHashtags(long tweetID) {
		
		Connection conn = null;
		PreparedStatement getHashtagsStatement = null;
		
		List<String> hashtags = new ArrayList<String>();
		
		try {
			conn = pool.getConnection();
		    getHashtagsStatement = conn.prepareStatement(getHashtagsSQL);
			getHashtagsStatement.setLong(1, tweetID);
			ResultSet resultSet = getHashtagsStatement.executeQuery();
			while(resultSet.next()) { hashtags.add(resultSet.getString(3)); }
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getHashtagsStatement != null) {
		        try { getHashtagsStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return hashtags;
	}*/
/*
	*//**
	 * Get a tweet's associated URLs from the database.
	 * 
	 * @param tweetID
	 * @return A list of URLs in String format (empty if none are found)
	 *//*
	public List<String> getURLs(long tweetID) {
		
		Connection conn = null;
		PreparedStatement getURLStatement = null;
		
		List<String> URLs = new ArrayList<String>();
		try {
			conn = pool.getConnection();
		    getURLStatement = conn.prepareStatement(getURLSQL);
			getURLStatement.setLong(1, tweetID);
			ResultSet resultSet = getURLStatement.executeQuery();
			while(resultSet.next()) { URLs.add(resultSet.getString(3)); }
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			if (getURLStatement != null) {
		        try { getURLStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return URLs;
	}*/

/*	*//**
	 * Get a tweet's associated user mentions from the database.
	 * 
	 * @param tweetID
	 * @return A list of user mentions in String format (empty if none are found)
	 *//*
	public ArrayList<String> getUserMentions(long tweetID) {
		
		Connection conn = null;
		PreparedStatement getUserMentionsStatement = null;
		
		ArrayList<String> userMentions = new ArrayList<String>();
		try {
			conn = pool.getConnection();
		    getUserMentionsStatement = conn.prepareStatement(getUserMentionsSQL);
			getUserMentionsStatement.setLong(1, tweetID);
			ResultSet resultSet = getUserMentionsStatement.executeQuery();
			while(resultSet.next()) { userMentions.add(resultSet.getString(3)); }
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getUserMentionsStatement != null) {
		        try { getUserMentionsStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return userMentions;
	}*/

/*	*//**
	 * Get a tweet's associated media from the database.
	 * 
	 * @param tweetID
	 * @return A list of media in String format (empty if none are found)
	 *//*
	public List<String> getMedia(long tweetID) {
		
		Connection conn = null;
		PreparedStatement getMediaStatement = null;
		
		List<String> media = new ArrayList<String>();
		try {
			conn = pool.getConnection();
		    getMediaStatement = conn.prepareStatement(getMediaSQL);
			getMediaStatement.setLong(1, tweetID);
			ResultSet resultSet = getMediaStatement.executeQuery();
			while(resultSet.next()) { media.add(resultSet.getString(3)); }
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getMediaStatement != null) {
		        try { getMediaStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return media;
	}*/

/*	*//**
	 * Get a tweet's associated location from the database.
	 * 
	 * @param tweetID
	 * @return A location in String format (empty String if none is found)
	 *//*
	public String getLocation(long tweetID) {
		
		Connection conn = null;
		PreparedStatement getLocationStatement = null;
		
		String location = "";
		try {
			conn = pool.getConnection();
		    getLocationStatement = conn.prepareStatement(getLocationSQL);
			getLocationStatement.setLong(1, tweetID);
			ResultSet resultSet = getLocationStatement.executeQuery();
			while(resultSet.next()) { location = resultSet.getString(3); }
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getLocationStatement != null) {
		        try { getLocationStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return location;
	}*/

	/**
	 * Get a user's followers from the database.
	 * 
	 * @param userID
	 * @return a List of follower user IDs
	 */
	public List<Long> getUserFollowers(long userID) {
		
		Connection conn = null;
		PreparedStatement getUserFollowersStatement = null;
		List<Long> followers = new ArrayList<Long>();

		try {
			conn = pool.getConnection();
		    getUserFollowersStatement = conn.prepareStatement(getUserFollowersSQL);
			getUserFollowersStatement.setLong(1, userID);
			ResultSet resultSet = getUserFollowersStatement.executeQuery();

			while(resultSet.next()) {
				followers.add(resultSet.getLong(2));
			}
		} catch (SQLException sqle) { 
			sqle.printStackTrace();
		} finally {
			if (getUserFollowersStatement != null) {
		        try { getUserFollowersStatement.close(); } catch (SQLException e) {}
		    }
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}

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
