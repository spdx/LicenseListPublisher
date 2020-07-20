/**
 * Copyright (c) 2016 Source Auditor Inc.
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
package org.spdx.licensexml;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.license.ISpdxListedLicenseProvider;
import org.spdx.rdfparser.license.LicenseRestrictionException;
import org.spdx.rdfparser.license.ListedLicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.license.SpdxListedLicenseException;
import org.spdx.spdxspreadsheet.SpreadsheetException;

import com.google.common.io.Files;

/**
 * Provide license information from XML files
 * @author Gary O'Neall
 *
 */
public class XmlLicenseProvider implements ISpdxListedLicenseProvider {

	Logger logger = LoggerFactory.getLogger(XmlLicenseProvider.class.getName());
	private List<String> warnings = new ArrayList<String>();

	class XmlLicenseIterator implements Iterator<SpdxListedLicense> {
		private int xmlFileIndex = 0;
		private SpdxListedLicense nextListedLicense = null;
		private Iterator<SpdxListedLicense> fileListedLicenseIter = null;

		public XmlLicenseIterator() {
			findNextItem();
		}

		private void findNextItem() {
			nextListedLicense = null;
			if (fileListedLicenseIter == null || !fileListedLicenseIter.hasNext()) {
				fileListedLicenseIter = null;
				while (xmlFileIndex < xmlFiles.size() && fileListedLicenseIter == null) {
					try {
						LicenseXmlDocument licDoc = new LicenseXmlDocument(xmlFiles.get(xmlFileIndex));
						try {
							List<SpdxListedLicense> licList = licDoc.getListedLicenses();
							if (licList != null && !licList.isEmpty()) {
								fileListedLicenseIter = licList.iterator();
							}
						} catch (InvalidSPDXAnalysisException e) {
							warnings.add(e.getMessage() + ", Skipping file "+xmlFiles.get(xmlFileIndex).getName());
							logger.warn(e.getMessage() + ", Skipping file "+xmlFiles.get(xmlFileIndex).getName());
						}
					} catch(LicenseXmlException e) {
						warnings.add(e.getMessage() + ", Skipping file "+xmlFiles.get(xmlFileIndex).getName());
						logger.warn(e.getMessage() + ", Skipping file "+xmlFiles.get(xmlFileIndex).getName());
					} finally {
						xmlFileIndex++;
					}
				}
			}
			if (fileListedLicenseIter != null && fileListedLicenseIter.hasNext()) {
				nextListedLicense = fileListedLicenseIter.next();
			}
		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return this.nextListedLicense != null;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public SpdxListedLicense next() {
			SpdxListedLicense retval = this.nextListedLicense;
			this.findNextItem();
			return retval;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// Not implemented
		}
	}

	class XmlExceptionIterator implements Iterator<ListedLicenseException> {
		private int xmlFileIndex = 0;
		private ListedLicenseException nextLicenseException = null;
		private Iterator<ListedLicenseException> fileExceptionIterator = null;

		public XmlExceptionIterator() {
			findNextItem();
		}

		private void findNextItem() {
			nextLicenseException = null;
			if (fileExceptionIterator == null || !fileExceptionIterator.hasNext()) {
				fileExceptionIterator = null;
				while (xmlFileIndex < xmlFiles.size() && fileExceptionIterator == null) {
					try {
						LicenseXmlDocument licDoc = new LicenseXmlDocument(xmlFiles.get(xmlFileIndex));
						List<ListedLicenseException> exceptionList = licDoc.getLicenseExceptions();
						if (exceptionList != null && !exceptionList.isEmpty()) {
							fileExceptionIterator = exceptionList.iterator();
						}
					} catch(LicenseXmlException e) {
						warnings.add(e.getMessage() + ", Skipping file "+xmlFiles.get(xmlFileIndex).getName());
						logger.warn(e.getMessage() + ", Skipping file "+xmlFiles.get(xmlFileIndex).getName());
					}
					xmlFileIndex++;
				}
			}
			if (fileExceptionIterator != null && fileExceptionIterator.hasNext()) {
				nextLicenseException = fileExceptionIterator.next();
			}

		}
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return this.nextLicenseException != null;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public ListedLicenseException next() {
			ListedLicenseException retval = this.nextLicenseException;
			this.findNextItem();
			return retval;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// Not implemented
		}
	}

	private List<File> xmlFiles = new ArrayList<File>();

	/**
	 * @param xmlFileDirectory directory of XML files
	 * @throws SpdxListedLicenseException
	 */
	public XmlLicenseProvider(File xmlFileDirectory) throws SpdxListedLicenseException {
		if (!xmlFileDirectory.isDirectory()) {
			throw(new SpdxListedLicenseException("XML File Directory is not a directory"));
		}
		this.xmlFiles = new ArrayList<File>();
		addXmlFiles(xmlFileDirectory, this.xmlFiles);
	}

	/**
	 * Add all XML files in the directory and subdirectories
	 * @param xmlFileDirectory
	 * @param alFiles
	 */
	private void addXmlFiles(File xmlFileDirectory, List<File> alFiles) {

		File[] directories = xmlFileDirectory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}

		});

		if (directories != null) {
			for (File subDir:directories) {
				addXmlFiles(subDir, alFiles);
			}
		}

		File[] localFiles = xmlFileDirectory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && "xml".equals(Files.getFileExtension(pathname.getName().toLowerCase()));
			}

		});

		if (localFiles != null) {
			for (File file:localFiles) {
				alFiles.add(file);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getLicenseIterator()
	 */
	@Override
	public Iterator<SpdxListedLicense> getLicenseIterator()
			throws SpdxListedLicenseException {
		return new XmlLicenseIterator();
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getExceptionIterator()
	 */
	@Override
	public Iterator<ListedLicenseException> getExceptionIterator()
			throws LicenseRestrictionException, SpreadsheetException {
		return new XmlExceptionIterator();
	}

	public List<String> getWarnings() {
		return this.warnings;
	}
}
