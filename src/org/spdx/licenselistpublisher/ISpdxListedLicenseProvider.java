/**
 * Copyright (c) 2012 Source Auditor Inc.
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

import java.util.Iterator;
import java.util.List;

import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.license.ListedLicenseException;
import org.spdx.library.model.license.SpdxListedLicense;
import org.spdx.library.model.license.SpdxListedLicenseException;

/**
 * Interface to provide SPDX standard licenses
 * @author Gary O'Neall
 *
 */
public interface ISpdxListedLicenseProvider {

	public Iterator<SpdxListedLicense> getLicenseIterator() throws SpdxListedLicenseException;
	public Iterator<ListedLicenseException> getExceptionIterator() throws InvalidSPDXAnalysisException;
	public List<String> getWarnings();

}