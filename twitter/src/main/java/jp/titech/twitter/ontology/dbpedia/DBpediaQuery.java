/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		25 okt. 2012
 */
package jp.titech.twitter.ontology.dbpedia;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jp.titech.twitter.data.UserOntology;
import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.DBpediaType;
import jp.titech.twitter.ontology.types.YAGOType;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;
import jp.titech.twitter.ontology.dbpedia.DBpediaResourceOccurrence;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class DBpediaQuery {

	private static DBpediaQuery instance;
	private String yagoQueryFile = Vars.SPARQL_PREFIXES + Util.readFile(Vars.SPARQL_SCRIPT_DIRECTORY + "collect_yago_lite.sparql");

	private boolean remote;
	private DBpediaOntologyRepository dbpediaOntologyRepository;

	public DBpediaQuery() {
		remote = Vars.DBPEDIA_REMOTE;
		if(!remote)	dbpediaOntologyRepository = DBpediaOntologyRepository.getInstance();
	}

	public void collectClasses(List<DBpediaResourceOccurrence> occs, UserOntology userOntology) {
		for(Iterator<DBpediaResourceOccurrence> occIt = occs.iterator(); occIt.hasNext();) {
			DBpediaResourceOccurrence occ = occIt.next();
			String resourceURI = occ.getResource().getFullUri();
			String yagoQuery = "";
			try {
				yagoQuery = yagoQueryFile.replaceAll("%URI%", resourceURI);
			} catch(IndexOutOfBoundsException e) {
				resourceURI = resourceURI.replaceAll("\\$", "|");
				yagoQuery = yagoQueryFile.replaceAll("%URI%", resourceURI);
				yagoQuery = yagoQuery.replaceAll("|", "\\$");
			}
			//String categoryQuery = Vars.SPARQL_PREFIXES + Util.readFile(Vars.SPARQL_SCRIPT_DIRECTORY + "collect_categories.sparql").replaceAll("%URI%", resourceURI);

			this.collectYAGOTypes(yagoQuery, userOntology);
			//this.collectCategories(categoryQuery, userOntology);
		}
	}

	private void collectYAGOTypes(String query, UserOntology userOntology) {
		List<BindingSet> bindingList = dbpediaOntologyRepository.query(query);

		for (BindingSet bindingSet : bindingList) {
			Value value = bindingSet.getValue("yago");
			String stringValue = value.stringValue();

			if(stringValue.contains("class/yago/"))
				userOntology.addClass(new YAGOType(stringValue.split("class/yago/")[1]));
		}
	}

	/**
	 * @param aTweet
	 */
	public void collectCategories(String query, UserOntology userOntology) {
		List<BindingSet> bindingList = dbpediaOntologyRepository.query(query);

		for (BindingSet bindingSet : bindingList) {
			Value value = bindingSet.getValue("category");
			String stringValue = value.stringValue();

			if(stringValue.contains("resource/Category:"))
				userOntology.addClass(new Category(stringValue.split("resource/Category:")[1]));					
		}

	}

	/**
	 * @param type
	 * @return
	 */
	public Set<DBpediaType> getDirectDBpediaSubclasses(DBpediaType type) {
		String typeURI = type.getFullUri();
		Set<DBpediaType> subClasses = new HashSet<DBpediaType>();
		//Log.getLogger().info("Getting direct subclasses for " + type.typeID() + "...");
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
		//Log.getLogger().info("Getting direct subclasses for " + type.typeID() + "...");
		String query = Vars.SPARQL_PREFIXES + "SELECT ?yago WHERE { ?yago rdfs:subClassOf <" + typeURI + "> }";
		List<BindingSet> bindingList = dbpediaOntologyRepository.query(query);
		int count = 0;

		for (BindingSet bindingSet : bindingList) {
			Value value = bindingSet.getValue("yago");
			String stringValue = value.stringValue();
			//Log.getLogger().info("Class found: " + stringValue);

			if(stringValue.contains("class/yago/") && !stringValue.contains(type.typeID().split(":")[1])){
				subClasses.add(new YAGOType(stringValue.split("class/yago/")[1]));
				//Log.getLogger().info("Class type ID: " + type.typeID());
				count++;
			}
		}
		//Log.getLogger().info(count + " direct YAGO subclasses found.");

		return subClasses;
	}

	public Set<YAGOType> getDirectYAGOSuperclasses(YAGOType type){
		String typeURI = type.getFullUri();
		Set<YAGOType> superClasses = new HashSet<YAGOType>();
		//Log.getLogger().info("Getting direct superclasses for " + type.typeID() + "...");
		String query = Vars.SPARQL_PREFIXES + "SELECT DISTINCT ?yago WHERE { <" + typeURI + "> rdfs:subClassOf ?yago }";
		List<BindingSet> bindingList = dbpediaOntologyRepository.query(query);
		int count = 0;

		for (BindingSet bindingSet : bindingList) {
			Value value = bindingSet.getValue("yago");
			String stringValue = value.stringValue();
			Log.getLogger().info("Class found: " + stringValue);

			if(stringValue.contains("class/yago/") && !stringValue.contains(type.typeID().split(":")[1])){
				superClasses.add(new YAGOType(stringValue.split("class/yago/")[1]));
				//Log.getLogger().info("Class type ID: " + type.typeID());
				count++;
			}
		}
		//Log.getLogger().info(count + " direct YAGO subclasses found.");

		return superClasses;
	}

	public int getYAGODistance(YAGOType v, YAGOType w) {
		Log.getLogger().info("Getting YAGO ontology distance (hops) between classes " + v.typeID() + " and " + w.typeID() + "...");

		Set<YAGOType> vSet = new HashSet<YAGOType>(),
				wSet = new HashSet<YAGOType>();

		vSet.add(v); wSet.add(w);
		int distance = getYAGODistance(vSet, wSet, 0);
		Log.getLogger().info("Distance: " + distance);
		return distance;
	}

	public int getYAGODistance(Set<YAGOType> vSet, Set<YAGOType> wSet, int distance) {

		for (YAGOType v : vSet)
			if(wSet.contains(v))
				return distance;

		distance++;
		Log.getLogger().info("Current distance: " + distance);

		Set<YAGOType> vSuperSet = new HashSet<YAGOType>(vSet);

		for (YAGOType v : vSet) {
			Log.getLogger().info("Adding direct superclasses for " + v.typeID());
			vSuperSet.addAll(getDirectYAGOSuperclasses(v));
		}


		for(YAGOType v : vSuperSet) {
			if(wSet.contains(v))
				return distance;
		}


		Set<YAGOType> wSuperSet = new HashSet<YAGOType>(wSet);

		for (YAGOType w : wSet)
			wSuperSet.addAll(getDirectYAGOSuperclasses(w));

		return getYAGODistance(vSuperSet, wSuperSet, distance);
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

	public static DBpediaQuery getInstance(){
		if(instance == null) instance = new DBpediaQuery();
		return instance;
	}
}
