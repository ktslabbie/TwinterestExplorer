/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		17 dec. 2012
 */
package jp.titech.twitter.ontology.pruning;

import java.util.HashMap;
import java.util.Map;

import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.FreebaseType;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.OntologyType;
import org.dbpedia.spotlight.model.SchemaOrgType;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class LowOccurrencePruner implements Pruner {

	private UserOntology userOntology;
	private UserOntology prunedUserOntology = new UserOntology();

	private int dbpediaThreshold, schemaOrgThreshold, freebaseThreshold, yagoThreshold, categoryThreshold;

	public LowOccurrencePruner(UserOntology userOntology){
		this.userOntology = userOntology;
	}

	public void prune() {

		dbpediaThreshold = (int)Math.round((userOntology.getDBpediaTypeCount() / (100 / Vars.DBPEDIA_LOW_OCCURRENCE_PRUNING_RATE)));
		Log.getLogger().info("Total number of DBpedia type occurrences: " + userOntology.getDBpediaTypeCount() + ". Low-occurrence pruning threshold: " + dbpediaThreshold);

		schemaOrgThreshold = (int)Math.round((userOntology.getSchemaOrgTypeCount() / (100 / Vars.SCHEMA_LOW_OCCURRENCE_PRUNING_RATE)));
		Log.getLogger().info("Total number of Schema.org type occurrences: " + userOntology.getSchemaOrgTypeCount() + ". Low-occurrence pruning threshold: " + schemaOrgThreshold);

		freebaseThreshold = (int)Math.round((userOntology.getFreebaseTypeCount() / (100 / Vars.FREEBASE_LOW_OCCURRENCE_PRUNING_RATE)));
		Log.getLogger().info("Total number of Freebase type occurrences: " + userOntology.getFreebaseTypeCount() + ". Low-occurrence pruning threshold: " + freebaseThreshold);

		yagoThreshold = (int)Math.round((userOntology.getYAGOTypeCount() / (100 / Vars.YAGO_LOW_OCCURRENCE_PRUNING_RATE)));
		Log.getLogger().info("Total number of YAGO type occurrences: " + userOntology.getYAGOTypeCount() + ". Low-occurrence pruning threshold: " + yagoThreshold);

		categoryThreshold = (int)Math.round((userOntology.getCategoryCount() / (100 / Vars.CATEGORY_LOW_OCCURRENCE_PRUNING_RATE)));
		Log.getLogger().info("Total number of Category type occurrences: " + userOntology.getCategoryCount() + ". Low-occurrence pruning threshold: " + categoryThreshold);

		for(DBpediaType type : userOntology.getDBpediaTypes().keySet()){
			int cardinality = userOntology.getDBpediaTypes().get(type);
			if(cardinality >= dbpediaThreshold)	prunedUserOntology.addDBpediaType(type, cardinality);
		}

		for(SchemaOrgType type : userOntology.getSchemaOrgTypes().keySet()){
			int cardinality = userOntology.getSchemaOrgTypes().get(type);
			if(cardinality >= schemaOrgThreshold) prunedUserOntology.addSchemaOrgType(type, cardinality);
		}

		for(FreebaseType type : userOntology.getFreebaseTypes().keySet()){
			int cardinality = userOntology.getFreebaseTypes().get(type);
			if(cardinality >= freebaseThreshold) prunedUserOntology.addFreebaseType(type, cardinality);
		}

		for(YAGOType type : userOntology.getYAGOTypes().keySet()){
			int cardinality = userOntology.getYAGOTypes().get(type);
			if(cardinality >= yagoThreshold) prunedUserOntology.addYAGOType(type, cardinality);
		}

		for(Category type : userOntology.getCategories().keySet()){
			int cardinality = userOntology.getCategories().get(type);
			if(cardinality >= categoryThreshold) prunedUserOntology.addCategory(type, cardinality);
		}
	}

	public UserOntology getPrunedUserOntology() {
		return prunedUserOntology;
	}
}
