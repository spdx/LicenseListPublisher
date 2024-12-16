/**
 * Copyright (c) Source Auditor Inc
 * SPDX-License-Identifier:	Apache-2.0
 */

package org.spdx.licenselistpublisher;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Dispatch to the individual tools
 */
public class Main {

	public static void main(String[] args) {

		if (args.length < 1) {
			usage();
			return;
		}

		String spdxTool = args[0];
		args = ArrayUtils.removeElement(args, args[0]);
		if ("LicenseRDFAGenerator".equals(spdxTool)) {
			LicenseRDFAGenerator.main(args);
		} else if ("LicenseSingleHTMLGenerator".equals(spdxTool)) {
			LicenseSingleHTMLGenerator.main(args);
		} else if ("TestLicenseXML".equals(spdxTool)) {
			LicenseXmlTester.main(args);
		} else {
			usage();
		}
	}

	private static void usage() {
		System.out.println("Usage: java -jar spdx-tools-jar-with-dependencies.jar <function> <parameters>");
		System.out.println("LicenseRDFAGenerator - Generates license data");
		System.out.println("LicenseSingleHTMLGenerator - Generates license data as single HTML file");
		System.out.println("TestLicenseXML - Tests a license XML file");
	}
}
