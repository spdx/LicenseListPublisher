/**
 * SpdxLicenseIdentifier: Apache-2.0
 * <p>
 * Copyright (c) 2019 Source Auditor Inc.
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
 *
*/
package org.spdx.licenselistpublisher;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.spdx.core.IModelCopyManager;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.model.v3_0_1.SpdxConstantsV3;
import org.spdx.licenselistpublisher.licensegenerator.LicenseTester;
import org.spdx.licenselistpublisher.licensegenerator.SimpleLicenseTester;
import org.spdx.licensexml.LicenseXmlDocument;
import org.spdx.licensexml.LicenseXmlException;
import org.spdx.licensexml.XmlLicenseProvider;
import org.spdx.storage.IModelStore;
import org.spdx.storage.simple.InMemSpdxStore;
import org.spdx.utility.compare.SpdxCompareException;

import javax.annotation.Nullable;

/**
 * Tests a license XML file against license text expected to match
 * TestLicenseXML licenseXmlFile textFile
 * licenseXmlFile XML - file
 * textFile - Text file which should match the license text for the licenseXmlFile
 * testDirectory - optional directory of text files to test with the pattern {license-id}/(license|header|exception)/(good|bad)/{test-id}.txt
 *
 * @author Gary O'Neall
 *
 */
public class LicenseXmlTester {

	static int MIN_ARGS = 2;
	static int MAX_ARGS = 3;
	static final int ERROR_STATUS = 1;
	static final int WARNING_STATUS = 64;

	/**
	 * @param args arguments - see usage for documentation
	 */
	public static void main(String[] args) {
		if (args == null || args.length < MIN_ARGS || args.length > MAX_ARGS) {
			System.out.println("Invalid arguments");
			usage();
			System.exit(ERROR_STATUS);
		}
		File licenseXmlFile = new File(args[0]);
		if (!licenseXmlFile.exists()) {
			System.out.println("License XML file "+licenseXmlFile.getName()+" does not exist");
			usage();
			System.exit(ERROR_STATUS);
		}
		if (!licenseXmlFile.isFile()) {
			System.out.println("License XML file "+licenseXmlFile.getName()+" is not a valid file");
			usage();
			System.exit(ERROR_STATUS);
		}
		File testFile = new File(args[1]);
		if (!testFile.exists()) {
			System.out.println("Test file "+testFile.getName()+" does not exist");
			usage();
			System.exit(ERROR_STATUS);
		}
		if (!testFile.isFile()) {
			System.out.println("Test file "+testFile.getName()+" is not a valid file");
			usage();
			System.exit(ERROR_STATUS);
		}

		File testDirectory = null;
		if (args.length > 2) {
			testDirectory = new File(args[2]);
			if (!testDirectory.exists()) {
				System.out.println("Test directory "+testDirectory.getName()+" does not exist");
				usage();
				System.exit(ERROR_STATUS);
			}
			if (!testDirectory.isDirectory()) {
				System.out.println("Test directory "+testDirectory.getName()+" is not a directory");
				usage();
				System.exit(ERROR_STATUS);
			}
		}
		try {
			List<String> result = testLicenseXml(licenseXmlFile, testFile, testDirectory);
			if (result.isEmpty()) {
				System.out.println("License passed");
			} else {
				System.out.println("The following error(s) were found:");
				for (String error:result) {
					System.out.print("\t");
					System.out.println(error);
				}
				System.exit(WARNING_STATUS);
			}
		} catch (LicenseXmlException e) {
			System.out.println("Invalid license XML document: "+e.getMessage());
			System.exit(ERROR_STATUS);
		} catch (InvalidSPDXAnalysisException e) {
			System.out.println("Invalid license SPDX definition: "+e.getMessage());
			System.exit(ERROR_STATUS);
		} catch (IOException e) {
			System.out.println("I/O error reading test file: "+e.getMessage());
			System.exit(ERROR_STATUS);
		} catch (SpdxCompareException e) {
			System.out.println("Compare error: "+e.getMessage());
			System.exit(ERROR_STATUS);
        }
    }

	/**
	 * test a license XML against a single file and optionally a directory of test files
	 * @param licenseXmlFile file in LicenseXML Format
	 * @param testFile single file to compare
	 * @param testDirectory optional directory to test against with the pattern {license-id}/(license|header|exception)/(good|bad)/{test-id}.txt
	 * @return list of errors when comparing - if empty, no errors occurs
	 * @throws InvalidSPDXAnalysisException on SPDX parsing errors
	 * @throws LicenseXmlException on errors parsing the LicenseXML
	 * @throws IOException on errors reading either the XML or test files
	 */
	public static List<String> testLicenseXml(File licenseXmlFile, File testFile, @Nullable File testDirectory) throws InvalidSPDXAnalysisException, LicenseXmlException, IOException, SpdxCompareException {
		IModelStore spdxV2ModelStore = new InMemSpdxStore();
		IModelStore spdxV3ModelStore = new InMemSpdxStore();
		IModelCopyManager copyManager = new ModelCopyManager();
		DateFormat format = new SimpleDateFormat(SpdxConstantsV3.SPDX_DATE_FORMAT);
		String now = format.format(new Date());
		LicenseXmlDocument licDoc = new LicenseXmlDocument(licenseXmlFile, spdxV2ModelStore, spdxV3ModelStore,
				copyManager, XmlLicenseProvider.createCreationInfo(spdxV3ModelStore, copyManager, now, "3.25.0" ));
		List<ListedLicenseContainer> licenses = licDoc.getListedLicenses();
		if (licenses.isEmpty()) {
            return List.of("Empty license XML file - no licenses found");
		}
		if (licenses.size() > 1) {
			return List.of("More than one licenses found");
		}
		List<String> retval = SimpleLicenseTester.testLicense(licenses.get(0).getV2ListedLicense(), testFile);
		if (Objects.nonNull(testDirectory)) {
			LicenseTester tester = new LicenseTester(testDirectory);
			retval.addAll(tester.testLicense(licenses.get(0)));
		}
		return retval;
	}

	private static void usage() {
		System.out.println("Usage:");
		System.out.println("TestLicenseXML licenseXmlFile textFile");
		System.out.println("   licenseXmlFile XML - file to test");
		System.out.println("   textFile - Text file which should match the the license text for the licenseXmlFile");
		System.out.println("   testDirectory - Optional directory of test files in the form {license-id}/(license|header|exception)/(good|bad)/{test-id}.txt");
	}
}
