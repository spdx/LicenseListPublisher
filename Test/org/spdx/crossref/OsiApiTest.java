/**
 * SpdxLicenseIdentifier: Apache-2.0
 * 
 * Copyright (c) 2022 Source Auditor Inc.
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
package org.spdx.crossref;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.core.DefaultModelStore;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxModelFactory;
import org.spdx.library.model.v2.license.CrossRef;
import org.spdx.library.model.v2.license.SpdxListedLicense;
import org.spdx.storage.simple.InMemSpdxStore;

/**
 * @author Gary O'Neall
 *
 */
public class OsiApiTest {
	
	private static final String DOC_URI = "https://mydoc.uri";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		SpdxModelFactory.init();
		DefaultModelStore.initialize(new InMemSpdxStore(), DOC_URI, new ModelCopyManager());
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws InvalidSPDXAnalysisException {
		SpdxListedLicense apache20 = new SpdxListedLicense("Apache-2.0");
		apache20.setLicenseText("Apache text");
		CrossRef osiApacheCrossRef = new CrossRef();
		String apache20OsiUrl = "https://opensource.org/licenses/Apache-2.0";
		osiApacheCrossRef.setUrl(apache20OsiUrl);
		OsiApi instance = OsiApi.getInstance();
		assertTrue(instance.isApiAvailable());
		assertTrue(OsiApi.isOsiUrl(apache20OsiUrl));
		instance.setCrossRefDetails(apache20OsiUrl, apache20, osiApacheCrossRef);
		assertFalse(osiApacheCrossRef.getIsWayBackLink().get());
		assertTrue(osiApacheCrossRef.getLive().get());
		assertEquals("N/A", osiApacheCrossRef.getMatch().get());
		assertTrue(osiApacheCrossRef.getTimestamp().isPresent());
		assertEquals(apache20OsiUrl, osiApacheCrossRef.getUrl().get());
		assertTrue(osiApacheCrossRef.getValid().get());
		
		// not OSI URL
		String nonOsiUrl = "https://notopensource.org/licenses/Apache-2.0";
		assertFalse(OsiApi.isOsiUrl(nonOsiUrl));
		
		// not matching URL
		CrossRef notMatchingCr = new CrossRef();
		String notTheUrl = "https://opensource.org/licenses/MIT";
		notMatchingCr.setUrl(notTheUrl);
		assertTrue(OsiApi.isOsiUrl(notTheUrl));
		instance.setCrossRefDetails(notTheUrl, apache20, notMatchingCr);
		assertFalse(notMatchingCr.getIsWayBackLink().get());
		assertFalse(notMatchingCr.getLive().get());
		assertEquals("N/A", notMatchingCr.getMatch().get());
		assertTrue(notMatchingCr.getTimestamp().isPresent());
		assertEquals(notTheUrl, notMatchingCr.getUrl().get());
		assertTrue(notMatchingCr.getValid().get());
	}

}
