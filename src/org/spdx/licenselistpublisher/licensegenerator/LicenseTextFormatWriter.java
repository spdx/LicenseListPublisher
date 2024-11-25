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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.license.ListedLicenseException;
import org.spdx.library.model.v2.license.SpdxListedLicense;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;


/**
 * Writes licenses in a simple text format
 *
 * @author Gary O'Neall
 *
 */
public class LicenseTextFormatWriter implements ILicenseFormatWriter {

	private static final int MAX_LINE_CHARS = 80;
	private static final int TYPICAL_WORD_CHARS = 8;
	private File textFolder;
	private Charset utf8 = Charset.forName("UTF-8");

	/**
	 * @param textFolder Folder to write the text files
	 */
	public LicenseTextFormatWriter(File textFolder) {
		this.textFolder = textFolder;
	}

	/**
	 * @return Folder to write the text files
	 */
	public File getTextFolder() {
		return textFolder;
	}

	/**
	 * @param textFolder Folder to write the text files
	 */
	public void setTextFolder(File textFolder) {
		this.textFolder = textFolder;
	}
	
	/**
	 * @param licenseContainer license to write
	 * @param deprecated true if deprecated
	 * @param deprecatedVersion version deprecated in
	 * @param wordWrapText if true, reformat the license text wrapping words
	 * @throws InvalidSPDXAnalysisException 
	 * @throws IOException 
	 */
	public void writeLicense(ListedLicenseContainer licenseContainer, boolean deprecated, String deprecatedVersion, boolean wordWrapText) throws InvalidSPDXAnalysisException, IOException {
		SpdxListedLicense license = licenseContainer.getV2ListedLicense();
		String licBaseHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(license.getLicenseId());
		if (deprecated) {
			licBaseHtmlFileName = "deprecated_" + licBaseHtmlFileName;
		}
		Path textFilePath = Paths.get(textFolder.getPath(), licBaseHtmlFileName + ".txt");
		String[] lines = license.getLicenseText().split("\\n");
		List<String> wordWrappedLines = new ArrayList<String>();
		for (String line:lines) {
			if (line.length() < MAX_LINE_CHARS || !wordWrapText) {
				wordWrappedLines.add(line);
			} else {
				String[] words = line.split(" ");
				StringBuilder currentLine = new StringBuilder();
				for (String word:words)
				{
					if (currentLine.length() > MAX_LINE_CHARS - TYPICAL_WORD_CHARS) {
						wordWrappedLines.add(currentLine.toString());
						currentLine.setLength(0);
					} else if (currentLine.length() > 0) {
						currentLine.append(' ');
					}
					currentLine.append(word);
				}
				if (currentLine.length() > 0) {
					wordWrappedLines.add(currentLine.toString());
				}
			}
		}
		Files.write(textFilePath, wordWrappedLines, utf8);
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeLicense(org.spdx.rdfparser.license.SpdxListedLicense, boolean)
	 */
	@Override
	public void writeLicense(ListedLicenseContainer licenseContainer, boolean deprecated, String deprecatedVersion) throws IOException, InvalidSPDXAnalysisException {
		writeLicense(licenseContainer, deprecated, deprecatedVersion, true);
	}

	@Override
	public void writeToC() throws IOException {
		// Don't need to do anything - no TOC

	}

	@Override
	public void writeException(ListedExceptionContainer exceptionContainer)
			throws IOException, InvalidSPDXAnalysisException {
		ListedLicenseException exception = exceptionContainer.getV2Exception();
		String exceptionHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		Path textFilePath = Paths.get(textFolder.getPath(), exceptionHtmlFileName + ".txt");
		Files.write(textFilePath, Arrays.asList(exception.getLicenseExceptionText().split("\\n")), utf8);
	}
}
