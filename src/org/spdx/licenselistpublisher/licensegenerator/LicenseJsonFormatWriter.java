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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.license.ListedLicenseException;
import org.spdx.library.model.v2.license.SpdxListedLicense;
import org.spdx.licenseTemplate.InvalidLicenseTemplateException;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;
import org.spdx.storage.listedlicense.ExceptionJson;
import org.spdx.storage.listedlicense.LicenseJson;
import org.spdx.storage.listedlicense.LicenseJsonTOC;
import org.spdx.storage.listedlicense.SortableExceptionJsonTOC;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Writes JSON format license information
 * @author Gary O'Neall
 *
 */
public class LicenseJsonFormatWriter implements ILicenseFormatWriter {

	static final String LICENSE_TOC_JSON_FILE_NAME = "licenses.json";
	static final String EXCEPTION_JSON_TOC_FILE_NAME = "exceptions.json";

	private File jsonFolder;
	private File jsonFolderExceptions;
	private File jsonFolderDetails;
	LicenseJson licJson;
	LicenseJsonTOC tableOfContentsJSON;
	SortableExceptionJsonTOC jsonExceptionToc;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * @param version License list version
	 * @param releaseDate release date for the license list
	 * @param jsonFolder Folder to output the main JSON file
	 * @param jsonFolderDetails Folder to output a detailed JSON file per license
	 * @param jsonFolderExceptions Folder to output a detailed JSON file per exception
	 */
	public LicenseJsonFormatWriter(String version, String releaseDate,
			File jsonFolder, File jsonFolderDetails, File jsonFolderExceptions) {
		this.jsonFolder = jsonFolder;
		this.jsonFolderDetails = jsonFolderDetails;
		this.jsonFolderExceptions = jsonFolderExceptions;
		licJson = new LicenseJson();
		tableOfContentsJSON = new LicenseJsonTOC(version, releaseDate);
		jsonExceptionToc = new SortableExceptionJsonTOC(version, releaseDate);
	}

	/**
	 * @return the jsonFolder
	 */
	public File getJsonFolder() {
		return jsonFolder;
	}

	/**
	 * @param jsonFolder the jsonFolder to set
	 */
	public void setJsonFolder(File jsonFolder) {
		this.jsonFolder = jsonFolder;
	}

	/**
	 * @return the jsonFolderExceptions
	 */
	public File getJsonFolderExceptions() {
		return jsonFolderExceptions;
	}

	/**
	 * @param jsonFolderExceptions the jsonFolderExceptions to set
	 */
	public void setJsonFolderExceptions(File jsonFolderExceptions) {
		this.jsonFolderExceptions = jsonFolderExceptions;
	}

	/**
	 * @return the jsonFolderDetails
	 */
	public File getJsonFolderDetails() {
		return jsonFolderDetails;
	}

	/**
	 * @param jsonFolderDetails the jsonFolderDetails to set
	 */
	public void setJsonFolderDetails(File jsonFolderDetails) {
		this.jsonFolderDetails = jsonFolderDetails;
	}


	@Override
	public void writeLicense(ListedLicenseContainer licenseContainer, boolean deprecated, String deprecatedVersion) 
			throws IOException, InvalidSPDXAnalysisException, InvalidLicenseTemplateException {
		SpdxListedLicense license = licenseContainer.getV2ListedLicense();
		licJson.copyFrom(license);
		String licBaseHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(license.getLicenseId());
		String licHtmlFileName = licBaseHtmlFileName + ".html";
		String licJsonFileName = licBaseHtmlFileName + ".json";
		String licHTMLReference = "./"+licHtmlFileName;
		String licJSONReference = "./"+licJsonFileName;
		File licJsonFile = new File(jsonFolder.getPath()+File.separator+"details"+File.separator+licJsonFileName);
		writeToFile(licJsonFile, licJson);
		tableOfContentsJSON.addLicense(license, licHTMLReference, licJSONReference, deprecated);
	}

	/**
	 * Serializes a Gson compatible POJO class to a file
	 * @param file File to write to
	 * @param jsonSerializableObject Object to serialize as a JSON file
	 * @throws IOException 
	 */
	private void writeToFile(File file, Object jsonSerializableObject) throws IOException {
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
			writer.write(gson.toJson(jsonSerializableObject));
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeToC()
	 */
	@Override
	public void writeToC() throws IOException {
		File tocJsonFile = new File(jsonFolder.getPath()+File.separator+LICENSE_TOC_JSON_FILE_NAME);
		Collections.sort(tableOfContentsJSON.getLicenses(), new Comparator<LicenseJsonTOC.LicenseJson>() {

		@Override
		public int compare(
				org.spdx.storage.listedlicense.LicenseJsonTOC.LicenseJson o1,
				org.spdx.storage.listedlicense.LicenseJsonTOC.LicenseJson o2) {
			return o1.getLicenseId().compareToIgnoreCase(o2.getLicenseId());
		}
		
	});
		writeToFile(tocJsonFile, tableOfContentsJSON);
		File exceptionJsonTocFile = new File(jsonFolder.getPath()+File.separator+EXCEPTION_JSON_TOC_FILE_NAME);
		jsonExceptionToc.sortExceptions();
		writeToFile(exceptionJsonTocFile, jsonExceptionToc);
	}

	@Override
	public void writeException(ListedExceptionContainer exceptionContainer)
			throws IOException, InvalidSPDXAnalysisException {
		ListedLicenseException exception = exceptionContainer.getV2Exception();
		String exceptionHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		String exceptionJsonFileName = exceptionHtmlFileName + ".json";
		String exceptionJSONReference= "./" + exceptionJsonFileName;
		String exceptionHTMLReference = "./"+exceptionHtmlFileName + ".html";
		ExceptionJson exceptionJson = new ExceptionJson();
		jsonExceptionToc.addException(exception, exceptionHTMLReference, exceptionJSONReference, exception.isDeprecated());
		exceptionJson.copyFrom(exception);
		File exceptionJsonFile = new File(jsonFolder.getPath() + File.separator + "exceptions" + File.separator +  exceptionJsonFileName);
		writeToFile(exceptionJsonFile, exceptionJson);
	}



}
