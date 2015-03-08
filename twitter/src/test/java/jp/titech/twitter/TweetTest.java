package jp.titech.twitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import jp.titech.twitter.data.Tweet;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class TweetTest {
	
	private Tweet testTweet, testRetweet;
	private Date date;
	
	@Before
    public void setUp() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String dateInString = "23-12-2012 10:20:56";
		date = new Date();
		try {
			date = sdf.parse(dateInString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		testTweet = new Tweet(1, 234, date.getTime(), "@mention_user This is the #test tweet. http://www.google.com http://www.youtube.com #YOLO #selfie End lol.", 
								false, "Tokyo", "en", new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		testTweet.addUserMention("mention_user");
		testTweet.addURL("http://www.google.com");
		testTweet.addHashtag("YOLO");
		testTweet.addHashtag("selfie");
		testTweet.addHashtag("test");
		testTweet.addMedia("http://www.youtube.com");
		
		testRetweet = new Tweet(2, 234, date.getTime(), 
				"RT This is a #test retweet. #YOLO #selfie End.", false, "Tokyo", "en", 
				new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		testRetweet.addHashtag("YOLO");
		testRetweet.addHashtag("selfie");
		testRetweet.addHashtag("test");
	}

	@Test
	public void testTweet() {
		System.out.println("testTweet content: " + testTweet.getContent());
		Assert.assertEquals(testTweet.getTweetID(), 1);
        Assert.assertEquals(testTweet.getUserID(), 234);
        Assert.assertNotNull(testTweet.getContent());
		//Assert.assertEquals(testTweet.getScreenName(), "test_tweet_user");
        Assert.assertEquals(testTweet.getCreatedAt(), date.getTime());
        Assert.assertFalse(testTweet.isRetweet());
        Assert.assertEquals(testTweet.getLocationName(), "Tokyo");
        Assert.assertEquals(testTweet.getLanguage(), "en");
        Assert.assertTrue(testTweet.getHashtags().contains("selfie"));
	}

	@Test
	public void testStripUserMentions() {
        Assert.assertTrue(testTweet.getContent().contains("@mention_user"));
		testTweet.stripUserMentions();
        Assert.assertFalse(testTweet.getContent().contains("@mention_user"));
		System.out.println("Mentions stripped: " + testTweet.getContent());
	}

	@Test
	public void testStripHashtags() {
        Assert.assertTrue(testTweet.getContent().contains("#YOLO"));
		testTweet.stripHashtags();
        Assert.assertFalse(testTweet.getContent().contains("#YOLO"));
        Assert.assertFalse(testTweet.getContent().contains("YOLO"));
		System.out.println("Hashtags stripped: " + testTweet.getContent());
	}

	@Test
	public void testStripHashtagTags() {
        Assert.assertTrue(testRetweet.getContent().contains("#test"));
		testRetweet.stripHashtagTags();
        Assert.assertFalse(testRetweet.getContent().contains("#test"));
        Assert.assertTrue(testRetweet.getContent().contains("test"));
	}

	@Test
	public void testStripURLs() {
        Assert.assertTrue(testTweet.getContent().contains("http://www.google.com"));
		testTweet.stripURLs();
        Assert.assertFalse(testTweet.getContent().contains("http://www.google.com"));
		System.out.println("URLs stripped: " + testTweet.getContent());
	}

	@Test
	public void testStripMedia() {
        Assert.assertTrue(testTweet.getContent().contains("http://www.youtube.com"));
		testTweet.stripMedia();
        Assert.assertFalse(testTweet.getContent().contains("http://www.youtube.com"));
		System.out.println("Media stripped: " + testTweet.getContent());
	}

	@Test
	public void testStripNetslang() {
        Assert.assertTrue(testTweet.getContent().contains("lol"));
		testTweet.stripNetslang();
        Assert.assertNotNull(testTweet.getContent());
		System.out.println("Netslang stripped: " + testTweet.getContent());
        Assert.assertFalse(testTweet.getContent().contains("lol"));
	}

	@Test
	public void testStripElements() {
		String originalContent = testTweet.getContent();
		testTweet.stripElements();
        Assert.assertNotSame(originalContent, testTweet.getContent());
		System.out.println("All of the above: " + testTweet.getContent());
	}

	@Test
	public void testStripNonHashtagElements() {
		testTweet.stripNonHashtagElements();
		for(String tag : testTweet.getHashtags()) {
            Assert.assertTrue(testTweet.getContent().contains(tag));
            Assert.assertFalse(testTweet.getContent().contains("#" + tag));
		}
		
		for(String url : testTweet.getURLs()) {
            Assert.assertFalse(testTweet.getContent().contains(url));
		}
		
		System.out.println("Leave hashtags alone: " + testTweet.getContent());
	}

	@Test
	public void testTokenize() {
		testTweet.tokenize();
        Assert.assertFalse(testTweet.getContent().contains("google"));
        Assert.assertFalse(testTweet.getContent().contains("YOLO"));
        Assert.assertTrue(testTweet.getContent().contains("yolo"));
        Assert.assertFalse(testTweet.getContent().contains("."));
		System.out.println("Tokenize: " + testTweet.getContent());
	}
}
