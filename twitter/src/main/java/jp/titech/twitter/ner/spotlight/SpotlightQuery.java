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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.titech.twitter.ontology.dbpedia.DBpediaResourceOccurrence;

import org.json.JSONObject;

import jp.titech.twitter.data.Tweet;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Vars;


/**
 * @author Kristian Slabbekoorn
 *
 */
public class SpotlightQuery {
	
	private String spotlightURL;

	public SpotlightQuery(String pSpotlightURL){
		spotlightURL = pSpotlightURL;
	}

	/**
	 * @param text
	 * @return
	 */
	public List<DBpediaResourceOccurrence> annotate(String text) {
		JSONObject json = null;

		//Log.getLogger().info("Running DBpedia Spotlight annotator (retrieving all candidates)...");

		String query = "text=" + text.replace("&", "%26") + 
				"&confidence=" + Vars.SPOTLIGHT_CONFIDENCE + "&support=" + Vars.SPOTLIGHT_SUPPORT +
				"&policy=&types=";

		try {
			URI uri = new URI("http", spotlightURL, "/rest/candidates", query, null);
			URL url = new URL(uri.toASCIIString());
			
			//Log.getLogger().info(url.getQuery());
			Log.getLogger().info("Sending tweet content to DBpedia Spotlight for annotation...");
			
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestMethod("GET");
			urlc.setRequestProperty("accept", "application/json");

			if (urlc.getResponseCode() != 200) {
				Log.getLogger().error("Failed : HTTP error code : " + urlc.getResponseCode() + ". Returning empty occurrence map.");
				return new ArrayList<DBpediaResourceOccurrence>();
			}

			
			try {

				BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));

				String output, jsonString = "";
				while ((output = br.readLine()) != null) {
					jsonString += output;
				}
				
				//Log.getLogger().info("Faulty string (?): " + jsonString);
				json = new JSONObject(jsonString);

			} catch (Exception e) {
				Log.getLogger().error(e.getMessage());
				e.printStackTrace();
				
				Log.getLogger().info("Returning empty occurrence map.");
				return new ArrayList<DBpediaResourceOccurrence>();
			}
			
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
