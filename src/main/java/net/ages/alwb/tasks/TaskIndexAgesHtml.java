package net.ages.alwb.tasks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.joda.time.Instant;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.ocmc.ioc.liturgical.schemas.models.LDOM.AgesIndexTableRowData;
import org.ocmc.ioc.liturgical.schemas.models.LDOM.LDOM;
import org.ocmc.ioc.liturgical.schemas.models.keymap.TopicKeyUrl;
import org.ocmc.ioc.liturgical.schemas.models.keymap.UrlLdom;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.ResultJsonObjectArray;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ioc.liturgical.ws.managers.databases.external.neo4j.ExternalDbManager;
import net.ages.alwb.utils.transformers.adapters.AgesHtmlToEditableLDOM;

/**
 * Runs a task (separate thread) to index the HTML used for the editor
 * and generator, which mostly come from the GOARCH DCS website.
 * 
 * That is, it makes a list of all topic~keys and which html pages they occur in.
 * 
 * @author mac002
 *
 */
public class TaskIndexAgesHtml implements Runnable {
	private static final Logger logger = LoggerFactory
			.getLogger(TaskIndexAgesHtml.class);
	ExternalDbManager manager = null;
	private Gson gson = new Gson();
	boolean printPretty = true;
	
	public TaskIndexAgesHtml (
			ExternalDbManager manager
			) {
		this.manager = manager;
	}
	@Override
	public void run() {
		synchronized(this) {
			try {
				String start = Instant.now().toString();
				ResultJsonObjectArray indexArray = manager.getAgesIndexTableData();
				JsonArray array = indexArray.getFirstObject().get("tableData").getAsJsonArray();
				int urlCount = array.size();
				int keyCount = 0;
				int processed = 0;
				for (JsonElement e : array) {
					JsonObject o = e.getAsJsonObject();
					AgesIndexTableRowData row = gson.fromJson(o.toString(), AgesIndexTableRowData.class);
					keyCount = keyCount + this.process(row);
					processed++;
					logger.info(processed + " of " + urlCount + " "+ row.getUrl());
				}
				System.out.println("\n\nStart: " + start);
				System.out.println("Finish : " + Instant.now().toString());
				System.out.println("URL count = " + urlCount);
				System.out.println("Topic-Key count = " + keyCount);
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
		}
	}
	

	
	private int process(AgesIndexTableRowData row) {
		Connection c = null;
		Document doc = null;
		Elements content = null;
		UrlLdom urlLdom = new UrlLdom(row.getUrl());
		LDOM template = null;
		int keyCount = 0;
		Map<String,List<AgesIndexTableRowData>> map = new TreeMap<String,List<AgesIndexTableRowData>>();
		try {
			c = Jsoup.connect(row.getUrl());
			doc = c.timeout(60*1000).maxBodySize(0).get();
			int hash = doc.text().hashCode();
			String query = "match (n:UrlLdom) where n.id = 'en_sys_ldom~urls~"
					+ row.getUrl() + "' return n.hash";
			
			ResultJsonObjectArray queryResult = this.manager.getForQuery(
					query
					, false
					, false
					);
			int dbHash = 0;
			if (queryResult.valueCount > 0) {
				try {
					dbHash = queryResult.getFirstObjectValueAsObject().get("n.hash").getAsInt();
				} catch (Exception e) {
				}
			}
			
			if (dbHash != hash) {
				AgesHtmlToEditableLDOM ages = new AgesHtmlToEditableLDOM(
						row.getUrl()
						, this.manager
						, true // check Database to see if we already have the current LDOM
						, this.printPretty // print pretty
						);
				template = ages.toLDOM();
				for ( String topicKey : template.getTopicKeys()) {
					List<AgesIndexTableRowData> rowData = new ArrayList<AgesIndexTableRowData>();
					if (map.containsKey(topicKey)) {
						rowData = map.get(topicKey);
					} 
					rowData.add(row);
					map.put(topicKey, rowData);
				}
				urlLdom.setHash(hash);
				urlLdom.setLdom(template);
				String label = "Root:" + urlLdom.getClass().getSimpleName();
				// save the UrlLdom
				ExternalDbManager.neo4jManager.mergeWhereEqual(label,urlLdom);
				// save all the mappings
				for (Entry<String, List<AgesIndexTableRowData>> entry : map.entrySet()) {
					for (AgesIndexTableRowData tableRow : entry.getValue()) {
						TopicKeyUrl key = new TopicKeyUrl(entry.getKey(), tableRow.getUrl());
						key.setRowData(tableRow);
						label = "Root:" + key.getClass().getSimpleName();
						ExternalDbManager.neo4jManager.mergeWhereEqual(label,key);
					}
				}
				keyCount = map.size();
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return keyCount;
	}
		
}
