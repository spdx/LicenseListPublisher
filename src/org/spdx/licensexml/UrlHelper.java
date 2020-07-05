/**
 * 
 */
package org.spdx.licensexml;

import java.net.HttpURLConnection;
import java.net.URL;
<<<<<<< HEAD
import java.time.Instant;

import org.apache.commons.validator.UrlValidator;
import org.spdx.rdfparser.SpdxRdfConstants;
=======
import org.apache.commons.validator.UrlValidator;
>>>>>>> master

/**
 * @author Smith
 *
 */
public class UrlHelper {
	
	public static boolean urlLinkExists(String URLName){
	    try {
	      HttpURLConnection.setFollowRedirects(false);
	      HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
	      con.setRequestMethod("HEAD");
	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
	    }
	    catch (Exception e) {
	       return false;
	    }
	  }

	public static boolean urlValidator(String url){
		// Get an UrlValidator using default schemes
		UrlValidator defaultValidator = new UrlValidator();
		return defaultValidator.isValid(url);
	}
	
	public static String[] buildUrlDetails(String[] crossRefUrls) {
		String[] mk = new String[crossRefUrls.length];
		for(int i = 0; i < crossRefUrls.length; i++) {
			String url = crossRefUrls[i];
			boolean isValidUrl = urlValidator(url);
			boolean isDeadUrl = !urlLinkExists(url);
			Instant instant = Instant.now();
			String currentDate = instant.toString();
			String mk1 = String.format("{%s: %b,%s: %b,%s: %b,%s: %s,%s: %b, %s: %s}",
					SpdxRdfConstants.RDFS_PROP_SEE_ALSO_DETAILS_IS_VALID, isValidUrl,
					SpdxRdfConstants.RDFS_PROP_SEE_ALSO_DETAILS_WAYBACK_LINK, "true",
					SpdxRdfConstants.RDFS_PROP_SEE_ALSO_DETAILS_MATCH, "true",
					SpdxRdfConstants.RDFS_PROP_SEE_ALSO_DETAILS_URL, url,
					SpdxRdfConstants.RDFS_PROP_SEE_ALSO_DETAILS_IS_DEAD, isDeadUrl,
					SpdxRdfConstants.RDFS_PROP_SEE_ALSO_DETAILS_TIMESTAMP, currentDate);
			mk[i] = mk1;
		}
		return mk;
	}

}
