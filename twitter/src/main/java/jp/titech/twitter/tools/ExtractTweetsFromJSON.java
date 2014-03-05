/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		8 jan. 2013
 */
package jp.titech.twitter.tools;

import java.io.File;
import java.util.Date;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jp.titech.twitter.db.TweetBase;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class ExtractTweetsFromJSON {

	public static final String DATA_DIRECTORY = "../twitter/data/Twitter ranking/";
	//public static final String[] FILE_PATHS = {"20121219/"};
	public static final String[] FILE_PATHS = {"20121215/", "20121216/", "20121217/", "20121218/", "20121219/"};
	public static final String FILENAME = "malware.tweets.1";
	public static final String[] USERNAMES = {"TechL0G", "karthi4india", "sonuise", "PacketknifeToo", "BYODRT"};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		for (int i = 0; i < FILE_PATHS.length; i++) {
			/*Log.getLogger().info("Reading Noro file " + FILE_PATHS[i] + FILENAME + " to memory...");
			String json = Util.readFile(DATA_DIRECTORY + FILE_PATHS[i] + FILENAME);
			
			Log.getLogger().info("Converting to JSON...");
			json = json.replace("$VAR1 = ", "").replace("are so small?\"\n\"", "are so small?\"\"").replace("YARA\"", "YARA").replace("wins.\"", "wins.").replace("\"", "GRRG").replace("'", "GRRG").replace("=>", ":")
					.replace("undef", "\"undef\"").replace("GRRG", "'").replace("     '", "     \"")
					.replace("' : ", "\" : ").replace("\" : '", "\" : \"").replace("',\n", "\",\n").replace("'\n", "\"\n").replace("};", "}");
			
			Log.getLogger().info("Writing new file to disk...");
			Util.writeToFile(json, new File(DATA_DIRECTORY + FILE_PATHS[i] + FILENAME + ".stripped"));*/
			
			Log.getLogger().info("Reading JSON file " + FILE_PATHS[i] + FILENAME + " to memory...");
			String json = Util.readFile(DATA_DIRECTORY + FILE_PATHS[i] + FILENAME + ".stripped");
			
			JSONParser parser = new JSONParser();
			try {
				Log.getLogger().info("Parsing JSON object...");
				JSONObject object = (JSONObject)parser.parse(json);
				Log.getLogger().info("Successfully parsed!");
				
				JSONArray array = (JSONArray)object.get("results");
				
				for (Object object2 : array) {
					JSONObject o = (JSONObject)object2;
					
					for(String username : USERNAMES) {
						if(o.get("from_user").equals(username)){
							TweetBase.getInstance().addTweet(Long.parseLong((String)o.get("id")), Long.parseLong((String)o.get("from_user_id_str")), username,
							new Date((String)o.get("created_at")), (String)o.get("text"), false, null, null, null, null, null, null, null);
						}
					}
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
