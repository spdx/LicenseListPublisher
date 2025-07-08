/**
 * Copyright (c) 2017 Source Auditor Inc.
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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.license.License;
import org.spdx.library.model.v2.license.LicenseException;
import org.spdx.licenseTemplate.LicenseTextHelper;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;
import org.spdx.utility.compare.CompareTemplateOutputHandler.DifferenceDescription;
import org.spdx.utility.compare.LicenseCompareHelper;
import org.spdx.utility.compare.SpdxCompareException;

/**
 * Test SPDX licenses against a directory of test licenses.
 * <p>
 * The directory of test licenses contains license text with the following file naming convention:
 * <p>
 * {license-id}/(license|header|exception)/(good|bad)/{test-id}.txt
 *
 * @author Gary O'Neall
 *
 */
public class LicenseTester implements ILicenseTester {

	private final Map<String,File> licenseIdToTestMap;
	private static final FileFilter testFileFilter = arg0 -> (arg0.isFile() && arg0.getName().toLowerCase().endsWith(".txt"));

	/**
	 * @param licenseTestDirectory Directory of license text files for comparison in the form {license-id}/(license|header|exception)/(good|bad)/{test-id}.txt
	 */
	public LicenseTester(File licenseTestDirectory) {
		licenseIdToTestMap = new HashMap<>();
		File[] licenseIdDirs = licenseTestDirectory.listFiles();
		if (licenseIdDirs != null) {
			for (File dir:licenseIdDirs) {
				if (dir.isDirectory()) {
					licenseIdToTestMap.put(dir.getName(),dir);
				}
			}
		}
	}

	/**
	 * Test a license against the license test files
	 * @param licenseContainer license to test
	 * @return list of test failure descriptions.  List is empty if all tests pass.
	 * @throws IOException on I/O error reading test file(s)
	 * @throws SpdxCompareException on error comparing exceptions
	 * @throws InvalidSPDXAnalysisException on SPDX parsing errors
	 */
	@Override
	public List<String> testLicense(ListedLicenseContainer licenseContainer) throws IOException, SpdxCompareException, InvalidSPDXAnalysisException {
		License license = licenseContainer.getV2ListedLicense();
		List<String> retval = new ArrayList<>();
		File licenseDir = this.licenseIdToTestMap.get(license.getLicenseId());
		if (licenseDir == null || !licenseDir.exists()) {
			return retval;
		}
		File positiveTestDir = new File (licenseDir.getPath() + File.separator + "license" + File.separator + "good");
		if (positiveTestDir.exists() && positiveTestDir.isDirectory()) {
			File[] positiveTests = positiveTestDir.listFiles(testFileFilter);
			if (positiveTests != null) {
				for (File test:positiveTests) {
					String text = readText(test);
					DifferenceDescription result = LicenseCompareHelper.isTextStandardLicense(license, text);
					if (result.isDifferenceFound()) {
						retval.add("Test 'positive-"+test.toPath().getFileName()+"' failed due to difference found "+result.getDifferenceMessage());
					}
				}
			}
		}
		File negativeTestDir = new File (licenseDir.getPath() + File.separator + "license" + File.separator + "bad");
		if (negativeTestDir.exists() && negativeTestDir.isDirectory()) {
			File[] negativeTests = negativeTestDir.listFiles(testFileFilter);
			if (negativeTests != null) {
				for (File test:negativeTests) {
					String text = readText(test);
					DifferenceDescription result = LicenseCompareHelper.isTextStandardLicense(license, text);
					if (!result.isDifferenceFound()) {
						retval.add("Test 'negative-"+test.toPath().getFileName()+"' failed - no difference found");
					}
				}
			}
		}
		return retval;
	}

	private String readText(File f) throws IOException {
		StringBuilder text = new StringBuilder();
		try (Stream<String> fileLines = Files.lines(f.toPath())) {
		      fileLines.forEach(line -> {
		            text.append(line);
		            text.append("\n");
		            });
		}
		return text.toString();
	}

	/**
	 * Test exception against the test files directory
	 * @param exceptionContainer exception to test
	 * @return list of test failure descriptions.  List is empty if all tests pass.
	 * @throws IOException on I/O error reading test file(s)
	 * @throws InvalidSPDXAnalysisException on SPDX parsing errors
	 */
	@Override
	public List<String> testException(ListedExceptionContainer exceptionContainer) throws IOException, InvalidSPDXAnalysisException {
		LicenseException exception = exceptionContainer.getV2Exception();
		List<String> retval = new ArrayList<>();
		File exceptionDir = this.licenseIdToTestMap.get(exception.getLicenseExceptionId());
		if (exceptionDir == null || !exceptionDir.exists()) {
			return retval;
		}
		File positiveTestDir = new File (exceptionDir.getPath() + File.separator + "exception" + File.separator + "good");
		if (positiveTestDir.exists() && positiveTestDir.isDirectory()) {
			File[] positiveTests = positiveTestDir.listFiles(testFileFilter);
			if (positiveTests != null) {
				for (File test:positiveTests) {
					String text = readText(test);
					if (!LicenseTextHelper.isLicenseTextEquivalent(text, exception.getLicenseExceptionText())) {
						retval.add("Test 'positive-"+test.toPath().getFileName()+"' failed due to difference found");
					}
				}
			}
		}
		File negativeTestDir = new File (exceptionDir.getPath() + File.separator + "exception" + File.separator + "bad");
		if (negativeTestDir.exists() && negativeTestDir.isDirectory()) {
			File[] negativeTests = negativeTestDir.listFiles(testFileFilter);
			if (negativeTests != null) {
				for (File test:negativeTests) {
					String text = readText(test);
						if (LicenseTextHelper.isLicenseTextEquivalent(text, exception.getLicenseExceptionText())) {
						retval.add("Test 'negative-"+test.toPath().getFileName()+"' failed - no difference found");
					}
				}
			}
		}
		return retval;
	}

	@Override
	public String getLicenseTestText(String licenseId) {
		throw new RuntimeException("Unimplemented getLicenseTestText");
	}

	@Override
	public String getExceptionTestText(String licenseExceptionId) {
		throw new RuntimeException("Unimplemented getExceptionTestText");
	}
}
