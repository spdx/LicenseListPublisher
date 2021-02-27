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
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.license.LicenseException;
import org.spdx.library.model.license.ListedLicenseException;
import org.spdx.library.model.license.SpdxListedLicense;
import org.spdx.licenseTemplate.InvalidLicenseTemplateException;
import org.spdx.licenselistpublisher.UnitTestHelper;
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
			"\n\n   1.\n\n   List item 1\n\n   2.\n\n   List item 2\n\n" +
			"Last Paragraph Alternate Text Non matching line. Optional text\n\n";
	private static final String TEST_LICENSE_NAME = "Test License";
	private static final List<String>  TEST_LICENSE_URLS = Arrays.asList(new String[] {"http://test/url1","http://test/url2"});
	private static final String TEST_LICENSE_HEADER = "Test header optional var";
	private static final String TEST_LICENSE_HEADER_TEMPLATE = "Test header<<beginOptional>> optional<<endOptional>> <<var;name=\"h1test\";original=\"var\";match=\".+\">>";
	private static final String TEST_LICENSE_TEMPLATE = "<<var;name=\"copyright\";original=\"Test Copyright  \";match=\".{0,1000}\">>\n\nparagraph 1" +
			"\n\n   <<var;name=\"bullet\";original=\"1.\";match=\".{0,20}\">>\n\n   List item 1\n\n   <<var;name=\"bullet\";original=\"2.\";match=\".{0,20}\">>\n\n   List item 2\n\n" +
			"Last Paragraph <<var;name=\"alttest\";original=\"Alternate Text\";match=\".+\">> Non matching line.<<beginOptional>> Optional text<<endOptional>>\n\n";

	private static final String TEST_DEP_LICENSE_COMMENT = "Test dep note";
	private static final String TEST_DEP_LICENSE_ID = "test-dep";
	private static final String TEST_DEP_LICENSE_TEXT = "Test Copyright dep\n\nparagraph 1d" +
			"\n\n   1.d\n\n   List item 1d\n\n   2.d\n\n   List item 2d\n\n" +
			"Last Paragraph dep Alternate Text dep Non matching line dep. Optional text dep\n\n";
	private static final String TEST_DEP_LICENSE_NAME = "Test Deprecated License";
	private static final List<String> TEST_DEP_LICENSE_URLS = Arrays.asList(new String[] {"http://test/url1d","http://test/url2d"});
	private static final String TEST_DEP_LICENSE_HEADER = "Test header dep";
	private static final String TEST_DEP_LICENSE_TEMPLATE = "<<var;name=\"copyright\";original=\"Test Copyright dep  \";match=\".{0,1000}\">>\n\nparagraph 1d" +
			"\n\n   <<var;name=\"bullet\";original=\"1.d\";match=\".{0,20}\">>\n\n   List item 1d\n\n   <<var;name=\"bullet\";original=\"2.d\";match=\".{0,20}\">>\n\n   List item 2d\n\n" +
			"Last Paragraph dep <<var;name=\"alttestd\";original=\"Alternate Text dep\";match=\".+\">> Non matching line dep.<<beginOptional>> Optional text dep<<endOptional>>\n\n";

	private static final String TEST_EXCEPTION_COMMENT = "Test note exception";
	private static final String TEST_EXCEPTION_ID = "test-ex";
	private static final String TEST_EXCEPTION_TEXT = "Test Copyrighte\n\nparagraph 1e" +
			"\n\n   1.e\n\n   List item 1e\n\n   2.e\n\n   List item 2e\n\n" +
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


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
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
		new LicenseXmlDocument(licenseFile);
		// I guess if we don't get any exceptions, it passed
	}

	@Test
	public void testOptionalAnnotations() throws Exception {
		File licenseFile = new File(TEST_OPTIONAL_FILE_PATH);
		LicenseXmlDocument licenseDoc = new LicenseXmlDocument(licenseFile);
		SpdxListedLicense license = licenseDoc.getListedLicenses().get(0);
		String[] lines = license.getLicenseText().split("\\n");
		assertEquals(9, lines.length);
		assertEquals("before optional Default optional text after optional.", lines[0]);
		assertEquals("before optionalNone optional textafter optional.", lines[2]);
		assertEquals("before optional Before optional textafter optional.", lines[4]);
		assertEquals("before optionalAfter optional text after optional.", lines[6]);
		assertEquals("before optional Both optional text after optional.", lines[8]);
	}

	/**
	 * Test method for {@link org.spdx.licensexml.LicenseXmlDocument#getListedLicense()}.
	 * @throws LicenseXmlException
	 * @throws InvalidSPDXAnalysisException
	 */
	@Test
	public void testGetListedLicense() throws LicenseXmlException, InvalidSPDXAnalysisException {
		File licenseFile = new File(TEST_FILE_PATH);
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile);
		List<SpdxListedLicense> licenses = doc.getListedLicenses();
		assertEquals(2, licenses.size());
		for (SpdxListedLicense license : licenses) {
			if (license.isDeprecated()) {
				assertEquals(TEST_DEP_LICENSE_VERSION,license.getDeprecatedVersion());
				assertFalse(license.isOsiApproved());
				assertEquals(TEST_DEP_LICENSE_COMMENT, license.getComment());
				assertEquals(TEST_DEP_LICENSE_ID, license.getLicenseId());
				assertEquals(TEST_DEP_LICENSE_TEXT, license.getLicenseText());
				assertEquals(TEST_DEP_LICENSE_NAME, license.getName());
				assertTrue(UnitTestHelper.isCollectionsEqual(TEST_DEP_LICENSE_URLS, license.getSeeAlso()));
				assertEquals(TEST_DEP_LICENSE_HEADER, license.getStandardLicenseHeader());
				assertEquals(TEST_DEP_LICENSE_TEMPLATE, license.getStandardLicenseTemplate());
			} else {
				assertTrue(license.isOsiApproved());
				assertEquals(TEST_LICENSE_COMMENT, license.getComment());
				assertEquals(TEST_LICENSE_ID, license.getLicenseId());
				assertEquals(TEST_LICENSE_TEXT, license.getLicenseText());
				assertEquals(TEST_LICENSE_NAME, license.getName());
				assertTrue(UnitTestHelper.isCollectionsEqual(TEST_LICENSE_URLS, license.getSeeAlso()));
				assertEquals(TEST_LICENSE_HEADER, license.getStandardLicenseHeader());
				assertEquals(TEST_LICENSE_HEADER_TEMPLATE, license.getStandardLicenseHeaderTemplate());
				assertEquals(TEST_LICENSE_TEMPLATE, license.getStandardLicenseTemplate());
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
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile);
		List<ListedLicenseException> exceptions = doc.getLicenseExceptions();
		assertEquals(1, exceptions.size());
		LicenseException exception = exceptions.get(0);
		assertEquals(TEST_EXCEPTION_COMMENT, exception.getComment());
		assertEquals(TEST_EXCEPTION_ID, exception.getLicenseExceptionId());
		assertEquals(TEST_EXCEPTION_TEXT, exception.getLicenseExceptionText());
		assertEquals(TEST_EXCEPTION_NAME, exception.getName());
		assertTrue(UnitTestHelper.isCollectionsEqual(TEST_EXCEPTION_URLS, exception.getSeeAlso()));
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
		LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile);
		List<SpdxListedLicense> licenses = doc.getListedLicenses();
		assertEquals(1, licenses.size());
	}
	
	@Test
	public void testRegressionBsdProtection() throws LicenseXmlException, InvalidSPDXAnalysisException, InvalidLicenseTemplateException {
        File licenseFile = new File(BSD_PROTECTION_FILE_PATH);
        LicenseXmlDocument doc = new LicenseXmlDocument(licenseFile);
        List<SpdxListedLicense> licenses = doc.getListedLicenses();
        assertEquals(1, licenses.size());
        SpdxListedLicense result = licenses.get(0);
        String template = result.getStandardLicenseTemplate();
        Pattern matchingModificationLine = Pattern.compile("<<beginOptional>>\\s?----------------------------------------------------------------<<endOptional>>",Pattern.MULTILINE);
        assertTrue(matchingModificationLine.matcher(template).find());       
	}
}
