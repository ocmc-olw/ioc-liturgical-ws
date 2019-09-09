package net.ages.alwb.utils.transformers;


	import ioc.liturgical.ws.managers.databases.external.neo4j.utils.Neo4jConnectionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.ocmc.ioc.liturgical.schemas.models.ws.response.ResultJsonObjectArray;
import org.ocmc.ioc.liturgical.utils.FileUtils;

	
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

	/**
	 * Reads the database for system labels and creates a json representation.
	 * Also creates a javascript enum file for the label topics.
	 * Writes these to the ioc-liturgical-react and olw projects
	 * @author mac002
	 *
	 */
	public class RunToWriteUiLabelsToWebStormProject {
		private Neo4jConnectionManager dbManager = null;
		
		public RunToWriteUiLabelsToWebStormProject(
				String url
				, String uid
				, String pwd
				) {
			this.dbManager = new Neo4jConnectionManager(
					  url
					  , uid
					  , pwd
					  , false
					  );
		}
		
		private String getTopicsEnum(List<String> topics) {
			StringBuffer sb = new StringBuffer();
			sb.append("// run ioc-liturgical-ws net.ages.alwb.utils.transformers.LabelsNeoToJson to produce this file.\n\n");
			sb.append("class LabelTopics {};\n");
			for (String topic : topics) {
				sb.append("LabelTopics.");
				sb.append(topic);
				sb.append(" = '");
				sb.append(topic);
				sb.append("';\n");
			}
			sb.append("export default LabelTopics;");
			return sb.toString();
		}
		
		private void process(String system, String out) {
			JsonObject result = new JsonObject();
			
			String topicsEnum = "";
			String query = "match (n:UiLabel) return distinct n.topic as item";
			ResultJsonObjectArray queryResult = dbManager.getResultObjectForQuery(query);
			List<String> topics = new ArrayList<String>();
			for (JsonObject json : queryResult.values) {
				topics.add(json.get("item").getAsString());
			}
			topicsEnum = this.getTopicsEnum(topics);

			query = "match (n:UiLabel) return distinct split(n.library, \"_\")[0] as item";
			queryResult = dbManager.getResultObjectForQuery(query);
			List<String> languages = new ArrayList<String>();
			for (JsonObject json : queryResult.values) {
				languages.add(json.get("item").getAsString());
			}
			for (String language : languages) {
				JsonObject langJson = new JsonObject();
				String library = language + "_sys_" + system; 
				query = "match (n:UiLabel) where n.library starts with '" + language + "' and n.library ends with '" +  system + "' return distinct n.topic as item";
				ResultJsonObjectArray langQueryResult = dbManager.getResultObjectForQuery(query);
				
				for (String topic : topics) {
					JsonObject topicsJson = new JsonObject();
					query = "match (n:UiLabel) where n.library = '" + library +"' and n.topic = '" + topic + "' return distinct n.key as label, n.value as value";
					ResultJsonObjectArray topicQueryResult = dbManager.getResultObjectForQuery(query);
					for (JsonObject json : topicQueryResult.values) {
						String label = json.get("label").getAsString();
						String value = json.get("value").getAsString();
						topicsJson.addProperty(label, value);
					}
					langJson.add(topic, topicsJson);
				}
				result.add(language, langJson);
			}
			FileUtils.writeFile(out + "/labels.json",result.toString());
			FileUtils.writeFile(out + "/LabelTopics.js", topicsEnum);
		}
		
		public static void main(String[] args) {
			String reactPath = "/Users/mac002/Git/ocmc/ioc-liturgical-react/src/classes";
			String olwPath = "/Users/mac002/Git/ocmc/ioc-tms-app/src/labels";
			String uid = System.getenv("uid");
			String pwd = System.getenv("pwd");
			String url= "db.liml.org"; // "localhost"; // "159.203.89.233";
			Map<String, String> systems = new TreeMap<String,String>();
			systems.put("ilr", reactPath);
			systems.put("olw", olwPath);
			RunToWriteUiLabelsToWebStormProject labels2Neo = new RunToWriteUiLabelsToWebStormProject(url, uid, pwd);
			for (Entry<String,String> system : systems.entrySet()) {
				labels2Neo.process(system.getKey(), system.getValue());
			}
		}
	}
