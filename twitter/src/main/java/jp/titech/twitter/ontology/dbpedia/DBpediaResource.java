package jp.titech.twitter.ontology.dbpedia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jp.titech.twitter.ontology.types.OntologyType;
import jp.titech.twitter.util.Vars;

public class DBpediaResource implements Serializable {

	private static final long serialVersionUID = 2492318494061011203L;
	private String uri;
	private int support = 0;
	private double prior = 0.0;
	private List<OntologyType> types;

	public DBpediaResource(String uri, int support, double prior, List<OntologyType> types) {
		this.uri = uri.replace(Vars.DBPEDIA_NAMESPACE, "");
		this.support = support;
		this.prior = prior;
		this.types = types;
	}

	public DBpediaResource(String uri) {
		this.uri = uri.replace(Vars.DBPEDIA_NAMESPACE, "");
		this.types = new ArrayList<OntologyType>();
	}

	@Override
	public String toString() {
		String typesString = "";

		if (types != null && !types.isEmpty()) {
			typesString += "(";

			for (OntologyType type : types) {
				if(type != null && type.typeID() != null) {
					typesString += type.typeID() + ",";
				}
			}

			typesString = typesString.substring(0, typesString.length()-1) + ")";
		}

		return "DBpediaResource[" + this.uri + typesString + "]";
	}

	/**
	 * @return the full uri
	 */
	public String getFullUri() {
		return Vars.DBPEDIA_NAMESPACE + this.uri;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
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
	 * @return the prior
	 */
	public double getPrior() {
		return prior;
	}

	/**
	 * @param prior the prior to set
	 */
	public void setPrior(double prior) {
		this.prior = prior;
	}

	/**
	 * @return the types
	 */
	public List<OntologyType> getTypes() {
		return types;
	}

	/**
	 * @param types the types to set
	 */
	public void setTypes(List<OntologyType> types) {
		this.types = types;
	}


}


