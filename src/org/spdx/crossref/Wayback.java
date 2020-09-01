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

import org.apache.commons.validator.UrlValidator;

/**
 * Determines whether a url is from the wayback machine or not
 * @author Smith Tanjong
 *
 */
public class Wayback implements Callable<Boolean> {
	String url;

	/**
	 * @param url the url in string form
	 */
    public Wayback(String url) {
        this.url = url;
    }
    
    /**
	 * @param url the url in string form
	 * @return true/false if the url is a wayback url or not
	 */
    public static boolean isWayBackUrl(String url){
		// Check if url if from wayback machine
		for(int i = 0; i < UrlConstants.WAYBACK_URLS.length; i++) {
			if(url.contains(UrlConstants.WAYBACK_URLS[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Boolean call() throws Exception {
		return isWayBackUrl(url);
	}

}
