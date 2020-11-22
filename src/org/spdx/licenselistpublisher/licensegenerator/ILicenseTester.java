/**
 * Copyright (c) 2017 Source Auditor Inc.
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
 */
package org.spdx.licenselistpublisher.licensegenerator;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.license.License;
import org.spdx.library.model.license.LicenseException;
import org.spdx.utility.compare.SpdxCompareException;

/**
 * Interface for license testers
 * @author Gary O'Neall
 *
 */
public interface ILicenseTester {

	/**
	 * Test exception against the test files directory
	 * @param exception
	 * @return
	 * @throws IOException
	 * @throws InvalidSPDXAnalysisException 
	 */
	public List<String> testException(LicenseException exception) throws IOException, InvalidSPDXAnalysisException;

	/**
	 * Test a license against the license test files
	 * @param license license to test
	 * @return list of test failure descriptions.  List is empty if all tests pass.
	 * @throws IOException
	 * @throws SpdxCompareException
	 * @throws InvalidSPDXAnalysisException 
	 */
	public List<String> testLicense(License license) throws IOException, SpdxCompareException, InvalidSPDXAnalysisException;

	/**
	 * @param licenseId
	 * @return text for the test for a license ID, null if no license text is found
	 */
	public @Nullable String getLicenseTestText(String licenseId) throws IOException;

	/**
	 * @param licenseExceptionId
	 * @return text for the test for a exception ID, null if no license text is found
	 * @throws IOException 
	 */
	public @Nullable String getExceptionTestText(String licenseExceptionId) throws IOException;

}
