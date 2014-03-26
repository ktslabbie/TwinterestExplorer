package jp.titech.twitter.data;

import java.util.HashMap;
import java.util.Map;

import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.FreebaseType;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Util;

import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.OntologyType;
import org.dbpedia.spotlight.model.SchemaOrgType;

/**
 * Data class to store user ontologies (ontology types with their occurrence numbers).
 * 
 * @author Kristian
 *
 */
public class UserOntology {

	private Map<OntologyType, Integer> ontology  = new HashMap<OntologyType, Integer>();
	private Map<DBpediaType, Integer> dbpediaTypes = new HashMap<DBpediaType, Integer>();
	private Map<SchemaOrgType, Integer> schemaOrgTypes = new HashMap<SchemaOrgType, Integer>();
	private Map<FreebaseType, Integer> freebaseTypes = new HashMap<FreebaseType, Integer>();
	private Map<YAGOType, Integer> yagoTypes = new HashMap<YAGOType, Integer>();
	private Map<Category, Integer> categories = new HashMap<Category, Integer>();
	
	private int dbpediaTypeCount = 0, schemaOrgTypeCount = 0, freebaseTypeCount = 0, yagoTypeCount = 0, categoryCount = 0;
	
	public UserOntology() {}
	
	public UserOntology(Map<OntologyType, Integer> ontology) {
		this.ontology = ontology;
	}
	
	public boolean isEmpty() {
		return ontology.isEmpty();
	}

	public Map<OntologyType, Integer> getOntology() {
		return ontology;
	}

	public void setOntology(Map<OntologyType, Integer> ontology) {
		this.ontology = ontology;
	}
	
	public void addClass(OntologyType type) {
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
	public void addClass(OntologyType type, int cardinality) {
		this.ontology.put(type, cardinality);
		
		if(type instanceof Category){
			this.categories.put((Category) type, cardinality);
			this.categoryCount += cardinality;
		} else if(type instanceof YAGOType){
			this.yagoTypes.put((YAGOType) type, cardinality);
			this.yagoTypeCount += cardinality;
		} else if(type instanceof FreebaseType){
			this.freebaseTypes.put((FreebaseType) type, cardinality);
			this.freebaseTypeCount += cardinality;
		} else if(type instanceof DBpediaType){
			this.dbpediaTypes.put((DBpediaType) type, cardinality);
			this.dbpediaTypeCount += cardinality;
		} else if(type instanceof SchemaOrgType){
			this.schemaOrgTypes.put((SchemaOrgType) type, cardinality);
			this.schemaOrgTypeCount += cardinality;
		} 
	}
	
	/**
	 * Add a DBpedia type to the ontology.
	 * 
	 * @param type
	 * @param cardinality
	 */
	public void addDBpediaType(DBpediaType type, int cardinality) {
		this.ontology.put(type, cardinality);
		this.dbpediaTypes.put(type, cardinality);
	}
	
	/**
	 * Add a YAGO type to the ontology.
	 * 
	 * @param type
	 * @param cardinality
	 */
	public void addYAGOType(YAGOType type, int cardinality) {
		this.ontology.put(type, cardinality);
		this.yagoTypes.put(type, cardinality);
	}
	
	/**
	 * Add a Freebase type to the ontology.
	 * 
	 * @param type
	 * @param cardinality
	 */
	public void addFreebaseType(FreebaseType type, int cardinality) {
		this.ontology.put(type, cardinality);
		this.freebaseTypes.put(type, cardinality);
	}
	
	/**
	 * Add a Schema.org type to the ontology.
	 * 
	 * @param type
	 * @param cardinality
	 */
	public void addSchemaOrgType(SchemaOrgType type, int cardinality) {
		this.ontology.put(type, cardinality);
		this.schemaOrgTypes.put(type, cardinality);
	}
	
	/**
	 * Add a category to the ontology.
	 * 
	 * @param type
	 * @param cardinality
	 */
	public void addCategory(Category type, int cardinality) {
		this.ontology.put(type, cardinality);
		this.categories.put(type, cardinality);
	}
	
	public int getOccurrences(DBpediaType dbpediaType) {
		return dbpediaTypes.get(dbpediaType);
	}
	
	public int getOccurrences(SchemaOrgType schemaOrgType) {
		return schemaOrgTypes.get(schemaOrgType);
	}
	
	public int getOccurrences(FreebaseType freebaseType) {
		return freebaseTypes.get(freebaseType);
	}
	public int getOccurrences(YAGOType yagoType) {
		return yagoTypes.get(yagoType);
	}
	public int getOccurrences(Category category) {
		return categories.get(category);
	}
	
	public boolean contains(DBpediaType dbpediaType) {
		return dbpediaTypes.containsKey(dbpediaType);
	}
	
	public boolean contains(SchemaOrgType schemaOrgType) {
		return schemaOrgTypes.containsKey(schemaOrgType);
	}
	
	public boolean contains(FreebaseType freebaseType) {
		return freebaseTypes.containsKey(freebaseType);
	}
	public boolean contains(YAGOType yagoType) {
		return yagoTypes.containsKey(yagoType);
	}
	public boolean contains(Category category) {
		return categories.containsKey(category);
	}

	/**
	 * @return the dbpediaTypes
	 */
	public Map<DBpediaType, Integer> getDBpediaTypes() {
		return dbpediaTypes;
	}

	/**
	 * @param dbpediaTypes the dbpediaTypes to set
	 */
	public void setDBpediaTypes(Map<DBpediaType, Integer> dbpediaTypes) {
		this.dbpediaTypes = dbpediaTypes;
	}

	/**
	 * @return the schemaOrgTypes
	 */
	public Map<SchemaOrgType, Integer> getSchemaOrgTypes() {
		return schemaOrgTypes;
	}

	/**
	 * @param schemaOrgTypes the schemaOrgTypes to set
	 */
	public void setSchemaOrgTypes(Map<SchemaOrgType, Integer> schemaOrgTypes) {
		this.schemaOrgTypes = schemaOrgTypes;
	}

	/**
	 * @return the freebaseTypes
	 */
	public Map<FreebaseType, Integer> getFreebaseTypes() {
		return freebaseTypes;
	}

	/**
	 * @param freebaseTypes the freebaseTypes to set
	 */
	public void setFreebaseTypes(Map<FreebaseType, Integer> freebaseTypes) {
		this.freebaseTypes = freebaseTypes;
	}

	/**
	 * @return the yagoTypes
	 */
	public Map<YAGOType, Integer> getYAGOTypes() {
		return yagoTypes;
	}

	/**
	 * @param yagoTypes the yagoTypes to set
	 */
	public void setYAGOTypes(Map<YAGOType, Integer> yagoTypes) {
		this.yagoTypes = yagoTypes;
	}

	/**
	 * @return the categories
	 */
	public Map<Category, Integer> getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(Map<Category, Integer> categories) {
		this.categories = categories;
	}

	/**
	 * @return the dbpediaTypeCount
	 */
	public int getDBpediaTypeCount() {
		return dbpediaTypeCount;
	}

	/**
	 * @param dbpediaTypeCount the dbpediaTypeCount to set
	 */
	public void setDBpediaTypeCount(int dbpediaTypeCount) {
		this.dbpediaTypeCount = dbpediaTypeCount;
	}

	/**
	 * @return the schemaOrgTypeCount
	 */
	public int getSchemaOrgTypeCount() {
		return schemaOrgTypeCount;
	}

	/**
	 * @param schemaOrgTypeCount the schemaOrgTypeCount to set
	 */
	public void setSchemaOrgTypeCount(int schemaOrgTypeCount) {
		this.schemaOrgTypeCount = schemaOrgTypeCount;
	}

	/**
	 * @return the freebaseTypeCount
	 */
	public int getFreebaseTypeCount() {
		return freebaseTypeCount;
	}

	/**
	 * @param freebaseTypeCount the freebaseTypeCount to set
	 */
	public void setFreebaseTypeCount(int freebaseTypeCount) {
		this.freebaseTypeCount = freebaseTypeCount;
	}

	/**
	 * @return the yagoTypeCount
	 */
	public int getYAGOTypeCount() {
		return yagoTypeCount;
	}

	/**
	 * @param yagoTypeCount the yagoTypeCount to set
	 */
	public void setYAGOTypeCount(int yagoTypeCount) {
		this.yagoTypeCount = yagoTypeCount;
	}

	/**
	 * @return the categoryCount
	 */
	public int getCategoryCount() {
		return categoryCount;
	}

	/**
	 * @param categoryCount the categoryCount to set
	 */
	public void setCategoryCount(int categoryCount) {
		this.categoryCount = categoryCount;
	}
	
	/**
	 * Override the toString() method to provide a nice, sorted representation of the ontology. 
	 * 
	 * @return the string representation of the full ontology
	 */
	@Override
	public String toString() {
		
		Map<DBpediaType, Integer> dbpediaSortedMap 		= Util.sortByValue(this.dbpediaTypes);
		Map<YAGOType, Integer> yagoSortedMap 			= Util.sortByValue(this.yagoTypes);
		Map<SchemaOrgType, Integer> schemaSortedMap 	= Util.sortByValue(this.schemaOrgTypes);
		Map<FreebaseType, Integer> freebaseSortedMap 	= Util.sortByValue(this.freebaseTypes);
		Map<Category, Integer> categorySortedMap 		= Util.sortByValue(this.categories);
		
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

		return out;
	}
}
