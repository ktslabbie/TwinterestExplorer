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
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.OntologyType;
import org.dbpedia.spotlight.model.SchemaOrgType;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class LowOccurrencePruner extends AbstractPruner {
	
	private double dbpediaPercentage = Vars.DBPEDIA_LOW_OCCURRENCE_PRUNING_RATE;
	private double schemaPercentage = Vars.SCHEMA_LOW_OCCURRENCE_PRUNING_RATE;
	private double freebasePercentage = Vars.FREEBASE_LOW_OCCURRENCE_PRUNING_RATE;
	private double yagoPercentage = Vars.YAGO_LOW_OCCURRENCE_PRUNING_RATE;
	private double categoryPercentage = Vars.CATEGORY_LOW_OCCURRENCE_PRUNING_RATE;

	public LowOccurrencePruner(Map<OntologyType, Integer> tOntology){
		initMaps(tOntology);
	}

	public void prune() {
		
		dbpediaThreshold = (int)Math.round((dbpediaTotal / (100/dbpediaPercentage)));
		Log.getLogger().info("Total number of DBpedia type occurrences: " + dbpediaTotal + ". Low-occurrence pruning threshold: " + dbpediaThreshold);
		
		schemaOrgThreshold = (int)Math.round((schemaOrgTotal / (100/schemaPercentage)));
		Log.getLogger().info("Total number of Schema.org type occurrences: " + schemaOrgTotal + ". Low-occurrence pruning threshold: " + schemaOrgThreshold);
		
		freebaseThreshold = (int)Math.round((freebaseTotal / (100/freebasePercentage)));
		Log.getLogger().info("Total number of Freebase type occurrences: " + freebaseTotal + ". Low-occurrence pruning threshold: " + freebaseThreshold);
		
		yagoThreshold = (int)Math.round((yagoTotal / (100/yagoPercentage)));
		Log.getLogger().info("Total number of YAGO type occurrences: " + yagoTotal + ". Low-occurrence pruning threshold: " + yagoThreshold);
		
		categoryThreshold = (int)Math.round((categoryTotal / (100/categoryPercentage)));
		Log.getLogger().info("Total number of Category type occurrences: " + categoryTotal + ". Low-occurrence pruning threshold: " + categoryThreshold);
		
		for(DBpediaType type : dbpediaTypes.keySet()){
			int cardinality = dbpediaTypes.get(type);
			if(cardinality >= dbpediaThreshold)	prunedDBpediaTypes.put(type, cardinality);
		}
		
		for(SchemaOrgType type : schemaOrgTypes.keySet()){
			int cardinality = schemaOrgTypes.get(type);
			if(cardinality >= schemaOrgThreshold) prunedSchemaOrgTypes.put(type, cardinality);
		}
		
		for(FreebaseType type : freebaseTypes.keySet()){
			int cardinality = freebaseTypes.get(type);
			if(cardinality >= freebaseThreshold) prunedFreebaseTypes.put(type, cardinality);
		}
		
		for(YAGOType type : yagoTypes.keySet()){
			int cardinality = yagoTypes.get(type);
			if(cardinality >= yagoThreshold) prunedYAGOTypes.put(type, cardinality);
		}
		
		for(Category type : categories.keySet()){
			int cardinality = categories.get(type);
			if(cardinality >= categoryThreshold) prunedCategories.put(type, cardinality);
		}
		
		prunedFullOntology.putAll(prunedDBpediaTypes);
		prunedFullOntology.putAll(prunedSchemaOrgTypes);
		prunedFullOntology.putAll(prunedFreebaseTypes);
		prunedFullOntology.putAll(prunedYAGOTypes);
		prunedFullOntology.putAll(prunedCategories);
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
