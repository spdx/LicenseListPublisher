/**
 * SpdxLicenseIdentifier: Apache-2.0
 *
 * Copyright (c) 2019 Source Auditor Inc.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 *
 * @author Gary O'Neall
 *
 */
public class XmlLicenseProviderSingleFile implements ISpdxListedLicenseProvider {

	Logger logger = LoggerFactory.getLogger(XmlLicenseProviderSingleFile.class.getName());
	private List<String> warnings = new ArrayList<String>();
	LicenseXmlDocument licDoc = null;
	protected IModelStore v2ModelStore = new InMemSpdxStore();
	protected IModelStore v3ModelStore = new InMemSpdxStore();
	protected IModelCopyManager copyManager = new ModelCopyManager();

	public XmlLicenseProviderSingleFile(File licenseXmlFile, String currentListVersion, String releaseDate) throws LicenseXmlException, InvalidSPDXAnalysisException {
		licDoc = new LicenseXmlDocument(licenseXmlFile, v2ModelStore, v3ModelStore, copyManager, 
				XmlLicenseProvider.createCreationInfo(v3ModelStore, copyManager, releaseDate, currentListVersion));
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getLicenseIterator()
	 */
	@Override
	public Iterator<ListedLicenseContainer> getLicenseIterator() throws SpdxListedLicenseException {
		try {
			return licDoc.getListedLicenses().iterator();
		} catch (InvalidSPDXAnalysisException e) {
			logger.error("SPDX Analysis exception getting license iterator",e);
			throw new SpdxListedLicenseException("SPDX Analysis exception getting license iterator",e);
		} catch (LicenseXmlException e) {
			logger.error("License XML exception getting license iterator",e);
			throw new SpdxListedLicenseException("Invalid License XML Document",e);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getExceptionIterator()
	 */
	@Override
	public Iterator<ListedExceptionContainer> getExceptionIterator() throws InvalidSPDXAnalysisException {
		try {
			return licDoc.getLicenseExceptions().iterator();
		} catch (LicenseXmlException e) {
			logger.error("License XML exception getting license iterator",e);
			throw new RuntimeException(new SpdxListedLicenseException("Invalid License XML Document",e));
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.ISpdxListedLicenseProvider#getWarnings()
	 */
	@Override
	public List<String> getWarnings() {
		return warnings;
	}

}
