/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.matching;

import java.util.List;
import java.util.Map;

import org.dbpedia.spotlight.model.DBpediaResourceOccurrence;
import jp.titech.twitter.data.AnnotatedTweet;
import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.matching.spotlight.SpotlightFunction;
import jp.titech.twitter.matching.spotlight.SpotlightQuery;
import jp.titech.twitter.util.Log;

public class Annotator extends SpotlightFunction {

	public Annotator(){	}

	public AnnotatedTweet annotate(Tweet tweet) {
		
		SpotlightQuery sq = SpotlightQuery.getInstance();
		Map<String, List<DBpediaResourceOccurrence>> occs = sq.annotate(tweet.getContent());
		if(!occs.isEmpty()) {
			Log.getLogger().info("Found matches!");
			for (String key : occs.keySet()) {
				Log.getLogger().info(key + ": " + occs.get(key));
			}
		}
		
		return new AnnotatedTweet(tweet, occs);
	}
}
