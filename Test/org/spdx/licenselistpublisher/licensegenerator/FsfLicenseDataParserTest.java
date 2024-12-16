package org.spdx.licenselistpublisher.licensegenerator;

import junit.framework.TestCase;
import org.spdx.licenselistpublisher.LicenseGeneratorException;

public class FsfLicenseDataParserTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsSpdxLicenseFsfLibre() throws LicenseGeneratorException {
        assertTrue(FsfLicenseDataParser.getFsfLicenseDataParser().isSpdxLicenseFsfLibre("GPL-2.0-or-later"));
        assertFalse(FsfLicenseDataParser.getFsfLicenseDataParser().isSpdxLicenseFsfLibre("CC-BY-NC-2.0"));
        assertNull(FsfLicenseDataParser.getFsfLicenseDataParser().isSpdxLicenseFsfLibre("something"));
    }
}