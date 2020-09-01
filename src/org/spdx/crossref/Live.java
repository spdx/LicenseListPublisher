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

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determines whether the url is live or not
 * @author Smith Tanjong
 *
 */
public class Live implements Callable<Boolean>  {
	static final Logger logger = LoggerFactory.getLogger(Live.class.getName());
	String url;

	/**
	 * @param url the url in string form
	 */
    public Live(String url) {
        this.url = url;
    }
	
    /**
	 * @param url the url in string form
	 * @return true/false if the url is live or not
	 */
	public static boolean urlLinkExists(String URLName){
	    try {
	      HttpURLConnection.setFollowRedirects(false);
	      HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
	      con.setRequestMethod("HEAD");
	      con.setConnectTimeout(8500);
	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK || con.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED);
	    }
	    catch (Exception e) {
	    	logger.warn("Failed checking live status.",e.getMessage());
	        return false;
	    }
	  }
	
	@Override
	public Boolean call() throws Exception {
		return urlLinkExists(url);
	}

}
