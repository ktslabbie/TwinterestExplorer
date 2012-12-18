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

import jp.titech.twitter.ontology.DBpediaQuery;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
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
import org.openrdf.sail.nativerdf.NativeStore;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class DBpediaOntologyRepository {

	private static DBpediaOntologyRepository instance;
	
	private Repository dbpediaOntologyRepository;
	
	/**
	 * Constructor with default directory.
	 * 
	 * TODO: something about the inferencing thing.
	 */
	public DBpediaOntologyRepository() {
		File directory = new File(Vars.DBPEDIA_REPOSITORY_DIRECTORY);
		dbpediaOntologyRepository = new SailRepository(new ForwardChainingRDFSInferencer(new NativeStore(directory)));
		//dbpediaOntologyRepository = new SailRepository(new NativeStore(directory));
		try {
			dbpediaOntologyRepository.initialize();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Construct repository with set directory.
	 * @param directory the directory
	 */
	public DBpediaOntologyRepository(String directory) {
		dbpediaOntologyRepository = new SailRepository(new ForwardChainingRDFSInferencer(new NativeStore(new File(directory))));
		//dbpediaOntologyRepository = new SailRepository(new NativeStore(new File(directory)));
		try {
			dbpediaOntologyRepository.initialize();
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
	
	public static DBpediaOntologyRepository getInstance(String directory){
		if(instance == null){
			instance = new DBpediaOntologyRepository(directory);
		}
		return instance;
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

	/**
	 * @return
	 */
	public RepositoryConnection getConnection() {
		RepositoryConnection conn = null;
		try {
			conn = dbpediaOntologyRepository.getConnection();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return conn;
	}
}
