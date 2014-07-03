/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;

import jp.titech.twitter.data.TwitterUser;
import jp.titech.twitter.ontology.dbpedia.DBpediaQuery;
import jp.titech.twitter.ontology.types.Category;
import jp.titech.twitter.ontology.types.FreebaseType;
import jp.titech.twitter.ontology.types.YAGOType;

import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.OntologyType;
import org.dbpedia.spotlight.model.SchemaOrgType;

import ch.qos.logback.core.status.WarnStatus;

public class Util {

	private static Set<String> stopWords = new HashSet<String>();

	/** 
	 * Write a String to a file
	 * 
	 * @param log The string
	 * @param file The file
	 */
	public static void writeToFile(String log, File file) {
		try {
			file.getParentFile().mkdirs();
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
	public static void addToFile(String log, File file) {
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
		BufferedReader br = null;
		String out = "";

		try {
			String currentLine;
			br = new BufferedReader(new FileReader(file));
			while ((currentLine = br.readLine()) != null) out += currentLine + "\n";
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
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
		return readFile(new File(path));
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
	public static String removeNetslang(String text) {
		// Load the stopwords file if it hasn't been loaded yet
		if(stopWords.isEmpty()) loadStopwords(Vars.STOPWORDS_FILE);
		
		// If there are stopwords, strip them.
		if(!stopWords.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			Scanner scanner = new Scanner(text);
			String next = "";
			while(scanner.hasNext()){
				if(!(next = scanner.nextLine()).isEmpty()){
					String[] strs = next.split("\\s+");
					for (int i = 0; i < strs.length; i++) {
						if(!stopWords.contains(removeSymbols(strs[i]).toLowerCase())) {
							builder.append(strs[i] + " ");
						}
					}
				}
				builder.append("\n");
			}
			return builder.toString();
		}
		
		// If not, just return the text.
		return text;
	}
	
	/**
	 * Strip symbols and return the clean text.
	 * 
	 * @param text
	 * @return the clean string
	 */
	public static String removeSymbols(String text) {
		return text.replaceAll("[@,—.:;<>“”{}\\[\\]()\"'’*!?]", "").trim();
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

	/**
	 * Converts an immutable Scala List to a mutable Java list.
	 * 
	 * @param types
	 * @return returnTypes The Java list.
	 */
	public static List<OntologyType> convertScalaList(scala.collection.immutable.List<OntologyType> types) {
		List<OntologyType> returnTypes = new ArrayList<OntologyType>();
		scala.collection.Iterator<OntologyType> it = types.iterator();
		while(it.hasNext()) {
			OntologyType type = it.next();
			returnTypes.add(type);
		}
		return returnTypes;
	}

	/**
	 * Create an OntologyType object from a type URI.
	 * 
	 * @param string A type URI
	 * @return
	 */
	public static OntologyType determineOntologyType(String uri) {
		if(uri.contains("dbpedia.org/ontology/")){
			return new DBpediaType(uri.split("dbpedia.org/ontology/")[1]);
		} else if(uri.contains("schema.org/")){
			return new SchemaOrgType(uri.split("schema.org/")[1]);
		} else if(uri.contains("rdf.freebase.com/ns/")){
			String[] pathSplit = uri.split("rdf.freebase.com/ns/");
			String[] domainSplit = pathSplit[1].split("/");
			return (domainSplit.length == 1) ? new FreebaseType(domainSplit[0]) : new FreebaseType(domainSplit[0], domainSplit[1]);
		} else if(uri.contains("dbpedia.org/class/yago/")){
			return new YAGOType(uri.split("dbpedia.org/class/yago/")[1]);
		} else if(uri.contains("dbpedia.org/resource/Category:")){
			return new Category(uri.split("dbpedia.org/resource/Category:")[1]);
		} else {
			Log.getLogger().error("Unknown ontology type: " + uri);
			return null;
		}
	}

	/**
	 * Returns the file name without extension.
	 * 
	 * @param file
	 * @return The extension string
	 */
	public static String getName(File file) {
		String[] name = file.getName().split("\\.");
		return name[0];
	}

	/**
	 * Returns the file extension.
	 * 
	 * @param file
	 * @return The extension string
	 */
	public static String getExtension(File file) {
		String[] name = file.getName().split("\\.");
		return (name.length <= 1) ? "" : name[name.length-1];
	}

	/**
	 * Sorts a map by its values (descending order).
	 * 
	 * @param map
	 * @return the sorted map
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) result.put(entry.getKey(), entry.getValue());

		return result;
	}

	public static double calculateYAGOOntologyDistance(File fileOne, File fileTwo) {

		Log.getLogger().info("Calculating average YAGO ontology path length for files " + fileOne.getName() + " and " + fileTwo.getName() + "...");
		HashMap<YAGOType, Integer> mapOne = new HashMap<YAGOType, Integer>(), mapTwo = new HashMap<YAGOType, Integer>();
		int totalOne = 0, totalTwo = 0;

		YAGOType currentYAGOType = null;

		try {
			Scanner sc = new Scanner(new FileReader(fileOne));
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				if(line.startsWith("YAGO:")){
					String[] split = line.split("\t");
					int number = Integer.parseInt(split[1]);
					if(!split[0].contains("LivingPeople")){
						currentYAGOType = new YAGOType(split[0].split(":")[1]);
						mapOne.put(currentYAGOType, number);
						totalOne += number;
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Scanner sc = new Scanner(new FileReader(fileTwo));
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				if(line.startsWith("YAGO:")){
					String[] split = line.split("\t");
					int number = Integer.parseInt(split[1]);
					if(!split[0].contains("LivingPeople")){
						currentYAGOType = new YAGOType(split[0].split(":")[1]);
						mapTwo.put(currentYAGOType, number);
						totalTwo += number;

					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		DBpediaQuery dbpq = DBpediaQuery.getInstance();
		int totalDistance = 0;

		for (YAGOType v : mapOne.keySet()) {
			int occsV = mapOne.get(v);
			for (YAGOType w : mapTwo.keySet()) {
				int occsW = mapTwo.get(w);
				if(!v.equals(w))
					totalDistance += dbpq.getYAGODistance(v, w)*(occsV*occsW);
			}
		}

		double apl = (1/(double)(totalOne*totalTwo)*totalDistance);

		return apl;
	}

	/**
	 * Formats a number to a given amount of decimal places.
	 * 
	 * @param decs Decimal numbers to display
	 * @param number The number to format
	 * @return Formatted number String
	 */
	public static String format(int decs, double number) {
		return String.format("%." + decs + "f", number);
	}

	/**
	 * Rounds a double to a given amount of decimal places.
	 * 
	 * @param places Decimal numbers to round to
	 * @param value The number to round
	 * @return Rounded double
	 */

	public static double round(int places, double value) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * Returns a Date object with the given year, month and day.
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return the date
	 */
	public static Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DAY_OF_MONTH, day);

		return cal.getTime();
	}

	public static Map<String, Integer> stringToRelevanceMap(String text) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		String lines[] = text.split("\n");

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] parts = line.split("\t");
			if(parts.length < 2) {
				Log.getLogger().warn("Relevance map is missing a score. Assigning a default of 0.");
				map.put(parts[0], 0);
			} else 
				map.put(parts[0], Integer.parseInt(parts[1]));
		}

		return map;
	}

	public static List<Entry<String, Double>> sortSimilarityMapByValue(SortedMap<String, Double> temp) {
		Set<Entry<String, Double>> entryOfMap = temp.entrySet();

		List<Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>(entryOfMap);
		Collections.sort(entries, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		return entries;
	}
	
	public static void writeUserTFIDFMap(TwitterUser user) {
		Util.writeToFile(user.tfidfMapString(), new File(Vars.USER_DIRECTORY + Vars.PARAMETER_STRING + "/" + user.getScreenName() + "/TF-IDF.txt"));
	}
	
	public static void writeUserOntology(TwitterUser user) {
		Util.writeToFile(user.getUserOntology().toString(), new File(Vars.USER_DIRECTORY + Vars.PARAMETER_STRING + "/" + user.getScreenName() + "/ontology.gb=[" + Vars.GENERALITY_BIAS + "].txt"));
		if(!Vars.PRUNING_MODE.equals("NONE")) {
			Util.writeToFile(user.getUserOntology().toString(), 
					new File(Vars.USER_DIRECTORY + Vars.PARAMETER_STRING + "/" + user.getScreenName() + "/" + Vars.PRUNING_MODE + "/ontology.gb=[" + Vars.GENERALITY_BIAS + "].txt"));
		}
	}
}
