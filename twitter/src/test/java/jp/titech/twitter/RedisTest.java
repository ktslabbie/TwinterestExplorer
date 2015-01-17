package jp.titech.twitter;

import java.util.List;

import jp.titech.twitter.ontology.dbpedia.RedisClient;
import jp.titech.twitter.util.Log;
import junit.framework.TestCase;

import org.junit.Test;

public class RedisTest extends TestCase {
	
	
	protected void setUp() {
		
	}

	@Test
	public void testRedis() {
		Log.getLogger().info("Testing Redis.");
		
		RedisClient cl = RedisClient.getInstance();
		String uri = "http://dbpedia.org/resource/!!!_(album)";
		
		Log.getLogger().info("Querying: " + uri);
		List<String> types = cl.query(uri, "yago");
		
		for(String type: types) {
			Log.getLogger().info("Type: " + type);
		}
		
//		assertEquals(testTweet.getTweetID(), 1);
//		assertEquals(testTweet.getUserID(), 234);
//		assertNotNull(testTweet.getContent());
//		//assertEquals(testTweet.getScreenName(), "test_tweet_user");
//		assertEquals(testTweet.getCreatedAt().getTime(), date.getTime());
//		assertFalse(testTweet.isRetweet());
//		assertEquals(testTweet.getLocationName(), "Tokyo");
//		assertEquals(testTweet.getLanguage(), "en");
//		assertTrue(testTweet.getHashtags().contains("selfie"));
	}

}
