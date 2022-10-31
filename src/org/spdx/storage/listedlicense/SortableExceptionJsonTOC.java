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
package org.spdx.storage.listedlicense;

import java.util.Collections;
import java.util.Comparator;

/**
 * @author gary
 *
 */
public class SortableExceptionJsonTOC extends ExceptionJsonTOC {

	/**
	 * 
	 */
	public SortableExceptionJsonTOC() {
		super();
	}

	/**
	 * @param version
	 * @param releaseDate
	 */
	public SortableExceptionJsonTOC(String version, String releaseDate) {
		super(version, releaseDate);
	}
	
	public void sortExceptions() {
		Collections.sort(getExceptions(), new Comparator<ExceptionJsonTOC.ExceptionJson>() {

			@Override
			public int compare(ExceptionJson o1, ExceptionJson o2) {
				return o1.getLicenseExceptionId().compareToIgnoreCase(o2.getLicenseExceptionId());
			}
			
		});
	}

}
