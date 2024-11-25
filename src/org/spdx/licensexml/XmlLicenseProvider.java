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
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.compress.utils.FileNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.core.IModelCopyManager;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.model.v2.license.SpdxListedLicenseException;
import org.spdx.licenselistpublisher.ISpdxListedLicenseProvider;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;
import org.spdx.storage.IModelStore;
import org.spdx.storage.simple.InMemSpdxStore;

/**
 * Provide license information from XML files
 * @author Gary O'Neall
 *
 */
public class XmlLicenseProvider implements ISpdxListedLicenseProvider {

	Logger logger = LoggerFactory.getLogger(XmlLicenseProvider.class.getName());
	protected List<String> warnings = new ArrayList<String>();
	protected IModelStore v2ModelStore = new InMemSpdxStore();
	protected IModelStore v3ModelStore = new InMemSpdxStore();
	protected IModelCopyManager copyManager = new ModelCopyManager();

	class XmlLicenseIterator implements Iterator<ListedLicenseContainer> {
		private int xmlFileIndex = 0;
		protected ListedLicenseContainer nextListedLicense = null;
		private Iterator<ListedLicenseContainer> fileListedLicenseIter = null;
		private IModelCopyManager copyManager;
		private IModelStore v2ModelStore;
		private IModelStore v3ModelStore;

		public XmlLicenseIterator(IModelStore v2ModelStore, IModelStore v3ModelStore, IModelCopyManager copyManager) {
			this.v2ModelStore = v2ModelStore;
			this.v3ModelStore = v3ModelStore;
			this.copyManager = copyManager;
			findNextItem();
		}

		protected void findNextItem() {
			nextListedLicense = null;
			if (fileListedLicenseIter == null || !fileListedLicenseIter.hasNext()) {
				fileListedLicenseIter = null;
				while (xmlFileIndex < xmlFiles.size() && fileListedLicenseIter == null) {
					try {
						LicenseXmlDocument licDoc = new LicenseXmlDocument(xmlFiles.get(xmlFileIndex),
								v2ModelStore, v3ModelStore, copyManager);
						try {
							List<ListedLicenseContainer> licList = licDoc.getListedLicenses();
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
		public ListedLicenseContainer next() {
			ListedLicenseContainer retval = this.nextListedLicense;
			this.findNextItem();
			if (Objects.isNull(retval)) {
			    throw new NoSuchElementException();
			} else {
			    return retval;
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// Not implemented
		}
	}

	class XmlExceptionIterator implements Iterator<ListedExceptionContainer> {
		private int xmlFileIndex = 0;
		private ListedExceptionContainer nextLicenseException = null;
		private Iterator<ListedExceptionContainer> fileExceptionIterator = null;
		private IModelStore v2ModelStore;
		private IModelStore v3ModelStore;
		private IModelCopyManager copyManager;

		public XmlExceptionIterator(IModelStore v2ModelStore, IModelStore v3ModelStore, IModelCopyManager copyManager) throws InvalidSPDXAnalysisException {
			this.v2ModelStore = v2ModelStore;
			this.v3ModelStore = v3ModelStore;
			this.copyManager = copyManager;
			findNextItem();
		}

		private void findNextItem() throws InvalidSPDXAnalysisException {
			nextLicenseException = null;
			if (fileExceptionIterator == null || !fileExceptionIterator.hasNext()) {
				fileExceptionIterator = null;
				while (xmlFileIndex < xmlFiles.size() && fileExceptionIterator == null) {
					try {
						LicenseXmlDocument licDoc = new LicenseXmlDocument(xmlFiles.get(xmlFileIndex), v2ModelStore, v3ModelStore, copyManager);
						List<ListedExceptionContainer> exceptionList = licDoc.getLicenseExceptions();
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
		public ListedExceptionContainer next() {
			ListedExceptionContainer retval = this.nextLicenseException;
			try {
				this.findNextItem();
			} catch (InvalidSPDXAnalysisException e) {
				throw new RuntimeException(e);
			}
			if (Objects.isNull(retval)) {
			    throw new NoSuchElementException();
			} else {
			    return retval;
			}
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
				return pathname.isFile() && "xml".equals(FileNameUtils.getExtension(pathname.toPath()).toLowerCase());
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
	public Iterator<ListedLicenseContainer> getLicenseIterator()
			throws SpdxListedLicenseException {
		return new XmlLicenseIterator(v2ModelStore, v3ModelStore, copyManager);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getExceptionIterator()
	 */
	@Override
	public Iterator<ListedExceptionContainer> getExceptionIterator() throws InvalidSPDXAnalysisException {
		return new XmlExceptionIterator(v2ModelStore, v3ModelStore, copyManager);
	}

	public List<String> getWarnings() {
		return this.warnings;
	}
}
