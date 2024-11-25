/**
 * SpdxLicenseIdentifier: Apache-2.0
 * 
 * Copyright (c) 2024 Source Auditor Inc.
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
package org.spdx.licenselistpublisher;

import org.spdx.library.model.v3_0_1.expandedlicensing.ListedLicense;

/**
 * Simple class that holds both the SPDX Spec version 2 and SPDX Spec version 3 forms of the Listed License
 * 
 * @author Gary O'Neall
 */
public class ListedLicenseContainer {
	
	private org.spdx.library.model.v2.license.SpdxListedLicense v2ListedLicense;
	private ListedLicense v3ListedLicense;

	public ListedLicenseContainer(org.spdx.library.model.v2.license.SpdxListedLicense v2ListedLicense,
			ListedLicense v3ListedLicense) {
		this.v2ListedLicense = v2ListedLicense;
		this.v3ListedLicense = v3ListedLicense;
	}

	/**
	 * @return the v2ListedLicense
	 */
	public org.spdx.library.model.v2.license.SpdxListedLicense getV2ListedLicense() {
		return v2ListedLicense;
	}

	/**
	 * @param v2ListedLicense the v2ListedLicense to set
	 */
	public void setV2ListedLicense(
			org.spdx.library.model.v2.license.SpdxListedLicense v2ListedLicense) {
		this.v2ListedLicense = v2ListedLicense;
	}

	/**
	 * @return the v3ListedLicense
	 */
	public ListedLicense getV3ListedLicense() {
		return v3ListedLicense;
	}

	/**
	 * @param v3ListedLicense the v3ListedLicense to set
	 */
	public void setV3ListedLicense(ListedLicense v3ListedLicense) {
		this.v3ListedLicense = v3ListedLicense;
	}
	
	
}
