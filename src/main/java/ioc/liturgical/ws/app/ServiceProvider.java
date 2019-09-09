package ioc.liturgical.ws.app;

import static spark.Spark.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import spark.QueryParamsMap;
import spark.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import ioc.liturgical.ws.constants.Constants;
import ioc.liturgical.ws.controllers.admin.AuthorizationsController;
import ioc.liturgical.ws.controllers.admin.DomainsController;
import ioc.liturgical.ws.controllers.admin.LabelsController;
import ioc.liturgical.ws.controllers.admin.LoginController;
import ioc.liturgical.ws.controllers.admin.NewFormsController;
import ioc.liturgical.ws.controllers.admin.ResourcesController;
import ioc.liturgical.ws.controllers.admin.UsersContactController;
import ioc.liturgical.ws.controllers.admin.UsersPreferencesController;
import ioc.liturgical.ws.controllers.admin.UsersPasswordController;
import ioc.liturgical.ws.controllers.admin.UtilitiesController;
import ioc.liturgical.ws.controllers.db.neo4j.Neo4jController;
import ioc.liturgical.ws.controllers.db.neo4j.ReferencesController;
import ioc.liturgical.ws.controllers.ldp.LdpController;
import ioc.liturgical.ws.managers.auth.AuthDecoder;
import ioc.liturgical.ws.managers.auth.UserStatus;
import ioc.liturgical.ws.managers.databases.external.neo4j.ExternalDbManager;
import ioc.liturgical.ws.managers.databases.internal.InternalDbManager;
import ioc.liturgical.ws.managers.ldp.LdpManager;
import ioc.liturgical.ws.managers.synch.SynchManager;
import net.ages.alwb.tasks.SynchPullTask;
import net.ages.alwb.tasks.SynchPushTask;
import net.ages.alwb.utils.core.datastores.json.manager.JsonObjectStoreManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.ocmc.ioc.liturgical.schemas.constants.ENDPOINTS_DB_API;
import org.ocmc.ioc.liturgical.schemas.constants.HTTP_RESPONSE_CODES;
import org.ocmc.ioc.liturgical.schemas.models.messaging.Message;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.RequestStatus;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.ResultJsonObjectArray;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.ocmc.ioc.liturgical.utils.MessageUtils;

/**
 * Main Class for the IOC Liturgical Web Services. Provides the basic service.
 * 
 * The service can be started either by running main or calling the runService method.
 * 
 *  When you start the service, it is necessary to pass in the root (web service admin) password.
 *  
 *  This password must be the same one used in the external (backend) database.
 *
 *  If the *internal* database is empty, it will be initialized.  The wsadmin user will be
 *  added as the web service administrator using the password passed in when
 *  the service is started.  
 *  
 * The class reads properties set in the file named serviceProvider.config
 * 
 * 
 * Key concepts:
 * 
 * The IOC-Liturgical-WS protects and provides access to a back-end database.
 * A database contains libraries.
 * A library contains docs.
 * Docs are organized in the library by a topic.
 * A doc is uniquely identified by its ID.
 * A doc ID is library + topic + key, stored with a pipe delimiter, e.g. the | character
 * in the internal database, and with a tilde ~ character in the Neo4j (external) database.
 * 
 * There are three types of resource categories, and therefore there are three types of 
 * administrators:
 * 
 * 1. web service administrator (wsAdmin) manages the web service itself.
 * 2. database administrator (dbAdmin) manages the backend database.
 * 3. library administrator (libAdmin) manages a specific library in the database.
 * 
 * Supported application programming interfaces (api's) are defined as constants in ioc.liturgical.ws.contants.Constants.java:
 * 
 * 1. INTERNAL_DATASTORE_API_PATH is the api path for web services administration
 * 2. EXTERNAL_DATASTORE_API_PATH is the api path for the protected backend database
 * 3. INTERNAL_LITURGICAL_PROPERTIES_API_PATH is the api path for liturgical day properties

 * The backend database is protected using basic authentication.  
 * There is a single backend database user: wsadmin.
 * The hash of wsadmin's password from the backend database must be placed in the serviceProvider.config file.
 *
 * The web service provides authentication and authorization as a proxy to the external backend database.
 * User accounts and user authorizations are maintained using a ws admin app. 
 * 
 * Note: all values of text stored in the database are converted to Normalizer.Form.NFC
 * and all searches and matches use Normalizer.Form.NFC.  That way there is a 
 * consistent way to work with text that has diacritics.
 * 
 * @author mac002
 *
 */
public class ServiceProvider {

	private static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);
	public static String ws_pwd;
	public static String ws_usr;
	public static String keystorePath;

	private static int maxInactiveMinutes = 10 * 60; // default to 10 minutes

	public static InternalDbManager storeManager;
	public static ExternalDbManager docService;
	public static LdpManager ldpManager;
	public static SynchManager synchManager = null;
	
	private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	
	public static boolean enableCors = false;
	static boolean externalDbIsReadOnly = false;
	static boolean externalDbAccessIsProtected = true;
	public static String externalDbDomain = "localhost";
	static boolean debug = false; // can be overridden by serviceProvider.config
	static boolean logAllQueries = false; // can be overridden by serviceProvider.config
	static boolean logQueriesWithNoMatches = false; // can be overridden by serviceProvider.config
	static boolean createTestUsers = false; // can be overridden by serviceProvider.config
	static boolean runningJUnitTests = false; // can be overridden by serviceProvider.config
	static boolean useExternalStaticFiles = true; // can be overridden by serviceProvider.config
	public static String staticExternalFileLocation = null;
	
	public static boolean synchEnabled = false; // can be overridden by serviceProvider.config
	public static boolean synchPullEnabled = false; // can be overridden by serviceProvider.config
	public static boolean synchPushEnabled = false; // can be overridden by serviceProvider.config
	public static String synchDomain = "";  // can be overridden by serviceProvider.config
	public static String synchBoltPort = "";  // can be overridden by serviceProvider.config
	public static String synchDomainWithPort = "";
	
	public static boolean messagingEnabled = true; // can be overridden by serviceProvider.config
	private static String messagingToken = null;
	
	private static String hostname = "unknown server";

	
	/**
	 * If the property is null, the method returns
	 * back the value of var, otherwise it checks
	 * prop to see if it starts with "true".  If so
	 * it returns true, else false.  
	 * 
	 * The var is passed in so that if the config file lacks 
	 * the specified property, the default value gets used.
	 * @param var
	 * @param prop
	 * @return
	 */
	public static boolean toBoolean(boolean var, String prop) {
		if (prop == null) {
			return var;
		} else {
			return prop.startsWith("true");
		}
	}

	public static void main(String[] args) {
		
		boolean allOk = true;
		
		int maxThreads = 15;
		int minThreads = 2;
		int timeOutMillis = 30000;
		
		InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName() + " (" + ip + ")";
        } catch (UnknownHostException e) {
        	ErrorUtils.report(logger, e);
        }		
		ws_pwd = System.getenv("WS_PWD");
		if (ws_pwd == null) {
			ws_pwd = args[0];
		}
		ws_usr = "wsadmin";
		boolean initializeExternalDb = true;
		if (args.length > 2) {
			initializeExternalDb =  !(args[2].equals("noexdb"));
			logger.info("External Db disabled by parameter passed to main: " + args[2]);
		}
		
		/**
		 * Values from Properties file
		 */
		Properties prop = new Properties();
		InputStream input = null;
		boolean useSsl = false;
		boolean debugStore = true;
		boolean deleteExistingDataStoreFile = false;
		boolean deleteExistingDataStoreRecords = false;
		String datastore_type = "db";
		JsonObjectStoreManager.STORE_TYPE store_type = JsonObjectStoreManager.STORE_TYPE.DB;

		try {
			logger.info("ioc-liturgical-ws version: " + Constants.VERSION);
			ServiceProvider.class.getClassLoader();
			String location = getLocation();
			logger.info("Jar is executing from: " + location);
			try {
				input = new FileInputStream(new File(location+"/resources/serviceProvider.config"));
			} catch (Exception e) {
				// load from bundled config file
				input = ServiceProvider.class.getClassLoader().getResourceAsStream("serviceProvider.config");
			}
			prop.load(input);

			logger.info("java.version = " + System.getProperty("java.version"));
			logger.info("java.vm.version = " + System.getProperty("java.vm.version"));
			debug = toBoolean(debug, prop.getProperty("debug"));
			String envDebug  = System.getenv("WS_DEBUG");
			if (envDebug != null && envDebug.startsWith("true")) {
				debug = true;
			}
			logger.info("debug: " + debug);

			logAllQueries = toBoolean(logAllQueries, prop.getProperty("logQueries"));
			String envLogAllQueries  = System.getenv("WS_LOG_ALL_QUERIES");
			if (envLogAllQueries != null) {
				logAllQueries = envLogAllQueries.startsWith("true");
			}
			logger.info("logQueries: " + logAllQueries);

			logQueriesWithNoMatches = toBoolean(logQueriesWithNoMatches, prop.getProperty("logQueriesWithNoMatches"));
			logger.info("logQueriesWithNoMatches: " + logQueriesWithNoMatches);

			createTestUsers = toBoolean(createTestUsers, prop.getProperty("createTestUsers"));
			logger.info("create test users: " + debug);

			runningJUnitTests = toBoolean(runningJUnitTests, prop.getProperty("runningJUnitTests"));
			logger.info("runningJUnitTests: " + runningJUnitTests);

			String ssl = prop.getProperty("use_ssl");
			logger.info("use_ssl: " + ssl);
			useSsl = ssl.toLowerCase().startsWith("true");
			URL keystore = null;
			if (useSsl) {
				keystore = ServiceProvider.class.getClassLoader().getResource("clientkeystore");
				keystorePath = keystore.getPath();
			}
			deleteExistingDataStoreFile = toBoolean(deleteExistingDataStoreFile, prop.getProperty("datastore_delete_existing"));
			logger.info("datastore_delete_existing: " + deleteExistingDataStoreFile );

			deleteExistingDataStoreRecords = toBoolean(deleteExistingDataStoreRecords, prop.getProperty("datastore_truncate_existing"));
			String envDeleteExistingDataStoreRecords  = System.getenv("deleteExistingDataStoreRecords");
			if (envDeleteExistingDataStoreRecords != null && envDeleteExistingDataStoreRecords.length() > 0) {
				if (envDeleteExistingDataStoreRecords.equals("true") || envDeleteExistingDataStoreRecords.equals("yes"))  {
					deleteExistingDataStoreRecords = true;
				} else {
					deleteExistingDataStoreRecords = false;
				}
			}
			logger.info("datastore_truncate_existing: " + deleteExistingDataStoreRecords );

			enableCors = toBoolean(enableCors, prop.getProperty("enable_cors"));
			logger.info("enable_cors: " + enableCors);

			externalDbIsReadOnly = toBoolean(externalDbIsReadOnly, prop.getProperty("external_db_is_read_only"));
			logger.info("external_db_is_read_only: " + externalDbIsReadOnly);
	
			externalDbAccessIsProtected = toBoolean(externalDbAccessIsProtected, prop.getProperty("external_db_access_is_protected"));
			logger.info("external_db_access_is_protected: " + externalDbAccessIsProtected);

			externalDbDomain = prop.getProperty("external_db_domain");
			String envExternalDbDomain  = System.getenv("EXTERNAL_DB_DOMAIN");
			if (envExternalDbDomain != null && envExternalDbDomain.length() > 0) {
				externalDbDomain = envExternalDbDomain;
			}
			logger.info("external_db_domain: " + externalDbDomain);
			useExternalStaticFiles = toBoolean(debug, prop.getProperty("useExternalStaticFiles"));
			logger.info("useExternalStaticFiles: " + useExternalStaticFiles);

			if (useExternalStaticFiles) {
				ServiceProvider.staticExternalFileLocation = prop.getProperty("staticExternalFileLocation");
			} else {
				ServiceProvider.staticExternalFileLocation = "/public";
			}
			logger.info("staticExternalFileLocation: " + ServiceProvider.staticExternalFileLocation);

			datastore_type = prop.getProperty("datastore_type");
			debugStore = toBoolean(debugStore, prop.getProperty("datastore_debug"));
			if (datastore_type.toLowerCase().startsWith("file")) {
				store_type = JsonObjectStoreManager.STORE_TYPE.FILE;
			} else if (datastore_type.toLowerCase().startsWith("db")) {
				store_type = JsonObjectStoreManager.STORE_TYPE.DB;
			} else {
				allOk = false;
				logger.error("Valid datastore_type values are: file, db");
			}
			try {
				maxInactiveMinutes = 60 * Integer.parseInt(prop.getProperty("session_max_minutes"));
			} catch (Exception e) {
				logger.error("Property maxInactiveInterval missing or not a number.");
			}
						
			messagingEnabled = toBoolean(messagingEnabled, prop.getProperty("messaging_enabled"));
			String envMessagingEnabled  = System.getenv("MESSAGING_ENABLED");
			if (envMessagingEnabled != null) {
				messagingEnabled = envMessagingEnabled.startsWith("true");
			}
			logger.info("messaging_enabled: " + messagingEnabled);

			if (messagingEnabled) {
				try {
					messagingToken = System.getenv("MESSAGING_TOKEN");
    	    		if (messagingToken == null) {
        	        	messagingToken = args[1];
    	    		}
				} catch (Exception e) {
					logger.info("main args [2] missing parameter for messaging token");
					throw e;
				}
			}

			synchPullEnabled = toBoolean(synchPullEnabled, prop.getProperty("synch_pull_enabled"));
			String envSynchPullEnabled  = System.getenv("SYNCH_PULL_ENABLED");
			if (envSynchPullEnabled != null) { 
					synchPullEnabled = envSynchPullEnabled.startsWith("true");
			}
			logger.info("synch_pull_enabled: " + synchPullEnabled);

			synchPushEnabled = toBoolean(synchPushEnabled, prop.getProperty("synch_push_enabled"));
			String envSynchPushEnabled  = System.getenv("SYNCH_PUSH_ENABLED");
			if (envSynchPushEnabled != null) { 
					synchPushEnabled = envSynchPushEnabled.startsWith("true");
			}
			logger.info("synch_push_enabled: " + synchPushEnabled);

			synchEnabled = synchPullEnabled || synchPushEnabled;
			
			if (synchEnabled) {
				synchDomain = prop.getProperty("synch_domain");
				String envSynchDomain  = System.getenv("SYNCH_DOMAIN");
				if (envSynchDomain != null && envSynchDomain.length() > 0) {
					synchDomain = envSynchDomain;
				}
				logger.info("synch_domain: " + synchDomain );

				synchBoltPort = prop.getProperty("synch_bolt_port");
				logger.info("synch_bolt_port: " + synchBoltPort );
				synchDomainWithPort = "bolt://" + synchDomain + ":" + synchBoltPort;
				
				synchManager = new SynchManager(
						synchDomain
						, synchBoltPort
						, ws_usr
						, ws_pwd
						);
			} else {
				synchDomainWithPort = "";
			}

		} catch (Exception e) {
			ErrorUtils.report(logger, e);
			allOk = false;
		}
//		port(8080);
		if (useSsl) {
			secure(keystorePath, ws_pwd, null, null);
		}
		/**
		 * Static file configuration:
		 * 
		 * If you want to bundle the static web apps with the jar, then put them
		 * into the src/main/resources/public folder, and set serviceProvider.config to:
		 * useExternalStaticFiles=false
		 * 
		 * If you want to keep the web apps separate, set the serviceProvider.config
		 * useExternalStaticFiles=true
		* staticExternalFileLocation=/some/path
		* 
		* where /some/path is the actual path you want to use.
		 */
		if (! runningJUnitTests) {
			if (useExternalStaticFiles) {
//				externalStaticFileLocation(ServiceProvider.staticExternalFileLocation);
				externalStaticFileLocation(Constants.PDF_FOLDER);
			} else {
				staticFileLocation("/public"); 
			}

			threadPool(maxThreads, minThreads, timeOutMillis);
		}

		if (allOk) {
			logger.info("Property file loaded OK");
			// Create the manager for operations involving the underlying datastore
			storeManager = new InternalDbManager(
					"datastore/ioc-liturgical-db"
					, "json"
					, deleteExistingDataStoreFile
					, deleteExistingDataStoreRecords
					, createTestUsers
					, ws_usr
					, ws_pwd
					);
			storeManager.setMaxInactiveMinutes(maxInactiveMinutes);
			storeManager.setPrettyPrint(debug); // if debugging, json will be pretty print formatted
			
			/**
			 * One-off delete for internal DB record
			 */
//			String deleteId = "misc~domains~en_UK_ware";
//			try {
//				if (storeManager.existsUnique(deleteId)) {
//					RequestStatus deleteStatus = storeManager.deleteForId(deleteId);
//					logger.info("Delete status for " + deleteId + " = " + deleteStatus.code);
//				}
//			} catch (Exception e) {
//				logger.error("error deleting " + deleteId);
//				ErrorUtils.report(logger, e);
//			}
			
			
			if (initializeExternalDb) {
				docService = new ExternalDbManager(
						externalDbDomain
						, logAllQueries
						, logQueriesWithNoMatches
						, externalDbIsReadOnly
						, storeManager
						  , ServiceProvider.ws_usr
						  , ServiceProvider.ws_pwd
						);
				docService.setPrettyPrint(debug);

				if (synchEnabled && synchManager != null) {
					
					docService.setSynchManager(synchManager);
					if (synchPushEnabled) {
						executorService.scheduleAtFixedRate(
								new SynchPushTask(
										ExternalDbManager.neo4jManager
										, synchManager
										)
								, 10
								, 10
								, TimeUnit.SECONDS
								);
					}
					
					if (synchPullEnabled) {
					executorService.scheduleAtFixedRate(
							new SynchPullTask(
									ExternalDbManager.neo4jManager
									, synchManager
									, messagingToken
									)
							, 10
							, 10
							, TimeUnit.SECONDS
							);
					}
				}
			} else {
				docService = null;
			}
			
			ldpManager = new LdpManager(storeManager);
			
			if (enableCors) {
				enableCORS("*", "*", "*");
			}
			/**
			 * Make sure that api calls do not have a path that exceeds
			 * /api/library/topic/key Make sure the user is authenticated and
			 * authorized
			 */
			before((request, response) -> {
				if (debug) {
					dump(request);
				}
				if ((! externalDbAccessIsProtected && request.pathInfo().startsWith("/db/api"))
						|| (
								request.pathInfo().toLowerCase().endsWith("/docs") && request.requestMethod().equals("GET")
							)
						|| (
								request.pathInfo().toLowerCase().endsWith("/tables/en_sys_tables/LexiconTable/OALD")
							)
						|| (
								request.pathInfo().toLowerCase().startsWith("/db/api/v1/nlp/text/analysis")
							)
						|| (
								request.pathInfo().toLowerCase().startsWith("/ldp/api")
							)
						|| (
								request.pathInfo().toLowerCase().contains("dropdowns")
							)
						|| (
								request.pathInfo().toLowerCase().contains("publications")
							)
						|| (
								request.pathInfo().toLowerCase().endsWith(ENDPOINTS_DB_API.UI_LABELS.pathname)
							)
						|| (
								request.requestMethod().toLowerCase().startsWith("options")
							)
					) {
				} else {
					request.session(true);
					AuthDecoder authDecoder = new AuthDecoder(request.headers("Authorization"));
					String method = request.requestMethod();
					String path = request.pathInfo();
					String library = libraryFrom(path);
					UserStatus status = storeManager.getUserStatus(
							authDecoder.getUsername()
							, authDecoder.getPassword()
							, method
							, library
							);
					if (debug) {
						logger.info("Method: " + method);
						logger.info("Path: " + path);
						logger.info("Library " + library);
						logger.info("User: " + authDecoder.getUsername());
						logger.info("Authenticated = " + status.isAuthenticated());
						logger.info("Authorized = " + status.isAuthorized());
					}
					if (status.isAuthenticated()) {
						if (status.isSessionExpired() && request.pathInfo().length() == 1
								&& request.pathInfo().startsWith("/")) {
							response.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
							response.status(HTTP_RESPONSE_CODES.UNAUTHORIZED.code);
						} else {
							if (status.isAuthorized()) {
									// let the request pass through to the handler
							} else {
								response.type(Constants.UTF_JSON);
								halt(HTTP_RESPONSE_CODES.UNAUTHORIZED.code, HTTP_RESPONSE_CODES.UNAUTHORIZED.message);
							}
						}
					} else {
						// we do not require authorization to attempt to login, or to get the Liturgical Day Properties
						if (
						request.pathInfo().startsWith(Constants.INTERNAL_DATASTORE_API_PATH +"/login/form")
								|| request.pathInfo().startsWith(Constants.INTERNAL_DATASTORE_API_PATH +"/info")
								|| request.pathInfo().startsWith(Constants.EXTERNAL_LITURGICAL_DAY_PROPERTIES_API_PATH)
								|| request.pathInfo().startsWith(Constants.EXTERNAL_DATASTORE_API_PATH + "/id")
								|| request.pathInfo().startsWith(Constants.EXTERNAL_DATASTORE_API_PATH + "/ltk")
								) {
							// pass through to handler
						} else {
							response.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
							halt(HTTP_RESPONSE_CODES.UNAUTHORIZED.code, HTTP_RESPONSE_CODES.UNAUTHORIZED.message);
						}
					}
				}
			});

			/**
			 * Routes
			 * 
			 * Important: order the routes from most specific to least,
			 * Keep in mind that routes are being inject both from the
			 * injected controllers and the explicit routes in this class.
			 */

			/**
			 * Inject controllers
			 * 
			 * In some cases, injected controllers only need to handle post and put.
			 * In such cases, they will use the get handlers in this class. 
			 */
			new AuthorizationsController(storeManager);
			new DomainsController(storeManager, docService);
			new LabelsController(storeManager);
			new LoginController(storeManager);
			new NewFormsController(storeManager);
			new Neo4jController(docService);
			new ReferencesController(docService);
			new ResourcesController(storeManager);
			new UsersContactController(storeManager);
			new UsersPreferencesController(storeManager);
			new UsersPasswordController(storeManager);
			new LdpController(ldpManager);
			new UtilitiesController(docService);

			get("api/sugar", (request, response) -> {
				response.type(Constants.UTF_JSON);

				if (request.queryParams().isEmpty()) {
					response.status(400);
					return "{'status': '400'}";
				} else {
					String query = request.queryParams("p").replaceAll("\\*", "%");
					response.status(200);
					return storeManager.getWhereLike(query).toString();
				}
			});
			
			/**
			 * Grant a right to a library Request Segments: api _rights role =
			 * libadmin or libauthor or libreader library, e.g. gr_GR_cog user =
			 * username
			 * 
			 * Preconditions: the role, library, and user must exist the
			 * requesting user must be authorized to grant this right against
			 * this library
			 */
			put("/api/v1/_rights/*/*/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				return HTTP_RESPONSE_CODES.NOT_FOUND;
			});

			/**
			 * Get by library and topic
			 */
			get("/api/v1/like/*/*/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				response.status(200);
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				query = query.replaceAll("\\*", "%");
				return storeManager.getWhereLike(query).toString();
			});

			/**
			 * Get by library and topic
			 */
			get("/api/v1/like/*/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				response.status(200);
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				query = query.replaceAll("\\*", "%");
				return storeManager.getWhereLike(query).toString();
			});

			/**
			 * Get by library
			 */
			get("/api/v1/like/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				response.status(200);
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				query = query.replaceAll("\\*", "%");
				return storeManager.getWhereLike(query).toString();
			});			
			
			/**
			 * Return the version number for the overall web service.  This
			 * needs to be updated in Constants every time a new jar is created.
			 */
			get(Constants.INTERNAL_DATASTORE_API_PATH  + "/info", (request, response) -> {
				response.type(Constants.UTF_JSON);
				JsonObject json = new JsonObject();
				boolean synchConnectionOk = false;
				if (synchEnabled && synchManager != null) {
					synchConnectionOk = synchManager.synchConnectionOK();
				}
				json.addProperty("wsVersion", Constants.VERSION);
				json.addProperty("dbServerDomain", externalDbDomain);
				json.addProperty("databaseReadOnly", externalDbIsReadOnly);
				json.addProperty("databaseProtected", externalDbAccessIsProtected);
				json.addProperty("synchEnabled", synchEnabled);
				json.addProperty("synchDbConnectionOk", synchConnectionOk);
				return json.toString();
			});

			get("user", (request, response) -> {
				response.type(Constants.UTF_JSON);
				AuthDecoder authDecoder = new AuthDecoder(request.headers("Authorization"));
				JsonObject json = new JsonObject();
				if (storeManager.authenticated(authDecoder.getUsername(), authDecoder.getPassword())) {
					json.addProperty("authorized", true);
					response.status(HTTP_RESPONSE_CODES.OK.code);
				} else {
					json.addProperty("authorized", false);
					response.status(HTTP_RESPONSE_CODES.UNAUTHORIZED.code);
				}
				return json.toString();
			});

			/**
			 * Get ws resources by library and topic and key
			 */
			get(Constants.INTERNAL_DATASTORE_API_PATH + "/*/*/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				ResultJsonObjectArray json = storeManager.getForId(query);
				if (json.getValueCount() > 0) {
					response.status(HTTP_RESPONSE_CODES.OK.code);
				} else {
					response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
				}
				return json.toJsonString();
			});

			/**
			 * Get ws resources by library and topic
			 */
			get(Constants.INTERNAL_DATASTORE_API_PATH + "/*/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				ResultJsonObjectArray json = storeManager.getForIdStartsWith(query);
				if (json.getValueCount() > 0) {
					response.status(HTTP_RESPONSE_CODES.OK.code);
				} else {
					response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
				}
				return json.toJsonString();
			});

			/**
			 * Get ws resources by library
			 */
			get(Constants.INTERNAL_DATASTORE_API_PATH + "/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				ResultJsonObjectArray json = storeManager.getForIdStartsWith(query);
				if (json.getValueCount() > 0) {
					response.status(HTTP_RESPONSE_CODES.OK.code);
				} else {
					response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
				}
				return json.toJsonString();
			});

			delete(Constants.INTERNAL_DATASTORE_API_PATH + "/*/*/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				String requestor = new AuthDecoder(request.headers("Authorization")).getUsername();
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				RequestStatus status = storeManager.deleteForId(requestor, query);
				return status.toJsonString();
			});

			/**
			 * Get api version 1
			 */
			get("/api/v1", (request, response) -> {
				response.type(Constants.UTF_JSON);
				return HTTP_RESPONSE_CODES.NOT_FOUND.code;
			});

			/**
			 * Get api
			 */
			get("/api", (request, response) -> {
				response.type(Constants.UTF_JSON);
				response.status(404);
				return HTTP_RESPONSE_CODES.NOT_FOUND.code;
			});

		
			/**
			 * Post by library and topic and key
			 */
			post("/api/v1/*/*/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				ResultJsonObjectArray json = storeManager.getForId(query);
				if (json.getValueCount() > 0) {
					response.status(HTTP_RESPONSE_CODES.OK.code);
				} else {
					response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
				}
				return json.toJsonString();
			});

			/**
			 * Post by library and topic
			 */
			post("/api/v1/*/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				ResultJsonObjectArray json = storeManager.getForIdStartsWith(query);
				if (json.getValueCount() > 0) {
					response.status(HTTP_RESPONSE_CODES.OK.code);
				} else {
					response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
				}
				return json.toJsonString();
			});

			/**
			 * Post by library
			 */
			post("/api/v1/*", (request, response) -> {
				response.type(Constants.UTF_JSON);
				String query = ServiceProvider.createStringFromSplat(request.splat(), Constants.ID_DELIMITER);
				ResultJsonObjectArray json = storeManager.getForIdStartsWith(query);
				if (json.getValueCount() > 0) {
					response.status(HTTP_RESPONSE_CODES.CREATED.code);
				} else {
					response.status(HTTP_RESPONSE_CODES.NOT_FOUND.code);
				}
				return json.toJsonString();
			});
    		ServiceProvider.sendMessage("ServiceManager started.");
		} else {
    		ServiceProvider.sendMessage("Could not properly start ServiceManager.");
		}
		// this can greatly bog down your workstation, so use only when necessary.
		after((request, response) -> {
			if (debug) {
//				postDump(response);
			}
		});
	}


	public static String createPathFromSplat(String[] splat) {
		StringBuilder sb = new StringBuilder();
		int size = splat.length;
		for (int i = 0; i < size; i++) {
			sb.append(splat[i] + "/");
		}
		return deSlash(sb.toString());
	}

	public static String deSlash(String s) {
		if (s.endsWith("/")) {
			return s.substring(0, s.length() - 1);
		} else {
			return s;
		}
	}

	/**
	 * Converts parameters into the format of a database ID
	 * 
	 * @param splat
	 * @param delimiter
	 * @return
	 */
	public static String createStringFromSplat(String[] splat, String delimiter) {
		StringBuilder sb = new StringBuilder();
		int size = splat.length;
		for (int i = 0; i < size; i++) {
			sb.append(splat[i] + delimiter);
		}
		return deDelimit(sb.toString(), delimiter);
	}

	public static String deDelimit(String s, String delimiter) {
		if (s.endsWith(delimiter)) {
			return s.substring(0, s.length() - 1);
		} else {
			return s;
		}
	}

	public static Map<String, Object> toObjectMap(Set<String> keys, QueryParamsMap map) {
		if (map == null) {
			return null;
		} else {
			Map<String, Object> result = new TreeMap<String, Object>();
			try {
				for (String key : keys) {
					String value = map.get(key).value();
					result.put(key, value);
				}
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
			return result;
		}
	}

	public static void dump(Request request) {
		StringBuffer sb = new StringBuffer();
		sb.append(request.requestMethod() + ": " + request.pathInfo());
		Set<String> headers = request.headers();
		if (headers.size() > 0) {
			sb.append("\nHeaders:\n");
			for (String header : headers) {
				sb.append("\t" + header + ": " + request.headers(header) + "\n");
			}
		}
		Set<String> params = request.queryParams();
		if (params.size() > 0) {
			sb.append("\nParms:\n");
			for (String parm : params) {
				sb.append("\t" + parm + "=" + request.queryParams(parm) + "\n");
			}
		}
		sb.append("body: \n\t");
		sb.append(request.body());
		print(sb.toString());
	}

	public static void postDump(spark.Response response) {
		StringBuffer sb = new StringBuffer();
		sb.append(response.body());
		sb.append(response.status());
		print(sb.toString());
	}

	public static void print(String message) {
		System.out.println(message);
	}

	/**
	 * Safely try to get the first value from splat
	 * 
	 * @param splat
	 * @return
	 */
	private static String libraryFrom(String path) {
		String result = path;
		try {
			int apiIndex = 0;
			if (path.startsWith(Constants.INTERNAL_DATASTORE_API_PATH)) {
				apiIndex = Constants.INTERNAL_DATASTORE_API_PATH.split("/").length;
			} else if (path.startsWith(Constants.EXTERNAL_DATASTORE_API_PATH)) {
				apiIndex = Constants.EXTERNAL_DATASTORE_API_PATH.split("/").length;
			}
			String[] parts = path.split("/");
			try {
				result = parts[apiIndex];
			} catch (Exception ouch) {
				String caughtYou = "";
				caughtYou = "";
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}

	// Enables CORS on requests. This method is an initialization method and should be called once.
	private static void enableCORS(
			final String origin
			, final String methods
			, final String headers
			) {

	    options("/*", (request, response) -> {

	        String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
	        if (accessControlRequestHeaders != null) {
	            response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
	        }

	        String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
	        if (accessControlRequestMethod != null) {
	            response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
	        }

	        return "OK";
	    });

	    before((request, response) -> {
	        response.header("Access-Control-Allow-Origin", origin);
	        response.header("Access-Control-Request-Method", methods);
	        response.header("Access-Control-Allow-Headers", headers);
	        // Note: this may or may not be necessary in your particular application
	        response.type("application/json");
	    });
	}
	
	  public static String getNeo4jUrl() {
	        String urlVar = System.getenv("NEO4J_URL_VAR");
	        if (urlVar==null) urlVar = "NEO4J_URL";
	        String url =  System.getenv(urlVar);
	        if(url == null || url.isEmpty()) {
	            return Constants.DEFAULT_URL;
	        }
	        return url;
	    }
	  
	  public static String getLocation() {
		  try {
			return new File(ServiceProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
		} catch (URISyntaxException e) {
			ErrorUtils.report(logger, e);
			return null;
		}
	  }

	  /**
	   * 
	   * This method sends a message to the message service.
	   * First, though, it uses a digest of the message to see if we
	   * have already sent this message. That way a message is only
	   * sent once even if a situation keeps occurring.  The digest
	   * is store as the key of the message in the database.
	   * 
	   * @param message the message to be sent
	   * @return the status about sending the message
	   */
	  public static String sendMessage(String message) {
		  String response = "";
		  boolean messageExists = false;
		  String digest = "";

		  try {
			  if (ServiceProvider.messagingEnabled) {
				  if (! message.equals("ServiceManager started.")) {
					  if (docService != null && docService.isConnectionOK) {
						  digest = DigestUtils.md5Hex(message);
						  Message theMessage = new Message(digest, message);
						  messageExists = docService.messageExists(theMessage.getId());
						  if (! messageExists) {
							  docService.insertMessage(theMessage);
						  }
					  }
				  } 
				  if (!messageExists) {
					  String hostAndMessage = hostname + message;
					  response = MessageUtils.sendMessage(messagingToken, hostAndMessage);
				  } else {
					  response = "Message previously sent";
				  }
			  } else {
				  response = "Messaging not enabled";
			  }
		  } catch (Exception e) {
			  response = e.getMessage();
			  ErrorUtils.report(logger, e);
		  }

		  return response;
	  }

}
