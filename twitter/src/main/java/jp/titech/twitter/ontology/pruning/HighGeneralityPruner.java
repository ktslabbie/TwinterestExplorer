/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		17 dec. 2012
 */
package jp.titech.twitter.ontology.pruning;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.titech.twitter.ontology.DBpediaQuery;
import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.FreebaseType;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.OntologyType;
import org.dbpedia.spotlight.model.SchemaOrgType;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class HighGeneralityPruner extends AbstractPruner {

	private int percentage = Vars.DBPEDIA_HIGH_GENERALITY_PRUNING_RATE;

	public HighGeneralityPruner(Map<OntologyType, Integer> tOntology){
		initMaps(tOntology);
	}

	public void prune() {

		this.divideOntologyTypes();
		
		DBpediaQuery dbpediaQuery = DBpediaQuery.getInstance();

		for(DBpediaType type : dbpediaTypes.keySet()){
			int superClassCount = dbpediaTypes.get(type);
			int subClassCount = 0;
			Set<DBpediaType> subClasses = dbpediaQuery.getDirectDBpediaSubclasses(type);
			for (DBpediaType subDBpediaType : subClasses) {
				if(subDBpediaType.equals(type)) continue;
				if(dbpediaTypes.containsKey(subDBpediaType)){
					subClassCount += dbpediaTypes.get(subDBpediaType);
					Log.getLogger().info("subClasaCount: " + subClassCount);
				}
			}
			double ratio = ((double)subClassCount / (double)superClassCount);
			Log.getLogger().info("Parent/child count ratio for " + type.typeID() + ": " + ratio*100 + "%");
			if((ratio*100) < percentage){
				Log.getLogger().info("Lower than " + percentage + "% so it stays!");
				prunedDBpediaTypes.put(type, dbpediaTypes.get(type));
			} else {
				Log.getLogger().info("Higher than " + percentage + "% so it's gone!");
			}
		}

		prunedSchemaOrgTypes.putAll(schemaOrgTypes);
		prunedFreebaseTypes.putAll(freebaseTypes);

		Log.getLogger().info("High-generality pruning YAGO types...");

		for(YAGOType type : yagoTypes.keySet()){
			int superClassCount = yagoTypes.get(type);
			int subClassCount = 0;
			Set<YAGOType> subClasses = dbpediaQuery.getDirectYAGOSubclasses(type);
			for (YAGOType subYAGOType : subClasses) {
				if(yagoTypes.containsKey(subYAGOType)){
					subClassCount += yagoTypes.get(subYAGOType);
				}
			}
			
			double ratio = ((double)subClassCount / (double)superClassCount);
			Log.getLogger().info("Parent/child count ratio for " + type.typeID() + ": " + ratio*100 + "%");
			if((ratio*100) < percentage){
				Log.getLogger().info("Lower than " + percentage + "% so it stays!");
				prunedYAGOTypes.put(type, yagoTypes.get(type));
			} else {
				Log.getLogger().info("Higher than " + percentage + "% so it's gone!");
			}
		}

		Log.getLogger().info("High-generality pruning categories...");

		for(Category type : categories.keySet()){
			int superCategoryCount = categories.get(type);
			int subCategoryCount = 0;
			Set<Category> subCategories = dbpediaQuery.getDirectSubcategories(type);
			for (Category subCategory : subCategories) {
				if(categories.containsKey(subCategory)){
					subCategoryCount += categories.get(subCategory);
				}
			}
			double ratio = ((double)subCategoryCount / (double)superCategoryCount);
			Log.getLogger().info("Parent/child count ratio for " + type.typeID() + ": " + ratio*100 + "%");
			if((ratio*100) < percentage){
				Log.getLogger().info("Lower than " + percentage + "% so it stays!");
				prunedCategories.put(type, categories.get(type));
			} else {
				Log.getLogger().info("Higher than " + percentage + "% so it's gone!");
			}
		}

		/*ArrayList<Integer> vals = new ArrayList<Integer>(categories.values());
		Collections.sort(vals);
		Collections.reverse(vals);

		for (Category category : categories.keySet()) {
			for(int i = 0; i < Vars.CATEGORY_TOP_K; i++){
				if(categories.get(category).equals(vals.get(i))){
					Log.getLogger().info("Category " + category.typeID() + " in top 10 occurrences (" + vals.get(i) + "). Keeping.");
					prunedCategories.put(category, vals.get(i));
				}
			}
		}*/
		
		prunedFullOntology.putAll(prunedDBpediaTypes);
		prunedFullOntology.putAll(prunedSchemaOrgTypes);
		prunedFullOntology.putAll(prunedFreebaseTypes);
		prunedFullOntology.putAll(prunedYAGOTypes);
		prunedFullOntology.putAll(prunedCategories);
	}

	/**
	 * @param prunedFullOntology
	 * @return
	 */
	public String printMapCSV() {
		String out = "DBpedia_type,count:\n";

		for (DBpediaType type : prunedDBpediaTypes.keySet()) {
			out += type.typeID() + "," + prunedDBpediaTypes.get(type) + "\n";
		}

		out += "\nYAGO_type,count:\n";

		for (YAGOType type : prunedYAGOTypes.keySet()) {
			out += type.typeID() + "," + prunedYAGOTypes.get(type) + "\n";
		}

		out += "\nSchema_type,count:\n";

		for (SchemaOrgType type : prunedSchemaOrgTypes.keySet()) {
			out += type.typeID() + "," + prunedSchemaOrgTypes.get(type) + "\n";
		}
		
		out += "\nFreebase_type,count:\n";

		for (FreebaseType type : prunedFreebaseTypes.keySet()) {
			out += type.typeID() + "," + prunedFreebaseTypes.get(type) + "\n";
		}
		
		out += "\nCategory,count:\n";

		for (Category type : prunedCategories.keySet()) {
			out += type.typeID() + "," + prunedCategories.get(type) + "\n";
		}
		
		Util.writeToFile(out, new File(Vars.DATA_DIRECTORY + "type_output.csv"));

		return out;
	}
	
	@Override
	public Map<OntologyType, Integer> getPrunedFullOntology(){
		if(!prunedFullOntology.isEmpty()) return prunedFullOntology;
		else {
			prune();
			return prunedFullOntology;
		}
	}
}
