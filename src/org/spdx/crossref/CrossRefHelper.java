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

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.CrossRef;
import org.spdx.rdfparser.license.SpdxListedLicense;

/**
 * Helper class that provides details for each url in the array it receives
 * @author Smith Tanjong
 *
 */
public class CrossRefHelper implements Callable<CrossRef[]> {
	static final Logger logger = LoggerFactory.getLogger(CrossRefHelper.class.getName());
	
	SpdxListedLicense license;

	/**
	 * @param crossRefUrls an array of url in string format
	 */
    public CrossRefHelper(SpdxListedLicense license) {
        this.license = license;
    }

    /**
	 * @param crossRefUrls the array of urls
	 * @return urlDetails the Array of CrossRefs containing the details from the SeeAlso or existing CrossRef array
	 */
	public static CrossRef[] buildUrlDetails(SpdxListedLicense license) {
		CrossRef[] crossRefs;
		try {
			crossRefs = license.getCrossRef();
		} catch (InvalidSPDXAnalysisException e1) {
			crossRefs = null;
		}
		if (Objects.isNull(crossRefs) || crossRefs.length == 0) {
			String[] crossRefUrls = license.getSeeAlso();
			if (Objects.isNull(crossRefUrls)) {
				crossRefs = new CrossRef[0];
			} else {
				crossRefs = new CrossRef[crossRefUrls.length];
				for (int i = 0; i < crossRefs.length; i++) {
					crossRefs[i] = new CrossRef(crossRefUrls[i]);
				}
			}
		}
		for (int i = 0; i < crossRefs.length; i++) {
			String url = crossRefs[i].getUrl();
			ExecutorService executorService = Executors.newFixedThreadPool(10);
			Future<Boolean> isValid = executorService.submit(new Valid(url));
			Future<Boolean> isLive = executorService.submit(new Live(url));
			Future<Boolean> isWayback = executorService.submit(new Wayback(url));
			Future<String> timestamp = executorService.submit(new Timestamp());
			Future<String> match = executorService.submit(new Match(url, license));
			
			try {
				Boolean isValidUrl = isValid.get(10, TimeUnit.SECONDS);
		    	Boolean isLiveUrl = isValidUrl ? isLive.get(10, TimeUnit.SECONDS) : false;
		    	Boolean isWaybackUrl = isValidUrl ? isWayback.get(5, TimeUnit.SECONDS) : false;
		    	String currentDate = timestamp.get(5, TimeUnit.SECONDS);
		    	String matchStatus = isValidUrl ? match.get(50, TimeUnit.SECONDS) : "N/A";
		    	crossRefs[i].setDetails(isValidUrl, isLiveUrl, isWaybackUrl, matchStatus, currentDate);
		    } catch (Exception e) {
		        // interrupts if there is any possible error
		    	isValid.cancel(true);
		    	isLive.cancel(true);
		    	isWayback.cancel(true);
		    	timestamp.cancel(true);
		    	logger.error("Interrupted.",e.getMessage());
		    	crossRefs[i].setUrl(url);
		    	crossRefs[i].setDetails(Valid.urlValidator(url), false, Wayback.isWayBackUrl(url), "--", Timestamp.getTimestamp());
		    } finally {
		    	executorService.shutdown();
		    }
		    try {
				executorService.awaitTermination(1000, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("Interrupted while waiting for termination",e.getMessage());
			}
		}
		return crossRefs;
	}
	
	@Override
	public CrossRef[] call() throws Exception {
		return buildUrlDetails(license);
	}
}
