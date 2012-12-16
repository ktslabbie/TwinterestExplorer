/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		17 okt. 2012
 */
package jp.titech.twitter.ner.spotlight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbpedia.spotlight.model.DBpediaResourceOccurrence;
import org.json.JSONObject;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;



/**
 * @author Kristian Slabbekoorn
 *
 */
public class SpotlightQuery {

	private static SpotlightQuery spotlightQuery;
	private boolean remote;

	private SpotlightQuery() {
		remote = Vars.SPOTLIGHT_REMOTE;
	}

	public static SpotlightQuery getInstance(){
		if(spotlightQuery == null) {
			spotlightQuery = new SpotlightQuery();
		}
		return spotlightQuery;
	}


	/**
	 * @param text 	The Tweet content
	 * @return JSONObject
	 */
	public Map<String, List<DBpediaResourceOccurrence>> annotate(String text) {
		return remote ? annotateRemotely(text) : annotateLocally(text);
	}

	/**
	 * @param text
	 * @return
	 */
	private Map<String, List<DBpediaResourceOccurrence>> annotateLocally(String text) {
		// TODO Auto-generated method stub
		return null;
	}



	/**
	 * @param text
	 * @return
	 */
	private Map<String, List<DBpediaResourceOccurrence>> annotateRemotely(String text) {
		JSONObject json = null;

		Log.getLogger().info("Running DBpedia Spotlight annotator (retrieving all candidates)...");

		String query = "text=" + text.replace("&", "%26") + 
				"&confidence=" + Vars.SPOTLIGHT_CONFIDENCE + "&support=" + Vars.SPOTLIGHT_SUPPORT +
				"&policy=&types=";

		try {
			URI uri = new URI("http", "spotlight.dbpedia.org", "/rest/candidates", query, null);
			URL url = new URL(uri.toASCIIString());
			Log.getLogger().info(url.getQuery());

			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestMethod("GET");
			urlc.setRequestProperty("accept", "application/json");

			if (urlc.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + urlc.getResponseCode());
			}

			Log.getLogger().info("Sending query to DBpedia Spotlight.");

			boolean queried = false;
			while(!queried){
				try {

					BufferedReader br = new BufferedReader(new InputStreamReader((urlc.getInputStream())));

					String output, jsonString = "";
					while ((output = br.readLine()) != null) {
						jsonString += output;
					}

					json = new JSONObject(jsonString);

					//Log.getLogger().info(json.toString(2));
					queried = true;
				} catch (Exception e) {
					Log.getLogger().error(e.getMessage());
					Log.getLogger().info("Retrying in " + Vars.QUERY_RETRY / 1000 + " seconds...");
					//queried = true;
					synchronized(this){
						this.wait(Vars.QUERY_RETRY);
					}
				}
			}
		} catch (InterruptedException e){
			Log.getLogger().error(e.getMessage());
		} catch (URISyntaxException e) {
			Log.getLogger().error(e.getMessage());
		} catch (MalformedURLException e) {
			Log.getLogger().error(e.getMessage());
		} catch (IOException e) {
			Log.getLogger().error(e.getMessage());
		}

		return SpotlightUtil.jsonToResourceOccurrences(json);
	}



}
