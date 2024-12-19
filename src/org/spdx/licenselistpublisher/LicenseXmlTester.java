/**
 * SpdxLicenseIdentifier: Apache-2.0
 *
 * Copyright (c) 2019 Source Auditor Inc.
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.spdx.core.IModelCopyManager;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.model.v3_0_1.SpdxConstantsV3;
import org.spdx.licensexml.LicenseXmlDocument;
import org.spdx.licensexml.LicenseXmlException;
import org.spdx.licensexml.XmlLicenseProvider;
import org.spdx.storage.IModelStore;
import org.spdx.storage.simple.InMemSpdxStore;
import org.spdx.utility.compare.CompareTemplateOutputHandler.DifferenceDescription;
import org.spdx.utility.compare.LicenseCompareHelper;
import org.spdx.utility.compare.SpdxCompareException;

/**
 * Tests a license XML file against license text expected to match
 * TestLicenseXML licenseXmlFile textFile
 * licenseXmlFile XML - file
 * textFile - Text file which should match the the license text for the licenseXmlFile
 *
 * @author Gary O'Neall
 *
 */
public class LicenseXmlTester {

	static int MIN_ARGS = 2;
	static int MAX_ARGS = 2;
	static final int ERROR_STATUS = 1;
	static final int WARNING_STATUS = 64;

	/**
	 * @param args
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
		try {
			IModelStore spdxV2ModelStore = new InMemSpdxStore();
			IModelStore spdxV3ModelStore = new InMemSpdxStore();
			IModelCopyManager copyManager = new ModelCopyManager();
			DateFormat format = new SimpleDateFormat(SpdxConstantsV3.SPDX_DATE_FORMAT);
			String now = format.format(new Date());
			LicenseXmlDocument licDoc = new LicenseXmlDocument(licenseXmlFile, spdxV2ModelStore, spdxV3ModelStore,
					copyManager, XmlLicenseProvider.createCreationInfo(spdxV3ModelStore, copyManager, now, "3.25.0" ));
			List<ListedLicenseContainer> licenses = licDoc.getListedLicenses();
			if (licenses.size() == 0) {
				System.out.println("Empty license XML file - no licenses found");
				System.exit(ERROR_STATUS);
			}
			if (licenses.size() > 1) {
				System.out.println("More than one licenses found");
				System.exit(ERROR_STATUS);
			}
			String compareText = readText(testFile);
			DifferenceDescription diff = LicenseCompareHelper.isTextStandardLicense(licenses.get(0).getV3ListedLicense(), compareText);
			if (diff.isDifferenceFound()) {
				System.out.println("Difference found comparing to test file: "+diff.getDifferenceMessage());
				System.exit(ERROR_STATUS);
			}
			System.out.println("License "+licenses.get(0).getV3ListedLicense().getName()+" passed");
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
			System.out.println("Invalid license template: "+e.getMessage());
			System.exit(ERROR_STATUS);
		}
	}

	private static String readText(File f) throws IOException {
		Charset utf8 = Charset.forName("UTF-8");
		StringBuilder text = new StringBuilder();
		try(Stream<String> fileLines = Files.lines(f.toPath(), utf8)) {
		    fileLines.forEach(line -> {
    			text.append(line);
    			text.append("\n");
    			});
		}
        return text.toString();
	}

	private static void usage() {
		System.out.println("Usage:");
		System.out.println("TestLicenseXML licenseXmlFile textFile");
		System.out.println("   licenseXmlFile XML - file to test");
		System.out.println("   textFile - Text file which should match the the license text for the licenseXmlFile");
	}
}
