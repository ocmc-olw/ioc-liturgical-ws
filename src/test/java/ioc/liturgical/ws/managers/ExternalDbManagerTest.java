package ioc.liturgical.ws.managers;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ioc.liturgical.test.framework.LinkRefersToBiblicalTextTextFactory;
import ioc.liturgical.test.framework.TestUsers;
import org.ocmc.ioc.liturgical.schemas.constants.HTTP_RESPONSE_CODES;
import org.ocmc.ioc.liturgical.schemas.constants.RELATIONSHIP_TYPES;
import org.ocmc.ioc.liturgical.schemas.constants.TEMPLATE_NODE_TYPES;
import org.ocmc.ioc.liturgical.schemas.constants.TEMPLATE_TYPES;
import org.ocmc.ioc.liturgical.schemas.constants.TOPICS;
import ioc.liturgical.ws.managers.databases.external.neo4j.ExternalDbManager;
import ioc.liturgical.ws.managers.databases.internal.InternalDbManager;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.RequestStatus;
import org.ocmc.ioc.liturgical.schemas.models.ws.response.ResultJsonObjectArray;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.nlp.WordAnalyses;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.nlp.WordAnalysis;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.notes.UserNote;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.ontology.Animal;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.ontology.Being;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.ontology.Concept;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.ontology.Event;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.templates.Template;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.templates.TemplateNode;
import org.ocmc.ioc.liturgical.schemas.models.db.docs.templates.TemplateNodeFactory;
import org.ocmc.ioc.liturgical.schemas.models.forms.ontology.AnimalCreateForm;
import org.ocmc.ioc.liturgical.schemas.models.forms.ontology.ConceptCreateForm;
import org.ocmc.ioc.liturgical.schemas.models.forms.ontology.EventCreateForm;
import org.ocmc.ioc.liturgical.schemas.models.forms.ontology.LinkRefersToBiblicalTextCreateForm;
import org.ocmc.ioc.liturgical.schemas.models.forms.ontology.UserNoteCreateForm;
import org.ocmc.ioc.liturgical.schemas.models.db.links.LinkRefersToBiblicalText;
import org.ocmc.ioc.liturgical.utils.ErrorUtils;
import org.ocmc.ioc.liturgical.utils.FileUtils;
import net.ages.alwb.utils.nlp.fetchers.PerseusMorph;
import net.ages.alwb.utils.transformers.adapters.TemplateNodeCompiler;

public class ExternalDbManagerTest {

	private static InternalDbManager internalManager;
	private static ExternalDbManager externalManager;

	private static LinkRefersToBiblicalTextTextFactory testReferences;
	private static String pwd = "";
	private static Gson gson = new Gson();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		pwd = System.getenv("pwd");
		
		internalManager = new InternalDbManager(
				"test-db" // store name
				, "json" // tablename
				, true // delete old database
				, true // truncate tables (if you don't delete the old db)
				, true // create test users
				, TestUsers.WS_ADMIN.id // the username of the wsadmin
				, pwd
				);

		externalManager = new ExternalDbManager(
				"localhost"
				, ""
				, true
				, true
				, TestUsers.WS_ADMIN.id
				, pwd
				, false // do not build domainMap
				, false // not read only
				, internalManager
				);
		
		// do a clean up in case we aborted during a previous run without deleting Test entries
		externalManager.getForQuery("match (n:" + TOPICS.ONTOLOGY_ROOT.label + ") where n.name starts with \"Test\" delete(n)", false, false);
		
		testReferences = new LinkRefersToBiblicalTextTextFactory();
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	   public void testgetTopicForParaColTextEditor() {
			ResultJsonObjectArray result = externalManager.getTopicForParaColTextEditor(
					"gr_gr_cog"
					, "actors"
					);
			assertTrue(result.getCount() > 0);
			ResultJsonObjectArray dedes = externalManager.getTopicValuesForParaColTextEditor(
					"gr_gr_cog"
					, "actors"
					, "en_us_dedes,en_uk_lash".split(",")
					) ;
			System.out.println(dedes.getValues().get(0).toString());
			assertTrue(dedes.getCount() > 0);
	    }
	
	@Test
	   public void testCompileTemplateRootNode() {
			Template template = new Template(
				"en_us_ages"
				, "se.m01.d01.ma"
				);
			TemplateNode root = template.fetchNode();
			TemplateNode rootSection = root.children.get(0);
			TemplateNode actor = new TemplateNode();
			actor.setTitle(TEMPLATE_NODE_TYPES.Actor);
			TemplateNode sid = new TemplateNode();
			sid.setTitle(TEMPLATE_NODE_TYPES.SID);
			sid.setSubtitle("gr_gr_cog~actors~Priest");
			actor.appendNode(sid);
			rootSection.appendNode(actor);
			TemplateNode dialog = new TemplateNode();
			dialog.setTitle(TEMPLATE_NODE_TYPES.Dialog);
			sid.setSubtitle("gr_gr_cog~eu.lichrysbasil~euLI.Key0109.text");
			dialog.appendNode(sid);
			rootSection.appendNode(dialog);
			List<TemplateNode> children = new ArrayList<TemplateNode>();
			children.add(rootSection);
			root.setChildren(children);
			template.setNode(root.toJsonString());
			template.setPrettyPrint(true);
			System.out.println(template.fetchNode().toJsonString());
			TemplateNodeCompiler compiler = new TemplateNodeCompiler(
					template
					, externalManager
					);
			TemplateNode compiledNodes = compiler.getCompiledNodes();
			compiledNodes.setPrettyPrint(true);
			System.out.println(compiledNodes.toJsonString());
	    }

	@Test
	   public void testGetAnalysesForText() {
			ResultJsonObjectArray result = externalManager.getWordGrammarAnalyses(
					"wsadmin"
					, "gr_gr_cog~me.m01.d06~meMA.Ode3C2H5.text"
					);
			assertTrue(result.getCount() > 0);
	    }
	
	@Test
	   public void testGetForIdStartswith() {
			assertTrue(externalManager.getForIdStartsWith("gr_gr_cog~actors").getValueCount() > 0);
	    }
	
	@Test
	   public void testGetForId() {
    		assertTrue(externalManager.getForId("gr_gr_cog~actors~ClergyAndPeople").getValueCount() == 1);
	    }
	
	@Test
	   public void testReferenceCrud() {
		
		// create
		   LinkRefersToBiblicalTextCreateForm form = testReferences.getCreateForm(0);
		   RequestStatus status = externalManager.addReference(
	    			TestUsers.WS_ADMIN.id
	    			, form.toJsonString()
	    			);
	    	assertTrue(status.getCode() == 201); // created
	    	
	    	// read
	    	ResultJsonObjectArray result = externalManager.getReferenceObjectByRefId(
	    			testReferences.getCreateForm(0).getId()
	    			);
			LinkRefersToBiblicalText ref = (LinkRefersToBiblicalText) gson.fromJson(
					result.getValues().get(0)
					, LinkRefersToBiblicalText.class
			);	
    	assertTrue(ref.getId().equals(testReferences.getCreateForm(0).getId()));
	    	
	    	// update
	    	String update = "updated Bib";
	    	ref.setBib(update);
	    	externalManager.updateReference(
	    			TestUsers.WS_ADMIN.id
	    			, ref.getId()
	    			, ref.toJsonString()
	    			);
	    	ref = externalManager.getReference(ref.getId());
	    	assertTrue(ref.getBib().equals(update));
	    	
	    	// delete
	    	status = externalManager.deleteForRelationshipId(TestUsers.WS_ADMIN.id, ref.getId());
	    	assertTrue(status.getCode() == 200);
	    }
	
	@Test
	   public void testReferenceGetForType() {
		// if this fails, make sure you have run the CreateABunchOfReferences unit test first
	    	ResultJsonObjectArray result = 
	    			externalManager.getRelationshipForType(
	    					RELATIONSHIP_TYPES.REFERS_TO_BIBLICAL_TEXT.typename
	    					);
			assertTrue(result.getResultCount() > 0 && result.getStatus().getCode() == 200);
	    }

	@Test
	   public void testUserNoteCRUD() {

		// TODO: create multiple notes and then test to read notes by domain.
		
		String user = "mcolburn";
		String domain = externalManager.getUserDomain(user);
		RequestStatus status = null;
		String firstNoteId = null;
		String secondNoteId = null;
		
		// create
		UserNoteCreateForm form = new UserNoteCreateForm(
					domain
					, "gr_gr_cog~actors~Priest"
					, externalManager.getTimestamp()
				   );
		try {
			form.setValue("create a note");
			   status = externalManager.addNote(
		    			user
		    			, form.toJsonString()
		    			);
		    	assertTrue(status.getCode() == 201); // created = 201
		    	firstNoteId = form.getId();
		    	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		UserNote ref = null;
		ResultJsonObjectArray result = null;
		
		try {
	    	// read
	    	result = externalManager.getForId(
	    			form.getId()
	    			);
	    	ref = (UserNote) gson.fromJson(
					result.getValues().get(0)
					, UserNote.class
			);	
 	       assertTrue(ref.getId().equals(form.getId()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	    	
	    	// update
		try {
	    	String update = "updated note";
	    	ref.setValue(update);
			externalManager.updateLTKDbObject(
	    			user
	    			, ref.toJsonString()
					);
	    	result = externalManager.getForId(
	    			ref.getId()
	    			);
	    	ref = (UserNote) gson.fromJson(
					result.getValues().get(0)
					, UserNote.class
			);	
	    	assertTrue(ref.getValue().equals(update));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// create a second instance
			form = new UserNoteCreateForm(
						domain
						, "gr_gr_cog~actors~Priest"
						, externalManager.getTimestamp()
					   );
			form.setValue("create note number 2");
			   status = externalManager.addNote(
		    			user
		    			, form.toJsonString()
		    			);
		    	assertTrue(status.getCode() == 201); // created 201
		    	
		    	secondNoteId = form.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
	    	// get all notes for the user
	    	result = externalManager.getUsersNotes(user);
	    	assertTrue(result.valueCount > 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
	    	// delete first note
    	status = externalManager.deleteNoteAndRelationshipsForId(user, firstNoteId);
    	assertTrue(status.getCode() == 200);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
	    	// delete second note
    	status = externalManager.deleteNoteAndRelationshipsForId(user, secondNoteId);
    	assertTrue(status.getCode() == 200);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	   public void testReferenceSearch() {
		// if this fails, make sure you have run the CreateABunchOfReferences unit test first
	    	ResultJsonObjectArray result = 
	    			externalManager.searchRelationships(
	    					"wsadmin"
	    					, RELATIONSHIP_TYPES.REFERS_TO_BIBLICAL_TEXT.typename
	    					, "en_us_pentiuc"
	    					, "syn"  // query
	    					, "syn" // property
	    					, "c" // matcher
	    					, "a,b" // tags
	    					, "or" // operator
	    					, "n" // exclude biblical texts. 
	    					);
			assertTrue(result.getStatus().getCode() == 200);
	    }

	@Test
	   public void testOntologySearch() {
		// if this fails, make sure there are Ontology entries in the database
	    	ResultJsonObjectArray result = 
	    			externalManager.searchOntology(
	    					"wsadmin"
	    					, ""
	    					, "Being"
	    					, ""  // query
	    					, "" // property
	    					, "c" // matcher
	    					, "" // tags
	    					, "" // operator
	    					);
			assertTrue(result.getStatus().getCode() == 200);
	    }

	@Test
	   public void testRelationshipPropertiesMap() {
	    	ResultJsonObjectArray result = 
	    			externalManager.getRelationshipTypePropertyMaps();
			assertTrue(result.getStatus().getCode() == 200 && result.getResultCount() > 0);
	    }
	
	@Test
	   public void testRelationshipLabelsList() {
		// if this fails, make sure you have run the CreateABunchOfReferences unit test first
	    	JsonArray result = 
	    			externalManager.getRelationshipTags(
	    					RELATIONSHIP_TYPES.REFERS_TO_BIBLICAL_TEXT.typename
	    					, TOPICS.TEXT_BIBLICAL.label
	    					);
			assertTrue(result.size() > 0);
	    }
	
	

	@Test
	   public void testgetTopicsDropdown() {
	    	ResultJsonObjectArray result = 
	    			externalManager.getTopicsDropdown() ;
			assertNotNull(result.valueCount > 0);
	    }


	@Test
	   public void testRelationshipDomainsList() {
	    	JsonObject result = 
	    			externalManager.getRelationshipLibrarysForAllTypes() ;
			assertNotNull(result);
	    }

	@Test
	   public void testRelationshipDropdown() {
	    	ResultJsonObjectArray result = 
	    			externalManager.getRelationshipSearchDropdown();
			assertTrue(result.getStatus().getCode() == 200 && result.getResultCount() > 0);
	    }

	@Test
	   public void testGetUsers() {
			ResultJsonObjectArray result = externalManager.callDbmsSecurityListUsers();
			assertNotNull(result);
	    }
	

	@Test
	   public void testGetNewForms() {
			JsonObject result = externalManager.getUserDropdowns(TestUsers.WS_ADMIN.id , "get new forms");
			assertNotNull(result);
	    }

	@Test
	   public void testGetConstraintsObject() {
			JsonObject result = externalManager.callDbConstraints();
			assertNotNull(result);
	    }
	
	@Test
	   public void testDbHasConstraint() {
			assertTrue(externalManager.dbHasConstraint(TOPICS.ROOT.label));
	    }

	@Test
	   public void testGetConstraintsList() {
			List<String> result = externalManager.getDbConstraints();
			assertTrue(result.contains("Text"));
	    }

	@Test
	   public void testDbIsReadOnly() {
			assertFalse(externalManager.dbIsReadOnly());
	    }
	
	@Test
	   public void testOntologyCheck() {
			assertTrue(externalManager.dbHasOntologyEntries());
	    }
	
	@Test
	   public void testGetOntologyEntry() {
			assertNotNull(externalManager.getDropdownInstancesForOntologyType("Human"));
	    }

	@Test
	   public void testGetTopicAsOslwFileContents() {
		    JsonObject o = externalManager.getTopicAsOslwFileContents(
		    		"gr_gr_cog"
		    		, "me.m01.d06"
		    		, 201 // start of Theophany
		    		,  201 // 651 end of Theophany
		    		);
		    JsonArray a = o.get("values").getAsJsonArray();
		    String contents = a.get(0).getAsJsonObject().get("keys").getAsString();
		    FileUtils.writeFile("/Users/mac002/temp/res.me.m01.d06.tex", contents);
		    assertTrue(contents.length() > 0);
	    }
	
	@Test
	   public void testGetTopicAsJson() {
		    ResultJsonObjectArray o = externalManager.getTopicAsJson(
		    		"gr_gr_cog"
		    		, "me.m01.d06"
		    		, 201
		    		, 651
		    		);
		    List<JsonObject> a = o.getValues();
		    assertTrue(a.size() > 0);
	    }

	@Test
	   public void testGetTopicUniqueTokenSet() {
			Set<String> result = new TreeSet<String>();
		    Set<String> tokens = externalManager.getTopicUniqueTokens(
		    		"gr_gr_cog"
		    		, "me.m01.d06"
		    		, 201 // start of theophany
		    		, 201 // 651 // end of theophany
		    		);
		    for (String token : tokens) {
		    		PerseusMorph pm = new PerseusMorph(token);
		    		WordAnalyses analyses = pm.getAnalyses();
		    		for (WordAnalysis analysis : analyses.analyses ) {
		    			System.out.println(analysis.toJsonString());
		    			String s= analysis.toExPexInterlinear(true);
		    			if (!result.contains(s)) {
			    			result.add(s);
		    			}
		    		}
		    	}
    		for (String a : result) {
    			System.out.println(a);
    		}
		    assertTrue(tokens.size() > 0);
	    }

	@Test
	   public void testGetForSeqRange() {
		    ResultJsonObjectArray o = externalManager.getForSeqRange(
		    		"gr_gr_cog"
		    		, "me.m01.d06"
		    		, 201
		    		, 651
		    		);
		    JsonArray a = o.getValuesAsJsonArray();
		    assertTrue(a.size() > 0);
	    }

	@Test
	public void testGetWordList() {
		ResultJsonObjectArray o = externalManager.getWordListWithFrequencyCounts(
				"Biblical"
				, "en_uk_webbe~ACT~C01:01"
		);
	    JsonArray a = o.getValuesAsJsonArray();
		assertTrue(a.size() > 0);
	}

	@Test 
	public void getContext() {
		ResultJsonObjectArray o = externalManager.getContext(
				"gr_gr_cog~me.m01.d06~meMA.C1Poet"
				, 10
				);
	    JsonArray a = o.getValuesAsJsonArray();
	    assertTrue(a.size() > 0);
	}
	
	@Test
	   public void testGetDropdownsForOntologyInstances() {
			assertNotNull(externalManager.getDropdownsForOntologyInstances());
	    }

	@Test
	   public void testConstraintOnNodeCreateAndDrop() {
		String label = "banana";
		// create
			RequestStatus status = externalManager.createConstraintUniqueNodeId(label);
			assertTrue(status.getCode() == HTTP_RESPONSE_CODES.OK.code);
		// create again
			status = externalManager.createConstraintUniqueNodeId(label);
			assertTrue(status.getCode() == HTTP_RESPONSE_CODES.CONFLICT.code);
		// drop
			status = externalManager.dropConstraintUniqueNodeId(label);
			assertTrue(status.getCode() == HTTP_RESPONSE_CODES.OK.code);
		// drop again
			status = externalManager.dropConstraintUniqueNodeId(label);
			assertTrue(status.getCode() == HTTP_RESPONSE_CODES.BAD_REQUEST.code);
	}
	
	@Test
	   public void testCreateOntologyEntryCrudAnimal() {
			String name = "TestDove";
			String description = "A symbol of peace.";

			// create
			AnimalCreateForm form = new AnimalCreateForm(name);
			form.setDescription(description);
			RequestStatus result = 
					externalManager.addLTKDbObject(
							"wsadmin"
							, form.toJsonString()
							);
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.CREATED.code);
			
			// read
			ResultJsonObjectArray json = externalManager.getForId(form.getId());
			Animal entry = gson.fromJson(
					json.getFirstObject()
					, Animal.class
					);
			assertNotNull(entry.getId());
			assertNotNull(entry.getLibrary());
			assertNotNull(entry.getTopic());
			assertNotNull(entry.getKey());
			
			assertTrue(entry.getId().equals(form.getId()));
			assertTrue(entry.getLibrary().equals(form.getLibrary()));
			assertTrue(entry.getTopic().equals(form.getTopic()));
			assertTrue(entry.getKey().equals(form.getKey()));

			// update
			description = "this is an update to the comments";
			entry.setDescription(description);
			result = externalManager.updateLTKDbObject(
					"wsadmin"
					, entry.toJsonString()
					);
			
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.OK.code);
			
			// read again
			json = externalManager.getForId(form.getId());
			Animal revisedEntry = gson.fromJson(
					json.getFirstObject()
					, Animal.class
					);
			assertTrue(entry.getDescription().equals(revisedEntry.getDescription()));

			// delete
			result = externalManager.deleteForId(form.getId());
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.OK.code);
	}
	@Test
	   public void testCreateOntologyEntryCrudBeing() {
			String name = "TestAngel";
			String description = "A divine messenger.";
			
			// create
			Being form = new Being(name);
			form.setDescription(description);
			RequestStatus result = 
					externalManager.addLTKDbObject(
							"wsadmin"
							, form.toJsonString()
							);
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.CREATED.code);
			
			// read
			ResultJsonObjectArray json = externalManager.getForId(form.getId());
			Being entry = gson.fromJson(
					json.getFirstObject()
					, Being.class
					);
			assertNotNull(entry.getId());
			assertNotNull(entry.getLibrary());
			assertNotNull(entry.getTopic());
			assertNotNull(entry.getKey());
			
			assertTrue(entry.getId().equals(form.getId()));
			assertTrue(entry.getLibrary().equals(form.getLibrary()));
			assertTrue(entry.getTopic().equals(form.getTopic()));
			assertTrue(entry.getKey().equals(form.getKey()));

			// update
			description = "this is an update to the comments";
			entry.setDescription(description);
			result = externalManager.updateLTKDbObject(
					"wsadmin"
					, entry.toJsonString()
					);
			
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.OK.code);
			
			// read again
			json = externalManager.getForId(form.getId());
			Being revisedEntry = gson.fromJson(
					json.getFirstObject()
					, Being.class
					);
			assertTrue(entry.getDescription().equals(revisedEntry.getDescription()));

			// delete
			result = externalManager.deleteForId(form.getId());
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.OK.code);
	}
	@Test
	   public void testCreateOntologyEntryCrudConcept() {
			String name = "TestTrinity";
			String description = "Theological assertion that the Godhead is three persons.";

			// create
			ConceptCreateForm form = new ConceptCreateForm(name);
			form.setDescription(description);
			RequestStatus result = 
					externalManager.addLTKDbObject(
							"wsadmin"
							, form.toJsonString()
							);
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.CREATED.code);
			
			// read
			ResultJsonObjectArray json = externalManager.getForId(form.getId());
			Concept entry = gson.fromJson(
					json.getFirstObject()
					, Concept.class
					);
			assertNotNull(entry.getId());
			assertNotNull(entry.getLibrary());
			assertNotNull(entry.getTopic());
			assertNotNull(entry.getKey());
			
			assertTrue(entry.getId().equals(form.getId()));
			assertTrue(entry.getLibrary().equals(form.getLibrary()));
			assertTrue(entry.getTopic().equals(form.getTopic()));
			assertTrue(entry.getKey().equals(form.getKey()));

			// update
			description = "this is an update to the comments";
			entry.setDescription(description);
			result = externalManager.updateLTKDbObject(
					"wsadmin"
					, entry.toJsonString()
					);
			
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.OK.code);
			
			// read again
			json = externalManager.getForId(form.getId());
			Concept revisedEntry = gson.fromJson(
					json.getFirstObject()
					, Concept.class
					);
			assertTrue(entry.getDescription().equals(revisedEntry.getDescription()));

			// delete
			result = externalManager.deleteForId(form.getId());
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.OK.code);
	}
	@Test
	   public void testCreateOntologyEntryCrudEvent() {
			String name = "TestBirthOfChrist";
			String description = "Commemorated Dec 25.";

			// create
			EventCreateForm form = new EventCreateForm(name);
			form.setDescription(description);
			RequestStatus result = 
					externalManager.addLTKDbObject(
							"wsadmin"
							, form.toJsonString()
							);
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.CREATED.code);
			
			// read
			ResultJsonObjectArray json = externalManager.getForId(form.getId());
			Event entry = gson.fromJson(
					json.getFirstObject()
					, Event.class
					);
			assertNotNull(entry.getId());
			assertNotNull(entry.getLibrary());
			assertNotNull(entry.getTopic());
			assertNotNull(entry.getKey());
			
			assertTrue(entry.getId().equals(form.getId()));
			assertTrue(entry.getLibrary().equals(form.getLibrary()));
			assertTrue(entry.getTopic().equals(form.getTopic()));
			assertTrue(entry.getKey().equals(form.getKey()));

			// update
			description = "this is an update to the comments";
			entry.setDescription(description);
			result = externalManager.updateLTKDbObject(
					"wsadmin"
					, entry.toJsonString()
					);
			
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.OK.code);
			
			// read again
			json = externalManager.getForId(form.getId());
			Event revisedEntry = gson.fromJson(
					json.getFirstObject()
					, Event.class
					);
			assertTrue(entry.getDescription().equals(revisedEntry.getDescription()));

			// delete
			result = externalManager.deleteForId(form.getId());
			assertTrue(result.getCode() == HTTP_RESPONSE_CODES.OK.code);
	}
	
	
}
