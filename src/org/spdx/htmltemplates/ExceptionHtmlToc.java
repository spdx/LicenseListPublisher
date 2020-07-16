/**
 * Copyright (c) 2014 Source Auditor Inc.
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
package org.spdx.htmltemplates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.spdx.rdfparser.license.LicenseException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Generates the HTML Table of Contents for License Exceptions
 * @author Gary O'Neall
 *
 */
public class ExceptionHtmlToc {
	static final String TEMPLATE_CLASS_PATH = "resources" + "/" + "htmlTemplate";
	static final String TEMPLATE_ROOT_PATH = "resources" + File.separator + "htmlTemplate";
	static final String HTML_TEMPLATE = "ExceptionsTocHTMLTemplate.html";

	public static class DeprecatedExceptionRow {
		private int refNumber;
		String licenseExceptionId;
		private String reference;
		private String exceptionName;
		private String deprecatedVersion;

		public DeprecatedExceptionRow(String licenseExceptionId, String exceptionName,
				int refNumber, String reference, String deprecatedVersion) {
			this.licenseExceptionId = licenseExceptionId;
			this.exceptionName = exceptionName;
			this.refNumber = refNumber;
			this.reference = reference;
			this.deprecatedVersion = deprecatedVersion;
		}

		/**
		 * @return the licenseExceptionId
		 */
		public String getLicenseExceptionId() {
			return licenseExceptionId;
		}


		/**
		 * @param licenseExceptionId the licenseExceptionId to set
		 */
		public void setLicenseExceptionId(String licenseExceptionId) {
			this.licenseExceptionId = licenseExceptionId;
		}


		/**
		 * @return the refNumber
		 */
		public int getRefNumber() {
			return refNumber;
		}

		/**
		 * @param refNumber the refNumber to set
		 */
		public void setRefNumber(int refNumber) {
			this.refNumber = refNumber;
		}

		/**
		 * @return the reference
		 */
		public String getReference() {
			return reference;
		}

		/**
		 * @param reference the reference to set
		 */
		public void setReference(String reference) {
			this.reference = reference;
		}

		/**
		 * @return the exceptionName
		 */
		public String getExceptionName() {
			return exceptionName;
		}

		/**
		 * @param exceptionName the exceptionName to set
		 */
		public void setExceptionName(String exceptionName) {
			this.exceptionName = exceptionName;
		}

		/**
		 * @return the deprecatedVersion
		 */
		public String getDeprecatedVersion() {
			return deprecatedVersion;
		}

		/**
		 * @param deprecatedVersion the deprecatedVersion to set
		 */
		public void setDeprecatedVersion(String deprecatedVersion) {
			this.deprecatedVersion = deprecatedVersion;
		}
	}

	/**
	 * Holds the data one of the list rows of exceptions
	 * @author Gary O'Neall
	 *
	 */
	public static class ExceptionRow {
		private int refNumber;
		private String reference;
		private String exceptionName;
		/**
		 * @return the refNumber
		 */
		public int getRefNumber() {
			return refNumber;
		}

		/**
		 * @param refNumber the refNumber to set
		 */
		public void setRefNumber(int refNumber) {
			this.refNumber = refNumber;
		}

		/**
		 * @return the reference
		 */
		public String getReference() {
			return reference;
		}

		/**
		 * @param reference the reference to set
		 */
		public void setReference(String reference) {
			this.reference = reference;
		}

		/**
		 * @return the exceptionName
		 */
		public String getExceptionName() {
			return exceptionName;
		}

		/**
		 * @param exceptionName the exceptionName to set
		 */
		public void setExceptionName(String exceptionName) {
			this.exceptionName = exceptionName;
		}

		/**
		 * @return the licenseExceptionId
		 */
		public String getLicenseExceptionId() {
			return licenseExceptionId;
		}

		/**
		 * @param licenseExceptionId the licenseExceptionId to set
		 */
		public void setLicenseExceptionId(String licenseExceptionId) {
			this.licenseExceptionId = licenseExceptionId;
		}

		private String licenseExceptionId;

		public ExceptionRow(String licenseExceptionId, String exceptionName,
				int refNumber, String reference) {
			this.licenseExceptionId = licenseExceptionId;
			this.exceptionName = exceptionName;
			this.refNumber = refNumber;
			this.reference = reference;
		}
	}

	List<ExceptionRow> exceptions = Lists.newArrayList();
	List<DeprecatedExceptionRow> deprecatedExceptions = Lists.newArrayList();

	int currentRefNum = 1;

	/**
	 * Add an exception to the table of contents
	 * @param exception
	 * @param exceptionHTMLReference
	 */
	public void addException(LicenseException exception,
			String exceptionHTMLReference) {
		exceptions.add(new ExceptionRow(
				StringEscapeUtils.escapeHtml4(exception.getLicenseExceptionId()),
				StringEscapeUtils.escapeHtml4(exception.getName()),
				currentRefNum++, exceptionHTMLReference));
	}

	public void addDeprecatedException(LicenseException exception,
			String exceptionHTMLReference, String deprecatedVersion) {
		deprecatedExceptions.add(new DeprecatedExceptionRow(
				StringEscapeUtils.escapeHtml4(exception.getLicenseExceptionId()),
				StringEscapeUtils.escapeHtml4(exception.getName()),
				currentRefNum++, exceptionHTMLReference, deprecatedVersion));
	}

	/**
	 * Creates and writes an Exception Table of Contents file
	 * @param exceptionTocFile file to write to
	 * @param version Version of the License List
	 * @throws IOException
	 * @throws MustacheException
	 */
	public void writeToFile(File exceptionTocFile, String version) throws MustacheException, IOException {

		Map<String, Object> mustacheMap = Maps.newHashMap();
		mustacheMap.put("version", StringEscapeUtils.escapeHtml4(version));
		exceptions.sort(new Comparator<ExceptionRow>() {

			@Override
			public int compare(ExceptionRow arg0, ExceptionRow arg1) {
				return arg0.getLicenseExceptionId().compareTo(arg1.getLicenseExceptionId());
			}

		});
		mustacheMap.put("listedExceptions", exceptions);
		mustacheMap.put("deprecatedExceptions", deprecatedExceptions);
		FileOutputStream stream = null;
		OutputStreamWriter writer = null;
		if (!exceptionTocFile.exists()) {
			if (!exceptionTocFile.createNewFile()) {
				throw(new IOException("Can not create new file "+exceptionTocFile.getName()));
			}
		}
		String templateDirName = TEMPLATE_ROOT_PATH;
		File templateDirectoryRoot = new File(templateDirName);
		if (!(templateDirectoryRoot.exists() && templateDirectoryRoot.isDirectory())) {
			templateDirName = TEMPLATE_CLASS_PATH;
		}
		try {
			stream = new FileOutputStream(exceptionTocFile);
			writer = new OutputStreamWriter(stream, "UTF-8");
			DefaultMustacheFactory builder = new DefaultMustacheFactory(templateDirName);
			Mustache mustache = builder.compile(HTML_TEMPLATE);
	        mustache.execute(writer, mustacheMap);
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (stream != null) {
				stream.close();
			}
		}
	}

}
