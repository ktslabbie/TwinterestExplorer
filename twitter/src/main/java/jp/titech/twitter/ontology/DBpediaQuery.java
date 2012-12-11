/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		25 okt. 2012
 */
package jp.titech.twitter.ontology;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jp.titech.twitter.data.AnnotatedTweet;
import jp.titech.twitter.ontology.repository.DBpediaOntologyRepository;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

import org.dbpedia.spotlight.model.OntologyType;

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
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param aTweet
	 */
	public void collectCategories(AnnotatedTweet aTweet) {
		// TODO Auto-generated method stub
		
	}
	
	

	/**
	 * @param types 
	 * @return
	 *//*
	public Map<OntologyType, Integer> getAncestors(Map<OntologyType, Integer> types) {

		Map<OntologyType, Integer> ancestorTypes = new HashMap<OntologyType, Integer>();

		for(Iterator<OntologyType> typeIt = types.keySet().iterator(); typeIt.hasNext();){
			OntologyType type = typeIt.next();
			String typeID = type.typeID();
			
			Log.getLogger().info("Type: " + typeID);
			
			if(typeID.contains("DBpedia:") || typeID.contains("Freebase:") || typeID.contains("Schema:")){
				if(ancestorTypes.get(type) != null) {
					ancestorTypes.put(type, ancestorTypes.get(type) + 1);
				} else {
					ancestorTypes.put(type, 1);
				}
			} else {
				
				Log.getLogger().info("Impossible right now!? typeID: " + typeID);
				
				List<BindingSet> bindingList = dbpediaOntologyRepository.query(Vars.SPARQL_PREFIXES + " SELECT * WHERE { <"  + type.getFullUri() + "> rdfs:subClassOf ?x }");

				for (BindingSet bindingSet : bindingList) {
					Value value = bindingSet.getValue("x");
					String stringValue = value.stringValue();
					
					if(stringValue.contains("ontology")){
						OntologyType newType = new DBpediaType(stringValue);

						if(ancestorTypes.get(newType) != null) {
							ancestorTypes.put(newType, ancestorTypes.get(newType) + 1);
						} else {
							ancestorTypes.put(newType, 1);
						}
					}
				}
			}			
		}

		return ancestorTypes;
	}*/
}
