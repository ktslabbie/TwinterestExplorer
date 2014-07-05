package jp.titech.twitter.ontology.types;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class DBpediaType implements OntologyType {
	private final String baseURI = "http://dbpedia.org/resource/";
	private String fullURI, typeID;
	
	public DBpediaType(String type) {
		fullURI = baseURI + type;
		typeID = "DBpedia:" + type;
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

        DBpediaType rhs = (DBpediaType) obj;
        return new EqualsBuilder().
            append(fullURI, rhs.fullURI).
            append(typeID, rhs.typeID).
            isEquals();
    }
}
