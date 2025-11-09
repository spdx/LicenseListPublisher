/**
 * Copyright (c) 2011 Source Auditor Inc.
 * Copyright (c) 2024 Steve Winslow
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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.license.LicenseException;
import org.spdx.library.model.license.ListedLicenseException;
import org.spdx.library.model.license.SpdxListedLicense;
import org.spdx.library.model.license.SpdxListedLicenseException;
import org.spdx.licenseTemplate.InvalidLicenseTemplateException;
import org.spdx.licensexml.XmlLicenseProviderSingleFile;
import org.spdx.licensexml.XmlLicenseProviderWithCrossRefDetails;
import org.spdx.utility.compare.LicenseCompareHelper;
import org.spdx.utility.compare.SpdxCompareException;
import org.spdx.licenselistpublisher.licensegenerator.FsfLicenseDataParser;
import org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter;
import org.spdx.licenselistpublisher.licensegenerator.ILicenseTester;
import org.spdx.licenselistpublisher.licensegenerator.LicenseHtmlFormatWriter;
import org.spdx.licenselistpublisher.licensegenerator.LicenseJsonFormatWriter;
import org.spdx.licenselistpublisher.licensegenerator.LicenseMarkdownFormatWriter;
import org.spdx.licenselistpublisher.licensegenerator.LicenseRdfFormatWriter;
import org.spdx.licenselistpublisher.licensegenerator.LicenseRdfaFormatWriter;
import org.spdx.licenselistpublisher.licensegenerator.LicenseTemplateFormatWriter;
import org.spdx.licenselistpublisher.licensegenerator.LicenseTextFormatWriter;
import org.spdx.licenselistpublisher.licensegenerator.SimpleLicenseTester;
import org.spdx.licenselistpublisher.licensegenerator.SpdxWebsiteFormatWriter;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Converts input license text and metadata for a single license into a single HTML file.
 *
 * Supported input formats:
 *  - License XML file - File following the SPDX legal team license format
 *
 * Supported output formats:
 *  - Website - the content for the website available at https://spdx.org/licenses
 *
 * @author Gary O'Neall and Steve Winslow
 *
 */
public class LicenseSingleHTMLGenerator {

	static final Set<Character> INVALID_TEXT_CHARS = new HashSet<>();

	static {
		INVALID_TEXT_CHARS.add('\uFFFD');
	}
	static final int ERROR_STATUS = 1;
	static final int WARNING_STATUS = 64;
	private static final String LICENSE_XML_FOLDER_NAME = "license-list-XML";

	/**
	 * @param args Arg 0 is a license XML file in XML format, arg 1 is the directory for the output file
	 */
	public static void main(String[] args) {
		if (args == null || args.length != 2) {
			System.out.println("Invalid arguments");
			usage();
			System.exit(ERROR_STATUS);
		}
		File licenseXmlFile = new File(args[0]);
		if (!licenseXmlFile.exists()) {
			System.out.println("License XML "+licenseXmlFile.getName()+" does not exist");
			usage();
			System.exit(ERROR_STATUS);
		}
		File dir = new File(args[1]);
		if (!dir.exists()) {
			System.out.println("Output directory "+dir.getName()+" does not exist");
			usage();
			System.exit(ERROR_STATUS);
		}
		if (!dir.isDirectory()) {
			System.out.println("Output directory "+dir.getName()+" is not a directory");
			usage();
			System.exit(ERROR_STATUS);
		}
		
		try {
			List<String> warnings = generateLicenseData(licenseXmlFile, dir);
			if (warnings != null && warnings.size() > 0) {
				System.exit(WARNING_STATUS);
			}
		} catch (LicenseGeneratorException e) {
			System.out.println(e.getMessage());
			System.exit(ERROR_STATUS);
		}
	}
	/**
	 * Generate license data
	 * @param licenseXml License XML file or directory containing license XML files
	 * @param dir Output directory for the generated results
	 * @return warnings
	 * @throws LicenseGeneratorException
	 */
	public static List<String> generateLicenseData(File licenseXml, File dir) throws LicenseGeneratorException {
		List<String> warnings = new ArrayList<>();
		List<ILicenseFormatWriter> writers = new ArrayList<>();
		ISpdxListedLicenseProvider licenseProvider = null;
		try {
			licenseProvider = new XmlLicenseProviderSingleFile(licenseXml);
			File website = new File(dir.getPath());
			if (!website.isDirectory() && !website.mkdir()) {
				throw new LicenseGeneratorException("Error: Website folder is not a directory");
			}
			writers.add(new SpdxWebsiteFormatWriter(null, null, website));
			System.out.print("Processing License List");
			Set<String> licenseIds = writeLicenseList(licenseProvider, warnings, writers);
			System.out.println();
			System.out.print("Processing Exceptions");
			writeExceptionList(licenseProvider, warnings, writers, licenseIds);
			System.out.println();
			warnings.addAll(licenseProvider.getWarnings());
			if (warnings.size() > 0) {
				System.out.println("The following warning(s) were identified:");
				for (String warning : warnings) {
					System.out.println("\t"+warning);
				}
			}
			System.out.println("Completed processing licenses");
			return warnings;
		} catch (SpdxListedLicenseException e) {
			throw new LicenseGeneratorException("\nError reading standard licenses: "+e.getMessage(),e);
		} catch (LicenseGeneratorException e) {
			throw(e);
		} catch (Exception e) {
			throw new LicenseGeneratorException("\nUnhandled exception generating html: "+e.getMessage(),e);
		}
	}

	/**
	 * @param version License list version
	 * @param releaseDate release date for the license list
	 * @param licenseProvider Provides the licensing information
	 * @param warnings Populated with any warnings if they occur
	 * @param writers License Format Writers to handle the writing for the different formats
	 * @param tester License tester used to test the results of licenses
	 * @param licenseIds license IDs
	 * @param useTestText use the text file from the testFileDir for the verbatim text rather than the text from the XML document
	 * @throws IOException
	 * @throws SpreadsheetException
	 * @throws LicenseRestrictionException
	 * @throws LicenseGeneratorException
	 * @throws InvalidLicenseTemplateException
	 * @throws InvalidSPDXAnalysisException 
	*/
	private static void writeExceptionList(ISpdxListedLicenseProvider licenseProvider, List<String> warnings, List<ILicenseFormatWriter> writers,
			Set<String> licenseIds) throws IOException, LicenseGeneratorException, InvalidLicenseTemplateException, InvalidSPDXAnalysisException {
		Iterator<ListedLicenseException> exceptionIter = licenseProvider.getExceptionIterator();
		Map<String, String> addedExceptionsMap = new HashMap<>();
		while (exceptionIter.hasNext()) {
			System.out.print(".");
			ListedLicenseException nextException = exceptionIter.next();
			if (nextException.getLicenseExceptionId() != null && !nextException.getLicenseExceptionId().isEmpty()) {
				checkText(nextException.getLicenseExceptionText(),
						"License Exception Text for "+nextException.getLicenseExceptionId(), warnings);
				for (ILicenseFormatWriter writer:writers) {
					writer.writeException(nextException);
				}
			}
		}
	}

	/**
	 * Check text for invalid characters
	 * @param text Text to check
	 * @param textDescription Description of the text being check (this will be used to form warning messages)
	 * @param warnings Array list of warnings to add to if an problem is found with the text
	 */
	private static void checkText(String text, String textDescription,
			List<String> warnings) {
		BufferedReader reader = new BufferedReader(new StringReader(text));
		try {
			int lineNumber = 1;
			String line = reader.readLine();
			while (line != null) {
				for (int i = 0; i < line.length(); i++) {
					if (INVALID_TEXT_CHARS.contains(line.charAt(i))) {
						warnings.add("Invalid character in " + textDescription +
								" at line number " + String.valueOf(lineNumber) +
								" \"" +line + "\" at character location "+String.valueOf(i));
					}
				}
				lineNumber++;
				line = reader.readLine();
			}
		} catch (IOException e) {
			warnings.add("IO error reading text");
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				warnings.add("IO Error closing string reader");
			}
		}
	}

	/**
	 * Formats and writes the license list data
	 * @param licenseProvider Provides the licensing information
	 * @param warnings Populated with any warnings if they occur
	 * @param writers License Format Writers to handle the writing for the different formats
	 * @return list of license ID's which have been added
	 * @throws LicenseGeneratorException
	 * @throws InvalidSPDXAnalysisException
	 * @throws IOException
	 * @throws SpdxListedLicenseException
	 * @throws SpdxCompareException
	 * @throws InvalidLicenseTemplateException 
	 */
	private static Set<String> writeLicenseList(ISpdxListedLicenseProvider licenseProvider, List<String> warnings,
			List<ILicenseFormatWriter> writers) throws LicenseGeneratorException, InvalidSPDXAnalysisException, IOException, SpdxListedLicenseException, SpdxCompareException, InvalidLicenseTemplateException {
		Iterator<SpdxListedLicense> licenseIter = licenseProvider.getLicenseIterator();
		try {
			Map<String, String> addedLicIdTextMap = new HashMap<>();
			while (licenseIter.hasNext()) {
				System.out.print(".");
				SpdxListedLicense license = licenseIter.next();
				addExternalMetaData(license);
				if (license.getLicenseId() != null && !license.getLicenseId().isEmpty()) {
					checkText(license.getLicenseText(), "License text for "+license.getLicenseId(), warnings);
					for (ILicenseFormatWriter writer : writers) {
						if (writer instanceof LicenseTextFormatWriter) {
							((LicenseTextFormatWriter)(writer)).writeLicense(license, license.isDeprecated(), license.getDeprecatedVersion(), true);
						} else {
							writer.writeLicense(license, license.isDeprecated(), license.getDeprecatedVersion());
						}
					}
				}
			}
			return addedLicIdTextMap.keySet();
		} finally {
			if (licenseIter instanceof Closeable) {
				((Closeable)licenseIter).close();
				//TODO: Is there a cleaner way to handle this?  The XmlLicenseProviderWithCrossRefDetails uses executorService which must be closed
			}
		}
	}

	/**
	 * Update license fields based on information from external metadata
	 * @param license
	 * @throws LicenseGeneratorException
	 * @throws InvalidSPDXAnalysisException 
	 */
	private static void addExternalMetaData(SpdxListedLicense license) throws LicenseGeneratorException, InvalidSPDXAnalysisException {
		license.setFsfLibre(FsfLicenseDataParser.getFsfLicenseDataParser().isSpdxLicenseFsfLibre(license.getLicenseId()));
	}

	/**
	 * Copy a file from the resources directory to a destination file
	 * @param resourceFileName filename of the file in the resources directory
	 * @param destination target file - warning, this will be overwritten
	 * @throws IOException
	 */
	private static void copyResourceFile(String resourceFileName, File destination) throws IOException {
		File resourceFile = new File(resourceFileName);
		if (resourceFile.exists()) {
			Files.copy(resourceFile.toPath(), destination.toPath());
		} else {
			InputStream is = LicenseRDFAGenerator.class.getClassLoader().getResourceAsStream(resourceFileName);
			InputStreamReader reader = new InputStreamReader(is);
			FileWriter writer = new FileWriter(destination);
			try {
				char[] buf = new char[2048];
				int len = reader.read(buf);
				while (len > 0) {
					writer.write(buf, 0, len);
					len = reader.read(buf);
				}
			} finally {
				if (writer != null) {
					writer.close();
				}
				reader.close();
			}
		}
	}

	private static void usage() {
		System.out.println("Usage:");
		System.out.println("LicenseSingleHTMLGenerator licenseXmlFile outputDirectory");
		System.out.println("   licencenseXmlFile - a license XML file");
		System.out.println("   outputDirectory - Directory to store the output from the license generator");
	}

}
