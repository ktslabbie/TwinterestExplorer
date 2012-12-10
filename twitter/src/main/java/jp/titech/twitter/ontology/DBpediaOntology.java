/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		25 okt. 2012
 */
package jp.titech.twitter.ontology;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.titech.twitter.control.Controller;
import jp.titech.twitter.ontology.repository.DBpediaOntologyRepository;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.OntologyType;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class DBpediaOntology {

	private static DBpediaOntology instance;

	private DBpediaOntologyRepository dbpediaOntologyRepository;

	public DBpediaOntology() {
		dbpediaOntologyRepository = DBpediaOntologyRepository.getInstance();
	}

	public static DBpediaOntology getInstance(){
		if(instance == null){
			instance = new DBpediaOntology();
		}
		return instance;
	}

	/**
	 * @param types 
	 * @return
	 */
	public Map<OntologyType, Integer> getAncestors(Map<OntologyType, Integer> types) {

		Map<OntologyType, Integer> ancestorTypes = new HashMap<OntologyType, Integer>();

		for(Iterator<OntologyType> typeIt = types.keySet().iterator(); typeIt.hasNext();){
			OntologyType type = typeIt.next();
			
			Log.getLogger().info("Type: " + type);

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

		return ancestorTypes;
	}
}
