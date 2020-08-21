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

import org.spdx.htmltemplates.ExceptionHtml;
import org.spdx.htmltemplates.ExceptionHtmlToc;
import org.json.simple.JSONArray;
import org.spdx.html.ExceptionTOCJSONFile;
import org.spdx.html.InvalidLicenseTemplateException;
import org.spdx.html.LicenseExceptionJSONFile;
import org.spdx.htmltemplates.LicenseHTMLFile;
import org.spdx.html.LicenseJSONFile;
import org.spdx.htmltemplates.LicenseTOCHTMLFile;
import org.spdx.html.LicenseTOCJSONFile;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.LicenseException;
import org.spdx.rdfparser.license.ListedLicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.licenselistpublisher.LicenseContainer;
import org.spdx.licenselistpublisher.LicenseGeneratorException;

import com.github.mustachejava.MustacheException;

/**
 * Writer to format all files for the https://spdx.org/licenses website
 * @author Gary O'Neall
 *
 */
public class SpdxWebsiteFormatWriter implements ILicenseFormatWriter {

	static final String LICENSE_TOC_JSON_FILE_NAME = "licenses.json";
	static final String LICENSE_TOC_HTML_FILE_NAME = "index.html";
	static final String EXCEPTION_TOC_FILE_NAME = "exceptions-index.html";
	static final String EXCEPTION_JSON_TOC_FILE_NAME = "exceptions.json";

	private File websiteFolder;
	private LicenseHTMLFile licHtml;
	private LicenseTOCHTMLFile tableOfContentsHTML;
	LicenseJSONFile licJson;
	LicenseTOCJSONFile tableOfContentsJSON;
	String exceptionHtmlTocReference = "./" + EXCEPTION_TOC_FILE_NAME;
	ExceptionHtmlToc htmlExceptionToc;
	ExceptionTOCJSONFile jsonExceptionToc;
	private String version;
	private String releaseDate;

	/**
	 * @param version License list version
	 * @param releaseDate release date for the license list
	 * @param websiteFolder Folder to store all of the website files
	 */
	public SpdxWebsiteFormatWriter(String version, String releaseDate, File websiteFolder) {
		this.websiteFolder = websiteFolder;
		this.tableOfContentsHTML = new LicenseTOCHTMLFile(version, releaseDate);
		this.licHtml = new LicenseHTMLFile();
		licJson = new LicenseJSONFile();
		tableOfContentsJSON = new LicenseTOCJSONFile(version, releaseDate);
		htmlExceptionToc = new ExceptionHtmlToc();
		jsonExceptionToc = new ExceptionTOCJSONFile(version, releaseDate);
		this.version = version;
		this.releaseDate = releaseDate;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}



	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}



	/**
	 * @return the releaseDate
	 */
	public String getReleaseDate() {
		return releaseDate;
	}



	/**
	 * @param releaseDate the releaseDate to set
	 */
	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}



	/**
	 * @return the licJson
	 */
	public LicenseJSONFile getLicJson() {
		return licJson;
	}



	/**
	 * @return the tableOfContentsJSON
	 */
	public LicenseTOCJSONFile getTableOfContentsJSON() {
		return tableOfContentsJSON;
	}



	/**
	 * @return the htmlExceptionToc
	 */
	public ExceptionHtmlToc getHtmlExceptionToc() {
		return htmlExceptionToc;
	}



	/**
	 * @return the jsonExceptionToc
	 */
	public ExceptionTOCJSONFile getJsonExceptionToc() {
		return jsonExceptionToc;
	}



	/**
	 * @return the websiteFolder
	 */
	public File getWebsiteFolder() {
		return websiteFolder;
	}

	/**
	 * @param websiteFolder the websiteFolder to set
	 */
	public void setWebsiteFolder(File websiteFolder) {
		this.websiteFolder = websiteFolder;
	}

	/**
	 * @return the licHtml
	 */
	public LicenseHTMLFile getLicHtml() {
		return licHtml;
	}

	/**
	 * @param licHtml the licHtml to set
	 */
	public void setLicHtml(LicenseHTMLFile licHtml) {
		this.licHtml = licHtml;
	}

	/**
	 * @return the tableOfContentsHTML
	 */
	public LicenseTOCHTMLFile getTableOfContentsHTML() {
		return tableOfContentsHTML;
	}

	/**
	 * @param tableOfContentsHTML the tableOfContentsHTML to set
	 */
	public void setTableOfContentsHTML(LicenseTOCHTMLFile tableOfContentsHTML) {
		this.tableOfContentsHTML = tableOfContentsHTML;
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#addLicense(org.spdx.rdfparser.license.SpdxListedLicense, boolean)
	 */
	@Override
	public void writeLicense(SpdxListedLicense license, boolean deprecated, String deprecatedVersion) throws IOException, LicenseGeneratorException {
		this.licHtml.setLicense(license);
		String licBaseHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(license.getLicenseId());
		String licHtmlFileName = licBaseHtmlFileName + ".html";
		String licJsonFileName = licBaseHtmlFileName + ".json";
		String licHTMLReference = "./"+licHtmlFileName;
		String licJSONReference = "./"+licJsonFileName;
		String tocHTMLReference = "./"+LICENSE_TOC_HTML_FILE_NAME;
		// the base file is used for direct references from tools, the html is used for rendering by the website
		File licBaseHtmlFile = new File(websiteFolder.getPath()+File.separator+licBaseHtmlFileName);
		File licJsonFile = new File(websiteFolder.getPath()+File.separator+licJsonFileName);
		File licHtmlFile = new File(websiteFolder.getPath()+File.separator+licHtmlFileName);
		try {
			licHtml.writeToFile(licBaseHtmlFile, tocHTMLReference);
			licHtml.writeToFile(licHtmlFile, tocHTMLReference);
		} catch (MustacheException e) {
			throw new LicenseGeneratorException("Template33 error for license HTML file: "+e.getMessage(),e);
		} catch (InvalidLicenseTemplateException e) {
			throw new LicenseGeneratorException("License template error for license HTML file: "+e.getMessage(),e);
		}
		licJson.setLicense(license, deprecated);
		licJson.writeToFile(licJsonFile);
		tableOfContentsJSON.addLicense(license, licHTMLReference, licJSONReference, deprecated);
		if (deprecated) {
			tableOfContentsHTML.addDeprecatedLicense(license, licHTMLReference);
		} else {
			tableOfContentsHTML.addLicense(license, licHTMLReference);
		}
		// JSON-LD format
		LicenseContainer onlyThisLicense = new LicenseContainer();
		AnyLicenseInfo licenseClone = license.clone();
		try {
			licenseClone.createResource(onlyThisLicense);
		} catch (InvalidSPDXAnalysisException e) {
			throw new LicenseGeneratorException("SPDX Analysis error cloning license: "+e.getMessage(),e);
		}
		LicenseRdfFormatWriter.writeRdf(onlyThisLicense, websiteFolder.getPath() + File.separator + licBaseHtmlFileName + ".jsonld", "JSON-LD");
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeToC()
	 */
	@Override
	public void writeToC() throws IOException {
		File tocJsonFile = new File(websiteFolder.getPath()+File.separator+LICENSE_TOC_JSON_FILE_NAME);
		File tocHtmlFile = new File(websiteFolder.getPath()+File.separator+LICENSE_TOC_HTML_FILE_NAME);
		File exceptionTocFile = new File(websiteFolder.getPath()+File.separator+EXCEPTION_TOC_FILE_NAME);
		tableOfContentsJSON.writeToFile(tocJsonFile);
		tableOfContentsHTML.writeToFile(tocHtmlFile);
		htmlExceptionToc.writeToFile(exceptionTocFile, this.version);
		File exceptionJsonTocFile = new File(websiteFolder.getPath()+File.separator+EXCEPTION_JSON_TOC_FILE_NAME);
		jsonExceptionToc.writeToFile(exceptionJsonTocFile);
	}

	@Override
	public void writeException(ListedLicenseException exception)
			throws IOException, InvalidLicenseTemplateException, LicenseGeneratorException {
		ExceptionHtml exceptionHtml = new ExceptionHtml(exception);
		String exceptionHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		String exceptionHTMLReference = "./"+exceptionHtmlFileName + ".html";
		String exceptionJsonFileName = exceptionHtmlFileName + ".json";
		String exceptionJSONReference= "./" + exceptionJsonFileName;
		File exceptionHtmlFile = new File(websiteFolder.getPath()+File.separator+exceptionHtmlFileName + ".html");
		exceptionHtml.writeToFile(exceptionHtmlFile, exceptionHtmlTocReference);
		if (exception.isDeprecated()) {
			htmlExceptionToc.addDeprecatedException(exception, exceptionHTMLReference, exception.getDeprecatedVersion());
		} else {
			htmlExceptionToc.addException(exception, exceptionHTMLReference);
		}
		jsonExceptionToc.addException(exception, exceptionHTMLReference, exceptionJSONReference, exception.isDeprecated());
		LicenseExceptionJSONFile exceptionJson = new LicenseExceptionJSONFile();
		exceptionJson.setException(exception, exception.isDeprecated());
		File exceptionJsonFile = new File(websiteFolder.getPath() + File.separator + exceptionJsonFileName);
		exceptionJson.writeToFile(exceptionJsonFile);
		// JSON-LD format
		LicenseException exceptionClone = exception.clone();
		LicenseContainer onlyThisException = new LicenseContainer();
		try {
			exceptionClone.createResource(onlyThisException);
		} catch (InvalidSPDXAnalysisException e) {
			throw new LicenseGeneratorException("SPDX Analysis error cloning exception: "+e.getMessage(),e);
		}
		LicenseRdfFormatWriter.writeRdf(onlyThisException, websiteFolder.getPath() + File.separator + exceptionHtmlFileName + ".jsonld", "JSON-LD");
	}
}
