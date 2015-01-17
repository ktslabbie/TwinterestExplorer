/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		13 dec. 2012
 */
package jp.titech.twitter.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import jp.titech.twitter.config.Configuration;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;

import org.apache.log4j.PropertyConfigurator;
import org.openrdf.rio.RDFFormat;

/**
 * @author Kristian Slabbekoorn
 *
 * This tool is used to split huge files into manageable chunks by reading it line-by-line and saving into a new file after x lines.
 *
 */
public class FileSplitter {
	
	/*
	 * Variables
	 */
	static final String RDF_DIRECTORY = "../data/";									// Directory with RDF files
	static final String EXTENSION = "nt";											// Extension of RDF files
	static final int LINES_PER_FILE = 10000;										// Number of lines per split file
	
	public static void main( String[] args ) {
		PropertyConfigurator.configure(Configuration.PROPERTIES);
		Log.getLogger().info("Starting program.");

		File directory = new File(RDF_DIRECTORY);
		File[] files = directory.listFiles();
		
		for (File file : files) {
			String extension = Util.getExtension(file);
			if(extension.equals(EXTENSION)){
				Log.getLogger().info("Splitting " + file.getName() + "...");
				splitHugeFile(file, LINES_PER_FILE);
			}
		}
	}
	
	/**
	 * Read a text file and split it into chunks of lines.
	 * 
	 * @param file The file
	 * @param lines The number of lines per split file
	 * @return The (text) file contents
	 */
	public static void splitHugeFile(File file, int lines) {
		BufferedReader br = null;
		String out = "";
		try {
			br = new BufferedReader(new FileReader(file), 1000000);
			FileWriter fw;
			String currentLine;
			int count = 0;
			int fileIndex = 1;
			while ((currentLine = br.readLine()) != null) {
				out += currentLine + "\n";
				count++;
				//showProgress(count, lines);
				if(count == lines) {
					//String subfix = (fileIndex < 10) ? "_0" : "_";
					Log.getLogger().info("Writing " + file.getAbsolutePath() + "_" + fileIndex + "." + Util.getExtension(file) + " to disk...");
					fw = new FileWriter(new File(file.getAbsolutePath() + "_" + fileIndex + "." + Util.getExtension(file)));
					fw.write(out);
					fw.close();
					count = 0;
					fileIndex++;
					out = "";
				}
			}
			//String subfix = (fileIndex < 10) ? "_0" : "_";
			fw = new FileWriter(new File(file.getAbsolutePath() + "_" + fileIndex + "." + Util.getExtension(file)));
			fw.write(out);
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
