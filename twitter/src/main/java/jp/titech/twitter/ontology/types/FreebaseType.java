/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		11 dec. 2012
 */
package jp.titech.twitter.ontology.types;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class FreebaseType implements OntologyType {
	
	private final String baseURI = "http://rdf.freebase.com/ns/";
	private String fullURI, typeID;
	
	public FreebaseType(String domain) {
		fullURI = baseURI + domain;
		typeID = "Freebase:/" + domain;
	}
	
	public FreebaseType(String domain, String typeName){
		fullURI = baseURI + domain + "/" + typeName;
		typeID = "Freebase:/" + domain + "/" + typeName;
	}

	/* (non-Javadoc)
	 * @see org.dbpedia.spotlight.model.OntologyType#getFullUri()
	 */
	public String getFullUri() {
		return fullURI;
	}

	/* (non-Javadoc)
	 * @see org.dbpedia.spotlight.model.OntologyType#typeID()
	 */
	public String typeID() {
		return typeID;
	}
	
	public String toString(){
		return typeID;
	}
	
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
            append(fullURI).
            append(typeID).
            toHashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        FreebaseType rhs = (FreebaseType) obj;
        return new EqualsBuilder().
            append(fullURI, rhs.fullURI).
            append(typeID, rhs.typeID).
            isEquals();
    }

}
