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

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;
import org.spdx.licenselistpublisher.MarkdownTable;

/**
 * Formats MarkDown files for the licenses
 * @author Gary O'Neall
 *
 */
public class LicenseMarkdownFormatWriter implements ILicenseFormatWriter {

	private File markdownFile;
	private MarkdownTable markdownTable;

	/**
	 * @param version License list version
	 * @param releaseDate release date for the license list
	 * @param markdownFile Markdown formatted file written by this class
	 */
	public LicenseMarkdownFormatWriter(String version, String releaseDate, File markdownFile) {
		this.markdownFile = markdownFile;
		markdownTable = new MarkdownTable(version);
	}

	/**
	 * @return the markdownFile
	 */
	public File getMarkdownFile() {
		return markdownFile;
	}

	/**
	 * @param markdownFile the markdownFile to set
	 */
	public void setMarkdownFile(File markdownFile) {
		this.markdownFile = markdownFile;
	}

	@Override
	public void writeLicense(ListedLicenseContainer licenseContainer, boolean deprecated, String deprecatedVersion) throws IOException, InvalidSPDXAnalysisException {
		markdownTable.addLicense(licenseContainer.getV2ListedLicense(), deprecated);
	}

	@Override
	public void writeToC() throws IOException {
		markdownTable.writeToFile(markdownFile);
	}

	@Override
	public void writeException(ListedExceptionContainer exceptionContianer)
			throws IOException, InvalidSPDXAnalysisException {
		markdownTable.addException(exceptionContianer.getV2Exception(), exceptionContianer.getV2Exception().isDeprecated());
	}
}
