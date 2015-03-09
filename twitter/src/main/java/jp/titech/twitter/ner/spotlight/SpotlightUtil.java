/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.ner.spotlight;

import java.util.ArrayList;
import java.util.List;

import jp.titech.twitter.ontology.dbpedia.DBpediaResource;
import jp.titech.twitter.ontology.dbpedia.DBpediaResourceOccurrence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Kristian Slabbekoorn
 *
 */
class SpotlightUtil {

	public static List<DBpediaResourceOccurrence> jsonToResourceOccurrences(JSONObject json){

		List<DBpediaResourceOccurrence> bestCandidates = new ArrayList<DBpediaResourceOccurrence>();

		try {
			DBpediaResourceOccurrence occurrence;
			if(json == null) return bestCandidates;

			JSONObject jsonMain = json.getJSONObject("annotation");
			String context = jsonMain.getString("@text");
			
			Object object = jsonMain.opt("surfaceForm");
			JSONArray surfaceForms = new JSONArray();
			
			if(object == null) {
				return bestCandidates;
			} else if(object instanceof JSONArray){
				surfaceForms = (JSONArray) object;
			} else if(object instanceof JSONObject){
				surfaceForms.put(object);
			}
			
			for (int i = 0; i < surfaceForms.length(); i++) {
				JSONObject jsonSurfaceForm = surfaceForms.getJSONObject(i);
				//SurfaceForm surfaceForm = new SurfaceForm(jsonSurfaceForm.getString("@name"));
				String surfaceForm = jsonSurfaceForm.getString("@name");
				int textOffset = jsonSurfaceForm.getInt("@offset");

				JSONObject jsonResourceTemp = jsonSurfaceForm.optJSONObject("resource");
				
				JSONArray jsonResources;
				if(jsonResourceTemp == null) {
					jsonResources = jsonSurfaceForm.optJSONArray("resource");
				} else {
					jsonResources = new JSONArray();
					jsonResources.put(jsonResourceTemp);
				}

				if(jsonResources != null && jsonResources.length() > 0) {
					JSONObject jsonResource = jsonResources.getJSONObject(0);
					DBpediaResource dbpediaResource = jsonToDBpediaResource(jsonResource);
					
					occurrence = new DBpediaResourceOccurrence(dbpediaResource, surfaceForm, context, textOffset);
					occurrence.setPercentageOfSecondRank(jsonResource.getDouble("@percentageOfSecondRank"));
					occurrence.setSimilarityScore(jsonResource.getDouble("@finalScore"));
					bestCandidates.add(occurrence);
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return bestCandidates;
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
			String typeStr = jsonResource.getString("@types");
			//dbpediaResource.setTypes(typeStr.split(", "));
			
			if(!typeStr.isEmpty()) {
				String[] types = typeStr.split(", ");
				dbpediaResource.setTypes(types);
			}
			
			return dbpediaResource;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
