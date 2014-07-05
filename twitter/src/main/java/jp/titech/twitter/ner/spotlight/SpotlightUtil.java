/**
 * @author		Kristian Slabbekoorn
 * @version		1.0
 * @since		16 okt. 2012
 */
package jp.titech.twitter.ner.spotlight;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jp.titech.twitter.ontology.types.DBpediaType;
import jp.titech.twitter.ontology.types.FreebaseType;
import jp.titech.twitter.ontology.types.OntologyType;
import jp.titech.twitter.ontology.types.SchemaOrgType;
import jp.titech.twitter.util.Log;
import jp.titech.twitter.util.Util;
import jp.titech.twitter.util.Vars;
import jp.titech.twitter.ontology.dbpedia.DBpediaResource;
import jp.titech.twitter.ontology.dbpedia.DBpediaResourceOccurrence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

			//Text context = new Text(jsonMain.getString("@text"));
			String context = jsonMain.getString("@text");
			
			Object object = jsonMain.opt("surfaceForm");
			JSONArray surfaceForms = new JSONArray();
			
			if(object == null) {
				return resourceOccurrenceMap;
			} else if(object instanceof JSONArray){
				surfaceForms = (JSONArray) object;
			} else if(object instanceof JSONObject){
				((JSONObject) object).toJSONArray(surfaceForms);
			}
			
			for (int i = 0; i < surfaceForms.length(); i++) {
				List<DBpediaResourceOccurrence> resourceOccurrences = new ArrayList<DBpediaResourceOccurrence>();
				
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

				if(jsonResources != null) {
					for (int j = 0; j < jsonResources.length(); j++) {
						JSONObject jsonResource = jsonResources.getJSONObject(j);
						DBpediaResource dbpediaResource = jsonToDBpediaResource(jsonResource);
						occurrence = new DBpediaResourceOccurrence(dbpediaResource, surfaceForm, context, textOffset);
						occurrence.setPercentageOfSecondRank(jsonResource.getDouble("@percentageOfSecondRank"));
						occurrence.setSimilarityScore(jsonResource.getDouble("@finalScore"));
						resourceOccurrences.add(occurrence);
					}
					if(!Vars.INCLUDE_EMPTY_SURFACE_FORMS)
						resourceOccurrenceMap.put(surfaceForm, resourceOccurrences);
				}
				if(Vars.INCLUDE_EMPTY_SURFACE_FORMS)
					resourceOccurrenceMap.put(surfaceForm, resourceOccurrences);
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
				OntologyType type = determineNativeOntologyType(types[k]);
				if(type != null)
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
	private static OntologyType determineNativeOntologyType(String string) {
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
			returnType = (domainSplit.length == 2) ? new FreebaseType(domainSplit[1]) : new FreebaseType(domainSplit[1], domainSplit[2]);
		}
		return returnType;
	}
}
