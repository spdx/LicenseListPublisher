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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.license.License;
import org.spdx.library.model.license.LicenseException;
import org.spdx.utility.compare.CompareTemplateOutputHandler.DifferenceDescription;
import org.spdx.utility.compare.LicenseCompareHelper;
import org.spdx.utility.compare.SpdxCompareException;

/**
 * Tests licenses against cononical text.
 *
 * @author Gary O'Neall
 *
 */
public class SimpleLicenseTester implements ILicenseTester {


	private Charset utf8 = Charset.forName("UTF-8");
	private File testFileDir;

	/**
	 * @param testFileDir Directory containing license texts in the format [license-id].txt
	 */
	public SimpleLicenseTester(File testFileDir) {
		this.testFileDir = testFileDir;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseTester#testException(org.spdx.rdfparser.license.LicenseException)
	 */
	@Override
	public List<String> testException(LicenseException exception) throws IOException, InvalidSPDXAnalysisException {
		File exceptionFile = new File(testFileDir.getPath() + File.separator + exception.getLicenseExceptionId() + ".txt");
		List<String> retval = new ArrayList<String>();
		if (!exceptionFile.exists()) {
			retval.add("No test text exists for license exception ID "+exception.getLicenseExceptionId());
		} else {
			String compareText = readText(exceptionFile);
			DifferenceDescription result;
			try {
				result = LicenseCompareHelper.isTextStandardException(exception, compareText);
				if (result.isDifferenceFound()) {
					retval.add("Test for exception ID "+exception.getLicenseExceptionId() + " failed due to difference found "+result.getDifferenceMessage());
				}
			} catch (SpdxCompareException e) {
				retval.add("Invalid template found for exception ID "+exception.getLicenseExceptionId()+": "+e.getMessage());
			}
		}
		return retval;
	}

	private String readText(File f) throws IOException {
		StringBuilder text = new StringBuilder();
		try(Stream<String> fileLines = Files.lines(f.toPath(), utf8)) {
		      fileLines.forEach(line -> {
		            text.append(line);
		            text.append("\n");
		            });
		}
		return text.toString();
	}
	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseTester#testLicense(org.spdx.rdfparser.license.License)
	 */
	@Override
	public List<String> testLicense(License license) throws IOException, InvalidSPDXAnalysisException {
		List<String> retval = new ArrayList<String>();
		File licenseTextFile = new File(testFileDir.getPath() + File.separator + license.getLicenseId() + ".txt");
		if (!licenseTextFile.exists()) {
			if (!license.isDeprecated()) {
				retval.add("No test text exists for license ID "+license.getLicenseId());
			}
		} else {
			String compareText = readText(licenseTextFile);
			DifferenceDescription result;
			try {
				result = LicenseCompareHelper.isTextStandardLicense(license, compareText);
				if (result.isDifferenceFound()) {
					retval.add("Test for license ID "+license.getLicenseId() + " failed due to difference found "+result.getDifferenceMessage());
				}
			} catch (SpdxCompareException e) {
				retval.add("Invalid template found for license ID "+license.getLicenseId()+": "+e.getMessage());
			}
		}
		return retval;
	}

	@Override
	public String getLicenseTestText(String licenseId) throws IOException {
		File licenseTextFile = new File(testFileDir.getPath() + File.separator + licenseId + ".txt");
		if (licenseTextFile.exists()) {
			return readText(licenseTextFile);
		} else {
			return null;
		}
	}

	@Override
	public String getExceptionTestText(String licenseExceptionId) throws IOException {
		File exceptionFile = new File(testFileDir.getPath() + File.separator + licenseExceptionId + ".txt");
		if (exceptionFile.exists()) {
			return readText(exceptionFile);
		} else {
			return null;
		}
	}

}
