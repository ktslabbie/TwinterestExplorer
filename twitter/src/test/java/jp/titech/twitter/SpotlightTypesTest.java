package jp.titech.twitter;

import java.util.List;

import jp.titech.twitter.ner.spotlight.SpotlightQuery;
import jp.titech.twitter.ontology.dbpedia.DBpediaResourceOccurrence;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

import org.junit.Before;
import org.junit.Test;

public class SpotlightTypesTest {
	
	
	@Before
    public void setUp() {
		
	}

	@Test
	public void testTypes() {
		Log.getLogger().info("Testing DBpedia Spotlight type retrieval...");
		
		SpotlightQuery slq = new SpotlightQuery(Vars.SPOTLIGHT_BASE_URL + ":2222", 0.0f, 0);
		List<DBpediaResourceOccurrence> occs = slq.annotate("This is a test to obtain the Apple iPhone types.");
		
		for(DBpediaResourceOccurrence occ : occs) {
			Log.getLogger().info("Occ: " + occ);
			Log.getLogger().info("Types: " + occ.getResource());
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
