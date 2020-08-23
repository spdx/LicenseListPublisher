/**
 * 
 */
package org.spdx.crossref;

import org.spdx.rdfparser.SpdxRdfConstants;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Smith Tanjong
 *
 */
public class CrossRef {
	String match;
	String url;
	Boolean isValid;
	Boolean isLive;
	String timestamp;
	Boolean isWayBackLink;
	
	public CrossRef(String url, Boolean isValid, Boolean isLive, Boolean isWayBackLink, String match, String timestamp) {
		this.url = url;
		this.isValid = isValid;
		this.isLive = isLive;
		this.isWayBackLink = isWayBackLink;
		this.match = match;
		this.timestamp = timestamp;
	}
	
	public CrossRef() {
		this.url = null;
		this.isValid = null;
		this.isLive = null;
		this.isWayBackLink = null;
		this.match = null;
		this.timestamp = null;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setDetails(Boolean isValid, Boolean isLive, Boolean isWayBackLink, String match, String timestamp) {
		this.isValid = isValid;
		this.isLive = isLive;
		this.isWayBackLink = isWayBackLink;
		this.match = match;
		this.timestamp = timestamp;
	}
	
	public String toString(){
		String crossRefDetails = String.format("{%s: %s,%s: %b,%s: %b,%s: %b,%s: %s,%s: %s}",
				SpdxRdfConstants.PROP_CROSS_REF_URL, url,
				SpdxRdfConstants.PROP_CROSS_REF_IS_VALID, isValid,
				SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE, isLive,
				SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK, isWayBackLink,
				SpdxRdfConstants.PROP_CROSS_REF_MATCH, match,
				SpdxRdfConstants.PROP_CROSS_REF_TIMESTAMP, timestamp);
		return crossRefDetails;
	}
	
	public JsonObject toJsonObject() {
		Gson g = new Gson();		
		JsonElement el = g.toJsonTree(this);
		return el.getAsJsonObject();
	}
	
}
