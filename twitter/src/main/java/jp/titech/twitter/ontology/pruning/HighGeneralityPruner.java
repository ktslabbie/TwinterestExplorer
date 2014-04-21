/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		17 dec. 2012
 */
package jp.titech.twitter.ontology.pruning;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.ontology.dbpedia.DBpediaQuery;
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
public class HighGeneralityPruner implements Pruner {

	private UserOntology originalUserOntology, userOntology;
	private UserOntology prunedUserOntology = new UserOntology();
	
	private int dbpediaPercentage = Vars.DBPEDIA_HIGH_GENERALITY_PRUNING_RATE;
	private int schemaPercentage = Vars.SCHEMA_HIGH_GENERALITY_PRUNING_RATE;
	private int categoryPercentage = Vars.CATEGORY_HIGH_GENERALITY_PRUNING_RATE;
	private int yagoPercentage = Vars.YAGO_HIGH_GENERALITY_PRUNING_RATE;
	
	private boolean multiOntologyProcessing = false;

	public HighGeneralityPruner(UserOntology userOntology) {
		this.userOntology = userOntology;
	}

	public HighGeneralityPruner(UserOntology originalUserOntology, UserOntology userOntology) {
		this.originalUserOntology = originalUserOntology;
		this.userOntology = userOntology;
		this.multiOntologyProcessing = true;
	}

	public void prune() {

		DBpediaQuery dbpediaQuery = DBpediaQuery.getInstance();
		
		Log.getLogger().info("High-generality pruning DBpedia types...");

		for(DBpediaType type : userOntology.getDBpediaTypes().keySet()) {
			int superClassCount = userOntology.getDBpediaTypes().get(type);
			int subClassCount = 0;
			Set<DBpediaType> subClasses = dbpediaQuery.getDirectDBpediaSubclasses(type);
			
			for (DBpediaType subDBpediaType : subClasses) {
				
				if(subDBpediaType.equals(type)) continue;
				if(multiOntologyProcessing)
					if(originalUserOntology.contains(subDBpediaType))
						subClassCount += originalUserOntology.getOccurrences(subDBpediaType);
				else
					if(userOntology.contains(subDBpediaType))
						subClassCount += userOntology.getOccurrences(subDBpediaType);
			}
				
			double ratio = ((double) subClassCount / (double) superClassCount);
			
			//Log.getLogger().info("Parent/child count ratio for " + type.typeID() + ": " + ratio*100 + "%");
			
			if((ratio*100) < dbpediaPercentage) {
				//Log.getLogger().info("Lower than " + dbpediaPercentage + "% so it stays!");
				prunedUserOntology.addDBpediaType(type, userOntology.getOccurrences(type));
			} //else
				//Log.getLogger().info("Higher than " + dbpediaPercentage + "% so it's gone!");
		}

		prunedUserOntology.setSchemaOrgTypes(userOntology.getSchemaOrgTypes());
		prunedUserOntology.setFreebaseTypes(userOntology.getFreebaseTypes());

		Log.getLogger().info("High-generality pruning YAGO types...");

		for(YAGOType type : userOntology.getYAGOTypes().keySet()){
			int superClassCount = userOntology.getOccurrences(type);
			int subClassCount = 0;
			Set<YAGOType> subClasses = dbpediaQuery.getDirectYAGOSubclasses(type);
			
			for (YAGOType subYAGOType : subClasses) {

				if(multiOntologyProcessing) {
					if(originalUserOntology.contains(subYAGOType))
						subClassCount += originalUserOntology.getOccurrences(subYAGOType);
				} else {
					if(userOntology.contains(subYAGOType))
						subClassCount += userOntology.getOccurrences(subYAGOType);
				}
			}

			double ratio = ((double)subClassCount / (double)superClassCount);
			
			//Log.getLogger().info("Parent/child count ratio for " + type.typeID() + ": " + ratio*100 + "%");
			
			if((ratio*100) < yagoPercentage) {
				//Log.getLogger().info("Lower than " + yagoPercentage + "% so it stays!");
				prunedUserOntology.addYAGOType(type, userOntology.getOccurrences(type));
			} else {
				//Log.getLogger().info("Higher than " + yagoPercentage + "% so it's gone!");
			}
		}

		/*Log.getLogger().info("High-generality pruning categories...");

		for(Category type : categories.keySet()){
			int superCategoryCount = categories.get(type);
			int subCategoryCount = 0;
			Set<Category> subCategories = dbpediaQuery.getDirectSubcategories(type);
			for (Category subCategory : subCategories) {

				if(multiOntologyProcessing){
					if(preLOPcategories.containsKey(subCategory)){
						subCategoryCount += preLOPcategories.get(subCategory);
					} 
				} else {
					if(categories.containsKey(subCategory)){
						subCategoryCount += categories.get(subCategory);
					}
				}

			}
			double ratio = ((double)subCategoryCount / (double)superCategoryCount);
			Log.getLogger().info("Parent/child count ratio for " + type.typeID() + ": " + ratio*100 + "%");
			if((ratio*100) < categoryPercentage){
				//Log.getLogger().info("Lower than " + categoryPercentage + "% so it stays!");
				prunedCategories.put(type, categories.get(type));
			} else {
				Log.getLogger().info("Higher than " + categoryPercentage + "% so it's gone!");
			}
		}*/

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
	}

	public UserOntology getPrunedUserOntology(){
		return prunedUserOntology;
	}
}
