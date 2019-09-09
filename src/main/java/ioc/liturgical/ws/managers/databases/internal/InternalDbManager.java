package ioc.liturgical.ws.managers.databases.internal;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ioc.liturgical.ws.managers.interfaces.HighLevelDataStoreInterface;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.RequestStatus;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.ResultJsonObjectArray;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.Domain;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.Label;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.User;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.UserAuth;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.UserContact;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.UserHash;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.UserPreferences;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.UserStatistics;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.Utility;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.UtilityPdfGeneration;
import org.ocmc.ioc.liturgical.schemas.models.ws.db.ValueSchema;
import org.ocmc.ioc.liturgical.schemas.models.ws.forms.AuthorizationCreateForm;
import org.ocmc.ioc.liturgical.schemas.models.ws.forms.DomainCreateForm;
import org.ocmc.ioc.liturgical.schemas.models.ws.forms.LabelCreateForm;
import org.ocmc.ioc.liturgical.schemas.models.ws.forms.SelectionWidgetSchema;
import org.ocmc.ioc.liturgical.schemas.models.ws.forms.UserCreateForm;
import org.ocmc.ioc.liturgical.schemas.models.ws.forms.UserPasswordChangeForm;
import org.ocmc.ioc.liturgical.schemas.models.ws.forms.UserPasswordSelfChangeForm;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.DomainWorkflowInfo;
import org.ocmc.ioc.liturgical.schemas.constants.ENDPOINTS_ADMIN_API;
import ioc.liturgical.ws.constants.Constants;
import org.ocmc.ioc.liturgical.schemas.constants.DOMAIN_TYPES;
import org.ocmc.ioc.liturgical.schemas.constants.SYSTEM_MISC_LIBRARY_TOPICS;
import org.ocmc.ioc.liturgical.schemas.constants.TEMPLATE_CONFIG_MODELS;
import org.ocmc.ioc.liturgical.schemas.constants.USER_INTERFACE_DOMAINS;
import org.ocmc.ioc.liturgical.schemas.constants.HTTP_RESPONSE_CODES;
import org.ocmc.ioc.liturgical.schemas.constants.NEW_FORM_CLASSES_ADMIN_API;
import org.ocmc.ioc.liturgical.schemas.constants.RESTRICTION_FILTERS;
import org.ocmc.ioc.liturgical.schemas.constants.ROLES;
import org.ocmc.ioc.liturgical.schemas.constants.INTERNAL_DB_SCHEMA_CLASSES;
import org.ocmc.ioc.liturgical.schemas.constants.STATUS;
import org.ocmc.ioc.liturgical.schemas.constants.USER_TOPICS;
import org.ocmc.ioc.liturgical.schemas.constants.UTILITIES;
import org.ocmc.ioc.liturgical.schemas.constants.VERBS;
import org.ocmc.ioc.liturgical.schemas.constants.VISIBILITY;
import org.ocmc.ioc.liturgical.schemas.exceptions.BadIdException;
import org.ocmc.ioc.liturgical.schemas.constants.SCHEMA_CLASSES;
import ioc.liturgical.ws.managers.auth.UserStatus;
import ioc.liturgical.ws.managers.exceptions.DbException;
import net.ages.alwb.utils.core.auth.PasswordHasher;
import net.ages.alwb.utils.core.datastores.db.factory.DbConnectionFactory;
import net.ages.alwb.utils.core.datastores.db.h2.manager.H2ConnectionManager;
import net.ages.alwb.utils.core.datastores.json.exceptions.MissingSchemaIdException;
import org.ocmc.ioc.liturgical.schemas.models.DropdownItem;
import org.ocmc.ioc.liturgical.schemas.models.db.internal.LTKVJsonObject;
import org.ocmc.ioc.liturgical.schemas.models.db.internal.LTKVString;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import net.ages.alwb.utils.core.id.managers.IdManager;

/**
 * 
 * @author mac002
 *
 */
public class InternalDbManager implements HighLevelDataStoreInterface {

	private static final Logger logger = LoggerFactory.getLogger(InternalDbManager.class);
	private boolean suppressAuth = false; // for debugging purposes, if true, causes authorized() to always return true
	private boolean prettyPrint = true;
	private String wsAdmin = "wsadmin";
	private String wsAdminPwd = "";
	private boolean initialized = true;
	private Gson gson = new Gson();
	private String storename = null;
	private String tablename = null;
	private boolean deleteOldDb = false;
	public int getMaxInactiveMinutes() {
		return maxInactiveMinutes;
	}

	public void setMaxInactiveMinutes(int maxInactiveMinutes) {
		this.maxInactiveMinutes = maxInactiveMinutes;
	}
	private boolean deleteOldTableRows = false;
	private boolean createTestUsers = false;
	private String customerNumber = null;
	private int maxInactiveMinutes = 10;
	private List<String> agesDomains = new ArrayList<String>();
	private List<String> publicSystemDomains = new ArrayList<String>();
	
	public H2ConnectionManager manager;
	

		public InternalDbManager(
			String storename
			, String tablename
			, boolean deleteOldDb
			, boolean deleteOldTableRows
			, boolean createTestUsers
			, String wsAdmin
			, String wsAdminPwd
			) {
 
		this.storename = storename;
		this.tablename = tablename;
		this.deleteOldDb = deleteOldDb;
		this.deleteOldTableRows = deleteOldTableRows;
		this.createTestUsers = createTestUsers;
		this.wsAdmin = wsAdmin;
		this.wsAdminPwd = wsAdminPwd;
		
		try {
			manager = 
					DbConnectionFactory.getH2Manager(
							storename
							, tablename
							, deleteOldDb
							, deleteOldTableRows
							);
		} catch (SQLException e) {
			ErrorUtils.report(logger, e);
		}
    	verify();
	}
		
	/**
	 * Get all docs whose id matches specified pattern
	 * @param pattern, e.g. users
	 * @return all matching docs
	 */
	public JsonObject getWhereLike(String pattern) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(pattern.replaceAll("%", "*"));
		try {
			List<JsonObject> dbResults = filter(manager.queryForJsonWhereLike(pattern));
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (SQLException e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}
	
	public JsonObject getTopicsLike(String pattern) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(pattern);
		try {
			result.setResult(
					manager.queryForJsonWhereIdRegEx(
							".*" 
					+ Constants.ID_SPLITTER
					+ pattern 
					+ Constants.ID_SPLITTER 
					+ ".*")
					);
		} catch (SQLException e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}
	
	public JsonObject getWhereEndsWith(String pattern) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(pattern.replaceAll("%", "*"));
		try {
			List<JsonObject> dbResults = filter(manager.queryForJsonWhereEndsWith(pattern));
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (SQLException e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}

	/**
	 * Get all docs whose id starts with specified pattern
	 * @param pattern, e.g. _users
	 * @return all matching docs
	 */
	public JsonObject getWhereStartsWith(String pattern) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(pattern.replaceAll("%", "*"));
		try {
			List<JsonObject> dbResults = filter(manager.queryForJsonWhereStartsWith(pattern));
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (SQLException e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}
	
	/**
	 * Examines the list of JsonObjects.
	 * If the object contains a valueSchemaId,
	 * attempts to find the schema for it in the database.
	 * If found, it adds it to the map that the method will return.
	 * @param list
	 * @param username - if not null, used to filter list of domains to ones authorized
	 * TODO: ?? where does it filter???
	 * @return
	 */
	public Map<String, JsonObject> getSchemas(
			List<JsonObject> list
			, String username
			) {
		/**
		 * 4 lines below added June 22, 2017 by MAC.   
		 * Symptom is H2Connection manager throws an error saying the object is already closed
		 * 
		 */
		String user = username;
		if (user == null) {
			user = "admins~web_service~wsadmin";
		}
		Map<String,JsonObject> result = new TreeMap<String,JsonObject>();
		for (JsonObject json : list) {
			String id = null;
			if (json.has(Constants.VALUE_SCHEMA_ID)) {
				id = json.get(Constants.VALUE_SCHEMA_ID).getAsString();
			} else if (json.has("doc." + Constants.VALUE_SCHEMA_ID)) {
				id = json.get("doc." + Constants.VALUE_SCHEMA_ID).getAsString();
			} else if (json.has("link")) {
				if (json.get("link").getAsJsonObject().has("properties")) {
					if (json.get("link").getAsJsonObject().get("properties").getAsJsonObject().has(Constants.VALUE_SCHEMA_ID)) {
						id = json.get("link").getAsJsonObject().get("properties").getAsJsonObject().get(Constants.VALUE_SCHEMA_ID).getAsJsonObject().get("val").getAsString();
					}
				}
			} else if (json.has("properties(to)")) {
				id = json.get("properties(to)").getAsJsonObject().get(Constants.VALUE_SCHEMA_ID).getAsString();
			}
			if (id != null) {
				try {
					JsonObject schema = getSchema(id);
					if (schema != null && ! result.containsKey(id)) {
						if (id.startsWith(AuthorizationCreateForm.class.getSimpleName())) {
							JsonObject schemaObject = schema.get("schema").getAsJsonObject();
							JsonObject propertiesObject = schemaObject.get("properties").getAsJsonObject();
							propertiesObject.add("username", getUserIdsSelectionWidgetSchema());
							propertiesObject.add("library", getDomainIdsSelectionWidgetSchema(user));
							schemaObject.add("properties", propertiesObject);
							schema.add("schema", schemaObject);
						}
						result.put(id, schema);
					}
				} catch (Exception e) {
					ErrorUtils.report(logger, e);
				}
			} else {
				logger.info("InternalDbManager.getSchemas() reports missing schema id for " + json.toString());
			}
		}
		return result;
	}

	/**
	 * Get all docs whose id starts with specified pattern
	 * @param pattern, e.g. _users
	 * @return all matching docs
	 */
	public ResultJsonObjectArray getForIdStartsWith(String id) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(id);
		try {
			List<JsonObject> dbResults = filter(manager.queryForJsonWhereStartsWith(id));
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (SQLException e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setStatusMessage(e.getMessage());
		}
		return result;
	}
	
	public ResultJsonObjectArray getForIdEndsWith(String s) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(s);
		try {
			List<JsonObject> dbResults = filter(manager.queryForJsonWhereEndsWith(s));
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (SQLException e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setStatusMessage(e.getMessage());
		}
		return result;
	}

	/**
	 * Filter out user hashes from the list
	 * @param list
	 * @return
	 */
	private List<JsonObject> filter(List<JsonObject> list) {
		try {
			return list.stream() 
			.filter(obj -> ! obj.get("_id").getAsString().startsWith("users|hash"))	// filter out the user hashes
			.collect(Collectors.toList());
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return list;
	}

	/**
	 * Get doc whose id matches
	 * @param pattern, e.g. _users/{id}
	 * @return matching doc
	 */
	public ResultJsonObjectArray getForId(String id) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(id);
		try { 
			List<JsonObject> dbResults = manager.queryForJsonWhereEqual(id);
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (SQLException e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setStatusMessage(e.getMessage());
		}
		return result;
	}
	
	public JsonObject getNewUserForm(String query) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(query);
		try {
			UserCreateForm form = new UserCreateForm();
			LTKVJsonObject record = 
					new LTKVJsonObject(
						USER_TOPICS.NEW.lib
						, USER_TOPICS.NEW.topic
						, "user"
						, form.schemaIdAsString()
						, form.toJsonObject()
						);

			List<JsonObject> dbResults = new ArrayList<JsonObject>();
			dbResults.add(record.toJsonObject());
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (Exception e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.SERVER_ERROR.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}

	private String userFromQuery(String query) {
		return query;
	}
	
	
	public LTKVJsonObject getUserSelfServicePasswordChangeForm(
			String requestor
			, String username
			) {
		LTKVJsonObject record = null;
		try {
			UserPasswordSelfChangeForm form = new UserPasswordSelfChangeForm();
			form.setUsername(username);
			record = 
					new LTKVJsonObject(
							new IdManager("users","password",username).getId()
						, form.schemaIdAsString()
						, form.toJsonObject()
						);
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return record;
	}

	public LTKVJsonObject getUserPasswordChangeForm(
			String requestor
			, String username
			) {
		LTKVJsonObject record = null;
		try {
			UserPasswordChangeForm form = new UserPasswordChangeForm();
			form.setUsername(username);
			// if user whose password we are changing is the requestor
			// they do not have to change the password after login.
			form.setRequiresChangeAfterLogin(!requestor.equals(username));
			record = 
					new LTKVJsonObject(
							new IdManager("users","password",username).getId()
						, form.schemaIdAsString()
						, form.toJsonObject()
						);
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return record;
	}

	public JsonObject getUserPasswordChangeFormObject(
			String requestor
			, String query
			) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(query);
		List<JsonObject> dbResults = new ArrayList<JsonObject>();
		try {
			String username = userFromQuery(query);
			dbResults.add(getUserPasswordChangeForm(requestor,username).toJsonObject());
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (Exception e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.SERVER_ERROR.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}

	public JsonObject getUserPasswordChangeForms(String query) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(query);
		List<JsonObject> dbResults = new ArrayList<JsonObject>();
		try {
			for (String id : getUserIDsForPasswordChange()) {
			UserPasswordChangeForm form = new UserPasswordChangeForm();
			form.setUsername(id);
			LTKVJsonObject record = 
					new LTKVJsonObject(
							"users|password|" + id
						, form.schemaIdAsString()
						, form.toJsonObject()
						);

			dbResults.add(record.toJsonObject());
			}
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (Exception e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.SERVER_ERROR.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}
	
	public JsonObject getUserPasswordChangeForms(String requestor, String query) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(query);
		List<JsonObject> dbResults = new ArrayList<JsonObject>();
		try {
			for (String id : getUserIDsForPasswordChange()) {
				dbResults.add(getUserPasswordChangeForm(requestor,id).toJsonObject());
			}
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (Exception e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.SERVER_ERROR.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}
	
	public JsonObject getUserPasswordChangeFormWithUiSchema(
			String requestor
			, String query) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(query);
		List<JsonObject> dbResults = new ArrayList<JsonObject>();
		try {
			dbResults.add(
					getUserSelfServicePasswordChangeForm(
					requestor
					, requestor
					).toJsonObject()
			);
			result.setValueSchemas(getSchemas(dbResults, null));
			result.setResult(dbResults);
		} catch (Exception e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.SERVER_ERROR.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}

	private void initializeAgesDomains() {
		for (String d : agesDomains) {
			String library = d.toLowerCase();
			if (! this.existsLibrary(library)) {
				String [] parts = library.split("_");
				DomainCreateForm domain = new DomainCreateForm();
				domain.setLanguageCode(parts[0]);
				domain.setCountryCode(parts[1]);
				domain.setRealm(parts[2]);
				domain.setDescription(library);
				List<String> labels = new ArrayList<String>();
				labels.add("Liturgical");
				domain.setLabels(labels);
				addDomain(wsAdmin, domain.toJsonString());
			}
		}
	}

	private void addAgesRoles(String user, ROLES role) {
		for (String d : agesDomains) {
			if (! this.hasRole(role, d, user)) {
				this.grantRole("wsadmin", role, d.toLowerCase(), user);
			}
		}
	}

	private void addPublicRoles(String user, ROLES role) {
		for (String d : publicSystemDomains) {
			if (! this.hasRole(role, d, user)) {
				this.grantRole("wsadmin", role, d.toLowerCase(), user);
			}
		}
	}
	private void initializePublicSystemDomainsMap() {
		publicSystemDomains.add("en_sys_ontology");
		publicSystemDomains.add("en_sys_linguistics");
		publicSystemDomains.add("en_sys_tables");
	}
	
	private void initializeAgesDomainsMap() {
		agesDomains.add("en_US_andronache");
		agesDomains.add("en_US_barrett");
		agesDomains.add("en_US_boyer");
		agesDomains.add("en_US_constantinides");
		agesDomains.add("en_US_dedes");
		agesDomains.add("en_US_goa");
		agesDomains.add("en_US_holycross");
		agesDomains.add("en_UK_lash");
		agesDomains.add("en_US_oca");
		agesDomains.add("en_US_public");
		agesDomains.add("en_US_repass");
		agesDomains.add("en_US_unknown");
		agesDomains.add("gr_GR_cog");
		
		// ages scripture
		agesDomains.add("en_UK_kjv");
		agesDomains.add("en_US_eob");
		agesDomains.add("en_US_kjv");
		agesDomains.add("en_US_net");
		agesDomains.add("en_US_nkjv");
		agesDomains.add("en_US_rsv");
		agesDomains.add("en_US_saas");
		
		// Kenya
		agesDomains.add("kik_KE_oak");
		agesDomains.add("swh_KE_oak");
		
		// added by Meg
		agesDomains.add("fra_FR_oaf");
		agesDomains.add("spa_GT_odg");

	}
	
	/**
	 * Get a list of the IDs of all users
	 * @return
	 */
	public List<String> getUserIds() {
		return getIds(USER_TOPICS.CONTACT.toId(""));
	}

	/**
	 * Get a domain based on its key
	 * @param key
	 * @return
	 */
	public Domain getDomain(String key) {
		try {			
			ResultJsonObjectArray obj = getForId(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId(key));
			Long count = obj.getResultCount();
			if (count != 1) {
				return null;
			} else {
				Domain domain = (Domain) gson.fromJson(
						obj.getFirstObjectValueAsObject()
						, Domain.class
				);
				return domain;
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
			return null;
		}
	}

	public TreeSet<String> getDomainAdmins(String domain) {
		return getUsersWithRoleForDomain(ROLES.ADMIN, domain);
	}

	public TreeSet<String> getDomainAuthors(String domain) {
		return getUsersWithRoleForDomain(ROLES.AUTHOR, domain);
	}

	public TreeSet<String> getDomainReaders(String domain) {
		return getUsersWithRoleForDomain(ROLES.READER, domain);
	}

	public TreeSet<String> getDomainReviewers(String domain) {
		return getUsersWithRoleForDomain(ROLES.REVIEWER, domain);
	}
	
	/**
	 * For the specified domain, get a JsonObject that has
	 * admins: a JsonArray of dropdown items that are users who have an admin role for the domain
	 * authors: a JsonArray of dropdown items that are users who have an author role for the domain
	 * readers: a JsonArray of dropdown items that are users who have an reader role for the domain
	 * reviewers: a JsonArray of dropdown items that areusers who have an reviewer role for the domain
	 * @param domain
	 * @return
	 */
	public JsonObject getDropdownsForUsersWithRoleForDomain(String domain) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery("workflow config, statuses, and users for " + domain);
		try {
			
			// get the workflow flags for this domain
			Domain theDomainObject = getDomain(domain);
			DomainWorkflowInfo workflowInfo = new DomainWorkflowInfo();
			workflowInfo.setDefaultStatusAfterEdit(theDomainObject.getDefaultStatusAfterEdit());
			workflowInfo.setDefaultStatusAfterFinalization(theDomainObject.getDefaultStatusAfterFinalization());
			workflowInfo.setStateEnabled(theDomainObject.isStateEnabled());
			workflowInfo.setWorkflowEnabled(theDomainObject.isWorkflowEnabled());
			workflowInfo.setType(theDomainObject.getType());
			JsonObject info = new JsonObject();
			info.add("config", workflowInfo.toJsonObject());
			
			// set the users and roles for this domain
			JsonObject dropdowns = new JsonObject();
			dropdowns.add("admins", userIdsToDropdown(getDomainAdmins(domain)));
			dropdowns.add("authors", userIdsToDropdown(getDomainAuthors(domain)));
			dropdowns.add("readers", userIdsToDropdown(getDomainReaders(domain)));
			dropdowns.add("reviewers", userIdsToDropdown(getDomainAdmins(domain)));
			JsonObject statuses = new JsonObject();
			JsonObject visibility = new JsonObject();
			
			// get the status dropdown
			statuses.add("statusDropdown", STATUS.toDropdownJsonArray());
			
			// get the visibility dropdown to use with the domain
			visibility.add("visibilityDropdown", VISIBILITY.toDropdownJsonArray());

			// build the JsonObject  list
			List<JsonObject> list = new ArrayList<JsonObject>();
			list.add(dropdowns);
			list.add(statuses);
			list.add(visibility);
			list.add(info);
			// add the list to the final result
			result.setResult(list);
		} catch (Exception e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}
	
	public JsonObject getLibraryWorkflowInfo(String library) {
		JsonObject result = new JsonObject();
		
		return result;
	}

	/**
	 * For each user ID, gets that user from the database
	 * and creates a DropdownItem for the user, that includes
	 * their title, first name, and last name.
	 * @param userIds
	 * @return
	 */
	private JsonArray userIdsToDropdown(TreeSet<String> userIds) {
		JsonArray result = new JsonArray();
		for (String id : userIds) {
			try {
				UserContact user = getUserContact(id);
				StringBuilder sb = new StringBuilder();
				sb.append(id);
				sb.append(" (");
				if (user.title.length() > 0) {
					sb.append(user.title);
					sb.append(" ");
				}
				sb.append(user.getFirstname());
				sb.append(" ");
				sb.append(user.getLastname());
				sb.append(")");
				result.add(new DropdownItem(sb.toString(),id).toJsonObject());
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
		}
		return result;
	}

	/**
	 * Returns an alphabetized list of users who have the specified role
	 * for this domain.  Note that it will also include admins, since an
	 * admin for a domain also automatically has all other roles.
	 * @param role
	 * @param domain
	 * @return
	 */
	private TreeSet<String> getUsersWithRoleForDomain(ROLES role, String domain) {
		TreeSet<String> result = new TreeSet<String>();
		// if we are not checking for admin users, include them since they automatically
		// have all other roles.
		if (! role.equals(ROLES.ADMIN)) {
			for (JsonElement e : getWhereLike(ROLES.ADMIN.keyname + "%" + domain).get("values").getAsJsonArray()) {
				String user = e.getAsJsonObject().get("key").getAsString();
				if (! result.contains(user)) {
					result.add(e.getAsJsonObject().get("key").getAsString());
				}
			}
		}
		for (JsonElement e : getWhereLike(role.keyname + "%" + domain).get("values").getAsJsonArray()) {
			String user = e.getAsJsonObject().get("key").getAsString();
			if (! result.contains(user)) {
				result.add(e.getAsJsonObject().get("key").getAsString());
			}
		}
		return result;
	}

	/**
	 * Get a list of the users with a path for changing their password
	 * These can be listed in the user interface for selection by
	 * an administrator.
	 * @return
	 */
	public List<String> getUserIDsForPasswordChange() {
		List<String> result = new ArrayList<String>();
		List<String> userIds = getUserIds();
		for (String id: userIds) {
			try {
				String [] parts = id.split(Constants.ID_SPLITTER);
				result.add(parts[2]);
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
		}
		return result;
	}


	/**
	 * Get the domains know to the system
	 * @return
	 */
	private List<String> getDomains() {
		List<String> result = new ArrayList<String>();
		for (String id : getDomainIds()) {
			IdManager idManager = new IdManager(id);
			result.add(idManager.getKey());
		}
		return result;
	}
	
	/**
	 * Get the IDs of domains, this is misc~domains~{the domain}, e.g. misc~domains~gr_gr_cog
	 * @return
	 */
	private List<String> getDomainIds() {
		return getIds(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId(""));
	}

	private JsonObject getUserIdsSelectionWidgetSchema() {
		return getIdsAsSelectionWidgetSchema("Users", USER_TOPICS.CONTACT.toId(""), false, true);
	}

	/**
	 * Provides a list of all domains found in the database without filtering based on user's admin auth
	 * @return
	 */
	private JsonObject getDomainIdsSelectionWidgetSchema() {
		return getIdsAsSelectionWidgetSchema("Domains", SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId(""), false, true);
	}

	/**
	 * Provides a list of Domains for which the user has admin authority,
	 * set up as a schema widget
	 * @param username
	 * @return
	 */
	public JsonObject getDomainIdsSelectionWidgetSchema(String username) {
		JsonObject result = new JsonObject();

		List<String> idsList = new ArrayList<String>();
		
		if (isWsAdmin(username)) { // add in the web_service as a library and all_domains as a library
			idsList.add(Constants.ID_DELIMITER+Constants.SYSTEM_LIB+Constants.ID_DELIMITER);
			idsList.add(Constants.ID_DELIMITER+Constants.DOMAINS_LIB+Constants.ID_DELIMITER);
		} else if (isDbAdmin(username)) { // add in all_domains as a library
			idsList.add(Constants.ID_DELIMITER+Constants.DOMAINS_LIB+Constants.ID_DELIMITER);
		}

		if (isDbAdmin(username)) { // get all domains
			idsList.addAll(getIds(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId("")));
		} else { // only get domains for which the user is authorized to be an administrator
			idsList.addAll(getIds(ROLES.ADMIN.keyname + "%" + username));
		}
		
		Collections.sort(idsList);
		
		String[] idsArray = new String[idsList.size()];
		String[] labelsArray = new String[idsList.size()];
		
		for (int i=0; i < idsList.size(); i++) {
			String x = "";
			IdManager idManager = new IdManager(idsList.get(i));
			if (idManager.get(0).startsWith(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.lib)) {
				idsArray[i] = idManager.get(2);
				labelsArray[i] = idManager.get(2);
			} else {
				idsArray[i] = idManager.get(1);
				labelsArray[i] = "* " + idManager.get(1);
			}
		}
		SelectionWidgetSchema widget = new SelectionWidgetSchema(
				"Domains"
				, idsArray
				, labelsArray
				);
		JsonParser parser = new JsonParser();
		result = parser.parse(widget.toWidgetJsonString()).getAsJsonObject();
		return result;
	}

	private JsonObject getIdsAsSelectionWidgetSchema(
			String title
			, String like
			, boolean keyOnlyForValue
			, boolean keyOnlyForLabel
			) {
		List<String> idsList = getIds(like);
		String[] idsArray = new String[idsList.size()];
		String[] labelsArray = new String[idsList.size()];
		idsArray = idsList.toArray(idsArray);
		labelsArray = idsArray;
		
		if (keyOnlyForValue) {
			for (int i=0; i < idsList.size(); i++) {
				IdManager idManager = new IdManager(idsArray[i]);
				idsArray[i] = idManager.get(2);
			}
		}
		if (keyOnlyForValue && keyOnlyForLabel) {
			labelsArray = idsArray;
		} else if (keyOnlyForLabel) {
			for (int i=0; i < idsList.size(); i++) {
				IdManager idManager = new IdManager(labelsArray[i]);
				labelsArray[i] = idManager.get(2);
			}
		}
		
		SelectionWidgetSchema widget = new SelectionWidgetSchema(
				title
				, idsArray
				, labelsArray
				);
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(widget.toWidgetJsonString()).getAsJsonObject();
		return json;
	}
	
	public List<String> getIds(String like) {
		List<String> list = new ArrayList<String>();
		for (JsonElement user : getWhereLike(like).get("values").getAsJsonArray()) {
			list.add(user.getAsJsonObject().get("_id").getAsString());
		}
		return list;
	}
	
	public List<Domain> getDomainObjects() {
		List<Domain> domains = new ArrayList<Domain>();
		for (String id : getDomainIds()) {
			try {
				IdManager idManager = new IdManager(id);
				domains.add(this.getDomain(idManager.getKey()));
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
		}
		return domains;
	}

	public List<JsonObject> getDomainJsonObjects() {
		List<JsonObject> domains = new ArrayList<JsonObject>();
		for (String id : getDomainIds()) {
			try {
				IdManager idManager = new IdManager(id);
				domains.add(this.getDomain(idManager.getKey()).toJsonObject());
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
		}
		return domains;
	}
	
	public JsonArray getDomainObjectArray() {
		JsonArray result = new JsonArray();
		for (JsonObject json : this.getDomainJsonObjects()) {
			result.add(json);
		}
		return result;
	}

	public JsonObject getDomainsGroupedByType() {
		JsonObject result = new JsonObject();
		JsonObject collective = new JsonObject();
		JsonObject collectiveLiturgical = new JsonObject();
		JsonObject collectiveBiblical = new JsonObject();
		JsonObject user = new JsonObject();
		for (Domain domain : this.getDomainObjects()) {
			if (domain.getType() == DOMAIN_TYPES.COLLECTIVE) {
				collective.add(domain.getDomain(), domain.toJsonObject());
				if (domain.getLabels().contains("Liturgical")) {
					collectiveLiturgical.add(domain.getDomain(), domain.toJsonObject());
				} else if (domain.getLabels().contains("Biblical")) {
					collectiveBiblical.add(domain.getDomain(), domain.toJsonObject());
				}
			} else {
				user.add(domain.getDomain(), domain.toJsonObject());
			}
		}
		result.add("collective", collective);
		result.add("collectiveBiblical", collectiveBiblical);
		result.add("collectiveLiturgical", collectiveLiturgical);
		result.add("user", user);
		return result;
	}

	public List<String> getDomainsThatAreCollectiveLiturgical() {
		List<String> result = new ArrayList<String>();
		for (Domain domain : this.getDomainObjects()) {
			if (domain.getType() == DOMAIN_TYPES.COLLECTIVE) {
				if (domain.getLabels().contains("Liturgical")) {
					result.add(domain.getDomain());
				}
			}
		}
		return result;
	}

	public JsonObject getDomainsThatAreCollectiveLiturgicalAsJson() {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery("get domains that are liturgical and collective");
		try {
			List<JsonObject> list = new ArrayList<JsonObject>();
			for (Domain domain : this.getDomainsObjectsThatAreCollectiveLiturgical()) {
				list.add(domain.toJsonObject());
			}
			result.setResult(list);
		} catch (Exception e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}

	public List<Domain> getDomainsObjectsThatAreCollectiveLiturgical() {
		List<Domain> result = new ArrayList<Domain>();
		for (Domain domain : this.getDomainObjects()) {
			if (domain.getType() == DOMAIN_TYPES.COLLECTIVE) {
				if (domain.getLabels().contains("Liturgical")) {
					result.add(domain);
				}
			}
		}
		Collections.sort(result);
		return result;
	}

	public Map<String, DropdownItem> getCollectiveLiturgicalDropdownDomainMap() {
		Map<String, DropdownItem> result = new TreeMap<String, DropdownItem>();
		for (Domain domain : this.getDomainObjects()) {
			if (domain.getType() == DOMAIN_TYPES.COLLECTIVE) {
				if (domain.getLabels().contains("Liturgical")) {
					String label = domain.getDescription().trim();
					if (label.length() == 0 || label.equals(domain.getDomain())) {
						label = domain.getDomain();
					} else {
						label = domain.getDomain() + " - " + domain.getDescription();
					}
					result.put(domain.getDomain(), new DropdownItem(label, domain.getDomain()));
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @param requestor
	 * @param restriction
	 * @return
	 */
	public boolean userAuthorizedForThisForm(
			String requestor
			, RESTRICTION_FILTERS restriction
			) {
		boolean result = false;
			switch (restriction) {
			case ALL_DOMAINS_ADMIN:
				result = isDbAdmin(requestor);
				break;
			case ALL_DOMAINS_AUTHOR:
				result = isDbAdmin(requestor);
				break;
			case ALL_DOMAINS_READER:
				result = isDbAdmin(requestor);
				break;
			case ALL_DOMAINS_REVIEWER:
				result = isDbAdmin(requestor);
				break;
			case DOMAIN_ADMIN:
				result = isAdminForAnyLib(requestor);
				break;
			case DOMAIN_AUTHOR:
				result = isAdminForAnyLib(requestor);
				break;
			case DOMAIN_READER:
				result = isAdminForAnyLib(requestor);
				break;
			case DOMAIN_REVIEWER:
				result = isAdminForAnyLib(requestor);
				break;
			case NONE:
				result = true;
				break;
			case WS_ADMIN:
				result = isWsAdmin(requestor);
				break;
			default:
				result = false;
				break;
			}
		return result;
	}
	
	public JsonObject getNewDocForms(String requestor, String query) {
		ResultJsonObjectArray result = new ResultJsonObjectArray(prettyPrint);
		result.setQuery(query);
		List<JsonObject> dbResults = new ArrayList<JsonObject>();
		try {
			for (NEW_FORM_CLASSES_ADMIN_API e : NEW_FORM_CLASSES_ADMIN_API.values()) {
				if (userAuthorizedForThisForm(requestor, e.restriction)) {
					LTKVJsonObject record = 
							new LTKVJsonObject(
								e.endpoint.library
								, "new"
								, e.name
								, e.obj.schemaIdAsString()
								, e.obj.toJsonObject()
								);
				dbResults.add(record.toJsonObject());
				}
			}
			result.setValueSchemas(getSchemas(dbResults, requestor));
			result.setResult(dbResults);
		} catch (Exception e) {
			result.setStatusCode(HTTP_RESPONSE_CODES.SERVER_ERROR.code);
			result.setStatusMessage(e.getMessage());
		}
		return result.toJsonObject();
	}

	private void verify() {
		try {
	       	// create the table.  Will only happen if doesn't already exist.
        	manager.createTable();
        	
        	if (deleteOldTableRows) {
            	manager.truncateTable();
        	}
        	
        	this.initializeAgesDomainsMap();
        	this.initializePublicSystemDomainsMap();
        	
        	// check to see if table has values
           	List<JsonObject> jsonList = manager.queryForJson();  
           	if (jsonList.isEmpty()) {
           		initializeTable(); // will also add schemas
           	} else {
    			addSchemas(); // this adds the schemas used by the system
           	}
           	
           	// add descriptions of utilities.
           	this.createUtilityDescriptions();
           	
           	// make sure all users have access to read public domains
           	for (String user : this.getUserIds()) {
           		if (user.endsWith("wsadmin")) {
           			// ignore
           		} else {
               		String [] parts = user.split(Constants.ID_SPLITTER);
               		if (parts.length == 3) {
                   		this.addAgesRoles(parts[2], ROLES.READER);
                   		this.addPublicRoles(parts[2], ROLES.READER);
               		}
           		}
           	}
           	
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
	}
	
	
	private void initializeUser(
			String username
			, String firstname
			, String lastname
			, ROLES role
			, String lib
			) throws BadIdException {
		
		UserCreateForm user = new UserCreateForm();
		user.setFirstname(firstname);
		user.setLastname(lastname);
		user.setPassword(this.wsAdminPwd);
		user.setPasswordReenter(user.getPassword());
		user.setUsername(username);
		user.setLanguageCode("en");
		user.setCountryCode("us");
		user.setEmail(user.getUsername()+"@liml.org");
		user.setEmailReenter(user.getEmail());
		addUser(user.getUsername(), user.toJsonString());
		UserStatistics stats = new UserStatistics();
		addUserStats(user.getUsername(),stats);
		if (role != null) {
			this.grantRole(user.getUsername(), role, lib, user.getUsername());
		}
		logger.info("test user " + user.getUsername() + " added");
	}
	
	private void initializeTable() {
		logger.info("Initializing table " + tablename + " for database " + storename);
		initialized = false;
		
		/***
		 * TODO: change authentication to use /users/hash/username
		 */
		try {
			addRoles();
   			addSchemas(); 
   			
			// add the ws admin user
			UserCreateForm user = new UserCreateForm();
			user.setFirstname("IOC Liturgical");
			user.setLastname("Web Services Admin");
			user.setEmail("admin@ioc-liturgical-db.org");
			user.setPassword(this.wsAdminPwd);
			user.setEmailReenter(user.getEmail());
			user.setPasswordReenter(user.getPassword());
			user.setUsername(wsAdmin);
			user.setLanguageCode("en");
			user.setCountryCode("sys");
			addUser("wsadmin", user.toJsonString());

			logger.info("ws admin user added");
			bootstrapSystemAdminRole(wsAdmin,ROLES.ADMIN, Constants.SYSTEM_LIB, wsAdmin);
			logger.info("system admin role added");
			UserStatistics stats = new UserStatistics();
			addUserStats(user.getUsername(),stats);

			// add domain for Commonly Used Greek text
			DomainCreateForm domain = new DomainCreateForm();
			domain.setLanguageCode("gr");
			domain.setCountryCode("gr");
			domain.setRealm("cog");
			domain.setDescription("Commonly used Orthodox Greek text");
			List<String> labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Archdiocese of America
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("us");
			domain.setRealm("goa");
			domain.setDescription("Translations by the Greek Orthodox Archdiocese of America");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add public domain for Fr. Eugen Pentiuc
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("us");
			domain.setRealm("pentiucpublic");
			domain.setDescription("Notes and Translations by the Rev. Dr. Eugen Pentiuc");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Fr. Seraphim Dedes
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("us");
			domain.setRealm("dedes");
			domain.setDescription("Translations by Fr. Seraphim Dedes");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for ocmc
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("us");
			domain.setRealm("ocmc");
			domain.setDescription("Notes and Translations by OCMC staff");
			domain.setLabels(labels);
			domain.setType(DOMAIN_TYPES.COLLECTIVE);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Metropolis of Asia and South East Asia
			domain = new DomainCreateForm();
			domain.setLanguageCode("zh");
			domain.setCountryCode("hk");
			domain.setRealm("omhksea");
			domain.setDescription("Translations in Chinese for Metropolis of Hong Kong and Southeast Asia");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Fr. Ephrem Lash of Blessed Memory
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("uk");
			domain.setRealm("lash");
			domain.setDescription("Translations by Fr. Ephrem Lash");
			domain.setLabels(labels);
			domain.setType(DOMAIN_TYPES.COLLECTIVE);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Global English Version - BOT
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("uk");
			domain.setRealm("gev");
			domain.setDescription("GEV Notes and Translations by Dr. Michael Colburn");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Global English Version - SOT
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("uk");
			domain.setRealm("gesot");
			domain.setDescription("GE-SOT Notes and Translations by Dr. Michael Colburn");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Global English Version - MOT
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("uk");
			domain.setRealm("gemot");
			domain.setDescription("GE-MOT Notes and Translations by Dr. Michael Colburn");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for the Festal Menaion (TFM)
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("uk");
			domain.setRealm("tfm");
			domain.setDescription("The Festal Menaion - Mother Mary and Metropolitan Kallistos");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Spanish - Guatemala
			domain = new DomainCreateForm();
			domain.setLanguageCode("spa");
			domain.setCountryCode("gt");
			domain.setRealm("odg");
			domain.setDescription("Translations in Spanish for the Orthodox Church in Guatemala");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for French
			domain = new DomainCreateForm();
			domain.setLanguageCode("fra");
			domain.setCountryCode("fra");
			domain.setRealm("pdg");
			domain.setDescription("Translations in French by Fr. Dennis Guillaume of blessed memory");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Kikuyu
			domain = new DomainCreateForm();
			domain.setLanguageCode("kik");
			domain.setCountryCode("ke");
			domain.setRealm("oak");
			domain.setDescription("Translations in Kikuyu for Archdiocese of Kenya");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for Swahili - Kenya
			domain = new DomainCreateForm();
			domain.setLanguageCode("swh");
			domain.setCountryCode("ke");
			domain.setRealm("oak");
			domain.setDescription("Translations in Swahili for Archdiocese of Kenya");
			labels = new ArrayList<String>();
			labels.add("Liturgical");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for system ontology
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("sys");
			domain.setRealm("ontology");
			domain.setDescription("System ontology entries");
			domain.setLabels(labels);
			domain.setType(DOMAIN_TYPES.COLLECTIVE);
			addDomain(wsAdmin, domain.toJsonString());

			// add domain for system linguistics
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("sys");
			domain.setRealm("linguistics");
			domain.setDescription("System linguistics entries");
			labels = new ArrayList<String>();
			labels.add("System");
			domain.setLabels(labels);
			domain.setType(DOMAIN_TYPES.COLLECTIVE);
			addDomain(wsAdmin, domain.toJsonString());
			
			// add domain for system abbreviations
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("sys");
			domain.setRealm("abbreviations");
			domain.setDescription("System abbreviations");
			labels = new ArrayList<String>();
			labels.add("System");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());


			// add domain for system Bibliography entries
			domain = new DomainCreateForm();
			domain.setLanguageCode("en");
			domain.setCountryCode("sys");
			domain.setRealm("bibliography");
			domain.setDescription("System bibliography entries");
			labels = new ArrayList<String>();
			labels.add("System");
			domain.setLabels(labels);
			addDomain(wsAdmin, domain.toJsonString());

			logger.info("domains added");

			// TODO: delete the code for the following users once the database is in use
			
			String linguisticsDomain = "en_sys_linguistics";
			String ontologyDomain = "en_sys_ontology";
			String ocmcDomain = "en_us_ocmc";
			
			// add Fr. Pentiuc if he is not in the database
			String username = "frepentiuc";
			if (! this.existsUser(username)) {
				user = new UserCreateForm();
				user.setFirstname("Eugen");
				user.setLastname("Pentiuc");
				user.setTitle("Rev. Dr.");
				user.setEmail("epentiuc@hchc.edu");
				user.setPassword(this.wsAdminPwd);
				user.setEmailReenter(user.getEmail());
				user.setPasswordReenter(user.getPassword());
				user.setUsername(username);
				user.setLanguageCode("en");
				user.setCountryCode("us");
				addUser("wsadmin", user.toJsonString());
				this.grantRole("wsadmin", ROLES.ADMIN, ontologyDomain, username);
				this.grantRole("wsadmin", ROLES.ADMIN, "en_us_pentiucpublic", username);
				logger.info("user Fr. Pentiuc added");
				stats = new UserStatistics();
				addUserStats(user.getUsername(),stats);
			}
			
			username = "sdedes";
			if (! this.existsUser(username)) {
				user = new UserCreateForm();
				user.setFirstname("Seraphim");
				user.setLastname("Dedes");
				user.setTitle("Fr.");
				user.setEmail("seraphimdedes@gmail.com");
				user.setPassword(this.wsAdminPwd);
				user.setEmailReenter(user.getEmail());
				user.setPasswordReenter(user.getPassword());
				user.setUsername(username);
				user.setLanguageCode("en");
				user.setCountryCode("us");
				addUser("wsadmin", user.toJsonString());
				this.grantRole("wsadmin", ROLES.ADMIN, ontologyDomain, username);
				this.addAgesRoles(username, ROLES.ADMIN);
				logger.info("user Fr. Dedes added");
				stats = new UserStatistics();
				addUserStats(user.getUsername(),stats);
			}
			
			username = "mcolburn";
			if (! this.existsUser(username)) {
				user = new UserCreateForm();
				user.setFirstname("Michael");
				user.setLastname("Colburn");
				user.setTitle("Dr.");
				user.setEmail("m.colburn@ocmc.org");
				user.setPassword(this.wsAdminPwd);
				user.setEmailReenter(user.getEmail());
				user.setPasswordReenter(user.getPassword());
				user.setUsername(username);
				user.setLanguageCode("en");
				user.setCountryCode("us");
				addUser("wsadmin", user.toJsonString());
				this.grantRole("wsadmin", ROLES.ADMIN, ontologyDomain, username);
				this.grantRole("wsadmin", ROLES.ADMIN, ocmcDomain, username);
				this.grantRole("wsadmin", ROLES.ADMIN, linguisticsDomain, username);
				this.grantRole("wsadmin", ROLES.ADMIN, "en_uk_gev", username);
				this.grantRole("wsadmin", ROLES.ADMIN, "en_uk_gesot", username);
				this.grantRole("wsadmin", ROLES.ADMIN, "en_uk_gemot", username);
				this.grantRole("wsadmin", ROLES.ADMIN, "en_uk_tfm", username);
				logger.info("user Colburn added");
				stats = new UserStatistics();
				addUserStats(user.getUsername(),stats);
			}
			
			this.initializeAgesDomains();
			for (String id : this.getUserIds()) {
				String parts [] = id.split("~");
				if (parts.length == 3) {
					this.addAgesRoles(parts[2], ROLES.READER);
				}
			}
			
			if (createTestUsers) {
				// add a test user who can administer all domains
				initializeUser(
						"adminForAllDomains"
						, "admin"
						, "ForAllDomains"
						, ROLES.ADMIN
						, Constants.DOMAINS_LIB
						);
	
				initializeUser(
						"adminForEnUsDedes"
						, "admin"
						, "ForEnUsDedes"
						, ROLES.ADMIN
						, "en_us_dedes"
						);
	

				initializeUser(
						"notAnAdmin"
						, "not"
						, "AnAdmin"
						, null
						, null
						);
				logger.info("Test users added");
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}

		logger.info("The database has been initialized.");
		logger.info(
				"The web services administrator user has been created: " 
				+ wsAdmin
		);
		logger.info(
				"The password for " 
				+ wsAdmin 
				+ " is the one you passed in as a parameter when you started the jar."
		);
		logger.info("Full rights have been granted to: " + ROLES.ADMIN.keyname + " on " + Constants.SYSTEM_LIB);
		logger.info("The user " + wsAdmin + " has been granted the role of " + ROLES.ADMIN.keyname + " on " + Constants.SYSTEM_LIB);
		logger.info("Use this first user and first role to add more users and more rights.");
		
		initialized = true;
	}

	public boolean isWildcardLib(String library) {
		if (isSystemWildcard(library) || isDomainsWildcard(library)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isSystemWildcard(String library) {
		if (library.startsWith(Constants.SYSTEM_LIB) && library.endsWith(Constants.SYSTEM_LIB)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isDomainsWildcard(String library) {
		if (library.startsWith(Constants.DOMAINS_LIB) && library.endsWith(Constants.DOMAINS_LIB)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param library
	 * @return true if is a wildcard library or a domain library
	 */
	public boolean existsLibrary(String library) {
		if (isWildcardLib(library)) {
			return true;
		} else {
			return isDomainLibrary(library);
		}
	}
	
	public boolean isDomainLibrary(String library) {
		ResultJsonObjectArray json = getForId(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId(library));
		return json.getCount() > 0;
	}

	/**
	 * Checks to see if the user has a UserContact record
	 * @param user
	 * @return
	 */
	public boolean existsUser(String user) {	
		if (user.length() > 0) {
			try {
				ResultJsonObjectArray json = getForId(USER_TOPICS.CONTACT.toId(user));
				return json.getCount() > 0;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean authorizedToGrantRole(
			String requestor
			, ROLES role
			, String library
			) {
		boolean authorized = false;
		if (isWsAdmin(requestor)) {
			authorized = true;
		} else if (isDbAdmin(requestor) &&  isDomainLibrary(library)){
			authorized = true;
		} else if (isLibAdmin(library, requestor)) {
			authorized = true;
		}
		return authorized;
	}
	
	public void grantGenericRoles(
			String requestor
			, ROLES role
			, String user
			) {
	}
	
	public RequestStatus grantRole(
			String requestor
			, ROLES role
			, String library
			, String user
			) {
		RequestStatus result = new RequestStatus();
		if (authorizedToGrantRole(requestor,role,library)) {
			if (existsUser(user)) {
				if (existsLibrary(library)) {
					UserAuth auth = new UserAuth();
					auth.setGrantedBy(requestor);
					auth.setGrantedWhen(getTimestamp());
					String description = " For the library " + library + ", is " + role.description;
					if (role.equals(ROLES.ADMIN)) {
						if ( isSystemWildcard(library)) {
							description = "The user has administrative authorization for the entire web service system and databases it protects.";
						} else if (isDomainsWildcard(library)) {
							description = "The user has administrative authorization for all domain libraries.";
						}
					}
					auth.setDescription(description);
					try {
						result = addLTKVJsonObject(
								role.keyname
								, library
								, user
								, auth.schemaIdAsString()
								,auth.toJsonObject()
								);
					} catch (MissingSchemaIdException e) {
						ErrorUtils.report(logger, e);
						result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
						result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
					} catch (Exception e) {
						result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
						result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
					}
				} else {
					result.setCode(HTTP_RESPONSE_CODES.LIBRARY_DOES_NOT_EXIST.code);
					result.setMessage(HTTP_RESPONSE_CODES.LIBRARY_DOES_NOT_EXIST.message);
				}
			} else {
				result.setCode(HTTP_RESPONSE_CODES.USER_DOES_NOT_EXIST.code);
				result.setMessage(HTTP_RESPONSE_CODES.USER_DOES_NOT_EXIST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.FORBIDDEN.code);
			result.setMessage(HTTP_RESPONSE_CODES.FORBIDDEN.message);
		}
    	return result;
	}

	/**
	 * The public grant method checks to see if the user is authorized.
	 * This bootstrap method is private and only used to initialize the
	 * authority of the web service root user account.
	 * @param requestor
	 * @param role
	 * @param library
	 * @param user
	 * @return
	 */
	private RequestStatus bootstrapSystemAdminRole(
			String requestor
			, ROLES role
			, String library
			, String user
			) {
		RequestStatus result = new RequestStatus();
		UserAuth auth = new UserAuth();
		auth.setGrantedBy(requestor);
		auth.setGrantedWhen(getTimestamp());
		String description = "The user has administrative authorization for the entire web service system and databases it protects.";
		auth.setDescription(description);
			try {
				result = addLTKVJsonObject(
						role.keyname
						, library
						, user
						, auth.schemaIdAsString()
						,auth.toJsonObject()
						);
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
		return result;
	}

		/**
	 * TODO Do we need this?
	 * @param role
	 * @param user
	 */
	private RequestStatus revokeRole(ROLES role, String user) {
		RequestStatus result = new RequestStatus();
		LTKVString tkv;
		try {
			tkv = new LTKVString(
					new IdManager(
							"rights"
							, role.keyname
							, user
							).getId()
					, role.description
					);
			manager.delete(tkv.toJsonObject());
		} catch (Exception e) {
			result = new RequestStatus(HTTP_RESPONSE_CODES.BAD_REQUEST);
		}
		return result;
	}

	/**
	 * 
	 * This method will create an instance of
	 * UserContact and UserHash
	 * 
	 * @param json from a UserCreateForm
	 * @return
	 */
	public RequestStatus addUser(
			String requestor
			, String json
			) {
	RequestStatus result = new RequestStatus();
	UserCreateForm userForm = new UserCreateForm();
	userForm = (UserCreateForm) userForm.fromJsonString(json);
	String validation = userForm.validate(json);
	if (validation.length() == 0) {
		String realm = "";
		if (userForm.getUsername().equals("wsadmin")) {
			realm = "wsadmin";
		} else if (userForm.getUsername().equals("frsdedes")) {
			realm = "dedes";
		} else {
			realm = userForm.getFirstname().toLowerCase().charAt(0)
			+ userForm.getLastname().toLowerCase();
		}
		try {
			// first create a UserContact
			UserContact user = new UserContact();
			user.setFirstname(userForm.getFirstname());
			String lastName = userForm.getLastname();
			lastName = lastName.replaceAll("-", ""); // disallow names with hyphen
			user.setLastname(lastName);
			user.setEmail(userForm.getEmail());
			user.setTitle(userForm.getTitle());
			user.setDomain(
					userForm.getLanguageCode()
					+ Constants.DOMAIN_DELIMITER
					+ userForm.getCountryCode()
					+ Constants.DOMAIN_DELIMITER
					+ realm
					);
			user.setCreatedBy(requestor);
			user.setModifiedBy(requestor);
			user.setCreatedWhen(getTimestamp());
			user.setModifiedWhen(user.getCreatedWhen());
			result = addUserContact(userForm.getUsername(), user);
			
			if (result.getCode() == HTTP_RESPONSE_CODES.CREATED.code) {
				// now create a UserHash
				UserHash hash = new UserHash();
				hash.setPassword(userForm.getPassword());
				// we will overwrite the original response code
				result = addUserHash(userForm.getUsername(), hash);
				
				// and create the user's personal domain if it does not already exist
				if (! existsLibrary(user.getDomain())) {
					Domain domain = new Domain();
					domain.setLanguageCode(userForm.getLanguageCode());
					domain.setCountryCode(userForm.getCountryCode());
					domain.setRealm(realm);
					domain.setType(DOMAIN_TYPES.USER);
					domain.setDescription(
							"Notes and/or translations by " 
							        + (userForm.getTitle().length() > 0 ? userForm.getTitle() + " " : "")
									+ userForm.getFirstname() 
									+ " "
									+ userForm.getLastname()
					);
					domain.labels.add("Liturgical");
					addDomain(requestor, domain.toJsonString());
					
					// make the user an admin for his/her personal domain
					grantRole(requestor, ROLES.ADMIN, domain.getDomain(), userForm.getUsername());
					// grant reader access to AGES and public system domains
					this.addAgesRoles(userForm.getUsername(), ROLES.READER);
					this.addPublicRoles(userForm.getUsername(), ROLES.READER);
				}
			}
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		}
	} else {
		result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
		JsonObject message = stringToJson(validation);
		if (message == null) {
			result.setMessage(validation);
		} else {
			result.setMessage(message.get("message").getAsString());
		}
	}
	return result;
}
	
	public RequestStatus deleteUser(
			String requestor
			, String username
			) {
		RequestStatus result = new RequestStatus();
		if (username == null || username.length() == 0) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} else if ( ! this.isDbAdmin(requestor)){
			result.setCode(HTTP_RESPONSE_CODES.UNAUTHORIZED.code);
			result.setMessage(HTTP_RESPONSE_CODES.UNAUTHORIZED.message);
		} else {
			try {
				ResultJsonObjectArray query = this.getForIdEndsWith(username);
				for (JsonObject o : query.values) {
					String id = o.get("_id").getAsString();
					logger.info(requestor + " deleted " + id);
					this.deleteForId(id);
				}
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		}
		return result;
	}

	/**
	 * 
	 * This method will update an instance of UserHash
	 * 
	 * @param json from a UserChangePasswordForm
	 * @return
	 */
	public RequestStatus updateUserPassword(String query, String json) {
	RequestStatus result = new RequestStatus();
	UserPasswordChangeForm userForm = new UserPasswordChangeForm();
	try {
		userForm = (UserPasswordChangeForm) userForm.fromJsonString(json);
		String validation = userForm.validate(json);
		if (validation.length() == 0) {
			try {
					// create a UserHash
					UserHash hash = new UserHash();
					hash.setPassword(userForm.getPassword());
					// we will overwrite the original response code
					result = updateUserHash(userForm.getUsername(), hash);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			JsonObject message = stringToJson(validation);
			if (message == null) {
				result.setMessage(validation);
			} else {
				result.setMessage(message.get("message").getAsString());
			}
		}
	} catch (Exception e) {
		result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
		result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
	}
	return result;
}

	/**
	 * Adds a new domain
	 * @param requestor - username of the requestor
	 * @param json - json string representation of a new domain form
	 * @return the status of the request
	 */
	public RequestStatus addDomain(String requestor, String json) {
		RequestStatus result = new RequestStatus();
		DomainCreateForm form = new DomainCreateForm();
		form = (DomainCreateForm) form.fromJsonString(json);
		String validation = form.validate(json);
		if (validation.length() == 0) {
			try {
				Domain domain = new Domain();
				domain.setLanguageCode(form.getLanguageCode());
				domain.setCountryCode(form.getCountryCode());
				domain.setRealm(form.getRealm());
				domain.setDescription(form.getDescription());
				domain.setLabels(form.getLabels());
				domain.setType(form.getType());
				domain.setDefaultStatusAfterEdit(form.getDefaultStatusAfterEdit());
				domain.setDefaultStatusAfterFinalization(form.getDefaultStatusAfterFinalization());
				domain.setStateEnabled(form.isStateEnabled());
				domain.setWorkflowEnabled(form.isWorkflowEnabled());
				domain.setCreatedBy(requestor);
				domain.setModifiedBy(requestor);
				domain.setCreatedWhen(getTimestamp());
				domain.setModifiedWhen(domain.getCreatedWhen());
				String key = Joiner.on("_").join(form.getLanguageCode(), form.getCountryCode(), form.getRealm());
				result = addLTKVJsonObject(
						SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.lib
						, SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.topic
						, key
						, domain.schemaIdAsString()
						, domain.toJsonObject()
						);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			JsonObject message = stringToJson(validation);
			if (message == null) {
				result.setMessage(validation);
			} else {
				result.setMessage(message.get("message").getAsString());
			}
		}
		return result;
	}

	/**
	 * This creates a description of a utility.
	 * It will not be exposed in the web service.
	 * However, a post handler is exposed to
	 * allow execution of a specific utility
	 * @param requestor
	 * @param json
	 * @return
	 */
	private RequestStatus addUtility(String requestor, String json) {
		// if you subtype Utility class, make sure you add the subtype to the Internal 
		// schemas in the ioc-liturgical-schemas constants.
		// and add an if clause like the one below for GeneratePdfFiles.
		RequestStatus result = new RequestStatus();
		Utility jsonObj =  new Utility();
		jsonObj = (Utility) jsonObj.fromJsonString(json);
		if (jsonObj.getName().equals(UTILITIES.GeneratePdfFiles.keyname)) {
			jsonObj = new UtilityPdfGeneration();
			jsonObj = (UtilityPdfGeneration) jsonObj.fromJsonString(json);
		}
		String validation = jsonObj.validate(json);
		if (validation.length() == 0) {
			try {
				jsonObj.setCreatedBy(requestor);
				jsonObj.setModifiedBy(requestor);
				jsonObj.setCreatedWhen(getTimestamp());
				jsonObj.setModifiedWhen(jsonObj.getCreatedWhen());
				String key = jsonObj.name;
				result = addLTKVJsonObject(
						SYSTEM_MISC_LIBRARY_TOPICS.UTILITIES.lib
						, SYSTEM_MISC_LIBRARY_TOPICS.UTILITIES.topic
						, key
						, jsonObj.schemaIdAsString()
						, jsonObj.toJsonObject()
						);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			JsonObject message = stringToJson(validation);
			if (message == null) {
				result.setMessage(validation);
			} else {
				result.setMessage(message.get("message").getAsString());
			}
		}
		return result;
	}

	public RequestStatus addLabel(String requestor, String json) {
		RequestStatus result = new RequestStatus();
		LabelCreateForm form = new LabelCreateForm();
		form = (LabelCreateForm) form.fromJsonString(json);
		String validation = form.validate(json);
		if (validation.length() == 0) {
			try {
				Label label = new Label();
				label.setLabel(form.getLabel());
				label.setTitle(form.getTitle());
				label.setDescription(form.getDescription());
				label.setCreatedBy(requestor);
				label.setModifiedBy(requestor);
				label.setCreatedWhen(getTimestamp());
				label.setModifiedWhen(label.getCreatedWhen());
				result = addLTKVJsonObject(
						SYSTEM_MISC_LIBRARY_TOPICS.LABELS.lib
						, SYSTEM_MISC_LIBRARY_TOPICS.LABELS.topic
						, label.getLabel()
						, label.schemaIdAsString()
						, label.toJsonObject()
						);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			JsonObject message = stringToJson(validation);
			if (message == null) {
				result.setMessage(validation);
			} else {
				result.setMessage(message.get("message").getAsString());
			}
		}
		return result;
	}


	public RequestStatus addAuthorization(String requestor, String json) {
		RequestStatus result = new RequestStatus();
		AuthorizationCreateForm form = new AuthorizationCreateForm();
		form = (AuthorizationCreateForm) form.fromJsonString(json);
		String validation = form.validate(json);
		if (validation.length() == 0) {
			try {
				ROLES role = ROLES.forWsname(form.getRole()+"s");
				result = grantRole(requestor, role, form.getLibrary(), form.getUsername());
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			JsonObject message = stringToJson(validation);
			if (message == null) {
				result.setMessage(validation);
			} else {
				result.setMessage(message.get("message").getAsString());
			}
		}
		return result;
	}

	private JsonObject stringToJson(String s) {
		try {
			return new JsonParser().parse(s).getAsJsonObject();
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return null;
	}
	
	/**
	 * Add a user based on a json string.
	 * @param key username for the user
	 * @param json string of UserContact properties
	 * @return
	 */
	public RequestStatus addUserContact(String key, String json) {
		RequestStatus result = new RequestStatus();
		UserContact user = new UserContact();
		user = (UserContact) user.fromJsonString(json);
		String validation = user.validate(json);
		if (validation.length() == 0) {
			try {
				result = addUserContact(key, user);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(validation);
		}
		return result;
	}

	public RequestStatus addUserContact(
			String key
			, UserContact user
			) throws BadIdException {
		RequestStatus result = new RequestStatus();
		try {
			result = addLTKVJsonObject(
					USER_TOPICS.CONTACT.lib
					, USER_TOPICS.CONTACT.topic
					, key
					, user.schemaIdAsString()
					,user.toJsonObject()
					);
			UserStatistics stats = new UserStatistics();
			addUserStats(key,stats);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		}
    	return result;
    }

	public RequestStatus addUserPreferences(String key, String json) {
		RequestStatus result = new RequestStatus();
		UserPreferences prefs = new UserPreferences();
		prefs = (UserPreferences) prefs.fromJsonString(json);
		String validation = prefs.validate(json);
		if (validation.length() == 0) {
			try {
				result = addUserPreferences(key, prefs);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(validation);
		}
		return result;
	}
	
	public RequestStatus addUserPreferences(
			String key
			, UserPreferences prefs
			) throws BadIdException {
		RequestStatus result = new RequestStatus();
		try {
			result = addLTKVJsonObject(
					USER_TOPICS.PREFERENCES.lib
					, USER_TOPICS.PREFERENCES.topic
					, key
					, prefs.schemaIdAsString()
					, prefs.toJsonObject()
					);
			UserStatistics stats = new UserStatistics();
			addUserStats(key,stats);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		}
    	return result;
    }

	public void addUserHash(
			String requestor
			, String key
			, String hashedPassword
			) throws BadIdException {
		UserHash userHash = new UserHash();
		userHash.setHashedPassword(hashedPassword);
		addUserHash(key, userHash);
    }
	
	public RequestStatus addUserHash(
			String key
			, UserHash userHash
			) throws BadIdException {
		RequestStatus result = new RequestStatus();
		try {
			result = addLTKVJsonObject(
					USER_TOPICS.HASH.lib
					, USER_TOPICS.HASH.topic
					, key
					, userHash.schemaIdAsString()
					,userHash.toJsonObject()
					);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		}
    	return result;
    }

	public RequestStatus updateUserHash(
			String key
			, UserHash userHash
			) throws BadIdException {
		RequestStatus result = new RequestStatus();
		try {
			result = updateLTKVJsonObject(
					USER_TOPICS.HASH.lib
					, USER_TOPICS.HASH.topic
					, key
					, userHash.schemaIdAsString()
					,userHash.toJsonObject()
					);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		}
    	return result;
    }

	public RequestStatus addUserStats(
			String key
			, UserStatistics userStats
			) throws BadIdException {
		RequestStatus result = new RequestStatus();
		try {
			result = addLTKVJsonObject(
					USER_TOPICS.STATISTICS.lib
					, USER_TOPICS.STATISTICS.topic
					, key
					, userStats.schemaIdAsString()
					,userStats.toJsonObject()
					);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		}
    	return result;
    }
	
	public RequestStatus updateDomain(String requestor, String key, String json) {
		RequestStatus result = new RequestStatus();
		Domain obj = new Domain();
		String validation = obj.validate(json);
		if (validation.length() == 0) {
			try {
				obj = (Domain) obj.fromJsonString(json);
				obj.setModifiedBy(requestor);
				obj.setModifiedWhen(getTimestamp());
				result = updateDomain(key, obj);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(validation);
		}
		return result;
	}
	
	private RequestStatus updateDomain(String key, Domain obj) {
		RequestStatus result = new RequestStatus();
		try {
	    	result = updateLTKVJsonObject(
	    			SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.lib
	    			, SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.topic
	    			, key
	    			, obj.schemaIdAsString()
	    			, obj.toJsonObject()
	    			);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(e.getMessage());
		}
		return result;

	}

	public RequestStatus updateLabel(String requestor, String key, String json) {
		RequestStatus result = new RequestStatus();
		Label obj = new Label();
		String validation = obj.validate(json);
		if (validation.length() == 0) {
			try {
				obj = (Label) obj.fromJsonString(json);
				obj.setModifiedBy(requestor);
				obj.setModifiedWhen(getTimestamp());
				result = updateLabel(key, obj);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(validation);
		}
		return result;
	}
	
	private RequestStatus updateLabel(String key, Label obj) {
		RequestStatus result = new RequestStatus();
		try {
	    	result = updateLTKVJsonObject(
	    			SYSTEM_MISC_LIBRARY_TOPICS.LABELS.lib
	    			, SYSTEM_MISC_LIBRARY_TOPICS.LABELS.topic
	    			, key
	    			, obj.schemaIdAsString()
	    			, obj.toJsonObject()
	    			);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(e.getMessage());
		}
		return result;

	}


	public RequestStatus updateUserContact(
			String requestor
			, String key
			, String json
			) {
		RequestStatus result = new RequestStatus();
		UserContact user = new UserContact();
		String validation = user.validate(json);
		if (validation.length() == 0) {
			try {
				user = (UserContact) user.fromJsonString(json);
				user.setModifiedBy(requestor);
				user.setModifiedWhen(getTimestamp());
				result = updateUserContact(key, user);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(validation);
		}
		return result;
	}
	
	public RequestStatus updateUserPreferences(
			String requestor
			, String key
			, String json
			) {
		RequestStatus result = new RequestStatus();
		UserPreferences prefs = new UserPreferences();
		String validation = prefs.validate(json);
		if (validation.length() == 0) {
			try {
				prefs = (UserPreferences) prefs.fromJsonString(json);
				prefs.setModifiedBy(requestor);
				prefs.setModifiedWhen(getTimestamp());
				result = updateUserPreferences(key, prefs);
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(validation);
		}
		return result;
	}

	private RequestStatus updateUserContact(String key, UserContact user) {
		RequestStatus result = new RequestStatus();
		try {

	    	result = updateLTKVJsonObject(
	    			USER_TOPICS.CONTACT.lib
	    			, USER_TOPICS.CONTACT.topic
	    			, key
	    			, user.schemaIdAsString()
	    			, user.toJsonObject()
	    			);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(e.getMessage());
		}
		return result;
	}
	
	private RequestStatus updateUserPreferences(String key, UserPreferences prefs) {
		RequestStatus result = new RequestStatus();
		try {

	    	result = updateLTKVJsonObject(
	    			USER_TOPICS.PREFERENCES.lib
	    			, USER_TOPICS.PREFERENCES.topic
	    			, key
	    			, prefs.schemaIdAsString()
	    			, prefs.toJsonObject()
	    			);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	private RequestStatus updateUserStats(String key, UserStatistics user) {
		RequestStatus result = new RequestStatus();
		try {
	    	result = updateLTKVJsonObject(
	    			USER_TOPICS.STATISTICS.lib
	    			, USER_TOPICS.STATISTICS.topic
	    			, key
	    			, user.schemaIdAsString()
	    			, user.toJsonObject()
	    			);
		} catch (MissingSchemaIdException e) {
			ErrorUtils.report(logger, e);
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
		} catch (Exception e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			result.setMessage(e.getMessage());
		}
		return result;

	}

	/**
	 * Initializes schemas for both the internal and external databases
	 */
	private void addSchemas() {
		try {
			for (INTERNAL_DB_SCHEMA_CLASSES s :INTERNAL_DB_SCHEMA_CLASSES.values()) {
				try {
					ValueSchema schema = new ValueSchema(s.obj);
					String id = new IdManager(
							SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.lib
							, SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.topic
							, s.obj.schemaIdAsString()
							).getId();
					if (existsUnique(id)) {
						updateSchema(s.obj.schemaIdAsString(), schema.toJsonObject());
					} else {
						addSchema(s.obj.schemaIdAsString(), schema.toJsonObject());
					}
				} catch (Exception e) {
					ErrorUtils.report(logger, e);
				}
			}
			for (SCHEMA_CLASSES s : SCHEMA_CLASSES.values()) {
				try {
					ValueSchema schema = new ValueSchema(s.ltk);
					String id = new IdManager(
							SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.lib
							, SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.topic
							, s.ltk.schemaIdAsString()
							).getId();
					if (existsUnique(id)) {
						updateSchema(s.ltk.schemaIdAsString(), schema.toJsonObject());
					} else {
						addSchema(s.ltk.schemaIdAsString(), schema.toJsonObject());
					}
					schema = new ValueSchema(s.ltkDb);
					id = new IdManager(
							SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.lib
							, SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.topic
							, s.ltkDb.schemaIdAsString()
							).getId();
					if (existsUnique(id)) {
						updateSchema(s.ltkDb.schemaIdAsString(), schema.toJsonObject());
					} else {
						addSchema(s.ltkDb.schemaIdAsString(), schema.toJsonObject());
					}
				} catch (Exception e) {
					ErrorUtils.report(logger, e);
				}
			}
			for (TEMPLATE_CONFIG_MODELS s : TEMPLATE_CONFIG_MODELS.values()) {
				try {
					ValueSchema schema = new ValueSchema(s.model);
					String id = new IdManager(
							SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.lib
							, SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.topic
							, s.model.schemaIdAsString()
							).getId();
					if (existsUnique(id)) {
						updateSchema(s.model.schemaIdAsString(), schema.toJsonObject());
					} else {
						addSchema(s.model.schemaIdAsString(), schema.toJsonObject());
					}
				} catch (Exception e) {
					ErrorUtils.report(logger, e);
				}
			}
			logger.info("Schemas added");
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
	}
	
	private void addRoles() throws BadIdException, SQLException {
		for (ROLES r : ROLES.values()) {
			addTKVString(Constants.SYSTEM_LIB,"roles", r.keyname, r.description);
		}
		logger.info("Roles added");
	}
	
	/**
	 * Add a doc whose value is a string and has an _id made of topic and key
	 * @param topic
	 * @param key
	 * @param value
	 * @throws BadIdException 
	 * @throws SQLException 
	 */
	private RequestStatus addTKVString(String library, String topic, String key, String value) throws BadIdException, SQLException {
		RequestStatus result = new RequestStatus();
		String id = new IdManager(library,topic,key).getId();
		if (existsUnique(id)) {
			result.setCode(HTTP_RESPONSE_CODES.CONFLICT.code);
			result.setMessage(HTTP_RESPONSE_CODES.CONFLICT.message + ": " + id);
		} else {
			LTKVString tkv;
			try {
				tkv = new LTKVString(
						library
						, topic
						, key
						, value
						);
		    	manager.insert(tkv.toJsonObject());		
		    	result.setCode(HTTP_RESPONSE_CODES.CREATED.code);
		    	result.setMessage(HTTP_RESPONSE_CODES.CREATED.message + ": " + id);
			} catch (org.ocmc.ioc.liturgical.schemas.exceptions.BadIdException e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(e.getMessage());
			}
		}
		return result;
	}
	
	/**
	 * Add a doc whose value is a JsonObject and whose _id is a library|topic|key
	 * @param library
	 * @param topic
	 * @param key
	 * @param json
	 * @param MissingSchemaIdException
	 * @throws SQLException 
	 * @throws BadIdException 
	 */
	public RequestStatus addLTKVJsonObject(
			String library
			, String topic
			, String key
			, String schemaId
			, JsonObject json
			) throws DbException, MissingSchemaIdException, BadIdException {
		RequestStatus result = new RequestStatus();
		if (existsSchema(schemaId)) {
			String id = new IdManager(library,topic,key).getId();
			if (existsUnique(id)) {
				result.setCode(HTTP_RESPONSE_CODES.CONFLICT.code);
				result.setMessage(HTTP_RESPONSE_CODES.CONFLICT.message + ": " + id);
			} else {
				LTKVJsonObject record = 
						new LTKVJsonObject(
							library
							, topic
							, key
							, schemaId
							, json
							);
				   try {
				    	manager.insert(record.toJsonObject());		
				   } catch (SQLException e) {
					   throw new DbException(
							   "Error adding " 
							   + library
							   +":" + topic 
							   +":" + key 
							   , e
							   );
				   }
			    	result.setCode(HTTP_RESPONSE_CODES.CREATED.code);
			    	result.setMessage(HTTP_RESPONSE_CODES.CREATED.message + ": " + id);
			}
		} else {
			throw new MissingSchemaIdException(schemaId);
		}
		return result;
	}

	/**
	 * Does this schemaId exist in the database?
	 * @param schemaId
	 * @return
	 */
	public boolean existsSchema(String schemaId) {
		return existsUnique(SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.toId(schemaId));
	}
	
	public JsonObject getSchema(String key) {
		JsonObject result = null;
		try {
			List<JsonObject> schemas = manager.queryForJsonWhereEqual(SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.toId(key));
			if (schemas.size() > 0) {
				result = manager.queryForJsonWhereEqual(SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.toId(key)).get(0);
			}
			if (result != null) {
				result = result.get("value").getAsJsonObject();
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
			return null;
		}
		return result;
	}

	public RequestStatus updateLTKVJsonObject(
			String library
			, String topic
			, String key
			, String schemaId
			, JsonObject json
			) throws BadIdException, MissingSchemaIdException, DbException {
		RequestStatus result = new RequestStatus();
		if (existsSchema(schemaId)) {
			String id = new IdManager(library,topic,key).getId();
			if (existsUnique(id)) {
				LTKVJsonObject record;
				record = new LTKVJsonObject(
						library
						, topic
						, key
						, schemaId
						, json
						);
				   try {
						manager.updateWhereEqual(record.toJsonObject());		
				   } catch (SQLException e) {
					   throw new DbException(
							   "Error updating " 
							   + library
							   +":" + topic 
							   +":" + key 
							   , e
							   );
				   }
			} else {
				result.setCode(HTTP_RESPONSE_CODES.NOT_FOUND.code);
				result.setMessage(HTTP_RESPONSE_CODES.NOT_FOUND.message + ": " + id);
			}
		} else {
			throw new MissingSchemaIdException(schemaId);
		}
		return result;
	}
	
	
	public UserStatus getUserStatus(
			String username
			, String password
			, String verb
			, String library
			) {
		UserStatus  status = new UserStatus();
		if (existsUser(username)) {
			UserStatistics userStats = getUserStats(username);
			status.setKnownUser(true);
			status.setAuthenticated(authenticated(username,password));
			status.setAuthorized(authorized(username,VERBS.forWsname(verb),library));
			long currentNano = System.nanoTime();
			long lastNano = userStats.getLastAccessNanos();
			if (lastNano == 0) {
				status.setSessionExpired(true);
			} else {
				long elapsedNano = currentNano - lastNano;
				long elapsedMinutes = TimeUnit.MINUTES.convert(elapsedNano, TimeUnit.NANOSECONDS);
				status.setSessionExpired(elapsedMinutes > maxInactiveMinutes);
			}
			userStats.setLastAccessNanos(currentNano);
			userStats.setLastSuccessfulAccessDateTime(getTimestamp());
			if (status.isAuthenticated()) {
				userStats.setAccessCount(userStats.getAccessCount() + 1);
			} else {
				userStats.setFailedLoginCount(userStats.getFailedLoginCount() + 1);
				userStats.setLastFailedAccessDateTime(Instant.now().toString());
			}
			updateUserStats(username, userStats);
		}
		return status;
	}
	
	private String getTimestamp() {
		return Instant.now().toString();
	}

	public String getUserDomain(String username) {
		try {
			return getUserContact(username).getDomain();
		} catch (Exception e) {
			return null;
		}
	}
	
	public User getUser(String username) {
		try {			
			ResultJsonObjectArray obj = getForId(SYSTEM_MISC_LIBRARY_TOPICS.USERS.toId(username));
			Long count = obj.getCount();
			if (count != 1) {
				return null;
			} else {
				User user = (User) gson.fromJson(
						obj.getFirstObjectValueAsObject()
						, User.class
				);
				return user;
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
			return null;
		}
	}

	public UserContact getUserContact(String username) {
		try {			
			ResultJsonObjectArray obj = getForId(USER_TOPICS.CONTACT.toId(username));
			Long count = obj.getCount();
			if (count != 1) {
				return null;
			} else {
				UserContact user = (UserContact) gson.fromJson(
						obj.getFirstObjectValueAsObject()
						, UserContact.class
				);
				return user;
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
			return null;
		}
	}
	
	public UserPreferences getUserPreferences(String username) {
		try {			
			ResultJsonObjectArray obj = getForId(USER_TOPICS.PREFERENCES.toId(username));
			UserPreferences prefs = new UserPreferences();
			Long count = obj.getCount();
			if (count != 1) {
				  this.addUserPreferences(username, prefs.toJsonString());
			} else {
				prefs = (UserPreferences) gson.fromJson(
						obj.getFirstObjectValueAsObject()
						, UserPreferences.class
				);
			}
			return prefs;
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
			return null;
		}
	}

	public UserHash getUserHash(String username) {
		try {			
			ResultJsonObjectArray obj = getForId(USER_TOPICS.HASH.toId(username));
			Long count = obj.getCount();
			if (count != 1) {
				return null;
			} else {
				UserHash user = (UserHash) gson.fromJson(
						obj.getFirstObjectValueAsObject()
						, UserHash.class
				);
				return user;
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
			return null;
		}
	}
	
	public UserStatistics getUserStats(String username) {
		try {			
			ResultJsonObjectArray obj = getForId(USER_TOPICS.STATISTICS.toId(username));
			Long count = obj.getCount();
			if (count != 1) {
				return new UserStatistics();
			} else {
				UserStatistics user = (UserStatistics) gson.fromJson(
						obj.getFirstObjectValueAsObject()
						, UserStatistics.class
				);
				return user;
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
			return null;
		}
	}

	public String hashPassword(String password) {
		try {
			return PasswordHasher.createHash(password);
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return null;
	}
	
	public boolean authenticated(String username, String password) {
		try {
			UserHash user = getUserHash(username);
			if (user == null) {
				return false;
			} else {
				if (PasswordHasher.checkPassword(password, user.getHashedPassword())) {
					return true;
				} else {
					return false;
				}
			}
		} catch (NoSuchAlgorithmException e) {
			ErrorUtils.report(logger, e);
		} catch (InvalidKeySpecException e) {
			ErrorUtils.report(logger, e);
		}
		return false;
	}
	
	/**
	 * forms
	 * api/_sys
	 * api/_app
	 * api/lib
	 * 
	 * sysadmin
	 * 		if exists _rights/_sys/sysadmin/mcolburn
	 * 		put _sys/_rights/sysadmin/hjones
	 * 		put _app/_rights/appadmin/cbrown
	 * appadmin
	 * 		put lib/_rights/libadmin/gr_gr_cog/mbarnes
	 * 		put lib/_rights/admin/gr_gr_cog/mbarnes
	 * libadmin
	 * 		put lib/_rights/libauthor/gr_gr_cog/frraphael
	 * 		put lib/_rights/libreader/gr_gr_cog/public   <-- reserved user
	 * 		put lib/_rights/author/gr_gr_cog/frraphael
	 * 		put lib/_rights/reader/gr_gr_cog/public   <-- reserved user
	 * libauthor
	 * 		write lib if exists _rights/lib/gr_gr_cog/frraphael
	 * libreader
	 * 		read lib if exists _rights/lib/gr_gr_cog/public
	 * 
	 * 
	 * username
	 * library
	 * method
	 * 
	 * method/library/username
	 * put/gr_gr_cog/mcolburn
	 * put/_sys/mcolburn <- by person
	 * put/_sys/sysadmin <- by role
	 * 
	 * When create a new library create these rights:
	 * _rights/post/{library}/{role} <- sysadmin, appadim, libadmin, libauthor 
	 * _rights/get/{library}/{role} <- sysadmin, appadim, libadmin, libauthor
	 * _rights/put/{library}/{role} <- sysadmin, appadim, libadmin, libauthor
	 * _rights/delete/{library}/{role} <- sysadmin, appadim, libadmin, libauthor
	 * 
	 * When grant role to user:
	 * 
	 * {library}/{role}/{username}
	 * 
	 * Issues
	 * 	1. a library is like a table.  It should exist before granting rights to it or posting to it.
	 * 2. a library has meta-data such as the language, country, realm, who it is for, etc.
	 * 
	 * 
	 * admin/_sys
	 * admin/_app
	 * admin/gr_gr_cog
	 * author/gr_gr_cog/sjones
	 * reader/gr_gr_cog/all
	 * 
	 * if role based,
	 * 1. get all the roles for the person
	 * 2. check to see if any of the roles match the action and lib
	 * 
	 */
	
	
	/**
	 * A user who is an admin for _sys has the power to do anything to the system or any database library. 
	 * A _sys admin can make grant _sys or _db admin to any other user.
	 * 
	 * A user who is an admin for the _db has the power to do anything to the docs in the database, and
	 * to add users to the system, and to grant their authorizations for _db.  But, he/she does not have
	 * the ability to make give another person the authority to by an admin of the _sys or the _db.
	 * 
	 * A user who has authorization for any other library can be either an author or reader.
	 * An author can create, read, update, and delete docs in that library.
	 * A reader can only read docs in that library.
	 * 
	 * @param username 
	 * @param verb
	 * @param library
	 * @return
	 */
	public boolean authorized(
			String username
			, VERBS verb
			, String library
			) {
		boolean isAuthorized = false;
		if (isWsAdmin(username)) {
			isAuthorized = true;
		} else if (isDbAdmin(username)) {
			isAuthorized = true;
		} else if (isAdminForAnyLib(username) && isAdminPath(library)) {
			isAuthorized = true;
			isAuthorized = true;
 		} else {
			if (isLibAdmin(library, username)) {
				isAuthorized = true;
			} else {
		    	switch (verb) {
		    	case GET: {
					if (isLibAuthor(library, username) 
							|| isLibReader(library, username)
							|| library.equals("docs")
							|| library.equals("login")
							|| library.equals("links")
							|| library.equals("nlp")
							|| library.equals("ontology")
							|| library.equals("linguistics")
							) {
						isAuthorized = true;
					}
		    		break;
		    	}
		    	case POST: {
					if (isLibAuthor(library, username) 
							|| (isAuthorForAnyLib(username) && isGenericLibrary(library))
							) {
						isAuthorized = true;
					}
		    		break;
		    	}
			case PUT:
				if (isLibAuthor(library, username) 
						|| (isAuthorForAnyLib(username) && isGenericLibrary(library))
						) {
					isAuthorized = true;
				}
				break;
			case DELETE:
				if (isLibAuthor(library, username) 
						|| (isAuthorForAnyLib(username) && isGenericLibrary(library))
						) {
					isAuthorized = true;
				}
				break;
			default:
				break;
	    	}
			}
		}
		if (suppressAuth) { // used for debugging purposes
			return true;
		} else {
			if (isAuthorized) {
				return true;
			} else {
				logger.info(username + " not authorized for " + verb + " against " + library);
				return false;
			}
		}
	}
	
	/**
	 * Is this path one of the web service admin paths?
	 * @param path
	 * @return
	 */
	private boolean isAdminPath(String path) {
		if (path.matches(Constants.RESOURCES_PATH)) {
			return true;
		} else {
			for (ENDPOINTS_ADMIN_API e : ENDPOINTS_ADMIN_API.values()) {
				if (e.library.equals(path)) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean isGenericLibrary(String library) {
		boolean result = false;
		if (
				library.equals("docs")
				|| library.equals("login")
				|| library.equals("links")
				|| library.equals("nlp")
			) {
			result = true;
		}
		return result;
	}
	/**
	 * Is this person an administrator of the Web Service?
	 * 
	 * @param username
	 * @return
	 */
	public boolean isWsAdmin(String username) {
		return hasRole(ROLES.ADMIN, Constants.SYSTEM_LIB, username);
	}
	

	/**
	 * Is this person an administrator of the backend database?
	 * @param username
	 * @return
	 */
	public boolean isDbAdmin(String username) {
		return isWsAdmin(username) || hasRole(ROLES.ADMIN,Constants.DOMAINS_LIB,username);
	}
	
	public boolean isAdminForAnyLib(String username) {
		return isDbAdmin(username) ||
				getWhereLike(ROLES.ADMIN.keyname + "%" + username).get("valueCount").getAsInt() > 0
				;
	}
	
	public boolean isAuthorForAnyLib(String username) {
		return isDbAdmin(username) 
				|| isAdminForAnyLib(username) 
				|| getWhereLike(ROLES.AUTHOR.keyname + "%" + username).get("valueCount").getAsInt() > 0
				;
	}

	public boolean isReaderForAnyLib(String username) {
		return isDbAdmin(username) 
				|| isAuthorForAnyLib(username) 
				|| getWhereLike(ROLES.READER.keyname + "%" + username).get("valueCount").getAsInt() > 0
				;
	}

	public List<String> getDomainsTheUserAdministers(String username) {
		List<String> result = new ArrayList<String>();
		if (isDbAdmin(username)) {
			return getDomains();
		} else {
			JsonObject json = getWhereLike(ROLES.ADMIN.keyname + "%" + username);
			if (json.get("valueCount").getAsInt() > 0) {
				for (JsonElement value : json.get("values").getAsJsonArray()) {
					result.add(value.getAsJsonObject().get("topic").getAsString());
				}
			}
			return result;
		}
	}

	public List<String> getDomainsTheUserReads(String username) {
		List<String> result = new ArrayList<String>();
		if (isDbAdmin(username)) {
			return getDomains();
		} else {
			JsonObject json = getWhereLike(ROLES.READER.keyname + "%" + username);
			if (json.get("valueCount").getAsInt() > 0) {
				for (JsonElement value : json.get("values").getAsJsonArray()) {
					result.add(value.getAsJsonObject().get("topic").getAsString());
				}
			}
			return result;
		}
	}

	public JsonArray getDomainsUserCanRead(String username) {
		JsonArray result = new JsonArray();
		JsonObject json = getWhereLike(ROLES.READER.keyname + "%" + username);
		if (json.get("valueCount").getAsInt() > 0) {
			result = json.get("values").getAsJsonArray();
		}
		for (Domain domain : this.getDomainsObjectsThatAreCollectiveLiturgical()) {
			try {
				result.add(domain.toJsonObject());
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
		}
		return result;
	}

	public JsonArray getDomainsUserCanAuthor(String username) {
		JsonArray result = new JsonArray();
		JsonObject json = getWhereLike(ROLES.AUTHOR.keyname + "%" + username);
		if (json.get("valueCount").getAsInt() > 0) {
			result = json.get("values").getAsJsonArray();
		}
		return result;
	}
	


	public JsonArray getDomainsUserCanView(String username) {
		JsonArray result =  new JsonArray();
		List<String> domains = new ArrayList<String>();
		if (username == null || username.length() == 0) {
			// ignore
		} else {
			JsonArray admins = getDomainsUserCanAdminister(username).getAsJsonArray();
			JsonArray authors = getDomainsUserCanAuthor(username).getAsJsonArray();
			JsonArray reads = getDomainsUserCanRead(username).getAsJsonArray();

			for (JsonElement e : admins) {
				String domain = e.getAsJsonObject().get("topic").getAsString();
				if (! domains.contains(domain)) {
					domains.add(domain);
				}
			}
			for (JsonElement e : authors) {
				String domain = e.getAsJsonObject().get("topic").getAsString();
				if (! domains.contains(domain)) {
					domains.add(domain);
				}
			}
			for (JsonElement e : reads) {
				JsonObject eAsObject = e.getAsJsonObject();
				String domain = "";
				if (eAsObject.has("topic")) {
					domain = e.getAsJsonObject().get("topic").getAsString();
				} else if (eAsObject.has("domain")) {
					domain = e.getAsJsonObject().get("domain").getAsString();
				}
				if (! domains.contains(domain)) {
					domains.add(domain);
				}
			}
			for (String domain : domains) {
				result.add(domain);
			}
		}
		return result;
	}

	public JsonArray getDomainsUserCanAdminister(String username) {
		JsonArray result = new JsonArray();
		JsonObject json = getWhereLike(ROLES.ADMIN.keyname + "%" + username);
		if (json.get("valueCount").getAsInt() > 0) {
			result = json.get("values").getAsJsonArray();
		}
		return result;
	}

	public JsonArray getDomainsUserCanReview(String username) {
		JsonArray result = new JsonArray();
		JsonObject json = getWhereLike(ROLES.REVIEWER.keyname + "%" + username);
		if (json.get("valueCount").getAsInt() > 0) {
			result = json.get("values").getAsJsonArray();
		}
		return result;
	}

	/**
	 * Get a JsonArray of the domains the user can read
	 * The values of the JsonArrays are domains.
	 * Each domain is stored as a JsonObject with a key and label.
	 * @param username
	 * @return
	 */
	public JsonArray getDropdownOfDomainsForWhichTheUserIsAReader(String username) {
		JsonArray result = new JsonArray();
		try {
			List<String> domains = new ArrayList<String>();
			if (isDbAdmin(username)) {
				domains = getDomains();
			} else {
				for (JsonElement value : this.getDomainsUserCanAdminister(username)) {
					String domain = value.getAsJsonObject().get("topic").getAsString();
					domains.add(domain);
				}
				for (JsonElement value : this.getDomainsUserCanAuthor(username)) {
					String domain = value.getAsJsonObject().get("topic").getAsString();
					if (! domains.contains(domain)) {
						domains.add(domain);
					}
				}
				for (JsonElement value : this.getDomainsUserCanRead(username)) {
					JsonObject valueObject = value.getAsJsonObject();
					String domain = "";
					if (valueObject.has("topic")) {
						domain = value.getAsJsonObject().get("topic").getAsString();
					} else if (valueObject.has("domain")) {
						domain = value.getAsJsonObject().get("domain").getAsString();
					}
					if (! domains.contains(domain)) {
						domains.add(domain);
					}
				}
			}
			Collections.sort(domains);
			for (String domain : domains) {
				JsonArray domainRecords = getWhereLike(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId(domain)).get("values").getAsJsonArray();
				if (domainRecords.size() > 0) {
					JsonObject domainObject = domainRecords.get(0).getAsJsonObject();
					String key = domainObject.get("key").getAsString();
					String description = domainObject.get("value").getAsJsonObject().get("description").getAsString();
					result.add(new DropdownItem(key + ": " + description, key).toJsonObject());
				}
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}

	public JsonArray getDropdownOfLiturgicalDomainsTheUserCanSearch(String username) {
		JsonArray result = new JsonArray();
		try {
			List<String> domains = new ArrayList<String>();
			if (isDbAdmin(username)) {
				domains = getDomains();
			} else {
				for (JsonElement value : this.getDomainsUserCanAdminister(username)) {
					String domain = value.getAsJsonObject().get("topic").getAsString();
					domains.add(domain);
				}
				for (JsonElement value : this.getDomainsUserCanAuthor(username)) {
					String domain = value.getAsJsonObject().get("topic").getAsString();
					if (! domains.contains(domain)) {
						domains.add(domain);
					}
				}
				for (JsonElement value : this.getDomainsUserCanRead(username)) {
					try {
						String domain = "";
						JsonObject valueObject = value.getAsJsonObject();
						if (valueObject.has("topic")) {
							domain = value.getAsJsonObject().get("topic").getAsString();
						} else if (valueObject.has("domain")) {
							domain = value.getAsJsonObject().get("domain").getAsString();
						}
						if (! domains.contains(domain)) {
							domains.add(domain);
						}
					} catch (Exception e) {
						ErrorUtils.report(logger, e);
					}
				}
			}
			Collections.sort(domains);
			for (String json : domains) {
				try {
					JsonArray domainRecords = getWhereLike(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId(json)).get("values").getAsJsonArray();
					if (domainRecords.size() > 0) {
						Domain domain = (Domain) gson.fromJson(
								domainRecords.get(0).getAsJsonObject().get("value").getAsJsonObject()
								, Domain.class
						);
							if (domain.labels.contains("Liturgical")) {
								String key = domain.getDomain();
								String description = domain.getDescription();
								result.add(new DropdownItem(key + ": " + description, key).toJsonObject());
							}
					}
				} catch (Exception e) {
					ErrorUtils.report(logger, e);
				}
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}
	/**
	 * Get a JsonArray of the domains the user can author
	 * The values of the JsonArrays are domains.
	 * Each domain is stored as a JsonObject with a key and label.
	 * @param username
	 * @return
	 */
	public JsonArray getDropdownOfDomainsForWhichTheUserIsAnAuthor(String username) {
		JsonArray result = new JsonArray();
		try {
			List<String> domains = new ArrayList<String>();
			if (isDbAdmin(username)) {
				domains = getDomains();
			} else {
				for (JsonElement value : this.getDomainsUserCanAdminister(username)) {
					String domain = value.getAsJsonObject().get("topic").getAsString();
					if (! domains.contains(domain)) {
						domains.add(domain);
					}
				}
				for (JsonElement value : this.getDomainsUserCanAuthor(username)) {
					String domain = value.getAsJsonObject().get("topic").getAsString();
					if (! domains.contains(domain)) {
						domains.add(domain);
					}
				}
			}
			for (String domain : domains) {
				JsonArray domainRecords = getWhereLike(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId(domain)).get("values").getAsJsonArray();
				if (domainRecords.size() > 0) {
					JsonObject domainObject = domainRecords.get(0).getAsJsonObject();
					String key = domainObject.get("key").getAsString();
					String description = domainObject.get("value").getAsJsonObject().get("description").getAsString();
					result.add(new DropdownItem(key + ": " + description, key).toJsonObject());
				}
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}

	public Map<String,String> getDomainDescriptionMap() {
		Map<String,String> result = new TreeMap<String,String>();
		List<String> domains = this.getDomains();
		for (String domain : domains) {
			JsonArray domainRecords = getWhereLike(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId(domain)).get("values").getAsJsonArray();
			if (domainRecords.size() > 0) {
				JsonObject domainObject = domainRecords.get(0).getAsJsonObject();
				String key = domainObject.get("key").getAsString();
				String description = domainObject.get("value").getAsJsonObject().get("description").getAsString();
				result.put(key, description);
			}
		}
		return result;
	}
	/**
	 * Get a JsonArray of the domains the user can administer
	 * The values of the JsonArrays are domains.
	 * Each domain is stored as a JsonObject with a key and label.
	 * @param username
	 * @return
	 */
	public JsonArray getDropdownOfDomainsForWhichTheUserIsAnAdmin(String username) {
		JsonArray result = new JsonArray();
		try {
			List<String> domains = new ArrayList<String>();
			if (isDbAdmin(username)) {
				domains = getDomains();
			} else {
				JsonObject json = getWhereLike(ROLES.ADMIN.keyname + "%" + username);
				if (json.get("valueCount").getAsInt() > 0) {
					for (JsonElement value : json.get("values").getAsJsonArray()) {
						String domain = value.getAsJsonObject().get("topic").getAsString();
						domains.add(domain);
					}
				}
			}
			Collections.sort(domains);
			for (String domain : domains) {
				JsonArray domainRecords = getWhereLike(SYSTEM_MISC_LIBRARY_TOPICS.DOMAINS.toId(domain)).get("values").getAsJsonArray();
				if (domainRecords.size() > 0) {
					JsonObject domainObject = domainRecords.get(0).getAsJsonObject();
					String key = domainObject.get("key").getAsString();
					String description = domainObject.get("value").getAsJsonObject().get("description").getAsString();
					result.add(new DropdownItem(key + ": " + description, key).toJsonObject());
				}
			}
		} catch (Exception e) {
			ErrorUtils.report(logger, e);
		}
		return result;
	}

	/**
	 * For this user, get three types of domain dropdowns:
	 * 
	 * 1. Domains the user can administer
	 * 2. Domains the user can author
	 * 3. Domains the user can read
	 * 
	 * The returned Json object has three keys: admin, author, reader.
	 * The value of each key is a JsonArray.
	 * The values of the JsonArrays are domains.
	 * Each domain is stored as a JsonObject with a key and label.
	 * @param username
	 * @return
	 */
	public JsonObject getDomainDropdownsForUser(String username) {
		JsonObject result = new JsonObject();
		result.add("admin", this.getDropdownOfDomainsForWhichTheUserIsAnAdmin(username));
		result.add("author", this.getDropdownOfDomainsForWhichTheUserIsAnAuthor(username));
		result.add("reader", this.getDropdownOfDomainsForWhichTheUserIsAReader(username));
		result.add("liturgicalSearch", this.getDropdownOfLiturgicalDomainsTheUserCanSearch(username));
		result.addProperty("isSuperAdmin", this.isDbAdmin(username));
		result.addProperty("isLabelEditor", this.isUiLabelsAuth(username));
		return result;
	}

	public boolean isUiLabelsAuth(String username) {
		for (String domain : USER_INTERFACE_DOMAINS.getMap().keySet()) {
			if (this.isUiLabelsAuth(domain, username)) {
				return true;
			}
		}
		return false;
	}

	public boolean isUiLabelsAuth(String library, String username) {
		return isDbAdmin(username) || hasRole(ROLES.AUTHOR,library,username);
	}

	/**
	 * Is this person an administrator for the specified library?
	 * @param library
	 * @param username
	 * @return
	 */
	public boolean isLibAdmin(String library, String username) {
		return isDbAdmin(username) || hasRole(ROLES.ADMIN,library,username);
	}
	
	/**
	 * Is this person an authorized author for the specified library?
	 * @param library
	 * @param username
	 * @return
	 */
	public boolean isLibAuthor(String library, String username) {
		return isLibAdmin(library, username) || hasRole(ROLES.AUTHOR,library,username);
	}

	/**
	 * Is this person an authorized reader for the specified library?
	 * @param library
	 * @param username
	 * @return
	 */
	public boolean isLibReader(String library, String username) {
		return isLibAuthor(library, username) || hasRole(ROLES.READER, library,username);
	}

	/**
	 * Does the specified user have this role for this library?
	 * @param role
	 * @param library
	 * @param username
	 * @return true if that is the case
	 */
	public boolean hasRole(ROLES role, String library, String username) {
		return existsUnique(role.toId(library, username));
	}
	/**
	 * Is there a single doc that matches this id?
	 * @param _id
	 * @return true if there is only one doc that matches
	 */
	public boolean existsUnique(String _id) {
		ResultJsonObjectArray json = getForId(_id);
		return json.valueCount == 1;
	}
	
	public RequestStatus addSchema(String schemaId, JsonObject json) {
		RequestStatus result = new RequestStatus();
			try {
				LTKVJsonObject record;
				record = new LTKVJsonObject(
						SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.lib
						, SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.topic
						, schemaId
						, schemaId
						, json
						);
		    	manager.insert(record.toJsonObject());		
		    	result.setCode(HTTP_RESPONSE_CODES.CREATED.code);
		    	result.setMessage(HTTP_RESPONSE_CODES.CREATED.message + ": " + schemaId);
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		return result;
	}
	
	public RequestStatus updateSchema(String schemaId, JsonObject json) {
		RequestStatus result = new RequestStatus();
		String id = new IdManager(SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.lib,SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.topic,schemaId).getId();
		if (existsUnique(id)) {
			try {
				LTKVJsonObject record;
				record = new LTKVJsonObject(
						SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.lib
						, SYSTEM_MISC_LIBRARY_TOPICS.SCHEMAS.topic
						, schemaId
						, schemaId
						, json
						);
				manager.updateWhereEqual(record.toJsonObject());		
			} catch (Exception e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
		} else {
			result.setCode(HTTP_RESPONSE_CODES.NOT_FOUND.code);
			result.setMessage(HTTP_RESPONSE_CODES.NOT_FOUND.message + ": " + id);
		}
		return result;
	}

	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	@Override
	public RequestStatus deleteForId(String id) {
		RequestStatus result = new RequestStatus();
		try {
			ResultJsonObjectArray json = getForId(id);
			if (json.getResultCount() == 0) {
				result.setCode(HTTP_RESPONSE_CODES.NOT_FOUND.code);
				result.setMessage(HTTP_RESPONSE_CODES.NOT_FOUND.message + " " + id);
			} else {
				manager.delete(json.getFirstObject());
			}
		} catch (SQLException e) {
			result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
		}
		return result;
	}

	public RequestStatus deleteForId(String requestor, String id) {
		RequestStatus result = new RequestStatus();
		IdManager idManager = new IdManager(id);
		if (this.isDbAdmin(requestor) && (! id.endsWith(requestor))) { // can't delete yourself
			try {
				if (id.startsWith("users")) {
					// we need to delete everything associated with the user
					result = this.deleteUser(requestor, idManager.getKey());
				} else {
					ResultJsonObjectArray json = getForId(id);
					if (json.getResultCount() == 0) {
						result.setCode(HTTP_RESPONSE_CODES.NOT_FOUND.code);
						result.setMessage(HTTP_RESPONSE_CODES.NOT_FOUND.message + " " + id);
					} else {
						manager.delete(json.getFirstObject());
					}
				}
			} catch (SQLException e) {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			}
		} else {
			result.setMessage("Not authorized");
		}
		return result;
	}

	/**
	 * This method creates descriptions of utilities.
	 * The descriptions show up in the admin web app
	 * and are used to execute the utilities.
	 */
	private void createUtilityDescriptions() {
		for (Utility u : UTILITIES.toUtilityList()) {
			String id = SYSTEM_MISC_LIBRARY_TOPICS.UTILITIES.toId(u.getName());
			if (id.endsWith("GeneratePdfFiles")) {
				this.deleteForId(id);
			}
			if (! this.existsUnique(id)) {
				try {
					RequestStatus status = this.addUtility("wsadmin", u.toJsonString());
					if (status.getCode() != 201) {
						throw new Exception("Error creating record describing utility " + u.getName());
					}
				} catch (Exception e) {
					ErrorUtils.report(logger, e);
				}
			}
		}
	}
}
