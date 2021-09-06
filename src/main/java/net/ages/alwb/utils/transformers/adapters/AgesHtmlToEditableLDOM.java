package net.ages.alwb.utils.transformers.adapters;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.ocmc.ioc.liturgical.schemas.models.LDOM.LDOM;
import org.ocmc.ioc.liturgical.schemas.models.LDOM.LDOM_Element;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ioc.liturgical.ws.constants.Constants;
import ioc.liturgical.ws.managers.databases.external.neo4j.ExternalDbManager;
import net.ages.alwb.utils.core.id.managers.IdManager;
import net.ages.alwb.utils.transformers.adapters.models.LdomTitleRow;

import org.jsoup.nodes.Element;

/**
 * Reads an AGES HTML file and converts it to
 * a populated Template that has AGES Greek 
 * on the left and AGES English on the right.
 * The center column will be used for the translation
 * the user is editing.
 * 
 * TODO: ideally we would save the LDOM in the database and reuse it if the
 * timestamp has not changed on the AGES website.  But, the problem is
 * that the editable version inserts a middle column.  So, we need to modify
 * the code to save a version without the middle column, and insert it 
 * after retrieving the LDOM from the database.  Then we can reuse the LDOMs.
 * @author mac002
 *
 */
public class AgesHtmlToEditableLDOM {
	private static final Logger logger = LoggerFactory.getLogger(AgesHtmlToEditableLDOM.class);
	private boolean printPretty = false;
	private boolean checkDb = true;
	private String url = "";
	private String centerLibrary = "";
	private boolean isDailyReading = false;
	private String readingYear = "";
	private String readingMonth = "";
	private ExternalDbManager dbManager = null;
	private String timestamp = "unknown";
	
	public AgesHtmlToEditableLDOM(
			String url
			, ExternalDbManager dbManager
			, boolean checkDb
			, boolean printPretty
			) {
		this.url = url;
		this.dbManager = dbManager;
		this.printPretty = printPretty;
		this.checkDb = checkDb;
	}

	public AgesHtmlToEditableLDOM(
			String url
			, ExternalDbManager dbManager
			, String centerLibrary
			, boolean checkDb
			, boolean printPretty
			) {
		this.url = url;
		this.dbManager = dbManager;
		this.centerLibrary = centerLibrary;
		this.printPretty = printPretty;
		this.checkDb = checkDb;
	}

	/**
	 * 
	 * Walks up the hierarchy of the parents of element e
	 * until it finds the parent that is the td tag (the table cell).
	 * Returns the class for that cell.
	 * @param e
	 * @return
	 */
	private String getClassOfTd(Element e) {
		String result = "";
		for (Element p : e.parents()) {
			if (p.tagName().equals("td")) {
				result = p.attr("class");
			}
		}
		return result;
	}
	/**
	 * Gets the content for the specified URL
	 * Builds an array of the ids used in the content.  They are a set (no duplicates).
	 */
	public LDOM getValues(
			Elements valueSpans
			, Elements versionDesignations
			) throws Exception {
		LDOM result = new LDOM(url, this.timestamp, printPretty);
		try {
	        for (Element valueSpan : valueSpans) {
	        	String tdClass = this.getClassOfTd(valueSpan);
	        	String dataKey = valueSpan.attr("data-key");
	        	String [] parts = dataKey.split("\\|");
	        	if (parts.length == 0) {
	        		continue;
	        	}
	        	String key = parts[1];
	        	parts = parts[0].split("_");
	        	String domain = "";
	        	if (tdClass.equals("centerCell")) {
	        		domain = this.centerLibrary;
	        	} else {
	        		if (parts.length == 4) { // for some as yet unknown reason AGES html has some data-keys without a domain
		        		domain =
			        			parts[1] 
			    						+ Constants.DOMAIN_DELIMITER 
			    						+ parts[2].toLowerCase() 
			    						+ Constants.DOMAIN_DELIMITER 
			    	        			+ parts[3]
			    	        	;
	        		} else {
	        			domain = "gr_GR_cog"; // don't know what it should be so use Greek
	        		}
	        	}
	        	String topic = parts[0];
	        	String value = "";
	        	if (valueSpan.hasClass("key")) {
	        		value = valueSpan.parent().text();
	        	} else {
		        	value = valueSpan.text();
	        	}
	        	if (value.contains("~")) {
	        		value = value.replaceAll("~", " ~ ");
	        	}

        		if (domain.equals("gr_gr_cog") || domain.equals("en_us_dedes")) {
        			// ignore
        		} else {
    				if (value.startsWith("[saint") || value.startsWith("[para")) {
    					if (! tdClass.equals("centerCell")) {
        					domain = "en_us_dedes";
    					}
    				}
        		}

	        	String topicKey = topic + Constants.ID_DELIMITER + key;
	        	IdManager idManager = new IdManager(domain, topic, key);
	        	result.addDomain(domain);
	        	result.addTopicKey(topicKey);
	        	result.addValue(idManager.getId(), value, true);
	        	if (tdClass.equals("centerCell")) {
	        		valueSpan.attr(
	        				"data-key"
	        				, topic
	        					+ "_" 
	        						+ domain 
	        						+ "|" 
	        						+ key
	        		); // e.g., titles_en_US_dedes|OnSunday
	        	}
	        }
	        for (Element valueSpan : versionDesignations) {
	        	String tdClass = this.getClassOfTd(valueSpan);
	        	String dataKey = valueSpan.select("span.key").attr("data-key");
	        	if (dataKey.length() > 0) {
		        	String key = "";
		        	String [] parts = dataKey.split("\\|");
		        	if (parts.length == 2) {
			        	key = parts[1];
			        	parts = parts[0].split("_");
			        	String domain = "";
			        	if (tdClass.equals("centerCell")) {
			        		domain = this.centerLibrary;
			        	} else {
			        		domain =
				        			parts[1] 
				    						+ Constants.DOMAIN_DELIMITER 
				    						+ parts[2].toLowerCase() 
				    						+ Constants.DOMAIN_DELIMITER 
				    	        			+ parts[3]
				    	        	;
			        	}
			        	String topic = parts[0];
			        	String value = "";
			        	if (valueSpan.hasClass("key")) {
			        		value = valueSpan.parent().text();
			        	} else {
				        	value = valueSpan.text();
			        	}
			        	if (value.contains("~")) {
			        		value = value.replaceAll("~", " ~ ");
			        	}
			        	String topicKey = topic + Constants.ID_DELIMITER + key;
			        	IdManager idManager = new IdManager(domain, topic, key);
			        	result.addDomain(domain);
			        	result.addTopicKey(topicKey);
			        	result.addValue(idManager.getId(), value, true);
			        	if (tdClass.equals("centerCell")) {
			        		valueSpan.attr(
			        				"data-key"
			        				, topic
			        					+ "_" 
			        						+ domain 
			        						+ "|" 
			        						+ key
			        		); 
			        		valueSpan.addClass("kvp");
			        		valueSpan.children().remove();
			        	}
		        	}
	        	} else {
	        		logger.error("VersionDesignation missing dataKey " + valueSpan);
	        	}
	        }
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	private List<LDOM_Element> getChildren(Elements children, int seq) throws Exception {
		List<LDOM_Element> result = new ArrayList<LDOM_Element>();
		try {
			for (Element child : children) {
				try {
					LDOM_Element eChild = new LDOM_Element(true);
					eChild.setTag(child.tagName());
					if (child.hasAttr("class")) {
						eChild.setClassName(child.attr("class"));
					}
					if (child.parent().hasAttr("class")) {
						eChild.setParentClassName(child.parent().attr("class"));
					}
					if (child.hasAttr("data-key")) {
			        	String dataKey = child.attr("data-key");
			        	String [] parts = dataKey.split("\\|");
			        	if (parts.length == 0) {
			        		continue;
			        	}
			        	String key = parts[1];
			        	parts = parts[0].split("_");
			        	String domain = "gr_GR_cog";
			        	if (parts.length == 4) {
				        	domain = 
				        			parts[1] 
									+ Constants.DOMAIN_DELIMITER 
									+ parts[2].toLowerCase() 
									+ Constants.DOMAIN_DELIMITER 
				        			+ parts[3]
				        	;
			        	}
			        	String topic = parts[0];
			        	String topicKey = topic + Constants.ID_DELIMITER + key;
			        	IdManager idManager = new IdManager(domain, topic, key);
			        	eChild.setDataDomain(domain);
						eChild.setDataKey(idManager.getId());
						eChild.setTopicKey(topicKey);
					}
					seq = seq + 1;
					eChild.setKey("V" + seq);
					if (child.children().size() > 0) {
						eChild.setChildren(getChildren(child.children(), seq));
					}
					result.add(eChild);
				} catch (Exception inner) {
					throw inner;
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}
	
	/**
	 * This version removes the media-group
	 * @return
	 * @throws Exception
	 */
	public LDOM toLDOM() throws Exception {
		LDOM result = null;
		Connection c = null;
		Document doc = null;
		Element content = null;
		try {
			c = Jsoup.connect(url);
			doc = c.timeout(60*1000)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.2 Safari/605.1.15")
					.maxBodySize(0).get();
			this.timestamp = doc.select("title").attr("data-timestamp");
			
			if (this.checkDb) {
			}
			result = new LDOM(url, timestamp, printPretty);

			content = doc.select("div.content").first();
			// remove rows that contain a media-group
			content.select("tr:has(div.media-group)").remove();
			content.select("tr:has(div.media-group-empty)").remove();
			
			// see if this webpage is for daily readings for OCMC
			String titleText = doc.select("title").text();
			if (titleText.startsWith("cu.ocmc_guatemala")) {
				String [] titleParts = titleText.split("_");
				if (titleParts.length == 4) {
					this.isDailyReading = true;
					this.readingYear = titleParts[2];
					this.readingMonth = titleParts[3];
				}
			}
			
			// See if this is a lectionary.  If so, we need to handle the day titles.
			if (this.isDailyReading) {
				Elements boldredSpans = content.select("span.boldred");
				for (Element boldred : boldredSpans) {
					if (boldred.select("span.kvp").isEmpty()) {
						String library =  "gr_gr_cog";
						String day = "";
						if (boldred.parent().parent().hasClass("rightCell")) {
							library = "en_us_dedes";
						}
						String text = boldred.text();
						String [] parts = text.split(",");
						if (parts.length == 3) {
							parts = parts[1].trim().split(" ");
							if (parts.length == 2) {
								int intDay = Integer.parseInt(parts[1].trim());
								day = String.format("%02d", intDay);
							} else {
								logger.info("Unexpected text format.  Not a date? " + text);
							}
						} else {
							logger.info("Unexpected text format.  Not a date? " + text);
						}
						String newKey = "calendar_" 
								+ library 
								+ "|y" 
								+ this.readingYear 
								+ ".m" 
								+ this.readingMonth
								+ ".d" 
								+ day
								+ ".md";
						boldred.attr("class", "kvp");
						boldred.attr("data-key", newKey);
						boldred.parent().attr("class","designation");
//						boldred.parent().tagName("h3");
					}
				}
			}
			
			Elements htmlTabs = doc.select("span[data-key^=template.titles_gr_GR_cog]").select("span[data-key$=html.tab]");
			if (htmlTabs.size() == 0) {
				LdomTitleRow ldomTitleRow = new LdomTitleRow(this.dbManager, doc);
				if (ldomTitleRow.needsTitleRow()) {
					try {
						doc.select("tr").first().before(ldomTitleRow.getTitleRowHtml());
					} catch (Exception e) {
						ErrorUtils.report(logger, e);
					}
				}
			}

			this.normalizeRightColumnKeys(content); // if the right column does not have the expected English IDs, convert to en_us_dedes
			
			if (this.centerLibrary.length() > 0) {
				this.cloneRightColumn(content);
			}
			Elements versionDesignations = content.select("span.versiondesignation");
			Elements keys = content.select("span.kvp");
			if (keys.size() == 0) {
				keys = content.select("span.key");
			}
			LDOM values = this.getValues(keys, versionDesignations);
			result.setDomains(values.getDomains());
			result.setTopicKeys(values.getTopicKeys());
			result.setValues(values.getValues());
			
			if (this.centerLibrary.length() > 0) {
				content.select("td.leftCell").forEach(e -> e.attr("class", "cellOneOfThree"));
				content.select("td.centerCell").forEach(e -> e.attr("class", "cellTwoOfThree"));
				content.select("td.rightCell").forEach(e -> e.attr("class", "cellThreeOfThree"));
			} else  {
				content.select("td.leftCell").forEach(e -> e.attr("class", "cellOneOfTwo"));
				content.select("td.rightCell").forEach(e -> e.attr("class", "cellTwoOfTwo"));
			}

			LDOM_Element eContent = new LDOM_Element(printPretty);
			eContent.setTag(content.tagName());
			eContent.setClassName(content.attr("class"));
			if (content.parent().hasAttr("class")) {
				eContent.setParentClassName(content.parent().attr("class"));
			}
			eContent.setChildren(this.getChildren(content.children(), 0));
			result.setTopElement(eContent);
		} catch (Exception e) {
			throw e;
		}
		return result;
	}
	// <td class="rightCell"><p class="reading"><span class="kvp" data-key="sy.m01.d01_spa_GT_odg|cl.S01.commemoration.text">[saint/com 1]</span> </p></td>
	private void cloneRightColumn(Element content) {
		for (Element cell : content.select("td.rightCell")) {
			Element clone = cell.clone();
			clone.attr("class","centerCell");
			cell.before(clone);
		}
	}
	
	/**
	 * When Fr. Seraphim created the ALWB templates for the daily readings for OCMC Guatemala,
	 * he set the right-column domains for [saint... and [paragraph... to be spa_gt_odg.
	 * They should be en_us_dedes.  This fixes the situation.
	 * 
	 * @param content
	 */
	private void normalizeRightColumnKeys(Element content) {
		for (Element cell : content.select("td.rightCell")) {
			Element span = null;
			String dataKey = "";
			String id = "";
			IdManager idManager = null;
		    try {
				span = cell.select("span.kvp").first();
				if (span != null && span.hasAttr("data-key")) {
					dataKey = span.attr("data-key");
					id = org.ocmc.ioc.liturgical.schemas.id.managers.IdManager.dataKeyToId(dataKey);
					idManager = new IdManager(id);
					if (idManager.getLibraryLanguage() != null) {
						if (! idManager.getLibraryLanguage().equals("en")) {
							if (span.hasText() && span.text().startsWith("[")) {
								dataKey = idManager.getTopic() + "_en_us_dedes|" + idManager.getKey();
								span.attr("data-key", dataKey);
							}
						}
					}
				}
		    } catch (Exception e) {
		    	ErrorUtils.report(logger, e);
		    }
		}
	}
}
