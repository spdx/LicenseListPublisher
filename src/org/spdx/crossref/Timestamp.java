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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

import org.spdx.library.model.v2.SpdxConstantsCompatV2;


/**
 * Gets the current timestamp, to be added to the url details
 * @author Smith Tanjong
 *
 */
public class Timestamp implements Callable<String> {
    
	/**
	 * @return timestamp the current timestamp in UTC
	 */
    public static String getTimestamp(){
		// Get current timestamp
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SpdxConstantsCompatV2.SPDX_DATE_FORMAT);
		String timeStamp = ZonedDateTime.now( ZoneOffset.UTC ).format( formatter );
		return timeStamp.toString();
	}

	@Override
	public String call() throws Exception {
		return getTimestamp();
	}

}
