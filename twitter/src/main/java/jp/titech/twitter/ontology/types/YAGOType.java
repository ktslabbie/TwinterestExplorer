/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		11 dec. 2012
 */
package jp.titech.twitter.ontology.types;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.dbpedia.spotlight.model.OntologyType;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class YAGOType implements OntologyType {
	
	private final String baseURI = "http://dbpedia.org/class/yago/";
	private String fullURI, typeID;
	
	public YAGOType(String type)	{
		fullURI = baseURI + type;
		typeID = "YAGO:" + type;
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

        YAGOType rhs = (YAGOType) obj;
        return new EqualsBuilder().
            append(fullURI, rhs.fullURI).
            append(typeID, rhs.typeID).
            isEquals();
    }

}
