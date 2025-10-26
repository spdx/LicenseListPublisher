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

import java.util.List;

/**
 * OSI License as returned from the OSI API
 * 
 * @author gary
 *
 */
public class OsiLicense {

	public static class LinkType {
		String href;

		public String getHref() {
			return href;
		}
	}

	public static class Links {
		LinkType self;
		LinkType html;
		LinkType collection;

		public LinkType getSelf() {
			return self;
		}

		public LinkType getHtml() {
			return html;
		}

		public LinkType getCollection() {
			return collection;
		}
	}
	
	String id;
	Links _links;
	String name;
	String spdx_id;
	List<String> keywords;
	String version;
	String submission_date;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @return the identifiers
	 */
	public Links getLinks() {
		return _links;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the keywords
	 */
	public List<String> getKeywords() {
		return keywords;
	}
	public String getSpdx_id() {
		return spdx_id;
	}
	public String getVersion() {
		return version;
	}
	public String getSubmission_date() {
		return submission_date;
	}
}
