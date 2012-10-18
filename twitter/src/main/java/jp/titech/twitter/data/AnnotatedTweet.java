/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.data;

import java.util.List;
import java.util.Map;

import jp.titech.twitter.util.Vars;

import org.dbpedia.spotlight.model.DBpediaResourceOccurrence;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class AnnotatedTweet extends Tweet {
	
	private double confidence;
	private int support;
	private Map<String, List<DBpediaResourceOccurrence>> occurrences;
	
	/**
	 * 
	 */
	public AnnotatedTweet(Tweet tweet) {
		super(tweet.getTweetID(), tweet.getUserID(), tweet.getScreenName(), tweet.getCreatedAt(), tweet.getContent(), tweet.getHashtags(), tweet.getLocationName());
	}

	/**
	 * @param tweet
	 * @param occs
	 */
	public AnnotatedTweet(Tweet tweet, Map<String, List<DBpediaResourceOccurrence>> occs) {
		super(tweet.getTweetID(), tweet.getUserID(), tweet.getScreenName(), tweet.getCreatedAt(), tweet.getContent(), tweet.getHashtags(), tweet.getLocationName());
		confidence = Vars.SPOTLIGHT_CONFIDENCE;
		support = Vars.SPOTLIGHT_SUPPORT;
		occurrences = occs;
	}

	/**
	 * @return the confidence
	 */
	public double getConfidence() {
		return confidence;
	}

	/**
	 * @param confidence the confidence to set
	 */
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	/**
	 * @return the support
	 */
	public int getSupport() {
		return support;
	}

	/**
	 * @param support the support to set
	 */
	public void setSupport(int support) {
		this.support = support;
	}

	/**
	 * @return the occurrences
	 */
	public Map<String, List<DBpediaResourceOccurrence>> getOccurrences() {
		return occurrences;
	}

	/**
	 * @param occurrences the occurrences to set
	 */
	public void setOccurrences(Map<String, List<DBpediaResourceOccurrence>> occurrences) {
		this.occurrences = occurrences;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AnnotatedTweet [confidence=" + confidence + ", support="
				+ support + ", occurrences=" + occurrences + "]";
	}
}
