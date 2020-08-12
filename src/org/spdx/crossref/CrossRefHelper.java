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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.spdx.rdfparser.SpdxRdfConstants;

/**
 * Helper class that provides details for each url in the array it receives
 * @author Smith Tanjong
 *
 */
public class CrossRefHelper implements Callable<String[]> {
	
	String[] crossRefUrls;

	/**
	 * @param crossRefUrls an array of url in string format
	 */
    public CrossRefHelper(String[] crossRefUrls) {
        this.crossRefUrls = crossRefUrls;
    }

    /**
	 * @param crossRefUrls the array of urls
	 * @return urlDetails the Array of string containing the url details
	 */
	public static String[] buildUrlDetails(String[] crossRefUrls) {
		String[] urlDetails = new String[crossRefUrls.length];
		for (int i = 0; i < crossRefUrls.length; i++) {
			String url = crossRefUrls[i];
			
			ExecutorService executorService = Executors.newFixedThreadPool(20);

			Future<Boolean> isValid = executorService.submit(new Valid(url));
			Future<Boolean> isLive = executorService.submit(new Live(url));
			Future<Boolean> isWayback = executorService.submit(new Wayback(url));
			Future<String> timestamp = executorService.submit(new Timestamp());
			
			try {
				Boolean isValidUrl = isValid.get(3, TimeUnit.SECONDS);
		    	Boolean isLiveUrl = isLive.get(6, TimeUnit.SECONDS);
		    	Boolean isWaybackUrl = isWayback.get(3, TimeUnit.SECONDS);
		    	String currentDate = timestamp.get(3, TimeUnit.SECONDS);
		    	String crossRefDetails = String.format("{%s: %b,%s: %b,%s: %b,%s: %s,%s: %b, %s: %s}",
						SpdxRdfConstants.PROP_CROSS_REF_IS_VALID, isValidUrl,
						SpdxRdfConstants.PROP_CROSS_REF_WAYBACK_LINK, isWaybackUrl,
						SpdxRdfConstants.PROP_CROSS_REF_MATCH, "true",
						SpdxRdfConstants.PROP_CROSS_REF_URL, url,
						SpdxRdfConstants.PROP_CROSS_REF_IS_LIVE, isLiveUrl,
						SpdxRdfConstants.PROP_CROSS_REF_TIMESTAMP, currentDate);
		    	urlDetails[i] = crossRefDetails;
		    } catch (Exception e) {
		        // interrupts if there is any possible error
		    	isValid.cancel(true);
		    	isLive.cancel(true);
		    	isWayback.cancel(true);
		    	timestamp.cancel(true);
		    }
		    executorService.shutdown();
		    try {
				executorService.awaitTermination(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return urlDetails;
	}
	
	@Override
	public String[] call() throws Exception {
		return buildUrlDetails(crossRefUrls);
	}
}
