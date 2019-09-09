package ioc.liturgical.ws.controllers.db.neo4j;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletResponse;

import org.ocmc.ioc.liturgical.schemas.constants.SINGLETON_KEYS;
import org.ocmc.ioc.liturgical.schemas.constants.TOPICS;
import org.ocmc.ioc.liturgical.schemas.constants.HTTP_RESPONSE_CODES;
import org.apache.commons.io.FileUtils;
import org.joda.time.Instant;
import org.ocmc.ioc.liturgical.schemas.constants.ENDPOINTS_DB_API;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.RequestStatus;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.ResultJsonObjectArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.delete;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import ioc.liturgical.ws.app.ServiceProvider;
import ioc.liturgical.ws.constants.Constants;
import ioc.liturgical.ws.controllers.admin.ControllerUtils;
import ioc.liturgical.ws.managers.auth.AuthDecoder;
import ioc.liturgical.ws.managers.databases.external.neo4j.ExternalDbManager;

public class Neo4jController {
	private static final Logger logger = LoggerFactory.getLogger(Neo4jController.class);
	
    private Gson gson = new GsonBuilder().disableHtmlEscaping().create(); 
    
	/**
	 * returns a login form
	 * @param storeManager
	 */
	public Neo4jController(ExternalDbManager externalManager) {
	
		int pCnt = 0;
		// GET analyses for treebank matching specified parameters
		String path = ENDPOINTS_DB_API.TREEBANKS.toLibraryTopicPath();
		pCnt++;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String idTopic = request.splat()[1];
        	return gson.toJson(externalManager.getDependencyDiagramData(
        			requestor
        			, idTopic
        			, request.queryParams("t")  // ontology type
        			));
		});

		// GET Generation status
		pCnt++;
		path = ENDPOINTS_DB_API.GENERATION_STATUS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return gson.toJson(
        			externalManager.getGenerationStatus(
        					requestor
        					, request.queryParams("i")
        					)
        			);
		});
		
		// GET User Activity
		pCnt++;
		path = "/db/api/v1/activity";
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return gson.toJson(
        			externalManager.getUserActivity(
        					requestor
        					, request.queryParams("f")
        					, request.queryParams("t")
        					)
        			);
		});

		// GET publications
		pCnt++;
		path = "/db/api/v1/publications";
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return gson.toJson(externalManager.getPublications());
		});

		// GET suggestions, e.g. Abbreviations and Bibliography entries
		pCnt++;
		path = ENDPOINTS_DB_API.SUGGESTIONS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return gson.toJson(
        			externalManager.getSuggestions(
        					requestor
        					, request.queryParams("d")
        					)
        			);
		});
		
		// GET User Interface Labels (UI_LABELS)
		pCnt++;
		path = ENDPOINTS_DB_API.UI_LABELS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String[] libraries = null;
			try {
				libraries = request.queryParams("l").split(",");
			} catch (Exception e) {
				libraries = new String[]{"en_sys_ilr", "el_sys_ilr"};
			}
        	return externalManager.getTopicValuesForParaColLabelsEditor(
        			request.queryParams("e") // english system library
        			, libraries
        			).toJsonString();
		});

		// GET generate Text downloads for specified parameters
		pCnt++;
		path = ENDPOINTS_DB_API.LITURGICAL_TEXT_DOWNLOADS.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			String includePersonalNotes = request.queryParams("ip")  ;
			String includeAdviceNotes = request.queryParams("ia")  ;
			String includeGrammar = request.queryParams("ig")  ;
			String combineNotes = request.queryParams("cn")  ;
			String createToc = request.queryParams("toc")  ;
			String alignmentLibrary = request.queryParams("al")  ;
			String pdfTitle = request.queryParams("mt")  ;
			String pdfSubTitle = request.queryParams("st")  ;
			String author = request.queryParams("au")  ;
			String authorTitle = request.queryParams("at")  ;
			String authorAffiliation = request.queryParams("af")  ;
			String citestyle = request.queryParams("cs")  ;
        	return gson.toJson(
        			externalManager.createDownloads(
        					requestor
        					, id
        					, includePersonalNotes
        					, includeAdviceNotes
        					, includeGrammar
        					, combineNotes
        					, createToc
        					, alignmentLibrary
        					, pdfTitle
        					, pdfSubTitle
        					, author
        					, authorTitle
        					, authorAffiliation
        					, citestyle
        					)
        			);
		});

		// GET template by ID
		pCnt++;
		path = ENDPOINTS_DB_API.TEMPLATES.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			ResultJsonObjectArray json = externalManager.getTemplateForId(requestor, id);
			if (json.valueCount > 0) {
				response.status(HTTP_RESPONSE_CODES.OK.code);
			} else {
				response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
			}
			return json.toJsonString();
		});
		
		// GET system table by id 
		pCnt++;
		path = ENDPOINTS_DB_API.TABLES.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
        	return externalManager.getForId(id).toJsonString();
		});


		// GET Biblical or Liturgical Texts for specified parameters
		pCnt++;
		path = ENDPOINTS_DB_API.DOCS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = "*";
			try {
				requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
				if (requestor != "wsadmin" && requestor != "mcolburn") {
					externalManager.updateQueryStats(request.queryParams("l"));
				}
			} catch (Exception e) {
				requestor = "*";
			}
        	return gson.toJson(externalManager.searchText(
        			requestor
        			, request.queryParams("t")  // doc type (e.g. Liturgical, Biblical)
        			, request.queryParams("d")  // domain
        			, request.queryParams("b") // book
        			, request.queryParams("c") // chapter or other major part of book
        			, request.queryParams("q")  // query
        			, request.queryParams("p") // property of the doc (e.g. the ID, the value)
        			, request.queryParams("m") // matcher (e.g. contains, starts with, regex)
        			));
		});

		// GET results of a generic record existence check
		pCnt++;
		path = ENDPOINTS_DB_API.EXISTS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = "*";
			try {
				requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			} catch (Exception e) {
				requestor = "*";
			}
        	return gson.toJson(externalManager.genericIdCheck(
        			requestor
        			, request.queryParams("d")  // domain aka library
        			, request.queryParams("t")  // topic
        			, request.queryParams("k")  // key
        			));
		});

		// GET results of a generic search using the specified parameters
		pCnt++;
		path = ENDPOINTS_DB_API.GENERIC.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = "*";
			try {
				requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			} catch (Exception e) {
				requestor = "*";
			}
        	return gson.toJson(externalManager.searchGeneric(
        			requestor
        			, request.queryParams("t")  // node type (the schema label)
        			, request.queryParams("d")  // domain aka library
        			, request.queryParams("q")  // query
        			, request.queryParams("p") // property of the doc (e.g. the ID, the value)
        			, request.queryParams("m") // matcher (e.g. contains, starts with, regex)
        			, request.queryParams("l") // tags (~labels)
        			, request.queryParams("o") // tag operator (i.e. And vs Or)
        			, request.queryParams("r") // properties to be returned
        			));
		});

		// GET ontology entries for specified parameters
		pCnt++;
		path = ENDPOINTS_DB_API.ONTOLOGY.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = "*";
			try {
				requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			} catch (Exception e) {
				requestor = "*";
			}
        	return gson.toJson(externalManager.searchOntology(
        			requestor
        			, request.queryParams("t")  // ontology type (e.g. Animal)
        			, request.queryParams("g")  // generic type
        			, request.queryParams("q")   // query
        			, request.queryParams("p") // property of the doc (e.g. the ID, the value)
        			, request.queryParams("m") // matcher (e.g. contains, starts with, regex)
        			, request.queryParams("l") // tags (~labels)
        			, request.queryParams("o") // operator
        			));
		});

		// GET text analysis for specified ID
		pCnt++;
		path = ENDPOINTS_DB_API.TEXT_ANALYSIS.toLibraryPath();
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String id = request.splat()[0];
			return externalManager.getWordGrammarAnalyses(requestor, id).toJsonString();
		});

		// GET all notes for a user
		pCnt++;
		path = ENDPOINTS_DB_API.NOTES.toLibraryPath();
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			ResultJsonObjectArray json = externalManager.getForIdStartsWith(query, TOPICS.NOTE_USER);
			if (json.getValueCount() > 0) {
				response.status(HTTP_RESPONSE_CODES.OK.code);
			} else {
				response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
			}
			return json.toString();
		});

		// GET notes matching specified parameters
		pCnt++;
		path = ENDPOINTS_DB_API.NOTES.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return gson.toJson(externalManager.searchNotes(
        			requestor
        			, request.queryParams("t")  // note type (e.g. NoteUser)
        			, request.queryParams("q")   // query
        			, request.queryParams("p") // property of the doc (e.g. the ID, the value)
        			, request.queryParams("m") // matcher (e.g. contains, starts with, regex)
        			, request.queryParams("l") // tags (~labels)
        			, request.queryParams("o") // operator
        			, false // do not return all properties
        			));
		});

		// GET template or template sections matching specified parameters
		pCnt++;
		path = ENDPOINTS_DB_API.TEMPLATES.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return gson.toJson(externalManager.searchTemplates(
        			requestor
        			, request.queryParams("t")  // type (Template or Section)
        			, request.queryParams("q")   // query
        			, request.queryParams("p") // property of the doc (e.g. the ID, the value)
        			, request.queryParams("m") // matcher (e.g. contains, starts with, regex)
        			, request.queryParams("l") // tags (~labels)
        			, request.queryParams("o") // operator
        			));
		});

		// GET analyses for treebank matching specified parameters
		pCnt++;
		path = ENDPOINTS_DB_API.TREEBANKS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return gson.toJson(externalManager.searchTreebanks(
        			requestor
        			, request.queryParams("t")  // type (e.g. UD_ANCIENT_GREEK_PERSEUS)
        			, request.queryParams("r")   // dependency relationship label
        			, request.queryParams("q")   // query
        			, request.queryParams("p") // property of the doc (e.g. the ID, the value)
        			, request.queryParams("m") // matcher (e.g. contains, starts with, regex)
        			, request.queryParams("l") // tags (~labels)
        			, request.queryParams("o") // operator
        			));
		});

		// Get forms for creating new instances of nodes and relationships and ui labels and dropdowns
		pCnt++;
		path = ENDPOINTS_DB_API.NEW.toLibraryPath();
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			if (requestor != "wsadmin" && requestor != "mcolburn") {
				externalManager.updateLoginStats(request.queryParams("l"));
			}
			JsonObject json = externalManager.getUserDropdowns(requestor, query);
			if (json.get("valueCount").getAsInt() > 0) {
				response.status(HTTP_RESPONSE_CODES.OK.code);
			} else {
				response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
			}
			return json.toString();
		});

//		/**
//		 * provides a list of available REST endpoints (i.e. resources)
//		 * @param storeManager
//		 */
//		pCnt++;
//		path = ENDPOINTS_DB_API.NEW.toLibraryPath();
//		ControllerUtils.reportPath(logger, "GET", path, pCnt);
//		get(path, (request, response) -> {
//			response.type(Constants.UTF_JSON);
//			String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
//			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
//			JsonObject json = externalManager.getUserDropdowns(requestor, query);
//			if (json.get("valueCount").getAsInt() > 0) {
//				response.status(HTTP_RESPONSE_CODES.OK.code);
//			} else {
//				response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
//			}
//			return json.toString();
//		});

		// GET domain dropdown lists for specified user
		pCnt++;
		path = Constants.EXTERNAL_DATASTORE_API_PATH  + "/dropdowns/domains/*";
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String user = request.splat()[0];
        	return externalManager.getDomainDropdownsForUser(user).toString();
		});

		// GET domains as a dropdown list
		pCnt++;
		path = Constants.EXTERNAL_DATASTORE_API_PATH  + "/dropdowns/domains";
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getDropdownItemsForSearchingText().toString();
		});

		// GET gr_gr_cog topics as a dropdown list
		pCnt++;
	path = ENDPOINTS_DB_API.DROPDOWNS_GR_LIB_TOPICS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getTopicsDropdown().toJsonString();
		});

		// GET keys and values for specified topic and specified libraries
		// Used by client side Parallel Column Text Editor (ParaColTextEditor)
		pCnt++;
		path = ENDPOINTS_DB_API.VIEW_TOPIC.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String[] libraries = null;
			try {
				libraries = request.queryParams("l").split(",");
			} catch (Exception e) {
				// ignore
			}
        	return externalManager.getTopicValuesForParaColTextEditor(
        			"gr_gr_cog" // for now.  In future, support other source texts
        			, request.queryParams("t")  // topic
        			, libraries
        			).toJsonString();
		});

		// GET dropdowns for searching docs of type text
		pCnt++;
		path = ENDPOINTS_DB_API.DROPDOWNS_TEXTS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getDropdownItemsForSearchingText().toString();
		});

		// GET dropdowns for searching ontology properties
		pCnt++;
	path = ENDPOINTS_DB_API.DROPDOWNS_ONTOLOGY.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getOntologySearchDropdown().toJsonString();
		});

		// GET dropdowns for searching ontology properties
		pCnt++;
		path = ENDPOINTS_DB_API.DROPDOWNS_ONTOLOGY_ENTITIES.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getOntologyEntitiesDropdown(
        			request.queryParams("t")  // link type (e.g. REFERS_TO_HUMAN)
        			).toJsonString();
		});

		// GET dropdowns for searching abbreviations
		pCnt++;
		path = ENDPOINTS_DB_API.DROPDOWNS_ABBREVIATIONS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return externalManager.getDropdownsForSearchingAbbreviations(requestor).toJsonString();
		});

		// GET dropdowns for searching bibliographies
		pCnt++;
		path = ENDPOINTS_DB_API.DROPDOWNS_BIBLIOGRAPHY.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return externalManager.getDropdownsForSearchingBibliographies(requestor).toJsonString();
		});

		// GET dropdowns for searching notes
		pCnt++;
		path = ENDPOINTS_DB_API.DROPDOWNS_NOTES.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return externalManager.getNotesSearchDropdown(requestor).toJsonString();
		});

		// GET dropdowns for searching templates
		pCnt++;
		path = ENDPOINTS_DB_API.DROPDOWNS_TEMPLATES.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getTemplatesSearchDropdown().toJsonString();
		});

		// GET dropdowns for searching treebank
		pCnt++;
		path = ENDPOINTS_DB_API.DROPDOWNS_TREEBANKS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getTreebanksSearchDropdown().toJsonString();
		});

		// GET dropdowns for searching relationship properties
		pCnt++;
		path = ENDPOINTS_DB_API.DROPDOWNS_RELATIONSHIPS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getRelationshipSearchDropdown().toJsonString();
		});

		// GET link (relationships) for specified /library/topic/key
		pCnt++;
		path = ENDPOINTS_DB_API.LINKS.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String library = request.splat()[0];
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			response.type(Constants.UTF_JSON);
        	return gson.toJson(
        			externalManager.getRelationshipById(
        			requestor
        			, library
        			, id
        			)
        	);
		});

		// GET links (relationships) for specified parameters
		pCnt++;
		path = ENDPOINTS_DB_API.LINKS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = "*";
			try {
				requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			} catch (Exception e) {
				requestor = "*";
			}
        	return gson.toJson(externalManager.searchRelationships(
        			requestor
        			, request.queryParams("t")  // link type (e.g. REFERS_TO_BIBLICAL_TEXT)
        			, request.queryParams("d")  // domain
        			, request.queryParams("q")   // query
        			, request.queryParams("p") // property of the doc (e.g. the ID, the value)
        			, request.queryParams("m") // matcher (e.g. contains, starts with, regex)
        			, request.queryParams("l") // tags (~labels)
        			, request.queryParams("o") // operator
        			, request.queryParams("x") // exclude biblical texts
        			));
		});

		// GET AGES index table data
		pCnt++;
		path = ENDPOINTS_DB_API.AGES_INDEX.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getAgesIndexTableData(
        			).toJsonString();
		});

		// GET AGES AGES REACT TEMPLATE 
		pCnt++;
		path = ENDPOINTS_DB_API.AGES_REACT_TEMPLATE.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
        	return externalManager.getAgesLDOM(
        			request.queryParams("u")  // url
        			, request.queryParams("t")  // translation library
        			).toJsonString();
		});

		// GET AGES read only template created from the specified url parameter
		pCnt++;
		path = ENDPOINTS_DB_API.AGES_READ_ONLY_TEMPLATE.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return externalManager.getAgesService(
        			request.queryParams("u")  // url
        			, request.queryParams("l")  // left library
        			, request.queryParams("c")  // center library
        			, request.queryParams("r")  // right library
        			, request.queryParams("lf")  // left fallback library
        			, request.queryParams("cf")  // center fallback library
        			, request.queryParams("rf")  // right fallback library
        			, requestor
        			).toJsonString();
		});

		// GET PDF generated from an AGES HTML file using the specified url parameter
		pCnt++;
		path = ENDPOINTS_DB_API.PDF.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			  try {
      				String id = request.queryParams("id");
			        Path filePath = Paths.get(Constants.PDF_FOLDER + "/" + id + ".pdf");
			        File file = new File(Constants.PDF_FOLDER + "/" + id + ".pdf");
			        if (! file.exists()) { // wait because the pdf still might be generating
			        	long millis =  30000; // 60000 = 1 minute
			        	for (int i = 0; i < 5; i++) {
			        		Thread.sleep(millis);
			        		if (file.exists()) {
			        			break;
			        		}
			        	}
			        }
			        
			        File newFile = new File(
			        		ServiceProvider.staticExternalFileLocation 
			        		+ "/pdf/" 
			        		+ id
			        		+ ".pdf"
			        );
			        byte[] data = Files.readAllBytes(filePath);
			        HttpServletResponse httpServletResponse = response.raw();
			        httpServletResponse.setContentType("application/pdf");
			        httpServletResponse.setContentLength(data.length);
			        httpServletResponse.addHeader("Content-Disposition", "inline; filename=" + id + ".pdf");
			        httpServletResponse.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			        httpServletResponse.addHeader("Pragma", "no-cache");
			        httpServletResponse.addHeader("Expires", "0");
			        httpServletResponse.getOutputStream().write(data);
			        httpServletResponse.getOutputStream().flush();
			        httpServletResponse.getOutputStream().close();
			        
			        /**
    						headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
			         */
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			 
			    return "";
		});

		// GET TEX generated from an AGES HTML file using the specified url parameter
		pCnt++;
		path = ENDPOINTS_DB_API.TEX.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			  try {
      				String id = request.queryParams("id");
			        Path filePath = Paths.get(Constants.PDF_FOLDER + "/" + id + ".tex");
			        File file = new File(Constants.PDF_FOLDER + "/" + id + ".tex");
			        if (! file.exists()) { // wait because the pdf still might be generating
			        	long millis =  30000; // 60000 = 1 minute
			        	for (int i = 0; i < 5; i++) {
			        		Thread.sleep(millis);
			        		if (file.exists()) {
			        			break;
			        		}
			        	}
			        }
			        byte[] data = Files.readAllBytes(filePath);
			        HttpServletResponse httpServletResponse = response.raw();
			        httpServletResponse.setContentType("application/x-latex");
			        httpServletResponse.addHeader("Content-Disposition", "inline; filename=" + id + ".tex");
			        httpServletResponse.getOutputStream().write(data);
			        httpServletResponse.getOutputStream().close();
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			 
			    return "";
		});

		// GET the user's personal library as a json string
		pCnt++;
		path = ENDPOINTS_DB_API.USER_DOCS.pathname;
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			return externalManager.getUserPersonalDocs(requestor);
		});

		// GET LDOM
		pCnt++;
		path = ENDPOINTS_DB_API.LITURGICAL_DOCUMENT_OBJECT_MODEL.toLibraryPath();
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
        	return externalManager.getLdomForTemplate(
        			request.queryParams("id")  // template ID
        			, request.queryParams("y")  // year (if a dated service)
        			, request.queryParams("l")  // left library
        			, request.queryParams("c")  // center library
        			, request.queryParams("r")  // right library
        			, request.queryParams("lf")  // left fallback library
        			, request.queryParams("cf")  // center fallback library
        			, request.queryParams("rf")  // right fallback library
        			, requestor
        			).toJsonString();
		});
		pCnt++;
		path = ENDPOINTS_DB_API.DOCS.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			ResultJsonObjectArray json = externalManager.getForId(id);
			if (json.valueCount > 0) {
				response.status(HTTP_RESPONSE_CODES.OK.code);
			} else {
				response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
			}
			return json.toJsonString();
		});

		// Use this for a generic read only api /db/api/v1/ltk/*/*/*
		pCnt++;
		path = Constants.EXTERNAL_DATASTORE_API_PATH + "/ltk/*/*/*";
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			ResultJsonObjectArray json = externalManager.getForIdPublic(id);
			if (json.valueCount > 0) {
				response.status(HTTP_RESPONSE_CODES.OK.code);
			} else {
				response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
			}
			return json.toJsonString();
		});
		
		// Use this for a generic read only api /db/api/v1/id/*/*/*
		// Returns HTML
		pCnt++;
		path = Constants.EXTERNAL_DATASTORE_API_PATH + "/id/*/*/*";
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.TEXT_HTML);
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			return  externalManager.getHtmlForIdPublic(id);
		});
		
		// public endpoint to retrieve docs based on topic and key
		pCnt++;
		path = Constants.EXTERNAL_DATASTORE_API_PATH + "/ltk/*/*";
		ControllerUtils.reportPath(logger, "GET", path, pCnt);
		get(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			response.status(HTTP_RESPONSE_CODES.OK.code);
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			ResultJsonObjectArray json = externalManager.getForIdEndsWith(id);
			return json.toJsonString();
		});
		// DELETE relationship for parameter id, where id is the id of the properties in the relationship itself.
		path = ENDPOINTS_DB_API.LINKS.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "DELETE", path);
		delete(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			RequestStatus requestStatus = externalManager.deleteForRelationshipId(requestor, id);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});
		
	   // TODO: add a delete for a relationship between two nodes where there is no specific ID in the properties of the relationship

		// DELETE doc for /library/topic/key in path
		path = Constants.EXTERNAL_DATASTORE_API_PATH 	+ Constants.PATH_LIBRARY_TOPIC_KEY_WILDCARD;
		ControllerUtils.reportPath(logger, "DELETE", path);
		delete(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			String library = "";
			String topic = "";
			String key = "";
			if (request.splat().length == 3) {
				library = request.splat()[0];
				topic = request.splat()[1];
				key = request.splat()[2];
			}
			RequestStatus requestStatus = externalManager.deleteForId(requestor, library, topic, key);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});
		
		// DELETE doc for library, topic, key as parms
		path = Constants.EXTERNAL_DATASTORE_API_PATH 	+ Constants.PATH_DELETE;
		ControllerUtils.reportPath(logger, "DELETE", path);
		delete(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			RequestStatus requestStatus = externalManager.deleteForId(
					requestor
        			, request.queryParams("l")  // library
        			, request.queryParams("t")  // topic
        			, request.queryParams("k")  // key
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

		/**
		 * POST controllers
		 */
		
		// post a doc
		path = ENDPOINTS_DB_API.DOCS.pathname;
		ControllerUtils.reportPath(logger, "POST", path);
		post(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			RequestStatus requestStatus = externalManager.addLTKDbObject(
					requestor
					, request.body()
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});
		
		// post a template
		path = ENDPOINTS_DB_API.TEMPLATES.pathname;
		ControllerUtils.reportPath(logger, "POST", path);
		post(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			RequestStatus requestStatus = externalManager.addTemplate(
					requestor
					, request.body()
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});
		
		// post a note
		path = ENDPOINTS_DB_API.NOTES.pathname;
		ControllerUtils.reportPath(logger, "POST", path);
		post(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			RequestStatus requestStatus = externalManager.addNote(
					requestor
					, request.body()
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

		// post a reference
		path = ENDPOINTS_DB_API.LINKS.pathname;
		ControllerUtils.reportPath(logger, "POST", path);
		post(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			RequestStatus requestStatus = externalManager.addReference(
					requestor
					, request.body()
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

		/**
		 * PUT controllers - updates
		 */
		// put (update) the value of a doc 
		path = ENDPOINTS_DB_API.VALUE.toLibraryPath();
		ControllerUtils.reportPath(logger, "PUT", path);
		put(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String id = request.queryParams("i");
			String body = request.body();

			RequestStatus requestStatus = externalManager.updateValueOfLiturgicalText(
					requestor
					, id
					, body
			);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

		// put (update) the value of a user interface label
		path = ENDPOINTS_DB_API.UI_LABELS.toLibraryPath();
		ControllerUtils.reportPath(logger, "PUT", path);
		put(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String id = request.queryParams("i");
			String body = request.body();

			RequestStatus requestStatus = externalManager.updateValueOfUiLabel(
					requestor
					, id
					, body
			);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

		// put (update) a template 
		path = ENDPOINTS_DB_API.TEMPLATES.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "PUT", path);
		put(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			RequestStatus requestStatus = externalManager.updateTemplate(
					requestor
					, request.body()
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

		// put (update) a doc 
		path = ENDPOINTS_DB_API.DOCS.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "PUT", path);
		put(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			RequestStatus requestStatus = externalManager.updateLTKDbObject(
					requestor
					, request.body()
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

		// put (update) a note 
		path = ENDPOINTS_DB_API.NOTES.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "PUT", path);
		put(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			RequestStatus requestStatus = externalManager.updateLTKDbObject(
					requestor
					, request.body()
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

		// put (update) a reference
		path = ENDPOINTS_DB_API.LINKS.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "PUT", path);
		put(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			String id = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
			RequestStatus requestStatus = externalManager.updateReference(
					requestor
					, id
					, request.body()
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

		// put (update) a token analysis 
		path = ENDPOINTS_DB_API.TOKEN_ANALYSIS.toLibraryTopicKeyPath();
		ControllerUtils.reportPath(logger, "PUT", path);
		put(path, (request, response) -> {
			response.type(Constants.UTF_JSON);
			String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
			RequestStatus requestStatus = externalManager.updateTokenAnalysis(
					requestor
					, request.body()
					);
			response.status(requestStatus.getCode());
			return requestStatus.toJsonString();
		});

}

}
