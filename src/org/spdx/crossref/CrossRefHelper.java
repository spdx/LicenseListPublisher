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

import java.util.Collection;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.license.CrossRef;
import org.spdx.library.model.license.SpdxListedLicense;

/**
 * Helper class that provides details for each url in the array it receives
 * @author Smith Tanjong
 *
 */
public class CrossRefHelper implements Callable<Collection<CrossRef>> {
	static final Logger logger = LoggerFactory.getLogger(CrossRefHelper.class.getName());
	
	SpdxListedLicense license;

	/**
	 * @param license license
	 */
    public CrossRefHelper(SpdxListedLicense license) {
        this.license = license;
    }

    /**
	 * @param license license
	 * @return urlDetails the Array of CrossRefs containing the details from the SeeAlso or existing CrossRef array
     * @throws InvalidSPDXAnalysisException 
	 */
	public static Collection<CrossRef> buildUrlDetails(SpdxListedLicense license) throws InvalidSPDXAnalysisException {
		Collection<CrossRef> crossRefs;
		crossRefs = license.getCrossRef();
		if (crossRefs.size() == 0) {
			for (String seeAlso:license.getSeeAlso()) {
				crossRefs.add(license.createCrossRef(seeAlso).build());
			}
		}
		for (CrossRef crossRef:crossRefs) {
			String url = crossRef.getUrl().get();
			
			try {
				Boolean isValidUrl = Valid.urlValidator(url);
		    	Boolean isLiveUrl = isValidUrl ? Live.urlLinkExists(url) : false;
		    	Boolean isWaybackUrl = isValidUrl ? Wayback.isWayBackUrl(url) : false;
		    	String currentDate = Timestamp.getTimestamp();
		    	String matchStatus = isLiveUrl ? Match.checkMatch(url, license) : "N/A";
		    	crossRef.setDetails(isValidUrl, isLiveUrl, isWaybackUrl, matchStatus, currentDate);
		    } catch (Exception e) {
		    	logger.error("Unexpected exception",e.getMessage());
		    	crossRef.setUrl(url);
		    	crossRef.setDetails(Valid.urlValidator(url), false, Wayback.isWayBackUrl(url), "--", Timestamp.getTimestamp());
		    }
		}
		return crossRefs;
	}
	
	@Override
	public Collection<CrossRef> call() throws Exception {
		return buildUrlDetails(license);
	}
}
