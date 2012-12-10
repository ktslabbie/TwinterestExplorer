/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.matching.spotlight;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.titech.twitter.util.Log;

import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.DBpediaResourceOccurrence;
import org.dbpedia.spotlight.model.DBpediaType;
import org.dbpedia.spotlight.model.FreebaseType;
import org.dbpedia.spotlight.model.OntologyType;
import org.dbpedia.spotlight.model.SchemaOrgType;
import org.dbpedia.spotlight.model.SurfaceForm;
import org.dbpedia.spotlight.model.Text;

import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.internal.org.json.JSONArray;

/**
 * @author Kristian Slabbekoorn
 *
 */
public class SpotlightUtil {

	public static Map<String, List<DBpediaResourceOccurrence>> jsonToResourceOccurrences(JSONObject json){

		Map<String, List<DBpediaResourceOccurrence>> resourceOccurrenceMap = new LinkedHashMap<String, List<DBpediaResourceOccurrence>>();

		try {
			DBpediaResourceOccurrence occurrence = null;

			JSONObject jsonMain = json.getJSONObject("annotation");

			Text context = new Text(jsonMain.getString("@text"));
			JSONArray surfaceForms = jsonMain.getJSONArray("surfaceForm");
			for (int i = 0; i < surfaceForms.length(); i++) {
				List<DBpediaResourceOccurrence> resourceOccurrences = new ArrayList<DBpediaResourceOccurrence>();
				
				JSONObject jsonSurfaceForm = surfaceForms.getJSONObject(i);
				SurfaceForm surfaceForm = new SurfaceForm(jsonSurfaceForm.getString("@name"));
				int textOffset = jsonSurfaceForm.getInt("@offset");

				JSONObject jsonResourceTemp = jsonSurfaceForm.optJSONObject("resource");

				JSONArray jsonResources;
				if(jsonResourceTemp == null) {
					jsonResources = jsonSurfaceForm.optJSONArray("resource");
				} else {
					jsonResources = new JSONArray();
					jsonResources.put(jsonResourceTemp);
				}

				if(jsonResources != null) {
					for (int j = 0; j < jsonResources.length(); j++) {
						JSONObject jsonResource = jsonResources.getJSONObject(j);
						DBpediaResource dbpediaResource = jsonToDBpediaResource(jsonResource);
						occurrence = new DBpediaResourceOccurrence(dbpediaResource, surfaceForm, context, textOffset);
						occurrence.setPercentageOfSecondRank(jsonResource.getDouble("@percentageOfSecondRank"));
						occurrence.setSimilarityScore(jsonResource.getDouble("@finalScore"));
						resourceOccurrences.add(occurrence);
					}
				}
				resourceOccurrenceMap.put(surfaceForm.name(), resourceOccurrences);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return resourceOccurrenceMap;
	}

	/**
	 * @param jsonObject
	 * @return
	 */
	private static DBpediaResource jsonToDBpediaResource(JSONObject jsonResource) {		
		try {
			DBpediaResource dbpediaResource = new DBpediaResource(jsonResource.getString("@uri"));
			dbpediaResource.setPrior(jsonResource.getDouble("@priorScore"));
			dbpediaResource.setSupport(jsonResource.getInt("@support"));
			String[] types = jsonResource.getString("@types").split(", ");
			List<OntologyType> typeList = new ArrayList<OntologyType>();
			for (int k = 0; k < types.length; k++) {
				OntologyType type = SpotlightUtil.determineOntologyType(types[k]);
				typeList.add(type);
			}
			dbpediaResource.setTypes(typeList);
			return dbpediaResource;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private static OntologyType determineOntologyType(String string) {
		String[] split = string.split(":");
		if(split.length != 2) return null;

		OntologyType returnType = null;
		String type = split[0];
		if(type.equals("DBpedia")){
			returnType = new DBpediaType(split[1]);
		} else if(type.equals("Schema")){
			returnType = new SchemaOrgType(split[1]);
		} else if(type.equals("Freebase")){
			String[] domainSplit = split[1].split("/");
			if(domainSplit.length == 2){
				returnType = new FreebaseType(domainSplit[1], null);
			} else {
				returnType = new FreebaseType(domainSplit[1], domainSplit[2]);
			}
		}
		return returnType;
	}
}
