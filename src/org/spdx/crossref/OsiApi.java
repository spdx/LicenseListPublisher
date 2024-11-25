/**
 * SpdxLicenseIdentifier: Apache-2.0
 * 
 * Copyright (c) 2022 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.spdx.crossref;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.license.CrossRef;
import org.spdx.library.model.v2.license.SpdxListedLicense;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Singleton class that manages the OSI API to fetch license information
 * 
 * @author Gary O'Neall
 *
 */
public class OsiApi {
	static final Logger logger = LoggerFactory.getLogger(OsiApi.class.getName());
	public static final String OSI_PREFIX = "https://opensource.org/licenses";
	private static final String API_BASE_URL = "https://api.opensource.org";
	private static final String ALL_LICENSES_URL = API_BASE_URL + "/licenses/";
	private static final int READ_TIMEOUT = 5000;
	static final List<String> WHITE_LIST = Collections.unmodifiableList(Arrays.asList(
			"osi.org")); // currently, we're not allowing any redirects to sites other than OSI
	
	private static class InstanceHolder {
		private static final OsiApi INSTANCE = new OsiApi();
	}
	
	public static OsiApi getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	public static boolean isOsiUrl(String url) {
		return Objects.nonNull(url) && url.startsWith(OSI_PREFIX);
	}
	
	private boolean apiAvailable = false;
	private Map<String, List<String>> urlToSpdxIds = new HashMap<>();
	
	private OsiApi() {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(getUrlInputStream(new URL(ALL_LICENSES_URL))))) {
			StringBuilder osiLicensesStr = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
            	osiLicensesStr.append(line);
            }
            Gson gson = new Gson();
            Type osiLicenseListType = new TypeToken<ArrayList<OsiLicense>>() {}.getType();  
            List<OsiLicense> osiLicenses = gson.fromJson(osiLicensesStr.toString(), osiLicenseListType);
            for (OsiLicense osiLicense:osiLicenses) {
            	if (Objects.nonNull(osiLicense.getLinks()) &&
            			Objects.nonNull(osiLicense.getId()) && 
            		 Objects.nonNull(osiLicense.getIdentifiers())) {
            		List<String> spdxIds = new ArrayList<>();
                	for (OsiLicense.IdentifierType identifier:osiLicense.getIdentifiers()) {
                		if ("SPDX".equals(identifier.getScheme()) && 
                				Objects.nonNull(identifier.getIdentifier())) {
                			spdxIds.add(identifier.getIdentifier());
                		}
                	}
                	if (!spdxIds.isEmpty()) {
                		for (OsiLicense.Link link:osiLicense.getLinks()) {
                			if (Objects.nonNull(link.getUrl())) {
                				urlToSpdxIds.put(link.getUrl(), spdxIds);
                			}
                		}
                	}
                	
            	}
            }
			apiAvailable = true;
		} catch (MalformedURLException e) {
			logger.error("Malformed URL exception getting OSI licenses");
			apiAvailable = false;
		} catch (IOException e) {
			logger.error("I/O exception getting OSI licenses");
			apiAvailable = false;
		}
	}
	
	/**
	 * @param url url for fetching the OSI API JSON
	 * @return Input stream with the API JSON as the payload
	 */
	private InputStream getUrlInputStream(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setReadTimeout(READ_TIMEOUT);
		int status = connection.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK && 
			(status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)) {
				// redirect
			String redirectUrlStr = connection.getHeaderField("Location");
			if (Objects.isNull(redirectUrlStr) || redirectUrlStr.isEmpty()) {
				throw new IOException("Empty redirect URL response");
			}
			URL redirectUrl;
			try {
				redirectUrl = new URL(redirectUrlStr);
			} catch(Exception ex) {
				throw new IOException("Invalid redirect URL");
			}
			if (!redirectUrl.getProtocol().toLowerCase().startsWith("http")) {
				throw new IOException("Invalid redirect protocol");
			}
			if (!WHITE_LIST.contains(redirectUrl.getHost())) {
				throw new IOException("Invalid redirect host - not on the allowed 'white list'");
			}
			connection = (HttpURLConnection)redirectUrl.openConnection();
		}
		return connection.getInputStream();
	}
	
	/**
	 * @return true if the API is available for queries
	 */
	public boolean isApiAvailable() {
		return this.apiAvailable;
	}

	/**
	 * Sets the cross reference details for the cross ref givein the URL and license
	 * @param url URL reference to the OSI website
	 * @param license SPDX license containing the crossRef
	 * @param crossRef crossRef who's details are to be filled in
	 */
	public void setCrossRefDetails(String url, SpdxListedLicense license,
			CrossRef crossRef) throws InvalidSPDXAnalysisException {
		Boolean isValidUrl = Valid.urlValidator(url);
		List<String> spdxIds = urlToSpdxIds.get(url);
    	Boolean isLiveUrl = Objects.nonNull(spdxIds) && spdxIds.contains(license.getId());
    	Boolean isWaybackUrl = false;
    	String currentDate = Timestamp.getTimestamp();
    	String matchStatus = "N/A";
    	crossRef.setDetails(isValidUrl, isLiveUrl, isWaybackUrl, matchStatus, currentDate);
	}

}
