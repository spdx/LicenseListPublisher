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

/**
 * Constants which are useful for the check on the validity and url kind of the url
 * @author Smith Tanjong
 *
 */
public class UrlConstants {
	public static final String [] INVALID_URL_DOMAINS = {"localhost", "127.0.0.1"};
	public static final String [] WAYBACK_URLS = {"web.archive.org", "wayback.archive.org"};
	
	public static final Integer CROSS_REF_INDEX_URL = 0;
	public static final Integer CROSS_REF_INDEX_ISVALID = 1;
	public static final Integer CROSS_REF_INDEX_ISLIVE = 2;
	public static final Integer CROSS_REF_INDEX_ISWAYBACKLINK = 3;
	public static final Integer CROSS_REF_INDEX_MATCH = 4;
	public static final Integer CROSS_REF_INDEX_TIMESTAMP = 5;
	public static final Integer CROSS_REF_LICENSE_TEXT_START_CHAR_COUNT = 80;
	public static final Integer CROSS_REF_LICENSE_TEXT_END_CHAR_COUNT = 60;
}
