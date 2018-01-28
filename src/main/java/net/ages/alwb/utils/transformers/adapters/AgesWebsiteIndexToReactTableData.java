package net.ages.alwb.utils.transformers.adapters;

import java.util.Map.Entry;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ioc.liturgical.ws.managers.databases.external.neo4j.ExternalDbManager;
import net.ages.alwb.utils.transformers.adapters.models.AgesIndexTableData;
import net.ages.alwb.utils.transformers.adapters.models.AgesIndexTableRowData;

/**
 * Reads the index of available sacraments and services from
 * the AGES Initiatives website, and converts it to json
 * data that can be rendered by a React BootstrapTable
 * @author mac002
 *
 */
public class AgesWebsiteIndexToReactTableData {
	private static final Logger logger = LoggerFactory.getLogger(AgesWebsiteIndexToReactTableData.class);
	
	private String baseUrl = "http://www.agesinitiatives.com/dcs/public/dcs/";
	private String servicesIndex = baseUrl + "servicesindex.html";
	private String booksIndex = baseUrl + "booksindex.html";
	private String jsonservicesIndex = baseUrl + "servicesindex.json";
	private String agesOcmcBaseUrl = "http://www.agesinitiatives.com/dcs/ocmc/dcs/";
	private String agesOcmcIndex = "customindex.html";
	private String readingsIndex = agesOcmcBaseUrl + agesOcmcIndex;

	private boolean printPretty = false;

	public AgesWebsiteIndexToReactTableData() {
		
	}
	public AgesWebsiteIndexToReactTableData(boolean printPretty) {
		this.printPretty = printPretty;
	}
	
	public AgesIndexTableData  toReactTableDataFromDailyReadingHtml() throws Exception {
		AgesIndexTableData result = new AgesIndexTableData(printPretty);
		Document readingsIndexDoc = null;
		Connection readingsIndexConnection = null;
		try {
			readingsIndexConnection = Jsoup.connect(readingsIndex);
			readingsIndexDoc = readingsIndexConnection.timeout(60*1000).get();
			Elements months = readingsIndexDoc.select("a.index-custom-file-link");
			for (Element monthAnchor : months) {
				String href = monthAnchor.attr("href");
				String[] hrefParts = href.split("/");
				if (hrefParts.length == 5) {
					if (hrefParts[0].equals("h")) {
						if (hrefParts[3].equals("gr-en")) {
							String [] dateParts = hrefParts[2].split("_");
							// TODO: we need to compute the year or it needs to be included in the html
							String year = dateParts[2];
							String month = dateParts[3];
							String date = year + "/"  + month;
							AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
							row.setDate(date);
							row.setDayOfWeek("all");
							row.setType("daily readings");
							row.setUrl(agesOcmcBaseUrl + href);
							result.addRow(row);
						}
					}
				}
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}	
	
	public AgesIndexTableData  toReactTableDataFromHtml() throws Exception {
		AgesIndexTableData result = new AgesIndexTableData(printPretty);
		Document booksIndexDoc = null;
		Document servicesIndexDoc = null;
		Document dayIndexDoc = null;
		Document readingsIndexDoc = null;
		Connection datesIndexConnection = null;
		Connection servicesIndexConnection = null;
		Connection booksIndexConnection = null;
		Connection readingsIndexConnection = null;
		try {
			datesIndexConnection = Jsoup.connect(servicesIndex);
			servicesIndexDoc = datesIndexConnection.timeout(60*1000).get();
			Elements days = servicesIndexDoc.select("a.index-day-link");
			for (Element day : days) {
				String href = day.attr("href");
				String year = href.substring(8, 12);
				String month = href.substring(12, 14);
				String monthDay = href.substring(14,16);
				String date = year + "/" + month + "/" + monthDay;
				String dayOfWeekName = day.text().trim().split("-")[1];
				servicesIndexConnection = Jsoup.connect(baseUrl + href);
				dayIndexDoc = servicesIndexConnection.timeout(60*1000).get();
				Elements services = dayIndexDoc.select("a.index-file-link");
				for (Element service : services) {
					String fileHref = service.attr("href");
					String[] hrefParts = fileHref.split("/");
					if (hrefParts.length == 8) {
						if (hrefParts[0].equals("h")) {
							if (hrefParts[6].equals("gr-en")) {
								AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
								row.setDate(date);
								row.setDayOfWeek(dayOfWeekName);
								row.setType(hrefParts[5]);
								row.setUrl(baseUrl + fileHref);
								result.addRow(row);
							}
						}
					}
				}
			}
			booksIndexConnection = Jsoup.connect(booksIndex);
			booksIndexDoc = booksIndexConnection.timeout(60*1000).get();
			Element menu = booksIndexDoc.getElementById("dcs_tree");
			Elements anchors = menu.select("li > ul > li > ul > li");
			for (Element anchor : anchors) {
				if (anchor.hasAttr("dcslink")) {
					String fileHref =  anchor.attr("dcslink");
					String[] hrefParts = fileHref.split("/");
					if (hrefParts.length == 5) {
						if (hrefParts[0].equals("h")) {
							if (hrefParts[3].equals("gr-en")) {
								AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
								row.setDate("any");
								row.setDayOfWeek("any");
								row.setType(hrefParts[2]);
								row.setUrl(baseUrl + fileHref);
								result.addRow(row);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}

	public AgesIndexTableData  toReactTableDataFromJson() throws Exception {
		AgesIndexTableData result = new AgesIndexTableData(printPretty);
		Connection serviceIndexConnection = null;
		Connection booksIndexConnection = null;
		Document booksIndexDoc = null;
		try {
			serviceIndexConnection = Jsoup.connect(this.jsonservicesIndex);
			String s  = serviceIndexConnection
					.timeout(60*1000)
					.maxBodySize(0)
					.ignoreContentType(true)
					.execute()
					.body();
			JsonParser p = new JsonParser();
			JsonObject o = p.parse(s).getAsJsonObject();
			// years
			JsonArray years = o.get("years").getAsJsonArray();
			for (JsonElement year : years) {
				for (Entry<String, JsonElement> yearEntry : year.getAsJsonObject().entrySet()) {
					// months
					JsonArray months = yearEntry.getValue().getAsJsonArray();
					for (JsonElement month : months) {
						for (Entry<String, JsonElement> monthEntry : month.getAsJsonObject().entrySet()) {
							// days
							JsonArray days = monthEntry.getValue().getAsJsonArray();
							for (JsonElement day : days) {
								for (Entry<String, JsonElement> dayEntry : day.getAsJsonObject().entrySet()) {
									// service types
									JsonArray serviceTypes = dayEntry.getValue().getAsJsonArray();
									for (JsonElement serviceType : serviceTypes) {
										for (Entry<String, JsonElement> serviceTypeEntry : serviceType.getAsJsonObject().entrySet()) {
											// service language combinations
											JsonArray serviceLanguages = serviceTypeEntry.getValue().getAsJsonArray();
											for (JsonElement serviceLanguage : serviceLanguages) {
												for (Entry<String, JsonElement> serviceLanguageEntry : serviceLanguage.getAsJsonObject().entrySet()) {
													JsonObject serviceLanguageEntryValue = serviceLanguageEntry.getValue().getAsJsonArray().get(0).getAsJsonObject();
													String fileHref = serviceLanguageEntryValue.get("href").getAsString();
													String type = serviceLanguageEntryValue.get("type").getAsString();
													if (serviceLanguageEntry.getKey().startsWith("GR-EN") && type.startsWith("Text/Music")) {
														String theYear = fileHref.substring(4, 8);
														String theMonth = fileHref.substring(9, 11);
														String monthDay = fileHref.substring(12,14);
														String date = theYear + "/" + theMonth + "/" + monthDay;
														String dayOfWeekName = dayEntry.getKey().trim().split("-")[1];
														String [] hrefParts = fileHref.split("/");
														if (hrefParts.length == 8) {
																	AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
																	row.setDate(date);
																	row.setDayOfWeek(dayOfWeekName);
																	row.setType(serviceTypeEntry.getKey());
																	row.setUrl(baseUrl + fileHref);
																	result.addRow(row);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			booksIndexConnection = Jsoup.connect(booksIndex);
			booksIndexDoc = booksIndexConnection.timeout(60*1000).maxBodySize(0).get();
			Element menu = booksIndexDoc.getElementById("dcs_tree");
			Elements anchors = menu.select("li > ul > li > ul > li");
			for (Element anchor : anchors) {
				if (anchor.hasAttr("dcslink")) {
					String fileHref =  anchor.attr("dcslink");
					String[] hrefParts = fileHref.split("/");
					if (hrefParts.length == 5) {
						if (hrefParts[0].equals("h")) {
							if (hrefParts[3].equals("gr-en")) {
								AgesIndexTableRowData row = new AgesIndexTableRowData(printPretty);
								row.setDate("any");
								row.setDayOfWeek("any");
								row.setType(hrefParts[2]);
								row.setUrl(baseUrl + fileHref);
								result.addRow(row);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}
}