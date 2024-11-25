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
import java.util.HashSet;
import java.util.Set;

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.license.ListedLicenseException;
import org.spdx.library.model.v2.license.SpdxListedLicense;
import org.spdx.licenseTemplate.InvalidLicenseTemplateException;
import org.spdx.licenseTemplate.SpdxLicenseTemplateHelper;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;

/**
 * Generates HTML fragments with formatted license information
 *
 * @author Gary O'Neall
 *
 */
public class LicenseHtmlFormatWriter implements ILicenseFormatWriter {

	static final Set<Character> INVALID_FILENAME_CHARS = new HashSet<>();

	static {

		INVALID_FILENAME_CHARS.add('\\'); INVALID_FILENAME_CHARS.add('/'); INVALID_FILENAME_CHARS.add('*');
		INVALID_FILENAME_CHARS.add('<'); INVALID_FILENAME_CHARS.add('>'); INVALID_FILENAME_CHARS.add('[');
		INVALID_FILENAME_CHARS.add(']'); INVALID_FILENAME_CHARS.add('=');
		INVALID_FILENAME_CHARS.add(';'); INVALID_FILENAME_CHARS.add(':');
		INVALID_FILENAME_CHARS.add('\''); INVALID_FILENAME_CHARS.add('"'); INVALID_FILENAME_CHARS.add('|');
		INVALID_FILENAME_CHARS.add('\t'); INVALID_FILENAME_CHARS.add('?'); INVALID_FILENAME_CHARS.add('&');
		INVALID_FILENAME_CHARS.add('\ufffd');
	}

	private File htmlFolder;
	private Charset utf8 = Charset.forName("UTF-8");

	/**
	 * @param version License list version
	 * @param releaseDate release date for the license list
	 * @param htmlFolder Folder to output the HTML files
	 */
	public LicenseHtmlFormatWriter(String version, String releaseDate, File htmlFolder) {
		this.htmlFolder = htmlFolder;
	}

	/**
	 * @return the htmlFolder
	 */
	public File getHtmlFolder() {
		return htmlFolder;
	}

	/**
	 * @param htmlFolder the htmlFolder to set
	 */
	public void setHtmlFolder(File htmlFolder) {
		this.htmlFolder = htmlFolder;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#addLicense(org.spdx.rdfparser.license.SpdxListedLicense, boolean)
	 */
	@Override
	public void writeLicense(ListedLicenseContainer licenseContainer, boolean deprecated, String deprecatedVersion) throws IOException, InvalidSPDXAnalysisException {
		SpdxListedLicense license = licenseContainer.getV2ListedLicense();
		String licBaseHtmlFileName = formLicenseHTMLFileName(license.getLicenseId());
		String licHtmlFileName = licBaseHtmlFileName + ".html";
		File htmlTextFile = new File(htmlFolder.getPath() + File.separator + licHtmlFileName);
		try {
			Files.write(htmlTextFile.toPath(), license.getLicenseTextHtml().getBytes(utf8));
		} catch (InvalidLicenseTemplateException e) {
			Files.write(htmlTextFile.toPath(), SpdxLicenseTemplateHelper.formatEscapeHTML(license.getLicenseText()).getBytes(utf8));
		}
	}

	/**
	 * @param id
	 * @return HTML file name based on the license or exception ID replacing any invalid characters
	 */
	public static String formLicenseHTMLFileName(String id) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < id.length(); i++) {
			if (INVALID_FILENAME_CHARS.contains(id.charAt(i))) {
				sb.append('_');
			} else {
				sb.append(id.charAt(i));
			}
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeToC()
	 */
	@Override
	public void writeToC() throws IOException {
		// No table of contents - do nothing

	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeException(org.spdx.rdfparser.license.LicenseException, boolean, java.lang.String)
	 */
	@Override
	public void writeException(ListedExceptionContainer exceptionContainer)
			throws IOException, InvalidSPDXAnalysisException {
		ListedLicenseException exception = exceptionContainer.getV2Exception();
		String exceptionHtmlFileName = formLicenseHTMLFileName(exception.getLicenseExceptionId());
		File htmlTextFile = new File(htmlFolder.getPath() + File.separator + exceptionHtmlFileName + ".html");
		Files.write(htmlTextFile.toPath(), exception.getExceptionTextHtml().getBytes(utf8));
	}

}
