package jp.titech.twitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.mining.api.TwitterAPIAccountManager;
import jp.titech.twitter.ontology.dbpedia.RedisClient;
import jp.titech.twitter.util.Util;
import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

public class UserTest extends TestCase {
	
	protected TwitterUser testUser;
	
	protected void setUp() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String dateInString = "23-12-2012 10:20:56";
        Date date = new Date();
		try {
			date = sdf.parse(dateInString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		TweetBase.getInstance();
		RedisClient.getInstance();
		TwitterAPIAccountManager.getInstance();
		Util.loadStopwords();
	}

	@Test
	public void testUser() {
		//testUser = new TwitterUser(1, "TestName", new UserProperties("hello", "desc", "loc", 2, 3, 4, date, false, "http://lol.lol.lol"), 0.9);
		/*TweetBase.getInstance().addUser(new TwitterUser(1, "test", "test", "testdesc", "testloc", 2, 3, 4, date.getTime(), false, 0.9f, "http://test.test.test"));

		Log.getLogger().info("Hi, what");
		//TweetBase.getInstance().getUser(1);

		TwitterConnector connector = new TwitterConnector(1);
    	TwitterUser targetUser = connector.getTwitterUserWithScreenName("bnmuller");
    	
    	if(!targetUser.hasTweets()) {
    		// User has no tweets yet, so mine some.
    		MiningController miningController = MiningController.getInstance();
    		miningController.mineUser(targetUser, connector);
    	}
    	
    	// If the user has enough English tweets...
    	if(targetUser.getEnglishRate() > Vars.MIN_ENGLISH_RATE) {
    		
    		// Choose a DBpedia Spotlight instance based on the current user index (to assure even distribution).
        	int portIndex = 1;
        	String spotlightUrl = Vars.SPOTLIGHT_BASE_URL + ":" + Vars.SPOTLIGHT_PORTS[portIndex % Vars.SPOTLIGHT_PORTS.length];
    		
        	// Generate the user ontology.
			OntologyController ontologyController = new OntologyController(0, 0, 25);
			ontologyController.createUserOntology(targetUser, spotlightUrl);
    	}
    	
    	Log.getLogger().info("USER: " + targetUser);*/
	}
}