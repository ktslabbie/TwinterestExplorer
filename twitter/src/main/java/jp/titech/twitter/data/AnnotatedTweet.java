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

import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.YAGOType;
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
	private List<DBpediaResourceOccurrence> bestCandidates;
	private Map<OntologyType, Integer> nativeTypes, allTypes;
	private Map<YAGOType, Integer> yagoTypes;
	private Map<Category, Integer> categories;

	/**
	 * 
	 * 
	 * @param tweet
	 */
	public AnnotatedTweet(Tweet tweet) {
		super(tweet.getTweetID(), tweet.getUserID(), tweet.getScreenName(), tweet.getCreatedAt(), tweet.getContent(), tweet.isRetweet(), tweet.getUserMentions(), 
				tweet.getHashtags(), tweet.getURLs(), tweet.getMedia(), tweet.getLocationName(), tweet.getLanguage());
	}

	/**
	 * @param tweet
	 * @param occs
	 */
	public AnnotatedTweet(Tweet tweet, Map<String, List<DBpediaResourceOccurrence>> occs) {
		super(tweet.getTweetID(), tweet.getUserID(), tweet.getScreenName(), tweet.getCreatedAt(), tweet.getContent(), tweet.isRetweet(), tweet.getUserMentions(), 
				tweet.getHashtags(), tweet.getURLs(), tweet.getMedia(), tweet.getLocationName(), tweet.getLanguage());

		confidence = Vars.SPOTLIGHT_CONFIDENCE;
		support = Vars.SPOTLIGHT_SUPPORT;
		occurrences = occs;
		allTypes = new HashMap<OntologyType, Integer>();
		yagoTypes = new HashMap<YAGOType, Integer>();
		categories = new HashMap<Category, Integer>();
		
		this.initBestCandidates();
		this.initNativeTypes();
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

	/**
	 * @return the yAGOTypes
	 */
	public Map<YAGOType, Integer> getYAGOTypes() {
		return yagoTypes;
	}

	/**
	 * @param yAGOTypes the yAGOTypes to set
	 */
	public void setYAGOTypes(Map<YAGOType, Integer> tYAGOTypes) {
		yagoTypes = tYAGOTypes;
		allTypes.putAll(tYAGOTypes);
	}

	/**
	 * @return the categories
	 */
	public Map<Category, Integer> getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(Map<Category, Integer> tCategories) {
		this.categories = tCategories;
		allTypes.putAll(tCategories);
	}

	/**
	 * @param bestCandidates the bestCandidates to set
	 */
	public void setBestCandidates(List<DBpediaResourceOccurrence> bestCandidates) {
		this.bestCandidates = bestCandidates;
	}

	/**
	 * @param nativeTypes the nativeTypes to set
	 */
	public void setNativeTypes(Map<OntologyType, Integer> nativeTypes) {
		this.nativeTypes = nativeTypes;
	}
	
	/**
	 * @return the allTypes
	 */
	public Map<OntologyType, Integer> getAllTypes() {
		return allTypes;
	}

	/**
	 * @param allTypes the allTypes to set
	 */
	public void setAllTypes(Map<OntologyType, Integer> allTypes) {
		this.allTypes = allTypes;
	}

	/**
	 * @return the yagoTypes
	 */
	public Map<YAGOType, Integer> getYagoTypes() {
		return yagoTypes;
	}

	/**
	 * @param yagoTypes the yagoTypes to set
	 */
	public void setYagoTypes(Map<YAGOType, Integer> yagoTypes) {
		this.yagoTypes = yagoTypes;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AnnotatedTweet [confidence=" + confidence + ", support="
				+ support + ", occurrences=" + occurrences
				+ ", bestCandidates=" + bestCandidates + ", nativeTypes="
				+ nativeTypes + "]";
	}

	/**
	 * 
	 * @return A list of the 1st ranked candidate DBpedia resource occurrences
	 */
	public List<DBpediaResourceOccurrence> getBestCandidates() {
		return bestCandidates;
	}

	/**
	 * 
	 * @return A list of the 1st ranked candidate DBpedia resource occurrences
	 */
	private void initBestCandidates() {
		Collection<List<DBpediaResourceOccurrence>> col = occurrences.values();
		bestCandidates = new ArrayList<DBpediaResourceOccurrence>();

		for (Iterator<List<DBpediaResourceOccurrence>> iterator = col.iterator(); iterator.hasNext();) {
			List<DBpediaResourceOccurrence> candidates = iterator.next();
			if(!candidates.isEmpty())
				bestCandidates.add(candidates.get(0));
		}
	}

	/**
	 * 
	 * @return A map of each DBpediaType, FreebaseType and SchemaType and their number of occurrence, in no determined order. 
	 */
	public Map<OntologyType, Integer> getNativeTypes() {
		if(occurrences == null){
			return null;
		}else if(nativeTypes == null) {
			this.initNativeTypes();
		}
		return nativeTypes;
	}

	/**
	 * Initialize the types native to DBpedia Spotlight (DBpedia, Freebase, Schema).
	 * Each unique OntologyType and its cardinality is stored in the map nativeTypes. 
	 */
	private void initNativeTypes() {

		nativeTypes = new HashMap<OntologyType, Integer>();

		for (Iterator<DBpediaResourceOccurrence> iterator = bestCandidates.iterator(); iterator.hasNext();) {

			DBpediaResourceOccurrence bestCandidate = iterator.next();			
			List<OntologyType> ontoTypes = Util.convertScalaList(bestCandidate.resource().types());

			if(!ontoTypes.isEmpty() || ontoTypes != null){
				for(Iterator<OntologyType> typeIt = ontoTypes.iterator(); typeIt.hasNext();) {
					OntologyType currentType = typeIt.next();
					String typeID = currentType.typeID();
					Log.getLogger().info("Type: " + typeID);
	
					if(typeID.contains("DBpedia:") || typeID.contains("Freebase:") || typeID.contains("Schema:")){
						if(nativeTypes.get(currentType) != null) {
							nativeTypes.put(currentType, nativeTypes.get(currentType) + 1);
						} else {
							nativeTypes.put(currentType, 1);
						}
					}
				}
			}
		}
		allTypes.putAll(nativeTypes);
	}
}
