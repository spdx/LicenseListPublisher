/**
 *
 */
package org.spdx.licensexml;

import java.io.BufferedReader;
<<<<<<< HEAD
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
=======
>>>>>>> master
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Scanner;

import org.apache.commons.validator.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.spdx.rdfparser.SpdxRdfConstants;

/**
 * @author Smith
 *
 */
public class UrlHelper {
	static final String [] INVALID_URL_DOMAINS = {"localhost", "127.0.0.1"};
	static final String WAYBACK_URL = "web.archive.org";
	static final String LICENSE_TEXT_JSON = "resources" + File.separator + "license-text.json";

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

	public static String getUrlHtmlContent(String URLName) {
		String content = null;
		URLConnection connection = null;
		try {
		  connection =  new URL(URLName).openConnection();
		  Scanner scanner = new Scanner(connection.getInputStream());
		  scanner.useDelimiter("\\Z");
		  content = scanner.next();
		  scanner.close();
		}catch ( Exception ex ) {
		    ex.printStackTrace();
		}
        return content;
	}

//	public static String getLicenseText(String url, String CopyrightText) {
//		if(urlValidator(url) && urlLinkExists(url)) {
//			String htmlString = getUrlHtmlContent(url);
//			Document doc = Jsoup.parse(htmlString);
//	        String title = doc.title();
//	        String body = doc.body().text();
//	        String jsoupSearchText = "div:contains(" + CopyrightText + ")";
//	        doc.select(jsoupSearchText);
//	        String licenseText = doc.select(jsoupSearchText).first().parent().children().get(1).text();
//	        System.out.printf("Title: %s%n", title);
//	        System.out.printf("Body: %s", body);
//	        System.out.printf("Body: %s", jsoupSearchText);
//		}
//		return "";
//
//	}

	public static boolean urlValidator(String url){
		// Get an UrlValidator using default schemes
		boolean containsInvalidUrl = false;
		for(int i = 0; i < INVALID_URL_DOMAINS.length; i++) {
			if(url.contains(INVALID_URL_DOMAINS[i])) {
				containsInvalidUrl = url.contains(INVALID_URL_DOMAINS[i]);
			}
		}
		UrlValidator defaultValidator = new UrlValidator();
		return defaultValidator.isValid(url) && !containsInvalidUrl;
	}

	public static boolean isWayBackUrl(String url){
		// Check if url if from wayback machine
		return url.indexOf(WAYBACK_URL) !=-1? true: false;
	}

	public static String getTimestamp(){
		// Get current timestamp
		Instant instant = Instant.now();
		DateTimeFormatter formatter =
			    DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
			                     .withLocale( Locale.US )
			                     .withZone(ZoneOffset.UTC);
		String timeStamp = formatter.format( instant );
		return timeStamp.toString();
	}

	public static String[] buildUrlDetails(String[] crossRefUrls) {
		String[] mk = new String[crossRefUrls.length];
		for(int i = 0; i < crossRefUrls.length; i++) {
			String url = crossRefUrls[i];
			boolean isValidUrl = urlValidator(url);
			boolean isLiveUrl = urlLinkExists(url);
			boolean isWaybackUrl = isWayBackUrl(url);
			String currentDate = getTimestamp();
			String mk1 = String.format("{%s: %b,%s: %b,%s: %b,%s: %s,%s: %b, %s: %s}",
					SpdxRdfConstants.PROP_CROSS_REF_IS_VALID, isValidUrl,
					SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK, isWaybackUrl,
					SpdxRdfConstants.PROP_CROSS_REF_MATCH, "true",
					SpdxRdfConstants.PROP_CROSS_REF_URL, url,
					SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE, isLiveUrl,
					SpdxRdfConstants.PROP_CROSS_REF_TIMESTAMP, currentDate);
			mk[i] = mk1;
		}
		return mk;
	}
	
	public static String getLicenseText(String url) {
		InputStream input = null;
		if (input == null) {
			// try the file system
			try {
				input = new FileInputStream(LICENSE_TEXT_JSON);
			} catch (FileNotFoundException e) {
				input = null;
			}
		}
		return "";
	}

}
