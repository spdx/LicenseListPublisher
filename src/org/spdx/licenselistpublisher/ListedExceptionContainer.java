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

import org.spdx.library.model.v3_0_1.expandedlicensing.ListedLicenseException;

/**
 * Simple class that holds both the SPDX Spec version 2 and SPDX Spec version 3 forms of the Listed License Exceptions
 * 
 * @author Gary O'Neall
 */
public class ListedExceptionContainer {
	
	private org.spdx.library.model.v2.license.ListedLicenseException v2Exception;
	private ListedLicenseException v3Exception;
	
	public ListedExceptionContainer(org.spdx.library.model.v2.license.ListedLicenseException v2Exception,
			ListedLicenseException v3Exception) {
		this.v2Exception = v2Exception;
		this.v3Exception = v3Exception;
	}

	/**
	 * @return the v2Exception
	 */
	public org.spdx.library.model.v2.license.ListedLicenseException getV2Exception() {
		return v2Exception;
	}

	/**
	 * @param v2Exception the v2Exception to set
	 */
	public void setV2Exception(
			org.spdx.library.model.v2.license.ListedLicenseException v2Exception) {
		this.v2Exception = v2Exception;
	}

	/**
	 * @return the v3Exception
	 */
	public ListedLicenseException getV3Exception() {
		return v3Exception;
	}

	/**
	 * @param v3Exception the v3Exception to set
	 */
	public void setV3Exception(ListedLicenseException v3Exception) {
		this.v3Exception = v3Exception;
	}
}
