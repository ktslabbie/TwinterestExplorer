/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		1 nov. 2012
 */
package jp.titech.twitter.ontology.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.titech.twitter.ontology.DBpediaOntology;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class DBpediaOntologyRepository {

	private static DBpediaOntologyRepository instance;
	
	private Repository dbpediaOntologyRepository;
	
	/**
	 * 
	 */
	public DBpediaOntologyRepository() {
		dbpediaOntologyRepository = new SailRepository(new ForwardChainingRDFSInferencer(new MemoryStore()));
		try {
			dbpediaOntologyRepository.initialize();
			this.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	public static DBpediaOntologyRepository getInstance(){
		if(instance == null){
			instance = new DBpediaOntologyRepository();
		}
		return instance;
	}

	/**
	 * 
	 */
	private void initialize() {
		File file = new File(Vars.DBPEDIA_ONTOLOGY_FILE);
		String baseURI = "http://dbpedia.org/";

		try {
			RepositoryConnection con = dbpediaOntologyRepository.getConnection();
			
			try {
				con.add(file, baseURI, RDFFormat.RDFXML);
			}
			finally {
				con.close();
			}
		}
		catch (OpenRDFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param string
	 * @return
	 */
	public List<BindingSet> query(String query) {		
		List<BindingSet> bindingList = new ArrayList<BindingSet>();
		try {
			RepositoryConnection con = dbpediaOntologyRepository.getConnection();
			
			try {
				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
				TupleQueryResult result = tupleQuery.evaluate();
				try {
					
					while(result.hasNext()) {
						bindingList.add(result.next());
					}
				} catch (QueryEvaluationException e) {
					e.printStackTrace();
				}
			}
			finally {
				con.close();
			}
		}
		catch (OpenRDFException e) {
			e.printStackTrace();
		}
		return bindingList;
	}
}
