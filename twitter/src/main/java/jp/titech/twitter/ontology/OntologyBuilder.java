/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.ontology;

import java.util.ArrayList;

import jp.titech.twitter.data.AnnotatedTweet;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.matching.Annotator;
import jp.titech.twitter.util.Log;

public class OntologyBuilder {
	
	private int userID;

	public OntologyBuilder(int tUserID) {
		userID = tUserID;
	}

	public void build() {
		ArrayList<Tweet> tweets = TweetBase.getInstance().getTweets(userID);
		for (Tweet tweet : tweets) {
			Annotator annotator = new Annotator();
			AnnotatedTweet annotatedTweet = annotator.annotate(tweet);
			Log.getLogger().info(annotatedTweet.toString());
		}
	}
}
