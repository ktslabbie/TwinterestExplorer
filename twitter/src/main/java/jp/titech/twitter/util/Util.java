/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Util {

	private static Set<String> stopWords = new HashSet<String>();

	/** 
	 * Write a String to a file
	 * 
	 * @param log The string
	 * @param file The file
	 */
	public static void writeToFile(String log, File file){
		try {
			FileWriter fw = new FileWriter(file);
			fw.write(log);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a String to an existing file
	 * 
	 * @param log The String
	 * @param file The file
	 */
	public static void addToFile(String log, File file){
		try {
			FileWriter fw = new FileWriter(file, true);
			fw.write(log);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read a text file and return the contents as a String.
	 * 
	 * @param file The file
	 * @return The (text) file contents
	 */
	public static String readFile(File file) {
		String out = "";
		try {
			Scanner sc = new Scanner(new FileReader(file));
			while(sc.hasNextLine()){
				out += sc.nextLine() + "\n";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return out;
	}

	/**
	 * Read a text file from the given path and return the contents as a String.
	 * 
	 * @param file The file path
	 * @return The (text) file contents
	 */
	public static String readFile(String path) {
		String out = "";
		try {
			Scanner sc = new Scanner(new FileReader(new File(path)));
			while(sc.hasNextLine()){
				out += sc.nextLine() + "\n";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return out;
	}

	/**
	 * Loads stopwords from a stopword file
	 * 
	 * @param path The stopword file path
	 */
	public static void loadStopwords(String path){
		try {
			Scanner scanner = new Scanner(new FileReader(new File(path)));
			String next = "";
			while(scanner.hasNext())
				if(!(next = scanner.next()).isEmpty())
					stopWords.add(next);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Remove stopwords from a String of text.
	 * 
	 * @param text The String
	 * @return The String with stopwords removed
	 */
	public static String removeStopwords(String text) {
		StringBuilder builder = new StringBuilder();
		Scanner scanner = new Scanner(text);
		String next = "";
		while(scanner.hasNext()){
			if(!(next = scanner.nextLine()).isEmpty()){
				String[] strs = next.split(" ");
				for (int i = 0; i < strs.length; i++) {
					if(!stopWords.contains(strs[i].toLowerCase())){
						builder.append(strs[i] + " ");
					}
				}
			}
			builder.append("\n");
		}
		return builder.toString();
	}

	/**
	 * Convert an array to a String using spaces as glue
	 * 
	 * @param ngram The array to convert
	 * @return The String
	 */
	public static String arrayToString(String[] ngram) {
		String ret = "";
		for (int i = 0; i < ngram.length; i++) {
			ret += ngram + " ";
		}
		return ret.trim();
	}

	/**
	 * Convert an ArrayList to a String using spaces as glue
	 * 
	 * @param ngram The array to convert
	 * @return The String
	 */
	public static String arrayListToString(ArrayList<String> ngram) {
		String ret = "";
		for (String string : ngram) {
			ret += string + " ";
		}
		return ret.trim();
	}

	/**
	 * Return the length of an n-gram
	 * 
	 * @param text The text
	 * @return The wordcount
	 */
	public static int ngramLength(String text){
		return text.split(" ").length;
	}

	/**
	 * Check an array for containment of a String
	 * 
	 * @param array The array
	 * @param text The text to check
	 * @return true if contained, else false
	 */
	public static boolean arrayContains(String[] array, String text){
		for (String string : array)	{
			if(string.equals(text)) return true;
		}
		return false;
	}

	/**
	 * Create new file by deleting the old one first (if it exists) 
	 * 
	 * @param string
	 * @return
	 */
	public static File newFile(String path) {
		File file = new File(path);
		if(file.exists())
			file.delete();
		return file;
	}

	/**
	 * Show progress by steps of 10%
	 * 
	 * @param totalCount Total number of iterations
	 * @param count Current iteration
	 */
	public static void showProgress(int totalCount, int count) {
		int seg = totalCount / 10;
		if(count % seg == 0){
			Log.getLogger().info(110 - (count / seg)*10 + "% done.");
		}
	}
}
