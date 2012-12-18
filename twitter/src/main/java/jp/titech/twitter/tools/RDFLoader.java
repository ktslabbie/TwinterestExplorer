/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		13 dec. 2012
 */
package jp.titech.twitter.tools;

import java.io.File;
import java.io.IOException;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.ontology.repository.DBpediaOntologyRepository;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

import org.apache.log4j.PropertyConfigurator;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * Helper tool to load RDF files into a repository.
 * 
 * @author Kristian Slabbekoorn
 *
 */
public class RDFLoader {

	/*
	 *  Variables
	 */
	static final String RDF_DIRECTORY = Vars.DBPEDIA_RDF_DIRECTORY;					// Directory with RDF files
	static final String REPOSITORY_DIRECTORY = Vars.DBPEDIA_REPOSITORY_DIRECTORY;	// Directory to put the repository
	static final String BASE_URI = "http://dbpedia.org/";							// Base URI for repository graph
	static final RDFFormat RDF_FORMAT = RDFFormat.RDFXML;							// Format of RDF files
	static final String EXTENSION = "owl";											// Extension of RDF files
	
	static DBpediaOntologyRepository dbpr;

	public static void main( String[] args ) {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting loader.");
		dbpr = DBpediaOntologyRepository.getInstance(REPOSITORY_DIRECTORY);

		File directory = new File(RDF_DIRECTORY);
		File[] files = directory.listFiles();
		addRDF(files, BASE_URI, RDF_FORMAT);
	}

	/**
	 * @param file
	 * @param baseURI
	 * @param rdfFormat 
	 */
	private static void addRDF(File[] files, String baseURI, RDFFormat rdfFormat) {
		RepositoryConnection con = null;
		try {
			con = dbpr.getConnection();
			try {
				for (File file : files) {
					if(Util.getExtension(file).equals(EXTENSION)){
						Log.getLogger().info("Adding " + file.getName() + " to repository...");
						con.add(file, baseURI, rdfFormat);
					}
				}
			} finally {
				con.close();
				Log.getLogger().info("All files loaded successfully!");
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
