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
	private Map<OntologyType, Integer> nativeTypes;

	/**
	 * 
	 */
	public AnnotatedTweet(Tweet tweet) {
		super(tweet.getTweetID(), tweet.getUserID(), tweet.getScreenName(), tweet.getCreatedAt(), tweet.getContent(), tweet.isRetweet(), tweet.getUserMentions(), 
				tweet.getHashtags(), tweet.getURLs(), tweet.getMedia(), tweet.getLocationName());
	}

	/**
	 * @param tweet
	 * @param occs
	 */
	public AnnotatedTweet(Tweet tweet, Map<String, List<DBpediaResourceOccurrence>> occs) {
		super(tweet.getTweetID(), tweet.getUserID(), tweet.getScreenName(), tweet.getCreatedAt(), tweet.getContent(), tweet.isRetweet(), tweet.getUserMentions(), 
				tweet.getHashtags(), tweet.getURLs(), tweet.getMedia(), tweet.getLocationName());

		confidence = Vars.SPOTLIGHT_CONFIDENCE;
		support = Vars.SPOTLIGHT_SUPPORT;
		occurrences = occs;
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
	 * @return
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
	}


	/*	*//**
	 * 
	 * @return A map of each OntologyType and their number of occurrence, collected up to the root of the hierarchy, in no determined order. 
	 *//*
	public Map<OntologyType, Integer> getTransitiveTypes() {
		if(transitiveTypes == null) {

			transitiveTypes = new HashMap<OntologyType, Integer>();

			for(Iterator<OntologyType> typeIt = getNativeTypes().keySet().iterator(); typeIt.hasNext();){
				OntologyType type = typeIt.next();
				String typeID = type.typeID();

				Log.getLogger().info("Type: " + typeID);

				if(typeID.contains("DBpedia:") || typeID.contains("Freebase:") || typeID.contains("Schema:")){
					if(transitiveTypes.get(type) != null) {
						transitiveTypes.put(type, transitiveTypes.get(type) + 1);
					} else {
						transitiveTypes.put(type, 1);
					}
				} else {

					Log.getLogger().info("Impossible right now!? typeID: " + typeID);

					// TODO: Collect categories/YAGO types from full DBpedia ontology?

					//transitiveTypes = DBpediaOntology.getInstance().getAncestors(type);

					List<BindingSet> bindingList = dbpediaOntologyRepository.query(Vars.SPARQL_PREFIXES + " SELECT * WHERE { <"  + type.getFullUri() + "> rdfs:subClassOf ?x }");

					for (BindingSet bindingSet : bindingList) {
						Value value = bindingSet.getValue("x");
						String stringValue = value.stringValue();

						if(stringValue.contains("ontology")){
							OntologyType newType = new DBpediaType(stringValue);

							if(ancestorTypes.get(newType) != null) {
								ancestorTypes.put(newType, ancestorTypes.get(newType) + 1);
							} else {
								ancestorTypes.put(newType, 1);
							}
						}
					}
				}			
			}
		}
		return transitiveTypes;
	}*/
}
