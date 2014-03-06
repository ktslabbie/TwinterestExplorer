package jp.titech.twitter.data;

import java.util.HashMap;
import java.util.Map;

import org.dbpedia.spotlight.model.OntologyType;

/**
 * Data class to store user ontologies (ontology types with their occurrence numbers).
 * 
 * @author Kristian
 *
 */
public class UserOntology {

	private Map<OntologyType, Integer> ontology;
	private int tweetWindow;
	private double confidence;
	private int support;
	
	
	public UserOntology() {
		ontology = new HashMap<OntologyType, Integer>();
	}
	
	public void addOntologyType(OntologyType type) {
		
	}


	public Map<OntologyType, Integer> getOntology() {
		return ontology;
	}


	public void setOntology(Map<OntologyType, Integer> ontology) {
		this.ontology = ontology;
	}


	public int getTweetWindow() {
		return tweetWindow;
	}


	public void setTweetWindow(int tweetWindow) {
		this.tweetWindow = tweetWindow;
	}


	public double getConfidence() {
		return confidence;
	}


	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}


	public int getSupport() {
		return support;
	}


	public void setSupport(int support) {
		this.support = support;
	}
	
	
}
