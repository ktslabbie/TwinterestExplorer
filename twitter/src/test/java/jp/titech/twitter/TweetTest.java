package jp.titech.twitter;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.titech.twitter.data.Tweet;
import junit.framework.TestCase;

import org.junit.Test;

public class TweetTest extends TestCase {
	
	protected Tweet testTweet, testRetweet;
	protected Date date;
	
	protected void setUp() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String dateInString = "23-12-2012 10:20:56";
		date = new Date();
		try {
			date = sdf.parse(dateInString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		testTweet = new Tweet(1, 234, date, "@mention_user This is the #test tweet. http://www.google.com http://www.youtube.com #YOLO #selfie End lol.", 
								false, "Tokyo", "en", new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		testTweet.addUserMention("mention_user");
		testTweet.addURL("http://www.google.com");
		testTweet.addHashtag("YOLO");
		testTweet.addHashtag("selfie");
		testTweet.addHashtag("test");
		testTweet.addMedia("http://www.youtube.com");
		
		testRetweet = new Tweet(2, 234, date, 
				"RT This is a #test retweet. #YOLO #selfie End.", false, "Tokyo", "en", 
				new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		testRetweet.addHashtag("YOLO");
		testRetweet.addHashtag("selfie");
		testRetweet.addHashtag("test");
	}

	@Test
	public void testTweet() {
		System.out.println("testTweet content: " + testTweet.getContent());
		assertEquals(testTweet.getTweetID(), 1);
		assertEquals(testTweet.getUserID(), 234);
		assertNotNull(testTweet.getContent());
		//assertEquals(testTweet.getScreenName(), "test_tweet_user");
		assertEquals(testTweet.getCreatedAt().getTime(), date.getTime());
		assertFalse(testTweet.isRetweet());
		assertEquals(testTweet.getLocationName(), "Tokyo");
		assertEquals(testTweet.getLanguage(), "en");
		assertTrue(testTweet.getHashtags().contains("selfie"));
	}

	@Test
	public void testStripUserMentions() {
		assertTrue(testTweet.getContent().contains("@mention_user"));
		testTweet.stripUserMentions();
		assertFalse(testTweet.getContent().contains("@mention_user"));
		System.out.println("Mentions stripped: " + testTweet.getContent());
	}

	@Test
	public void testStripHashtags() {
		assertTrue(testTweet.getContent().contains("#YOLO"));
		testTweet.stripHashtags();
		assertFalse(testTweet.getContent().contains("#YOLO"));
		assertFalse(testTweet.getContent().contains("YOLO"));
		System.out.println("Hashtags stripped: " + testTweet.getContent());
	}

	@Test
	public void testStripHashtagTags() {
		assertTrue(testRetweet.getContent().contains("#test"));
		testRetweet.stripHashtagTags();
		assertFalse(testRetweet.getContent().contains("#test"));
		assertTrue(testRetweet.getContent().contains("test"));
	}

	@Test
	public void testStripURLs() {
		assertTrue(testTweet.getContent().contains("http://www.google.com"));
		testTweet.stripURLs();
		assertFalse(testTweet.getContent().contains("http://www.google.com"));
		System.out.println("URLs stripped: " + testTweet.getContent());
	}

	@Test
	public void testStripMedia() {
		assertTrue(testTweet.getContent().contains("http://www.youtube.com"));
		testTweet.stripMedia();
		assertFalse(testTweet.getContent().contains("http://www.youtube.com"));
		System.out.println("Media stripped: " + testTweet.getContent());
	}

	@Test
	public void testStripNetslang() {
		assertTrue(testTweet.getContent().contains("lol"));
		testTweet.stripNetslang();
		assertNotNull(testTweet.getContent());
		System.out.println("Netslang stripped: " + testTweet.getContent());
		assertFalse(testTweet.getContent().contains("lol"));
	}

	@Test
	public void testStripElements() {
		String originalContent = testTweet.getContent();
		testTweet.stripElements();
		assertNotSame(originalContent, testTweet.getContent());
		System.out.println("All of the above: " + testTweet.getContent());
	}

	@Test
	public void testStripNonHashtagElements() {
		testTweet.stripNonHashtagElements();
		for(String tag : testTweet.getHashtags()) {
			assertTrue(testTweet.getContent().contains(tag));
			assertFalse(testTweet.getContent().contains("#" + tag));
		}
		
		for(String url : testTweet.getURLs()) {
			assertFalse(testTweet.getContent().contains(url));
		}
		
		System.out.println("Leave hashtags alone: " + testTweet.getContent());
	}

	@Test
	public void testTokenize() {
		testTweet.tokenize();
		assertFalse(testTweet.getContent().contains("google"));
		assertFalse(testTweet.getContent().contains("YOLO"));
		assertTrue(testTweet.getContent().contains("yolo"));
		assertFalse(testTweet.getContent().contains("."));
		System.out.println("Tokenize: " + testTweet.getContent());
	}
}
