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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.crossref.CrossRefHelper;
import org.spdx.rdfparser.license.LicenseRestrictionException;
import org.spdx.rdfparser.license.ListedLicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;
import org.spdx.rdfparser.license.SpdxListedLicenseException;
import org.spdx.spdxspreadsheet.SpreadsheetException;

/**
 * Provide license information from XML files
 * @author Gary O'Neall
 *
 */
public class XmlLicenseProviderWithCrossRefDetails extends XmlLicenseProvider {

	/**
	 * Number of concurrent threads for processing cross reference license details
	 */
	private static final int NUMBER_THREADS = 10;
	
	Logger logger = LoggerFactory.getLogger(XmlLicenseProviderWithCrossRefDetails.class.getName());

	class XmlLicenseIterator extends XmlLicenseProvider.XmlLicenseIterator {
		private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);
		private Map<SpdxListedLicense, Future<String[]>> urlDetailsInProgress = new HashMap<>();
		
		private void fillCrossRefPool() {
			while (super.hasNext() && urlDetailsInProgress.size() < NUMBER_THREADS) {
				SpdxListedLicense nextLicense = super.next();
				urlDetailsInProgress.put(nextLicense, executorService.submit(new CrossRefHelper(nextLicense)));
			}
		}

		public XmlLicenseIterator() {
			super();
			fillCrossRefPool();
		}
		
		@Override
		public synchronized boolean hasNext() {
			return urlDetailsInProgress.size() > 0;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public synchronized SpdxListedLicense next() {
			Entry<SpdxListedLicense, Future<String[]>> readyLicense = null;
			for (Entry<SpdxListedLicense, Future<String[]>> licenseInProgress:urlDetailsInProgress.entrySet()) {
				if (licenseInProgress.getValue().isDone()) {
					readyLicense = licenseInProgress;
					break;
				}
			}
			
			if (Objects.isNull(readyLicense)) {
				// everything is busy - we'll just pick the first element and wait
				readyLicense = urlDetailsInProgress.entrySet().iterator().next();
				if (Objects.isNull(readyLicense)) {
					// hmmm - guess there isn't any more left
					return null;
				}
			}
			
			SpdxListedLicense retval = readyLicense.getKey();
			try {
				retval.setCrossRef(readyLicense.getValue().get());
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error getting URL value.  URL values will not be filled in for license ID "+retval.getLicenseId(),e);
				warnings.add("Error getting URL value.  URL values will not be filled in for license ID "+retval.getLicenseId());
			}
			urlDetailsInProgress.remove(retval);
			fillCrossRefPool();
			return retval;
		}
	}

	class XmlExceptionIterator extends XmlLicenseProvider.XmlExceptionIterator {
		public XmlExceptionIterator() {
			
		}
	}

	/**
	 * @param xmlFileDirectory directory of XML files
	 * @throws SpdxListedLicenseException
	 */
	public XmlLicenseProviderWithCrossRefDetails(File xmlFileDirectory) throws SpdxListedLicenseException {
		super(xmlFileDirectory);
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
}
