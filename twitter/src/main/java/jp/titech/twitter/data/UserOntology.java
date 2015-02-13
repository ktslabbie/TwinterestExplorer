package jp.titech.twitter.data;

import java.util.HashMap;
import java.util.Map;

import jp.titech.twitter.util.Util;

/**
 * Data class to store user ontologies (ontology types with their occurrence numbers).
 * 
 * @author Kristian
 *
 */
public class UserOntology {

	private Map<String, Integer> ontology  = new HashMap<String, Integer>();
	private int typeCount = 0;
	
	public UserOntology() {}
	
	public UserOntology(Map<String, Integer> ontology) {
		this.ontology = ontology;
	}

	public Map<String, Integer> getOntology() {
		return ontology;
	}

	public void setOntology(Map<String, Integer> ontology) {
		this.ontology = ontology;
	}
	
	public void addClass(String type) {
		Integer occs = this.ontology.get(type);
		if(occs != null)
			this.addClass(type, occs+1);			
		else
			this.addClass(type, 1);
	}
	
	/**
	 * Add an unknown type of class to the ontology. Type is determined and added to the ontology map. 
	 * 
	 * @param type
	 * @param cardinality
	 */
	public void addClass(String type, int cardinality) {
		this.ontology.put(type, cardinality);
		this.typeCount++;
	}

	/**
	 * @return the typeCount
	 */
	public int getTypeCount() {
		return typeCount;
	}

	/**
	 * @param typeCount the typeCount to set
	 */
	public void setTypeCount(int typeCount) {
		this.typeCount = typeCount;
	}

	
	/**
	 * Override the toString() method to provide a nice, sorted representation of the ontology. 
	 * 
	 * @return the string representation of the full ontology
	 */
	@Override
	public String toString() {
		
		Map<String, Integer> ontologySortedMap 	= Util.sortByValue(this.ontology);
		
		String out = "Ontology_type\tcount\n";
		
		for (String type : ontologySortedMap.keySet()) {
			out += type + "\t" + ontologySortedMap.get(type) + "\n";
		}

		return out;
	}
}
