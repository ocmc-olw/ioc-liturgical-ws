package net.ages.alwb.utils.transformers.adapters;

import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.ocmc.ioc.liturgical.schemas.models.LDOM.KvpCellElement;
import org.ocmc.ioc.liturgical.schemas.models.LDOM.LDOM;
import org.ocmc.ioc.liturgical.schemas.models.LDOM.LDOM_Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import net.ages.alwb.utils.core.id.managers.IdManager;
import net.ages.alwb.utils.oslw.OslwUtils;
import net.ages.alwb.utils.transformers.adapters.models.OslwCellElement;
import net.ages.alwb.utils.transformers.adapters.models.OslwRowElement;

/**
 * Liturgical books and services are provided by GOARCH DCS as
 * html and PDF files, and by OSLW as PDF files.
 * 
 * For both AGES and OSLW, template files are created that specify
 * the order of information and its formatting.  The information is 
 * retrieved using the combination of a domain, topic, and key.
 * 
 * A meta template can be created from the AGES HTML files, or from the OSLO tex files.
 *  
 * Such a meta template is used by this class to create a PDF file.
 *  
 * This class reads a json string encoding the meta Template for a liturgical book or service,
 * generates OSLO files from the metadata,
 * calls Xelatex to generate a PDF file
 * and returns the path to the file.
 * 
 * The generated PDF can have one, two, or three columns.
 * If one column, there is only a left library to be used.
 * If two columns, there is a left and right library to be used.
 * If three columns, there is a left, center, and right library to be used.
 * There are also 'fallback' libraries that can be specified.  That is,
 * if a specified library does not contain the required topic/key, then
 * the fallback library will be searched.
 * 
 * If the language = English, if the fallback is not found, it will default to AGES_ENGLISH,
 * i.e. en_us_dedes.
 * 
 * Otherwise, if the fallback is not found, it will use gr_gr_cog.
 * 
 */

public class MetaTemplateToPdf {
	private static final Logger logger = LoggerFactory.getLogger(MetaTemplateToPdf.class);
	private 	LDOM template  = null;
	private 	String metaTemplateJsonString = "";
	private 	String basePath = "";
	private 	String pathToPdf = "";
	private List<OslwRowElement> list = new ArrayList<OslwRowElement>();
	private StringBuffer texFileSb = new StringBuffer(); // latex tex file content
	
	public MetaTemplateToPdf (
			LDOM metaTemplate
			)   throws JsonParseException {
		this.template = metaTemplate;
		this.process();
	}

	/**
	 * @param metaTemplateJsonString - a json string containing the metadata of the service
	 * @throws JsonParseException if the metaService is not valid json
	 */
	public MetaTemplateToPdf (
			String metaTemplateJsonString
			)   throws JsonParseException {
		this.metaTemplateJsonString = metaTemplateJsonString;
		Gson gson = new Gson();
		template = gson.fromJson(
				metaTemplateJsonString
				, LDOM.class
				);
		this.process();
	}
	
	public void process() {

		String chinese = "zho_";
		String chineseShort = "zh_";
		String korean = "kor_";
		String slavonic = "chu_";
		
		boolean hasChinese = 
				template.getLeftLibrary().startsWith(chinese)
				|| template.getCenterLibrary().startsWith(chinese)
				|| template.getRightLibrary().startsWith(chinese)
				|| template.getLeftLibrary().startsWith(chineseShort)
				|| template.getCenterLibrary().startsWith(chineseShort)
				|| template.getRightLibrary().startsWith(chineseShort);

		boolean hasKorean = 
				template.getLeftLibrary().startsWith(korean)
				|| template.getCenterLibrary().startsWith(korean)
				|| template.getRightLibrary().startsWith(korean);

		boolean singleColumn = 
				(template.getCenterLibrary() == "" || template.getCenterLibrary().length() == 0)
				&& (template.getRightLibrary() == "" || template.getRightLibrary().length() == 0)
		;
		this.texFileSb.append("\\documentclass[extrafontsizes,14pt]{memoir}\n");
		
		if (hasChinese) {
			this.texFileSb.append("\\usepackage[hyphenate,chinese]{system/ocmc-liturgical-text}%\n");
		} else	if (hasKorean) {
				this.texFileSb.append("\\usepackage[hyphenate,korean]{system/ocmc-liturgical-text}%\n");
		} else {
			this.texFileSb.append("\\usepackage[hyphenate]{system/ocmc-liturgical-text}%\n");
		}
		this.texFileSb.append("\\input{system/control} %\n");
		if (singleColumn) {
			this.texFileSb.append("\\usepackage{multicol}%\n");
		}
		this.texFileSb.append("\\usepackage[defaultlines=4,all]{nowidow}%\n");
		
		this.texFileSb.append("\\begin{document}%\n");
		if (template.url.contains("guatemala")) {
//			this.texFileSb.append("\\renewcommand{\\contentsname}{Índice}}\n\n");
//			this.texFileSb.append("\n\\tableofcontents\n\n\\vfill");
		}

		this.texFileSb.append("\\mainmatter%\n");
		this.texFileSb.append("\\ltColumnsOn%\n");

		if (template.getLeftLibrary() != null && template.getLeftLibrary().length() > 0) {
			IdManager m = new IdManager();
			m.setLibrary(template.getLeftLibrary());
			this.texFileSb.append(m.getOslwSetDomain(IdManager.COLUMNS.Primary));
		}
		if (template.getCenterLibrary() != null && template.getCenterLibrary().length() > 0) {
			IdManager m = new IdManager();
			m.setLibrary(template.getCenterLibrary());
			if (template.getRightLibrary() == null || template.getRightLibrary().length() == 0) {
				this.texFileSb.append(m.getOslwSetDomain(IdManager.COLUMNS.Right));
			} else {
				this.texFileSb.append(m.getOslwSetDomain(IdManager.COLUMNS.Center));
			}
		}
		if (template.getRightLibrary() != null && template.getRightLibrary().length() > 0) {
			IdManager m = new IdManager();
			m.setLibrary(template.getRightLibrary());
			this.texFileSb.append(m.getOslwSetDomain(IdManager.COLUMNS.Right));
		}
		this.valuesToOslw();
		
		if (template.getLeftTitle().length() == 0) {
			StringBuffer sb = new StringBuffer();
			if (template.getUrl().contains("/h/b/oc/")) {
				try {
					URL url = new URL(template.getUrl());
					String [] parts = template.getUrl().split("/h/b/oc/");
					parts = parts[parts.length-1].split("/");
					if (parts.length > 3) {
						String mode = parts[0];
						String day = parts[1];
						sb.append("Octoechos Mode ");
						sb.append(mode.substring(1));
						sb.append(" Day ");
						sb.append(day.substring(1));
						if (parts.length > 4) {
							sb.append(" (Orthros)");
						}
					}
					String title = sb.toString();
					template.setLeftHeaderTitle(title);
					template.setLeftTitle(title);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// add the values for the titles
		this.texFileSb.append(OslwUtils.getOslwTitleResources(
				template.getLeftLibrary()
				, template.getLeftTitle()
				, template.getLeftTitle()
				, template.getLeftHeaderTitle()
				, template.getLeftTitleDate()
				)
		);
		if (template.getCenterLibrary() != null && template.getCenterLibrary() != "") {
			this.texFileSb.append(OslwUtils.getOslwTitleResources(
					template.getCenterLibrary()
					, template.getCenterTitle()
					, template.getCenterTitle()
					, template.getCenterHeaderTitle()
					, template.getCenterTitleDate()
					)
			);
		}
		if (template.getRightLibrary() != null && template.getRightLibrary() != "") {
			this.texFileSb.append(OslwUtils.getOslwTitleResources(
					template.getRightLibrary()
					, template.getRightTitle()
					, template.getRightTitle()
					, template.getRightHeaderTitle()
					, template.getRightTitleDate()
					)
			);
		}

		this.texFileSb.append("\n\\ltColumnsOn\n");

		// set the name of the template
		this.texFileSb.append("\\ltChapter{pdf}{title}\n");
		if (template.getLeftTitleDate() != null && template.getLeftTitleDate().length() > 0) {
			this.texFileSb.append("\\ltServiceDate{pdf}{title.date}\n");
		}
		
		if (singleColumn) {
			this.texFileSb.append("\\begin{multicols}{2}%\n");
		}
		this.process(template.getTopElement());
		int redirectCount = 0;
		for (OslwRowElement rowElement : list) {
			OslwCellElement cell1 = rowElement.getCells().get(0);
			OslwCellElement cell2 = null;
			OslwCellElement cell3 = null;
			
			/**
			 * The OSLW \lt commands can only handle up to four IDs. 
			 * So, we need to check the number of cell elements.
			 * If the exceed four, we will generate Rids instead (see below).
			 */
			boolean moreThanFourKeys = cell1.getKvpCellElements().size() > 4;

			if (rowElement.getCells().size() > 1) {
				cell2 = rowElement.getCells().get(1);
				if (rowElement.getCells().size() > 2) {
					cell3 = rowElement.getCells().get(2);
				}
			}
			/**
			 * Check to see if the keys match for all cells.
			 * If not, we will create a special set of redirects with RIDs.
			 */
			if (cell1.commands.size() > 0) {
				boolean allCellsKeysMatch = true;
				
				if (cell2 != null) {
					if (! cell2.keys.toString().equals(cell1.keys.toString())) {
						allCellsKeysMatch = false;
					}
				}
					
				if (cell3 != null) {
					if (! cell3.keys.toString().equals(cell1.keys.toString())) {
						allCellsKeysMatch = false;
					}
				}
				
				if (moreThanFourKeys || ! allCellsKeysMatch) {
					redirectCount++;
					this.texFileSb.append(
							this.getRids(
									rowElement
									, redirectCount
									, template.leftLibrary
									, template.centerLibrary
									, template.rightLibrary
									)
							);
				} else {
					this.texFileSb.append(cell1.toOslw());
				}
				
				if (singleColumn) {
					this.texFileSb.append("\n");
				}
			}
		}
		if (singleColumn) {
			this.texFileSb.append("\\end{multicols}%\n");
		}
		this.texFileSb.append("\\ltColumnsOff%\n");
		this.texFileSb.append(
				"\\tiny\\textit{Generated ");
		this.texFileSb.append(ZonedDateTime.now(ZoneOffset.UTC));
		this.texFileSb.append(" (Universal Time)  using OCMC liturgical software and a template from GOARCH DCS, Inc.}%\n");
		this.texFileSb.append("\\vfill%\n");
		this.texFileSb.append("\\pagebreak%\n");
		this.texFileSb.append("\\end{document}%\n");
	}
	
	private String getRids(
			OslwRowElement row
			, int redirectCount
			, String leftLibrary
			, String centerLibrary
			, String rightLibrary
			) {
		StringBuffer result = new StringBuffer();
		OslwCellElement cell1 = row.getCells().get(0);
		OslwCellElement cell2 = null;
		OslwCellElement cell3 = null;
		int cell1IdCount = cell1.getKvpCellElements().size();
		int cell2IdCount = 0;
		int cell3IdCount = 0;
		if (row.getCells().size() > 1) {
			cell2 = row.getCells().get(1);
			cell2IdCount = cell2.getKvpCellElements().size();
			if (row.getCells().size() > 2) {
				cell3 = row.getCells().get(2);
				cell3IdCount = cell3.getKvpCellElements().size();
			}
		}
		int maxIdCount = Math.max(Math.max(cell1IdCount, cell2IdCount), cell3IdCount);
		boolean hasDesignation = false; 
		result.append(cell1.toOslwRedirectResources(redirectCount, maxIdCount, leftLibrary));
		if (row.getCells().size() > 1) {
			result.append(cell2.toOslwRedirectResources(redirectCount, maxIdCount, centerLibrary));
			hasDesignation = cell2.getVersionDesignationId().length() > 0; 
			if (row.getCells().size() > 2) {
				result.append(cell3.toOslwRedirectResources(redirectCount, maxIdCount, rightLibrary));
				if (hasDesignation == false) {
					hasDesignation = cell3.getVersionDesignationId().length() > 0;
				}
			}
		}
		result.append(cell1.toOslwRedirect(redirectCount, maxIdCount, hasDesignation)); 
		return result.toString();
	}
	
	/**
	 * Process the json element to extract the information
	 * and create the latex template file data
	 */
	private void process(LDOM_Element e) {
		if (e.getTag().equals("tr")) {
			OslwRowElement oslwRow = new OslwRowElement();
			for (LDOM_Element child : e.getChildren()) {
				oslwRow.addCellElement(this.processRowCell(child));
			}
			OslwCellElement firstCell = oslwRow.getCells().get(0);
			String command = firstCell.commands.toString();
			if (firstCell.toOslw().length() < 2 
					|| command.toString().contains("bmc_")
					|| command.toString().contains("emc_")
					|| command.toString().equals("span")
					|| firstCell.keys.toString().contains("media.key")
				) {
				// ignore
			} else {
				this.list.add(oslwRow);
			}
		} else {
			for (LDOM_Element child : e.children) {
				this.process(child);
			}
		}
	}
	
	private OslwCellElement processRowCell(LDOM_Element cell) {
		OslwCellElement result  = new OslwCellElement();
		result.setCellClassName(cell.getClassName());
		for (LDOM_Element s : cell.getChildren()) {
			// the current class name for an ALWB HTML data-key is kvp.  We used to use just key 
			// as the classname.  Some html files on the AGES site still have the old class name.
			if (s.getClassName().startsWith("kvp") || s.getClassName().startsWith("key")) {
				result.addCommand(s.getTag());
				result.addKvpCellElement(new KvpCellElement(s));
			} else {
				result.addCommand(parseElement(s));
			}
			for (LDOM_Element t : s.getChildren()) {
				result.appendAll(processRowElement(t));
			}
		}
		return result;
	}

	private OslwCellElement processRowElement(LDOM_Element e) {
		OslwCellElement result  = new OslwCellElement();
		// the current class name for an ALWB HTML data-key is kvp.  We used to use just key 
		// as the classname.  Some html files on the AGES site still have the old class name.
		if (e.getClassName().startsWith("kvp") || e.getClassName().startsWith("key")) {
//			result.addCommand(e.parentClassName);
			if (e.parentClassName.toLowerCase().equals("mixed")) {
				result.addCommand("-text");
			}
			result.addKey(parseElement(e));
			result.addKvpCellElement(new KvpCellElement(e));
		} else {
//			if (e.getClassName().toLowerCase().equals("mixed")) {
				result.addCommand(parseElement(e));
//			}
		}
		for (LDOM_Element c : e.getChildren()) {
			if (c.getClassName().startsWith("kvp") || c.getClassName().startsWith("key")) {
				if (e.parentClassName.toLowerCase().equals("mixed")) {
					result.addCommand("-text");
				}
				result.addKey(parseElement(c));
				result.addKvpCellElement(new KvpCellElement(c));
				if (c.getDataKey().endsWith("version.designation")) {
					result.setVersionDesignationId(c.getDataKey());
				}
			} else {
				result.addCommand(parseElement(c) + "~");
			}
		}
		return result;
	}

	/**
	 * Parses the element to get either the data-key
	 * or the tag and/or class name depending on
	 * the type of element.
	 * @param e
	 * @return
	 */
	private String parseElement(LDOM_Element e) {
		StringBuffer sb = new StringBuffer();
		// the current class name for an ALWB HTML data-key is kvp.  We used to use just key 
		// as the classname.  Some html files on the AGES site still have the old class name.
		if (e.getClassName().startsWith("kvp") || e.getClassName().startsWith("key")) {
			sb.append(e.getDataKey());
		} else {
			if (! e.getTag().startsWith("span")) {
				sb.append(e.getTag()); 
			}
			if (e.getClassName().length() > 0) {
				sb.append("-"); 
				sb.append(e.getClassName()); 
			}
		}
		return sb.toString();
	}

	public void valuesToOslw() {
		for (Entry<String,String> entry :this.template.getValues().entrySet()) {
			IdManager m = new IdManager(entry.getKey());
			if (m.getKey().equals("OAnthraxToIsaia.text")) {
				System.out.print("");
			}
			List<String> values = new ArrayList<String>();
			values.add(entry.getKey());
			String oslw = m.getOslwResourceForValue(entry.getValue());
			if (oslw.contains("media.key")) {
				// ignore
			} else {
				this.texFileSb.append(oslw);
			}
		}
	}
	public String getMetaTemplateJsonString() {
		return metaTemplateJsonString;
	}

	public void setMetaTemplateJsonString(String metaTemplateJsonString) {
		this.metaTemplateJsonString = metaTemplateJsonString;
	}


	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getPathToPdf() {
		return pathToPdf;
	}

	public void setPathToPdf(String pathToPdf) {
		this.pathToPdf = pathToPdf;
	}

	public LDOM getTemplate() {
		return template;
	}

	public void setTemplate(LDOM template) {
		this.template = template;
	}
	
	/**
	 * Converts the template to Oslw Template Commands
	 * @return
	 */
	public String getOslwTemplateCommands() {
		StringBuffer result = new StringBuffer();
		for (OslwRowElement rowElement : this.list) {
			result.append(rowElement.getCells().get(0).toOslw());
		}
		return result.toString();
	}

	/**
	 * Get the the Xeatex tex file content that Xelatex can use
	 * to create the PDF.
	 * @return
	 */
	public StringBuffer getTexFileContent() {
		return texFileSb;
	}

	public void setTexFileContent(StringBuffer resFileSb) {
		this.texFileSb = resFileSb;
	}


}
