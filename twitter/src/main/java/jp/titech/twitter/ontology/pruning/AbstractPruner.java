/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		17 dec. 2012
 */
package jp.titech.twitter.ontology.pruning;

import java.util.HashMap;
import java.util.Map;

import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.FreebaseType;
import jp.titech.twitter.ontology.types.YAGOType;

import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.OntologyType;
import org.dbpedia.spotlight.model.SchemaOrgType;

/**
 * @author Kristian Slabbekoorn
 *
 */
public abstract class AbstractPruner {
	protected Map<OntologyType, Integer> fullOntology, prunedFullOntology;
	protected Map<DBpediaType, Integer> dbpediaTypes, prunedDBpediaTypes;
	protected Map<SchemaOrgType, Integer> schemaOrgTypes, prunedSchemaOrgTypes;
	protected Map<FreebaseType, Integer> freebaseTypes, prunedFreebaseTypes;
	protected Map<YAGOType, Integer> yagoTypes, prunedYAGOTypes;
	protected Map<Category, Integer> categories, prunedCategories;
	
	protected void initMaps(Map<OntologyType, Integer> tOntology){
		fullOntology = tOntology;
		prunedFullOntology = new HashMap<OntologyType, Integer>();
		this.dbpediaTypes = new HashMap<DBpediaType, Integer>();
		this.prunedDBpediaTypes = new HashMap<DBpediaType, Integer>();
		this.schemaOrgTypes = new HashMap<SchemaOrgType, Integer>();
		this.prunedSchemaOrgTypes = new HashMap<SchemaOrgType, Integer>();
		this.freebaseTypes = new HashMap<FreebaseType, Integer>();
		this.prunedFreebaseTypes = new HashMap<FreebaseType, Integer>();
		this.yagoTypes = new HashMap<YAGOType, Integer>();
		this.prunedYAGOTypes = new HashMap<YAGOType, Integer>();
		this.categories = new HashMap<Category, Integer>();
		this.prunedCategories = new HashMap<Category, Integer>();
	}
	
	/**
	 * 
	 */
	protected void divideOntologyTypes() {
		for (OntologyType type : fullOntology.keySet()) {
			int cardinality = fullOntology.get(type);
			if(type instanceof Category){
				categories.put((Category) type, cardinality);
			} else if(type instanceof YAGOType){
				yagoTypes.put((YAGOType) type, cardinality);
			} else if(type instanceof FreebaseType){
				freebaseTypes.put((FreebaseType) type, cardinality);
			} else if(type instanceof DBpediaType){
				dbpediaTypes.put((DBpediaType) type, cardinality);
			} else if(type instanceof SchemaOrgType){
				schemaOrgTypes.put((SchemaOrgType) type, cardinality);
			} 
		}
	}
	
	public Map<OntologyType, Integer> getPrunedFullOntology(){
		return prunedFullOntology;
	}

	/**
	 * @return the prunedDBpediaTypes
	 */
	public Map<DBpediaType, Integer> getPrunedDBpediaTypes() {
		return prunedDBpediaTypes;
	}

	/**
	 * @param prunedDBpediaTypes the prunedDBpediaTypes to set
	 */
	public void setPrunedDBpediaTypes(Map<DBpediaType, Integer> prunedDBpediaTypes) {
		this.prunedDBpediaTypes = prunedDBpediaTypes;
	}

	/**
	 * @return the prunedSchemaOrgTypes
	 */
	public Map<SchemaOrgType, Integer> getPrunedSchemaOrgTypes() {
		return prunedSchemaOrgTypes;
	}

	/**
	 * @param prunedSchemaOrgTypes the prunedSchemaOrgTypes to set
	 */
	public void setPrunedSchemaOrgTypes(
			Map<SchemaOrgType, Integer> prunedSchemaOrgTypes) {
		this.prunedSchemaOrgTypes = prunedSchemaOrgTypes;
	}

	/**
	 * @return the prunedFreebaseTypes
	 */
	public Map<FreebaseType, Integer> getPrunedFreebaseTypes() {
		return prunedFreebaseTypes;
	}

	/**
	 * @param prunedFreebaseTypes the prunedFreebaseTypes to set
	 */
	public void setPrunedFreebaseTypes(
			Map<FreebaseType, Integer> prunedFreebaseTypes) {
		this.prunedFreebaseTypes = prunedFreebaseTypes;
	}

	/**
	 * @return the prunedYAGOTypes
	 */
	public Map<YAGOType, Integer> getPrunedYAGOTypes() {
		return prunedYAGOTypes;
	}

	/**
	 * @param prunedYAGOTypes the prunedYAGOTypes to set
	 */
	public void setPrunedYAGOTypes(Map<YAGOType, Integer> prunedYAGOTypes) {
		this.prunedYAGOTypes = prunedYAGOTypes;
	}

	/**
	 * @return the prunedCategories
	 */
	public Map<Category, Integer> getPrunedCategories() {
		return prunedCategories;
	}

	/**
	 * @param prunedCategories the prunedCategories to set
	 */
	public void setPrunedCategories(Map<Category, Integer> prunedCategories) {
		this.prunedCategories = prunedCategories;
	}

	/**
	 * @param prunedFullOntology the prunedFullOntology to set
	 */
	public void setPrunedFullOntology(Map<OntologyType, Integer> prunedFullOntology) {
		this.prunedFullOntology = prunedFullOntology;
	}
}
