/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		25 okt. 2012
 */
package jp.titech.twitter.ontology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.titech.twitter.data.AnnotatedTweet;
import jp.titech.twitter.ontology.repository.DBpediaOntologyRepository;
import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

import org.dbpedia.spotlight.model.DBpediaResourceOccurrence;
import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.OntologyType;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class DBpediaQuery {

	private static DBpediaQuery instance;

	private boolean remote;
	private DBpediaOntologyRepository dbpediaOntologyRepository;

	public DBpediaQuery() {
		remote = Vars.DBPEDIA_REMOTE;
		if(!remote)	dbpediaOntologyRepository = DBpediaOntologyRepository.getInstance();
	}

	public static DBpediaQuery getInstance(){
		if(instance == null){
			instance = new DBpediaQuery();
		}
		return instance;
	}

	/**
	 * @param aTweet
	 */
	public void collectYAGOClasses(AnnotatedTweet aTweet) {

		Map<YAGOType, Integer> yagoTypes = new HashMap<YAGOType, Integer>();

		List<DBpediaResourceOccurrence> occs = aTweet.getBestCandidates();

		for(Iterator<DBpediaResourceOccurrence> occIt = occs.iterator(); occIt.hasNext();){
			DBpediaResourceOccurrence occ = occIt.next();
			String resourceURI = occ.resource().getFullUri();
			
			String query = Vars.SPARQL_PREFIXES + Util.readFile(Vars.SPARQL_SCRIPT_DIRECTORY + "collect_yago.sparql").replaceAll("%URI%", resourceURI);
			
			Log.getLogger().info("Querying local repository for YAGO types...");
			List<BindingSet> bindingList = dbpediaOntologyRepository.query(query);
			Log.getLogger().info("Done!");

			for (BindingSet bindingSet : bindingList) {
				Value value = bindingSet.getValue("yago");
				String stringValue = value.stringValue();
				//Log.getLogger().info("Class found: " + stringValue);

				if(stringValue.contains("class/yago/")){
					YAGOType newType = new YAGOType(stringValue.split("class/yago/")[1]);
					if(yagoTypes.get(newType) != null) {
						yagoTypes.put(newType, yagoTypes.get(newType) + 1);
					} else {
						yagoTypes.put(newType, 1);
					}
				}
			}
		}
		aTweet.setYAGOTypes(yagoTypes);
	}

	/**
	 * @param aTweet
	 */
	public void collectCategories(AnnotatedTweet aTweet) {
		Map<Category, Integer> categories = new HashMap<Category, Integer>();

		List<DBpediaResourceOccurrence> occs = aTweet.getBestCandidates();

		for(Iterator<DBpediaResourceOccurrence> occIt = occs.iterator(); occIt.hasNext();){
			DBpediaResourceOccurrence occ = occIt.next();
			String resourceURI = occ.resource().getFullUri();
			
			String query = Vars.SPARQL_PREFIXES + Util.readFile(Vars.SPARQL_SCRIPT_DIRECTORY + "collect_categories.sparql").replaceAll("%URI%", resourceURI);
			
			Log.getLogger().info("Querying local repository for categories...");
			List<BindingSet> bindingList = dbpediaOntologyRepository.query(query);
			Log.getLogger().info("Done!");

			for (BindingSet bindingSet : bindingList) {
				Value value = bindingSet.getValue("category");
				String stringValue = value.stringValue();
				//Log.getLogger().info("Class found: " + stringValue);

				if(stringValue.contains("resource/Category:")){
					Category newType = new Category(stringValue.split("resource/Category:")[1]);

					if(categories.get(newType) != null) {
						categories.put(newType, categories.get(newType) + 1);
					} else {
						categories.put(newType, 1);
					}
				}
			}
		}
		aTweet.setCategories(categories);
	}
	
	/**
	 * @param type
	 * @return
	 */
	public Set<DBpediaType> getDirectDBpediaSubclasses(DBpediaType type) {
		String typeURI = type.getFullUri();
		Set<DBpediaType> subClasses = new HashSet<DBpediaType>();
		Log.getLogger().info("Getting direct subclasses for " + type.typeID() + "...");
		String query = Vars.SPARQL_PREFIXES + "SELECT ?dbpedia WHERE { ?dbpedia rdfs:subClassOf <" + typeURI + "> }";
		
		List<BindingSet> bindingList = dbpediaOntologyRepository.query(query);
		
		for (BindingSet bindingSet : bindingList) {
			Value value = bindingSet.getValue("dbpedia");
			String stringValue = value.stringValue();
			//Log.getLogger().info("Class found: " + stringValue);

			if(stringValue.contains("dbpedia.org/ontology/")){
				subClasses.add(new DBpediaType(stringValue.split("dbpedia.org/ontology/")[1]));
			}
		}
		return subClasses;
	}
	
	public Set<YAGOType> getDirectYAGOSubclasses(YAGOType type){
		String typeURI = type.getFullUri();
		Set<YAGOType> subClasses = new HashSet<YAGOType>();
		Log.getLogger().info("Getting direct subclasses for " + type.typeID() + "...");
		String query = Vars.SPARQL_PREFIXES + "SELECT ?yago WHERE { ?yago rdfs:suBClassOf <" + typeURI + "> }";
		List<BindingSet> bindingList = dbpediaOntologyRepository.query(query);
		
		for (BindingSet bindingSet : bindingList) {
			Value value = bindingSet.getValue("yago");
			String stringValue = value.stringValue();
			//Log.getLogger().info("Class found: " + stringValue);

			if(stringValue.contains("class/yago/")){
				subClasses.add(new YAGOType(stringValue.split("class/yago/")[1]));
			}
		}
		return subClasses;
	}
	
	public Set<Category> getDirectSubcategories(Category cat){
		String catURI = cat.getFullUri();
		Set<Category> subCategories = new HashSet<Category>();
		Log.getLogger().info("Getting direct subclasses for " + cat.typeID() + "...");
		String query = Vars.SPARQL_PREFIXES + "SELECT ?cat WHERE { ?cat skos:broader <" + catURI + "> }";
		List<BindingSet> bindingList = dbpediaOntologyRepository.query(query);
		
		for (BindingSet bindingSet : bindingList) {
			Value value = bindingSet.getValue("cat");
			String stringValue = value.stringValue();
			//Log.getLogger().info("Class found: " + stringValue);

			if(stringValue.contains("resource/Category:")){
				subCategories.add(new Category(stringValue.split("resource/Category:")[1]));
			}
		}
		return subCategories;
	}

	/**
	 * Execute a single, manually input test query.
	 * 
	 * @param string
	 */
	public void testQuery(String query){
		
		Log.getLogger().info("Querying with: \n" + Vars.SPARQL_PREFIXES + query);
		List<BindingSet> bindingList = dbpediaOntologyRepository.query(Vars.SPARQL_PREFIXES + query);

		for (BindingSet bindingSet : bindingList) {
			Log.getLogger().info("Result: " + bindingSet.toString());
		}
	}
}
