package net.ages.alwb.loaders;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.joda.time.Instant;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.ocmc.ioc.liturgical.schemas.constants.HTTP_RESPONSE_CODES;
import org.ocmc.ioc.liturgical.schemas.models.ModelHelpers;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.ontology.TextLiturgical;
import org.ocmc.ioc.liturgical.schemas.models.supers.LTKDb;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.RequestStatus;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ioc.liturgical.ws.managers.exceptions.DbException;
import net.ages.alwb.gateway.library.ares.LibraryFileProxy;
import net.ages.alwb.gateway.library.ares.LibraryLine;
import net.ages.alwb.gateway.library.ares.LibraryProxyManager;
import net.ages.alwb.gateway.library.ares.LibraryUtils;

/**
 * Used to load a Neo4j Database from Ares files.
 * 
 * If the boolean useResolvedValues is set to true:
 * 
 * 1. If a value is a key, it will use the key to look up the value and use it instead.
 * 2. If the domain of the lefthand key is different than the domain of the
 *     righthand key (the key as value), it will ignore it.  The reason is that
 *     we don't want to mix domains.
 *     
 *   If the boolean useResolvedValues is set to false,
 *   the node will not have a value property.  Instead,
 *   it will have a relationship of type VALUE_FROM that points to the
 *   value to use for that node.
 *   
 *   TODO: study these and consider batching the transactions:
 *   https://github.com/neo4j/neo4j/issues/7228
 *   https://gist.github.com/nandosola/ebe2ced123e05a79e238edd6ec81fee5
 * 
 * @author mac002
 *
 */
public class AresToNeo4j {
	private static final Logger logger = LoggerFactory
			.getLogger(AresToNeo4j.class);

	private static List<String> constraints = new ArrayList<String>();

	/**
	 * Program to load a neo4j database from ares files.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String user =        System.getenv("UID");
		String pwd =      System.getenv("PWD");
		String url =         System.getenv("URL");

		boolean updateDatabaseNodes = true; 
		boolean updateDatabaseRelationships = true; 
		boolean useResolvedValues = false; // if true, will be used as read-only database
		// and, if true, there won't be any relationships between nodes
		
		boolean includeComment = true;
		boolean inspectLine = false; // if true, you can set a breakpoint for
										// when it matches idOfLineToInspect
		String idOfLineToInspect = "en_uk_lash~me.m01.d01~meHO.note1";
		String idSeparator = "~";
		Pattern punctPattern = Pattern.compile("[˙·,.;!?(){}\\[\\]<>%]"); // punctuation 
		
		// Load the ares
		LibraryProxyManager libProxyManager;
		String alwbPath = "/Users/mac002/canBeRemoved/gitlab/db2ares";
//		String alwbPath = "/Users/mac002/Git/alwb-repositories/ages";
//		String alwbPath = "/Users/mac002/Git/alwb-repositories/kenya";
		List<String> domainsToProcess = new ArrayList<String>();
		/**
		 * Add each domain that you want to process
		 */
//		domainsToProcess.add("en_US_andronache");
//		domainsToProcess.add("en_US_barrett");
//		domainsToProcess.add("en_US_boyer");
//		domainsToProcess.add("en_US_constantinides");
//		domainsToProcess.add("en_US_dedes");
//		domainsToProcess.add("en_US_goa");
//		domainsToProcess.add("en_US_holycross");
//		domainsToProcess.add("en_UK_lash");
//		domainsToProcess.add("en_US_oca");
//		domainsToProcess.add("en_US_public");
//		domainsToProcess.add("en_US_repass");
//		domainsToProcess.add("en_US_unknown");
		domainsToProcess.add("gr_GR_cog");
		
		// ages scripture
//		domainsToProcess.add("en_UK_kjv");
//		domainsToProcess.add("en_US_eob");
//		domainsToProcess.add("en_US_kjv");
//		domainsToProcess.add("en_US_net");
//		domainsToProcess.add("en_US_nkjv");
//		domainsToProcess.add("en_US_rsv");
//		domainsToProcess.add("en_US_saas");

		// Kenya
//		domainsToProcess.add("kik_KE_oak");
//		domainsToProcess.add("swh_KE_oak");
		
		// added by Meg
//		domainsToProcess.add("fra_FR_oaf");
//		domainsToProcess.add("spa_GT_odg");
		/**
		 * Prepare a list to hold the created Queries
		 */
		List<TextLiturgical> textsToCreate = new ArrayList<TextLiturgical>();

		/**
		 * Prepare a List to hold queries we will run to create relationships
		 * between nodes where one node's value points to another node.
		 */
		List<String> redirectQueries = new ArrayList<String>();

		/**
		 * Now read in all the ares files...
		 */
		System.out.println("Loading Ares files...");
		libProxyManager = new LibraryProxyManager(alwbPath);
		libProxyManager.loadAllLibraryFiles(domainsToProcess);

		/**
		 * Now create a node for each line of each ares file...
		 * 
		 */
		System.out.println("Preparing queries to create nodes and relationships...");
		// We are going to create a node for every line that has a value or is a
		// redirect.
		// We are also going to batch up queries to create VALUE_FROM
		// relationships from
		// nodes whose value is a redirect and the node it redirects to.
		// If the node that is pointed to does not exist, it will not be
		// created.
		int count = 0;
		try (Driver driver = GraphDatabase.driver("bolt://" + url, AuthTokens.basic(user, pwd))) {
			try (Session session = driver.session()) {
				for (LibraryFileProxy fileProxy : libProxyManager.getLoadedFiles().values()) {
					String fileDomain = fileProxy.getDomain().toLowerCase();
					String fileTopic = fileProxy.getTopic();

					for (LibraryLine line : fileProxy.getValues()) {
						boolean addToDb = true;
						if (line.isSimpleKeyValue || line.isRedirect()) {
							if (useResolvedValues) { // determine whether we will use this with a normalized database
								if (line.getValue() == null || line.getValue().trim().length() < 1) {
									addToDb = false;
								}
							}
							TextLiturgical theNode = new TextLiturgical(
									line.getDomain().toLowerCase()
									, line.getTopic()
									, line.getKey()
									);
							theNode.setSeq(
									fileDomain 
									+ idSeparator 
									+ fileTopic 
									+ idSeparator 
									+ line.getLineNbr()
									);
							theNode.setCreatedBy("wsadmin");
							theNode.setModifiedBy("wsadmin");
							theNode.setCreatedWhen(Instant.now().toString());
							theNode.setModifiedWhen(theNode.getCreatedWhen());
							if (line.isSimpleKeyValue()) {
								String value = Normalizer.normalize(LibraryUtils.escapeQuotes(line.getValue()),Normalizer.Form.NFC);
								theNode.setValue(value);
								if (includeComment) {
									if (line.hasCommentAfterValue) {
										String comment = line.getComment();
										if (comment != null && comment.length() > 0) {
											theNode.setComment(LibraryUtils.escapeQuotes(comment));
										}
									}
								}
							} else if (line.isRedirect()) {
								// System.out.print(line.getValue());
								String comment = line.getComment();
								if (comment != null && comment.length() > 0) {
									theNode.setComment(LibraryUtils.escapeQuotes(comment));
								}
								String redirectTopic = "";
								String redirectDomain = "";
								String redirectId = line.getValue();
								String[] parts = redirectId.split("_");
								LibraryLine redirectLine = null;
								if (parts.length > 3) {
									redirectTopic = parts[0];
									String redirectRealm = parts[3].substring(0, parts[3].indexOf("."));
									String redirectKey = parts[3].substring(parts[3].indexOf(".") + 1,
											parts[3].length());
									redirectDomain = parts[1] + "_" + parts[2] + "_" + redirectRealm;
									redirectLine = libProxyManager
											.getLine(redirectTopic + "_" + redirectDomain, redirectKey);
									if (redirectLine == null) {
										// this value points to a key that does
										// not exist.
										// TODO decide how to handle. For now,
										// we go ahead and create the node,
										// but it won't point to an existing
										// node.
										// You can find these bad nodes using:
										// match (l:LitText) where not
										// exists(l.value) and not
										// (l)-[:VALUE_FROM]->() return l.id,
										// l.value
										redirectLine = null;
										if (useResolvedValues) {
											addToDb = false;
										}
									}
									redirectDomain = redirectDomain.toLowerCase();
									redirectId = redirectDomain + "~" + redirectTopic + "~" + redirectKey;
									if (parts.length == 5) {
										redirectId = redirectId + "_" + parts[4];
									}
								} else {
									System.out.println("Bad redirect: " + theNode.getId() + ": " + redirectId);
								}

								String nodeRedirect = getRedirectCypherTo(
										theNode.getId()
										, theNode.getOntologyTopic().label
										, redirectId
										);
								if (useResolvedValues) {
									if (redirectLine == null 
											|| redirectLine.getValue() == null 
											|| redirectLine.getValue().length() < 1
											|| (! redirectLine.getDomain().toLowerCase().startsWith(line.getDomain().toLowerCase()))
											) {
										addToDb = false;
									} else {
										addToDb = true;
										String value = Normalizer.normalize(LibraryUtils.escapeQuotes(redirectLine.getValue()),Normalizer.Form.NFC);
										theNode.setValue(value);
									}
								} else {
									redirectQueries.add(nodeRedirect);
								}
							}
							if (addToDb) {
								textsToCreate.add(theNode);
							}
						}
					}
				}

				System.out.println(textsToCreate.size() + " create queries prepared...");
				if (updateDatabaseNodes) {
					System.out.println("Merging " + textsToCreate.size() + " nodes...");
					for (TextLiturgical text : textsToCreate) {
						try {
							RequestStatus result = insert(session, text);
							count++;
						} catch (Exception e) {
							System.out.println(e.getStackTrace());
						}
					}
				}

				/**
				 * Now create the relationships between nodes where a node's
				 * value is redirected to another node.
				 */
				int relCount = 0;
				if (updateDatabaseRelationships) {
					System.out
							.println("Merging VALUE_FROM relationships for " + redirectQueries.size() + " nodes...");
					for (String query : redirectQueries) {
						try {
							StatementResult result = session.run(query);
							relCount++;
						} catch (Exception e) {
							System.out.println(e.getStackTrace());
						}
					}
				}

				System.out.println("Done. Created " + count + " nodes and " + relCount + " relationships...");
			} // try session
		} // try driver
	}
	
	/**
	 * 
	 * @param redirectId the ID to redirect to
	 * @return a Cypher query to create a POINTS_TO relationship from this node to the node of the redirectId
	 */
	public static String getRedirectCypherTo(
			String id
			, String label
			, String redirectId
			) {
		return "MATCH (d:" + label + " {id:'" + id + "'}), (t:"
				+ label + " {id:'" + redirectId + "'}) CREATE (d)-[:VALUE_FROM]->(t)";
	}
	
	public static RequestStatus insert(Session session, LTKDb doc) throws DbException {
		RequestStatus result = new RequestStatus();
		int count = 0;
		setIdConstraints(session, doc.fetchOntologyLabelsList());
		String query = "create (n:" + doc.fetchOntologyLabels() + ") set n = {props} return n";
		try {
			Map<String,Object> props = ModelHelpers.getAsPropertiesMap(doc);
				StatementResult neoResult = session.run(query, props);
				count = neoResult.consume().counters().nodesCreated();
				if (count > 0) {
			    	result.setCode(HTTP_RESPONSE_CODES.CREATED.code);
			    	result.setMessage(HTTP_RESPONSE_CODES.CREATED.message + ": created " + doc.getId());
				} else {
			    	result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
			    	result.setMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message + "  " + doc.getId());
				}
		} catch (Exception e){
			if (e.getMessage().contains("already exists")) {
				result.setCode(HTTP_RESPONSE_CODES.CONFLICT.code);
				result.setDeveloperMessage(HTTP_RESPONSE_CODES.CONFLICT.message);
			} else {
				result.setCode(HTTP_RESPONSE_CODES.BAD_REQUEST.code);
				result.setDeveloperMessage(HTTP_RESPONSE_CODES.BAD_REQUEST.message);
			}
			result.setUserMessage(e.getMessage());
		}
		return result;
	}

	public static void setIdConstraints(Session session, List<String> labels) {
		try {
			for (String label : labels) {
				if (! AresToNeo4j.constraints.contains(label)) {
					setIdConstraint(session, label); 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * The purpose of this method is to ensure that any node with 
	 * an id property has a unique constraint so that duplicate IDs 
	 * are not allowed.
	 * @param label
	 * @return
	 */
	private static StatementResult setIdConstraint(Session session, String label) {
		StatementResult neoResult = null;
			String query = "create constraint on (p:" + label + ") assert p.id is unique"; 
			try  {
				neoResult = session.run(query);
				AresToNeo4j.constraints.add(label);
			} catch (Exception e) {
				ErrorUtils.report(logger, e);
			}
		return neoResult;
	}

}

