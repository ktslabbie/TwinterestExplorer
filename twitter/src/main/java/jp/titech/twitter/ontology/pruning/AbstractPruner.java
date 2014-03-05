/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		17 dec. 2012
 */
package jp.titech.twitter.ontology.pruning;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.FreebaseType;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

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
	
	protected int dbpediaTotal = 0, schemaOrgTotal = 0, freebaseTotal = 0, yagoTotal = 0, categoryTotal = 0;
	protected int dbpediaThreshold, schemaOrgThreshold, freebaseThreshold, yagoThreshold, categoryThreshold;
	
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
		
		this.divideOntologyTypes();
	}
	
	/**
	 * 
	 */
	protected void divideOntologyTypes() {
		for (OntologyType type : fullOntology.keySet()) {
			int cardinality = fullOntology.get(type);
			if(type instanceof Category){
				categories.put((Category) type, cardinality);
				categoryTotal += cardinality;
			} else if(type instanceof YAGOType){
				yagoTypes.put((YAGOType) type, cardinality);
				yagoTotal += cardinality;
			} else if(type instanceof FreebaseType){
				freebaseTypes.put((FreebaseType) type, cardinality);
				freebaseTotal += cardinality;
			} else if(type instanceof DBpediaType){
				dbpediaTypes.put((DBpediaType) type, cardinality);
				dbpediaTotal += cardinality;
			} else if(type instanceof SchemaOrgType){
				schemaOrgTypes.put((SchemaOrgType) type, cardinality);
				schemaOrgTotal += cardinality;
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
	
	/**
	 * @param prunedFullOntology
	 * @return
	 */
	public String printFullMapTSV(String fileName) {
		
		Map<DBpediaType, Integer> dbpediaSortedMap = Util.sortByValue(dbpediaTypes);
		Map<YAGOType, Integer> yagoSortedMap = Util.sortByValue(yagoTypes);
		Map<SchemaOrgType, Integer> schemaSortedMap = Util.sortByValue(schemaOrgTypes);
		Map<FreebaseType, Integer> freebaseSortedMap = Util.sortByValue(freebaseTypes);
		Map<Category, Integer> categorySortedMap = Util.sortByValue(categories);
		
		String out = "DBpedia_type\tcount:\n";

		for (DBpediaType type : dbpediaSortedMap.keySet()) {
			out += type.typeID() + "\t" + dbpediaSortedMap.get(type) + "\n";
		}

		out += "\nYAGO_type\tcount:\n";

		for (YAGOType type : yagoSortedMap.keySet()) {
			out += type.typeID() + "\t" + yagoSortedMap.get(type) + "\n";
		}

		out += "\nSchema_type\tcount:\n";

		for (SchemaOrgType type : schemaSortedMap.keySet()) {
			out += type.typeID() + "\t" + schemaSortedMap.get(type) + "\n";
		}

		out += "\nFreebase_type\tcount:\n";

		for (FreebaseType type : freebaseSortedMap.keySet()) {
			out += type.typeID() + "\t" + freebaseSortedMap.get(type) + "\n";
		}

		out += "\nCategory\tcount:\n";

		for (Category type : categorySortedMap.keySet()) {
			out += type.typeID() + "\t" + categorySortedMap.get(type) + "\n";
		}

		Util.writeToFile(out, new File(Vars.DATA_DIRECTORY + fileName));

		return out;
	}
	
	/**
	 * @param prunedFullOntology
	 * @return
	 */
	public String printPrunedMapTSV(String fileName) {
		
		Map<DBpediaType, Integer> dbpediaSortedMap = Util.sortByValue(prunedDBpediaTypes);
		Map<YAGOType, Integer> yagoSortedMap = Util.sortByValue(prunedYAGOTypes);
		Map<SchemaOrgType, Integer> schemaSortedMap = Util.sortByValue(prunedSchemaOrgTypes);
		Map<FreebaseType, Integer> freebaseSortedMap = Util.sortByValue(prunedFreebaseTypes);
		Map<Category, Integer> categorySortedMap = Util.sortByValue(prunedCategories);
		
		String out = "DBpedia_type\tcount:\n";

		for (DBpediaType type : dbpediaSortedMap.keySet()) {
			out += type.typeID() + "\t" + dbpediaSortedMap.get(type) + "\n";
		}

		out += "\nYAGO_type\tcount:\n";

		for (YAGOType type : yagoSortedMap.keySet()) {
			out += type.typeID() + "\t" + yagoSortedMap.get(type) + "\n";
		}

		out += "\nSchema_type\tcount:\n";

		for (SchemaOrgType type : schemaSortedMap.keySet()) {
			out += type.typeID() + "\t" + schemaSortedMap.get(type) + "\n";
		}

		out += "\nFreebase_type\tcount:\n";

		for (FreebaseType type : freebaseSortedMap.keySet()) {
			out += type.typeID() + "\t" + freebaseSortedMap.get(type) + "\n";
		}

		out += "\nCategory\tcount:\n";

		for (Category type : categorySortedMap.keySet()) {
			out += type.typeID() + "\t" + categorySortedMap.get(type) + "\n";
		}

		Util.writeToFile(out, new File(Vars.DATA_DIRECTORY + fileName));

		return out;
	}
}
