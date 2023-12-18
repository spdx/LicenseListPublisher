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
import java.util.concurrent.Callable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.library.model.license.SpdxListedLicense;
import org.spdx.utility.compare.LicenseCompareHelper;

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
    	try {
			Document doc = Jsoup.connect(url).get();
			String bodyText = doc.body().text();
			return String.valueOf(LicenseCompareHelper.isStandardLicenseWithinText(bodyText, license));
		} catch (IOException e) {
			logger.warn("IO exception comparing license text for license ID "+license.getLicenseId()+" and URL "+url);
			return String.valueOf(false);
		}
	}

	@Override
	public String call() throws Exception {
		return checkMatch(url, license);
	}

}
