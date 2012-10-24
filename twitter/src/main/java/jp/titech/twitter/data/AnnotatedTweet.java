/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

import org.dbpedia.spotlight.model.DBpediaResourceOccurrence;
import org.dbpedia.spotlight.model.OntologyType;

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
	
	/**
	 * 
	 * @return A list of the 1st ranked candidate DBpedia resource occurrences
	 */
	public List<DBpediaResourceOccurrence> getBestCandidates() {
		Collection<List<DBpediaResourceOccurrence>> col = occurrences.values();
		List<DBpediaResourceOccurrence> bestCandidates = new ArrayList<DBpediaResourceOccurrence>();
		
		for (Iterator<List<DBpediaResourceOccurrence>> iterator = col.iterator(); iterator.hasNext();) {
			List<DBpediaResourceOccurrence> candidates = iterator.next();
			bestCandidates.add(candidates.get(0));
		}
		
		return bestCandidates;
	}
	
	public Map<OntologyType, Integer> getTypes() {
		Collection<List<DBpediaResourceOccurrence>> col = occurrences.values();
		
		Map<OntologyType, Integer> types = new HashMap<OntologyType, Integer>();
		
		for (Iterator<List<DBpediaResourceOccurrence>> iterator = col.iterator(); iterator.hasNext();) {
			
			List<DBpediaResourceOccurrence> candidates = iterator.next();
			DBpediaResourceOccurrence bestCandidate = candidates.get(0);
			
			List<OntologyType> ontoTypes = Util.convertScalaList(bestCandidate.resource().types());
			
			for(Iterator<OntologyType> typeIt = ontoTypes.iterator(); typeIt.hasNext();) {
				OntologyType currentType = typeIt.next();
				if(types.get(currentType) != null) {
					types.put(currentType, types.get(currentType) + 1);
				} else {
					types.put(currentType, 1);
				}
			}
		}
		
		return types;
	}
}
