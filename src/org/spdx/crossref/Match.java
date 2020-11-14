/**
 * Copyright (c) 2014 Source Auditor Inc.
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.compare.LicenseCompareHelper;
import org.spdx.compare.SpdxCompareException;
import org.spdx.rdfparser.license.SpdxListedLicense;

public class Match implements Callable<String> {
	static final Logger logger = LoggerFactory.getLogger(Match.class.getName());
	String url;
	SpdxListedLicense license;

	/**
	 * @param url the url in string form
	 * @param license the license
	 */
    public Match(String url, SpdxListedLicense license) {
    	this.url = url;
    	this.license = license;
    }
    
	/**
	 * @return match; the match status
	 */
    public static String checkMatch(String url, SpdxListedLicense license){
		// Get match status
    	String match = "false";
    	try {
			Document doc = Jsoup.connect(url).get();
			String bodyText = doc.body().text();
			Integer startIndex = -1;
			Integer endIndex = -1;			
			List<String> nonOptionalText = null;
			try {
				nonOptionalText = LicenseCompareHelper.getNonOptionalLicenseText(license.getStandardLicenseTemplate(), true);
			} catch (SpdxCompareException e) {
				logger.warn("Error getting optional text for license ID "+license.getLicenseId(),e);
				return "false";
			}
			Pattern licenseMatchPattern = LicenseCompareHelper.nonOptionalTextToStartPattern(nonOptionalText, UrlConstants.CROSS_REF_NUM_WORDS_MATCH);
			String compareLicenseText = LicenseCompareHelper.normalizeText(bodyText);
			Matcher matcher = licenseMatchPattern.matcher(compareLicenseText);
			if(matcher.find()) {
				startIndex = matcher.start();
				endIndex = matcher.end();
				String completeText = compareLicenseText.substring(startIndex, endIndex);
				try {
					Boolean matchBool = LicenseCompareHelper.isTextStandardLicense(license, completeText).isDifferenceFound();
					if(!matchBool) {
						match = new Boolean(!matchBool).toString();
					}
				} catch (SpdxCompareException e) {
					logger.warn("Compare exception for license ID "+license.getLicenseId(),e);
					match = "false";
				}
			}
			
		} catch (IOException e) {
			logger.warn("IO exception comparing license text for license ID "+license.getLicenseId()+" and URL "+url);
			match = "false";
		}
    	return match;
	}

	@Override
	public String call() throws Exception {
		return checkMatch(url, license);
	}

}
