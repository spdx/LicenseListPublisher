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

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.license.ListedLicenseException;
import org.spdx.library.model.v2.license.SpdxListedLicense;
import org.spdx.licenselistpublisher.LicenseGeneratorException;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;

/**
 * Write license template format as described in the SPDX spec
 * @author Gary O'Neall
 *
 */
public class LicenseTemplateFormatWriter implements ILicenseFormatWriter {

	private File templateFolder;
	private Charset utf8 = Charset.forName("UTF-8");

	/**
	 * @param templateFolder Folder containing the template files
	 */
	public LicenseTemplateFormatWriter(File templateFolder) {
		this.templateFolder = templateFolder;
	}

	/**
	 * @return the templateFolder
	 */
	public File getTemplateFolder() {
		return templateFolder;
	}

	/**
	 * @param templateFolder the templateFolder to set
	 */
	public void setTemplateFolder(File templateFolder) {
		this.templateFolder = templateFolder;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeLicense(org.spdx.rdfparser.license.SpdxListedLicense, boolean)
	 */
	@Override
	public void writeLicense(ListedLicenseContainer licenseContainer, boolean deprecated, 
			String deprecatedVersion) throws IOException, InvalidSPDXAnalysisException {
		SpdxListedLicense license = licenseContainer.getV2ListedLicense();
		String licBaseHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(license.getLicenseId());
		if (deprecated) {
			licBaseHtmlFileName = "deprecated_" + licBaseHtmlFileName;
		}
		File templateFile = new File(templateFolder.getPath() + File.separator + licBaseHtmlFileName + ".template.txt");
		if (license.getStandardLicenseTemplate() != null && !license.getStandardLicenseTemplate().trim().isEmpty()) {
			Files.write(templateFile.toPath(), license.getStandardLicenseTemplate().getBytes(utf8));
		} else {
			Files.write(templateFile.toPath(), license.getLicenseText().getBytes(utf8));
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeToC()
	 */
	@Override
	public void writeToC() throws IOException, LicenseGeneratorException {
		// Nothing to write - no ToC

	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeException(org.spdx.rdfparser.license.LicenseException, boolean, java.lang.String)
	 */
	@Override
	public void writeException(ListedExceptionContainer exceptionContainer)
			throws IOException, LicenseGeneratorException, InvalidSPDXAnalysisException {
		ListedLicenseException exception = exceptionContainer.getV2Exception();
		String licBaseHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		if (exception.isDeprecated()) {
			licBaseHtmlFileName = "deprecated_" + licBaseHtmlFileName;
		}
		File templateFile = new File(templateFolder.getPath() + File.separator + licBaseHtmlFileName + ".template.txt");
		Files.write(templateFile.toPath(), exception.getLicenseExceptionTemplate().getBytes(utf8));
	}
}
