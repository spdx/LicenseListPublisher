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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.core.IModelCopyManager;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxModelFactory;
import org.spdx.library.model.v2.license.LicenseException;
import org.spdx.library.model.v2.license.SpdxListedLicense;
import org.spdx.library.model.v3_0_1.SpdxConstantsV3;
import org.spdx.library.model.v3_0_1.expandedlicensing.ListedLicense;
import org.spdx.library.model.v3_0_1.expandedlicensing.ListedLicenseException;
import org.spdx.licenseTemplate.InvalidLicenseTemplateException;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;
import org.spdx.licenselistpublisher.UnitTestHelper;
import org.spdx.storage.IModelStore;
import org.spdx.storage.simple.InMemSpdxStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * @author Gary O'Neall
 *
 */
public class LicenseXmlDocumentTest {

	static final String TEST_FILE_PATH = "TestFiles" + File.separator + "test-license.xml";
	static final String TEST_OPTIONAL_FILE_PATH = "TestFiles" + File.separator + "test-optional-annotations.xml";
	private static final String TEST_LICENSE_COMMENT = "Test note";
	private static final String TEST_LICENSE_ID = "test-id";
	private static final String TEST_LICENSE_TEXT = "Test Copyright\n\nparagraph 1" +
			"\n\n   1.\n   List item 1\n\n   2.\n   List item 2\n\n" +
			"Last Paragraph Alternate Text Non matching line. Optional text\n\n";
	private static final String TEST_LICENSE_NAME = "Test License";
	private static final List<String>  TEST_LICENSE_URLS = Arrays.asList(new String[] {"http://test/url1","http://test/url2"});
	private static final String TEST_LICENSE_HEADER = "Test header optional var";
	private static final String TEST_LICENSE_HEADER_TEMPLATE = "Test header<<beginOptional>> optional<<endOptional>> <<var;name=\"h1test\";original=\"var\";match=\".+\">>";
	private static final String TEST_LICENSE_TEMPLATE = "<<var;name=\"copyright\";original=\"Test Copyright  \";match=\".{0,5000}\">>\nparagraph 1" +
			"\n\n   <<var;name=\"bullet\";original=\"1.\";match=\".{0,20}\">>\n   List item 1\n\n   <<var;name=\"bullet\";original=\"2.\";match=\".{0,20}\">>\n   List item 2\n\n" +
			"Last Paragraph <<var;name=\"alttest\";original=\"Alternate Text\";match=\".+\">> Non matching line.<<beginOptional>> Optional text<<endOptional>>\n\n";

	private static final String TEST_DEP_LICENSE_COMMENT = "Test dep note";
	private static final String TEST_DEP_LICENSE_ID = "test-dep";
	private static final String TEST_DEP_LICENSE_TEXT = "Test Copyright dep\n\nparagraph 1d" +
			"\n\n   1.d\n   List item 1d\n\n   2.d\n   List item 2d\n\n" +
			"Last Paragraph dep Alternate Text dep Non matching line dep. Optional text dep\n\n";
	private static final String TEST_DEP_LICENSE_NAME = "Test Deprecated License";
	private static final List<String> TEST_DEP_LICENSE_URLS = Arrays.asList(new String[] {"http://test/url1d","http://test/url2d"});
	private static final String TEST_DEP_LICENSE_HEADER = "Test header dep";
	private static final String TEST_DEP_LICENSE_TEMPLATE = "<<var;name=\"copyright\";original=\"Test Copyright dep  \";match=\".{0,5000}\">>\nparagraph 1d" +
			"\n\n   <<var;name=\"bullet\";original=\"1.d\";match=\".{0,20}\">>\n   List item 1d\n\n   <<var;name=\"bullet\";original=\"2.d\";match=\".{0,20}\">>\n   List item 2d\n\n" +
			"Last Paragraph dep <<var;name=\"alttestd\";original=\"Alternate Text dep\";match=\".+\">> Non matching line dep.<<beginOptional>> Optional text dep<<endOptional>>\n\n";

	private static final String TEST_EXCEPTION_COMMENT = "Test note exception";
	private static final String TEST_EXCEPTION_ID = "test-ex";
	private static final String TEST_EXCEPTION_TEXT = "Test Copyrighte\n\nparagraph 1e" +
			"\n\n   1.e\n   List item 1e\n\n   2.e\n   List item 2e\n\n" +
			"Last Paragraph exc Alternate Text exc Non matching line. e Optional text exc\n\n";
	private static final String TEST_EXCEPTION_NAME = "Test Exception";
	private static final List<String> TEST_EXCEPTION_URLS = Arrays.asList(new String[] {"http://test/url1e","http://test/url2e"});
	@SuppressWarnings("unused")
	private static final String TEST_EXCEPTION_TEMPLATE = "Test Copyrighte\n\nparagraph 1e" +
			"\n   1.e\n   List item 1e\n   2.e\n   List item 2e\n" +
			"Last Paragraph exc <<var;name=\"altteste\";original=\"Alternate Text exc\";match=\".+\">> Non matching line. e<<beginOptional>> Optional text exc<<endOptional>>";
	private static final String TEST_DEP_LICENSE_VERSION = "2.2";
	private static final String AGPL3ONLY_FILE_PATH = "TestFiles" + File.separator + "AGPL-3.0-only.xml";
	private static final String BSD_PROTECTION_FILE_PATH = "TestFiles" + File.separator + "BSD-Protection.xml";

	private IModelStore v2ModelStore;
	private IModelStore v3ModelStore;
	private IModelCopyManager copyManager;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		SpdxModelFactory.init();
		v2ModelStore = new InMemSpdxStore();
		v3ModelStore = new InMemSpdxStore();
		copyManager = new ModelCopyManager();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#LicenseXmlDocument(java.io.File)}.
	 */
	@Test
	public void testLicenseXmlDocumentFile() throws Exception {
		File licenseFile = new File(TEST_FILE_PATH);
		try {
		    new LicenseXmlDocument(licenseFile, v2ModelStore, v3ModelStore, copyManager);
		} catch(Exception ex) {
		    fail("Error creating XML document: "+ex.getMessage());
		}
	}

	@Test
	public void testOptionalAnnotations() throws Exception {
		File licenseFile = new File(TEST_OPTIONAL_FILE_PATH);
		LicenseXmlDocument licenseDoc = new LicenseXmlDocument(licenseFile, v2ModelStore, v3ModelStore, copyManager);
		SpdxListedLicense license = licenseDoc.getListedLicenses().get(0).getV2ListedLicense();
		String[] lines = license.getLicenseText().split("\\n");
		assertEquals(5, lines.length);
		assertEquals("before optional Default optional text after optional.", lines[0]);
		assertEquals("before optionalNone optional textafter optional.", lines[1]);
		assertEquals("before optional Before optional textafter optional.", lines[2]);
		assertEquals("before optionalAfter optional text after optional.", lines[3]);
		assertEquals("before optional Both optional text after optional.", lines[4]);
		ListedLicense v3License = licenseDoc.getListedLicenses().get(0).getV3ListedLicense();
		lines = v3License.getLicenseText().split("\\n");
		assertEquals(5, lines.length);
		assertEquals("before optional Default optional text after optional.", lines[0]);
		assertEquals("before optionalNone optional textafter optional.", lines[1]);
		assertEquals("before optional Before optional textafter optional.", lines[2]);
		assertEquals("before optionalAfter optional text after optional.", lines[3]);
		assertEquals("before optional Both optional text after optional.", lines[4]);
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#getListedLicense()}.
	 * @throws LicenseXmlException
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testGetListedLicense() throws LicenseXmlException, InvalidSPDXAnalysisException {
		File licenseFile = new File(TEST_FILE_PATH);
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile, v2ModelStore, v3ModelStore, copyManager);
		List<ListedLicenseContainer> licenses = doc.getListedLicenses();
		assertEquals(2, licenses.size());
		for (ListedLicenseContainer licenseContainer : licenses) {
			SpdxListedLicense license = licenseContainer.getV2ListedLicense();
			ListedLicense v3License = licenseContainer.getV3ListedLicense();
			if (license.isDeprecated()) {
				assertTrue(v3License.getIsDeprecatedLicenseId().get());
				assertEquals(TEST_DEP_LICENSE_VERSION,license.getDeprecatedVersion());
				assertFalse(license.isOsiApproved());
				assertEquals(TEST_DEP_LICENSE_COMMENT, license.getComment());
				assertEquals(TEST_DEP_LICENSE_ID, license.getLicenseId());
				assertEquals(TEST_DEP_LICENSE_TEXT, license.getLicenseText());
				assertEquals(TEST_DEP_LICENSE_NAME, license.getName());
				assertTrue(UnitTestHelper.isCollectionsEqual(TEST_DEP_LICENSE_URLS, license.getSeeAlso()));
				assertEquals(TEST_DEP_LICENSE_HEADER, license.getStandardLicenseHeader());
				assertEquals(TEST_DEP_LICENSE_TEMPLATE, license.getStandardLicenseTemplate());
				
				assertEquals(TEST_DEP_LICENSE_VERSION,v3License.getDeprecatedVersion().get());
				assertFalse(v3License.getIsOsiApproved().get());
				assertEquals(TEST_DEP_LICENSE_COMMENT, v3License.getComment().get());
				assertEquals(SpdxConstantsV3.SPDX_LISTED_LICENSE_NAMESPACE + TEST_DEP_LICENSE_ID, v3License.getObjectUri());
				assertEquals(TEST_DEP_LICENSE_TEXT, v3License.getLicenseText());
				assertEquals(TEST_DEP_LICENSE_NAME, v3License.getName().get());
				assertTrue(UnitTestHelper.isCollectionsEqual(TEST_DEP_LICENSE_URLS, v3License.getSeeAlsos()));
				assertEquals(TEST_DEP_LICENSE_HEADER, v3License.getStandardLicenseHeader().get());
				assertEquals(TEST_DEP_LICENSE_TEMPLATE, v3License.getStandardLicenseTemplate().get());
			} else {
				assertFalse(v3License.getIsDeprecatedLicenseId().get());
				assertTrue(license.isOsiApproved());
				assertEquals(TEST_LICENSE_COMMENT, license.getComment());
				assertEquals(TEST_LICENSE_ID, license.getLicenseId());
				assertEquals(TEST_LICENSE_TEXT, license.getLicenseText());
				assertEquals(TEST_LICENSE_NAME, license.getName());
				assertTrue(UnitTestHelper.isCollectionsEqual(TEST_LICENSE_URLS, license.getSeeAlso()));
				assertEquals(TEST_LICENSE_HEADER, license.getStandardLicenseHeader());
				assertEquals(TEST_LICENSE_HEADER_TEMPLATE, license.getStandardLicenseHeaderTemplate());
				assertEquals(TEST_LICENSE_TEMPLATE, license.getStandardLicenseTemplate());
				
				assertTrue(v3License.getIsOsiApproved().get());
				assertEquals(TEST_LICENSE_COMMENT, v3License.getComment().get());
				assertEquals(SpdxConstantsV3.SPDX_LISTED_LICENSE_NAMESPACE + TEST_LICENSE_ID, v3License.getObjectUri());
				assertEquals(TEST_LICENSE_TEXT, v3License.getLicenseText());
				assertEquals(TEST_LICENSE_NAME, v3License.getName().get());
				assertTrue(UnitTestHelper.isCollectionsEqual(TEST_LICENSE_URLS, v3License.getSeeAlsos()));
				assertEquals(TEST_LICENSE_HEADER, v3License.getStandardLicenseHeader().get());
				assertEquals(TEST_LICENSE_TEMPLATE, v3License.getStandardLicenseTemplate().get());
			}
		}
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#getLicenseException()}.
	 * @throws LicenseXmlException
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testGetLicenseException() throws LicenseXmlException, InvalidSPDXAnalysisException {
		File licenseFile = new File(TEST_FILE_PATH);
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile, v2ModelStore, v3ModelStore, copyManager);
		List<ListedExceptionContainer> exceptions = doc.getLicenseExceptions();
		assertEquals(1, exceptions.size());
		LicenseException exception = exceptions.get(0).getV2Exception();
		ListedLicenseException v3Exception = exceptions.get(0).getV3Exception();
		assertEquals(TEST_EXCEPTION_COMMENT, exception.getComment());
		assertEquals(TEST_EXCEPTION_ID, exception.getLicenseExceptionId());
		assertEquals(TEST_EXCEPTION_TEXT, exception.getLicenseExceptionText());
		assertEquals(TEST_EXCEPTION_NAME, exception.getName());
		assertTrue(UnitTestHelper.isCollectionsEqual(TEST_EXCEPTION_URLS, exception.getSeeAlso()));
		
		assertEquals(TEST_EXCEPTION_COMMENT, v3Exception.getComment().get());
		assertEquals(SpdxConstantsV3.SPDX_LISTED_LICENSE_NAMESPACE + TEST_EXCEPTION_ID, v3Exception.getObjectUri());
		assertEquals(TEST_EXCEPTION_TEXT, v3Exception.getAdditionText());
		assertEquals(TEST_EXCEPTION_NAME, v3Exception.getName().get());
		assertTrue(UnitTestHelper.isCollectionsEqual(TEST_EXCEPTION_URLS, v3Exception.getSeeAlsos()));
	}

	@Test
	public void testParserBehavior() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		File file = new File(TEST_FILE_PATH);
		Document doc = builder.parse(file);
		String result = LicenseXmlHelper.dumpLicenseDom((Element) doc.getDocumentElement().getElementsByTagName("license").item(0));
		assertTrue(result.length() > 0);
	}

	@Test
	public void testRegressionAgpl3Only() throws LicenseXmlException, InvalidSPDXAnalysisException, InvalidLicenseTemplateException {
		File licenseFile = new File(AGPL3ONLY_FILE_PATH);
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile, v2ModelStore, v3ModelStore, copyManager);
		List<ListedLicenseContainer> licenses = doc.getListedLicenses();
		assertEquals(1, licenses.size());
	}
	
	@Test
	public void testRegressionBsdProtection() throws LicenseXmlException, InvalidSPDXAnalysisException, InvalidLicenseTemplateException {
        File licenseFile = new File(BSD_PROTECTION_FILE_PATH);
        LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile, v2ModelStore, v3ModelStore, copyManager);
        List<ListedLicenseContainer> licenses = doc.getListedLicenses();
        assertEquals(1, licenses.size());
        SpdxListedLicense result = licenses.get(0).getV2ListedLicense();
        String template = result.getStandardLicenseTemplate();
        Pattern matchingModificationLine = Pattern.compile("<<beginOptional>>\\s?----------------------------------------------------------------<<endOptional>>",Pattern.MULTILINE);
        assertTrue(matchingModificationLine.matcher(template).find());       
	}
}
