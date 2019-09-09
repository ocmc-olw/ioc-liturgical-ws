package ioc.liturgical.ws.constants;

import java.util.concurrent.TimeUnit;

public class Constants {
		public static final String VERSION = "2019.09.08.03"; // should match most recent jar
		public static final String ID_DELIMITER = "~";
		public static final String ID_SPLITTER = "~";
		public static final String PIPE_SPLITTER = "\\|";
		public static final String ALT_ID_DELIMITER = "~"; // TODO reconcile pipe vs tilde
		public static final String DOMAIN_DELIMITER = "_";
		public static final String DOMAIN_SPLITTER = "_";
		public static final String APP_DATA_PATH = "app_data_do_not_delete/";
		public static final String DB_NAME = "app_data";
		public static final String TABLE_SYNCH_HISTORY = "SYNCH_HISTORY";
		public static final  String SYSTEM_LIB = "web_service";
		public static final  String DOMAINS_LIB = "all_domains";
		public static final String SCHEMA_LIB = "schemas";
		public static final String USER_LIB = "users";
		public static final String ROLES_LIB = "roles";
		public static final String RESOURCES_PATH = "resources";
		public static final String GRAMMAR_PATH = "/grammar";
		public static final String LOGIN_PATH = "/login";
		public static final String UTF_JSON = "application/json ;charset=utf-8";
		public static final String JAVASCRIPT = "application/javascript";
		public static final String FONT = "font/opentype";
		public static final String TEXT_CSS = "text/css";
		public static final String TEXT_HTML = "text/html; charset=utf-8";
		public static final String INTERNAL_DATASTORE_API_PATH = "/admin/api/v1";
		public static final String EXTERNAL_LITURGICAL_DAY_PROPERTIES_API_PATH = "/ldp/api/v1";
		public static final String EXTERNAL_DATASTORE_SYNCH_API_PATH = "/synch/api/v1";
		public static final String EXTERNAL_DATASTORE_API_PATH = "/db/api/v1";
		public static final String EXTERNAL_DATASTORE_DROPDOWNS_PATH = "/dropdowns";
		public static final String EXTERNAL_DATASTORE_FORMS_PATH = "/forms";
		public static final String EXTERNAL_DATASTORE_NODE_PATH = "/docs";
		public static final String EXTERNAL_DATASTORE_RELATIONSHIP_PATH = "/links";
		public static final String EXTERNAL_DATASTORE_NATURAL_LANGUAGE_PROCESSING_PATH = "/nlp";
		public static final String EXTERNAL_DATASTORE_ONTOLOGY_PATH = "/ontology";
		public static final String LIBRARY_SYNCH = "en_sys_synch";
		public static final String TOPIC_SYNCH = "cypher";
		public static final String TOPIC_SYNCH_LOG = "synch";
		public static final String KEY_SYNCH_LOG = "log";
		public static final String PATH_DELETE = "/delete";
		public static final String PATH_DELETE_USER = "/delete/user/*";
		public static final String PATH_LIBRARY_WILDCARD = "/*";
		public static final String PATH_LIBRARY_TOPIC_WILDCARD = "/*/*";
		public static final String PATH_LIBRARY_TOPIC_KEY_WILDCARD = "/*/*/*";
		public static final String VALUE_SCHEMA_ID = "_valueSchemaId";
		public static final String UI_WIDGET_TEXTAREA = "textarea";
		public static final String UI_WIDGET_PASSWORD = "password";
		public static final String UI_WIDGET_USERS = "users";
		public static final String UI_WIDGET_DOMAINS = "domains";
		public static final String UI_WIDGET_CHECKBOXES = "checkboxes";
		public static final String UI_WIDGET_RADIO = "radio";
		public static final String UI_WIDGET_SELECT = "select";
		public static final String LIML_URL = "https://liml.org";
		public static final String LIML_STATIC = "/static/";
		public static final String DEFAULT_URL = "bolt://localhost:7687";// "bolt://neo4j:1234@localhost";
//		public static final String PDF_FOLDER  = "/Users/mac002/git/ocmc-translation-projects/ioc-liturgical-docker/pdf/data";  
		public static final String PDF_FOLDER  = "/private/var/lib/pdf/data";  
		public static final String GIT_FOLDER  = "/private/var/lib/git";  
//		public static final String PDF_FOLDER = "/usr/local/ocmc/docker/pdf/data";
		//		public static final String PDF_FOLDER  = "data";  
		public static final int boltDriverConnectionTimeout = 30;
		public static final TimeUnit boltDriverConnectionTimeoutUnits = TimeUnit.SECONDS;
}
