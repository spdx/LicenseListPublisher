/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Source Auditor Inc. 2024.
 *
 */

package org.spdx.licenselistpublisher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spdx.library.SpdxModelFactory;

import java.io.File;
import java.util.List;

public class LicenseXmlTesterTest {

    static final String SOURCE_XML = "TestFiles" + File.separator + "BSD-3-Clause.xml";
    static final String GOOD_TEST_DIR = "TestFiles" + File.separator + "good-tests";
    static final String BAD_TEST_DIR = "TestFiles" + File.separator + "bad-tests";
    static final String COMPARE_TEXT = GOOD_TEST_DIR + File.separator + "BSD-3-Clause" + File.separator +
            "license" + File.separator + "good" + File.separator + "original.txt";
    static final String BAD_COMPARE_TEXT = GOOD_TEST_DIR + File.separator + "BSD-3-Clause" + File.separator +
            "license" + File.separator + "bad" + File.separator + "must-not-reproduce.txt";

    @Before
    public void setUp() {
        SpdxModelFactory.init();
    }

    @Test
    public void testSingleFile() throws Exception {
        List<String> result = LicenseXmlTester.testLicenseXml(new File(SOURCE_XML), new File(COMPARE_TEXT), null);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testSingleBadFile() throws Exception {
        List<String> result = LicenseXmlTester.testLicenseXml(new File(SOURCE_XML), new File(BAD_COMPARE_TEXT), null);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testFullTest() throws Exception {
        List<String> result = LicenseXmlTester.testLicenseXml(new File(SOURCE_XML), new File(COMPARE_TEXT),
                new File(GOOD_TEST_DIR));
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testBadFullTest() throws Exception {
        List<String> result = LicenseXmlTester.testLicenseXml(new File(SOURCE_XML), new File(COMPARE_TEXT),
                new File(BAD_TEST_DIR));
        Assert.assertFalse(result.isEmpty());
    }
}