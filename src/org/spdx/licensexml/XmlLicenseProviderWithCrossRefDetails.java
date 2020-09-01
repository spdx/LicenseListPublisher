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
import org.spdx.crossref.CrossRefHelper;
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
public class XmlLicenseProviderWithCrossRefDetails extends XmlLicenseProvider {

	Logger logger = LoggerFactory.getLogger(XmlLicenseProviderWithCrossRefDetails.class.getName());
	private List<String> warnings = new ArrayList<String>();

	class XmlLicenseIterator extends XmlLicenseProvider.XmlLicenseIterator {

		public XmlLicenseIterator() {
			super();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public SpdxListedLicense next() {
			SpdxListedLicense retval = this.nextListedLicense;
			retval.setCrossRef(CrossRefHelper.buildUrlDetails(retval));
			this.findNextItem();
			return retval;
		}
	}

	class XmlExceptionIterator extends XmlLicenseProvider.XmlExceptionIterator {
		public XmlExceptionIterator() {
		}
	}

	private List<File> xmlFiles = new ArrayList<File>();

	/**
	 * @param xmlFileDirectory directory of XML files
	 * @throws SpdxListedLicenseException
	 */
	public XmlLicenseProviderWithCrossRefDetails(File xmlFileDirectory) throws SpdxListedLicenseException {
		super(xmlFileDirectory);
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
