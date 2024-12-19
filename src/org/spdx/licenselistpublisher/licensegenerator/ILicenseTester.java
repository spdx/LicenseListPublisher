/**
 * Copyright (c) 2017 Source Auditor Inc.
 *
 * <p>
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * <p>
 *       http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;
import org.spdx.utility.compare.SpdxCompareException;

/**
 * Interface for license testers
 * @author Gary O'Neall
 *
 */
public interface ILicenseTester {

	/**
	 * Test exception against the test files directory
	 * @param exceptionContainer Exception to test
	 * @return list of test failure descriptions.  List is empty if all tests pass.
	 * @throws IOException on I/O error reading test file(s)
	 * @throws InvalidSPDXAnalysisException on error parsing SPDX licenses
	 */
    List<String> testException(ListedExceptionContainer exceptionContainer) throws IOException, InvalidSPDXAnalysisException;

	/**
	 * Test a license against the license test files
	 * @param licenseContainer license to test
	 * @return list of test failure descriptions.  List is empty if all tests pass.
	 * @throws IOException on I/O error reading test file(s)
	 * @throws SpdxCompareException on error executing the compare
	 * @throws InvalidSPDXAnalysisException on error parsing SPDX licenses
	 */
    List<String> testLicense(ListedLicenseContainer licenseContainer) throws IOException, SpdxCompareException, InvalidSPDXAnalysisException;

	/**
	 * @param licenseId license ID for the license text
	 * @throws IOException on I/O error reading test file(s)
	 * @return text for the test for a license ID, null if no license text is found
	 */
	@Nullable String getLicenseTestText(String licenseId) throws IOException;

	/**
	 * @param licenseExceptionId license exception text
	 * @return text for the test for an exception ID, null if no license text is found
	 * @throws IOException on I/O error reading test file(s)
	 */
	@Nullable String getExceptionTestText(String licenseExceptionId) throws IOException;

}
