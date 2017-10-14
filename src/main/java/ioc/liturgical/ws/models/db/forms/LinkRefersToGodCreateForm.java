package ioc.liturgical.ws.models.db.forms;

import com.google.gson.annotations.Expose;

import ioc.liturgical.ws.annotations.UiWidget;
import ioc.liturgical.ws.constants.Constants;
import ioc.liturgical.ws.constants.ID_PART_TYPES;
import ioc.liturgical.ws.constants.RELATIONSHIP_TYPES;
import ioc.liturgical.ws.constants.db.external.TOPICS;
import ioc.liturgical.ws.models.db.supers.LTKLinkCreateForm;

import com.github.reinert.jjschema.Attributes;

/**
 * This class provides a POJO for use in web forms to create or update a reference
 * @author mac002
 *
 */
@Attributes(title = "Reference to God", description = "This is a doc that records information about a reference made in a text to God.")
public class LinkRefersToGodCreateForm extends LTKLinkCreateForm {
	
	private static double serialVersion = 1.1;
	private static String schema = LinkRefersToGodCreateForm.class.getSimpleName();
	private static RELATIONSHIP_TYPES type = RELATIONSHIP_TYPES.REFERS_TO_GOD;
	private static TOPICS ontoTopic = TOPICS.GOD;
	
	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Word or phrase that makes the reference")
	@Expose public String referredByPhrase = "";

	@UiWidget(Constants.UI_WIDGET_TEXTAREA)
	@Attributes(required = false, description = "Comments on the reference")
	@Expose public String comments = "";

	public LinkRefersToGodCreateForm(
			String library
			, String topic
			, String key
			) {
		super(
				library
				, topic
				, key
				, LinkRefersToGodCreateForm.schema
				,  LinkRefersToGodCreateForm.serialVersion
				, LinkRefersToGodCreateForm.type
				, LinkRefersToGodCreateForm.ontoTopic
				);
		this.partTypeOfTopic = ID_PART_TYPES.ID_OF_SELECTED_LITURGICAL_TEXT;
		this.partTypeOfKey = ID_PART_TYPES.ID_OF_SELECTED_ONTOLOGY_INSTANCE;
	}
	
	public String getReferredByPhrase() {
		return referredByPhrase;
	}

	public void setReferredByPhrase(String referredByPhrase) {
		this.referredByPhrase = referredByPhrase;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}