package ioc.liturgical.ws.managers.databases.external.neo4j;

public class CypherQuery {
	private String MATCH = "";
	private String LABEL = "";
	private String WHERE = "";
	private String CONTAINS = "";
	private String STARTS_WITH = "";
	private String ENDS_WITH = "";
	private String MATCHES_PATTERN = "";
	private String RETURN = "";
	private String ORDER_BY = "";
	
	public CypherQuery(
			String MATCH
			, String LABEL
			, String WHERE
			, String CONTAINS
			, String STARTS_WITH
			, String ENDS_WITH
			, String MATCHES_PATTERN
			, String RETURN
			, String ORDER_BY
			) {
		this.MATCH = MATCH; // empty string--just used to make builder look like cypher.
		this.LABEL = LABEL;
		this.WHERE = WHERE;
		this.CONTAINS = CONTAINS;
		this.STARTS_WITH = STARTS_WITH;
		this.ENDS_WITH = ENDS_WITH;
		this.MATCHES_PATTERN = MATCHES_PATTERN;
		this.RETURN = RETURN;
		this.ORDER_BY = ORDER_BY;
	};
	

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MATCH (doc");
		if (LABEL.length() >0) {
			sb.append(LABEL);
		}
		sb.append(") ");

		if (STARTS_WITH.length() > 0) {
			sb.append("WHERE doc." + WHERE + " STARTS WITH '" + STARTS_WITH + "' ");
		} else 	if (ENDS_WITH.length() > 0) {
			sb.append("WHERE doc." + WHERE + " ENDS WITH '" + ENDS_WITH + "' ");
		} else if (CONTAINS.length() > 0) {
			sb.append("WHERE doc." + WHERE + " CONTAINS '" + CONTAINS + "' ");
		} else if (MATCHES_PATTERN.length() > 0) {
			sb.append("WHERE doc." + WHERE + " =~ '" + MATCHES_PATTERN + "' ");
		} 

		if (RETURN.contains("split")) {
			sb.append("RETURN " + RETURN);
		} else {
			sb.append("RETURN doc." + RETURN.replaceAll(" ", "").replaceAll(",", ", doc."));
		}
		sb.append(" ORDER BY " + ORDER_BY);
		return sb.toString();
	}

	public String getCONTAINS() {
		return CONTAINS;
	}

	public void setCONTAINS(String CONTAINS) {
		this.CONTAINS = CONTAINS;
	}

	public String getMATCH() {
		return MATCH;
	}

	public void setMATCH(String mATCH) {
		this.MATCH = MATCH;
	}

	public String getWHERE() {
		return WHERE;
	}

	public void setWHERE(String WHERE) {
		this.WHERE = WHERE;
	}

	public String getRETURN() {
		return RETURN;
	}

	public void setRETURN(String RETURN) {
		this.RETURN = RETURN;
	}

	public String getLABEL() {
		return LABEL;
	}

	public void setLABEL(String lABEL) {
		LABEL = lABEL;
	}

	public String getSTARTS_WITH() {
		return STARTS_WITH;
	}

	public void setSTARTS_WITH(String sTARTS_WITH) {
		STARTS_WITH = sTARTS_WITH;
	}

	public String getENDS_WITH() {
		return ENDS_WITH;
	}


	public void setENDS_WITH(String eNDS_WITH) {
		ENDS_WITH = eNDS_WITH;
	}


	public String getMATCHES_PATTERN() {
		return MATCHES_PATTERN;
	}

	public void setMATCHES_PATTERN(String mATCHES_PATTERN) {
		MATCHES_PATTERN = mATCHES_PATTERN;
	}


	public String getORDER_BY() {
		return ORDER_BY;
	}


	public void setORDER_BY(String oRDER_BY) {
		ORDER_BY = oRDER_BY;
	}


}