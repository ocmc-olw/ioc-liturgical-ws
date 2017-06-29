package ioc.liturgical.ws.constants;

public enum ENDPOINT_TYPES {
	DROPDOWN
	, FORM
	, HTML
	, NLP
	, NODE
	, ONTOLOGY
	, RELATIONSHIP
	, TABLE // these are json objects whose values are arrays of Json objects.  For React js tables
	, VIEW // a list of keys and sometimes the values for a library~topic or a template
}
