package jp.titech.twitter.ontology.types;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class SchemaOrgType implements OntologyType {
	private final String baseURI = "http://schema.org/";
	private String fullURI, typeID;
	
	public SchemaOrgType(String type) {
		fullURI = baseURI + type;
		typeID = "Schema:" + type;
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

        SchemaOrgType rhs = (SchemaOrgType) obj;
        return new EqualsBuilder().
            append(fullURI, rhs.fullURI).
            append(typeID, rhs.typeID).
            isEquals();
    }
}
