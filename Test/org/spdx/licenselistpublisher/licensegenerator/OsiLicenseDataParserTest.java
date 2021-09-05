/**
 * SpdxLicenseIdentifier: Apache-2.0
 * 
 * Copyright (c) 2021 Source Auditor Inc.
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
package org.spdx.licenselistpublisher.licensegenerator;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spdx.licenselistpublisher.LicenseGeneratorException;

/**
 * @author gary
 *
 */
public class OsiLicenseDataParserTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        OsiLicenseDataParser.reset();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        OsiLicenseDataParser.reset();
    }

    /**
     * Test method for {@link org.spdx.licenselistpublisher.licensegenerator.OsiLicenseDataParser#getOsiLicenseDataParser()}.
     * @throws LicenseGeneratorException 
     */
    @Test
    public void testGetOsiLicenseDataParser() throws LicenseGeneratorException {
        OsiLicenseDataParser.getOsiLicenseDataParser();
        // just test to see if we get an exception
    }

    /**
     * Test method for {@link org.spdx.licenselistpublisher.licensegenerator.OsiLicenseDataParser#isSpdxLicenseOsiApproved(java.lang.String)}.
     * @throws LicenseGeneratorException 
     */
    @Test
    public void testIsSpdxLicenseOsiApproved() throws LicenseGeneratorException {
        OsiLicenseDataParser osildp = OsiLicenseDataParser.getOsiLicenseDataParser();
        Optional<Boolean> result = osildp.isSpdxLicenseOsiApproved("Apache-2.0");
        assertTrue(result.isPresent());
        assertTrue(result.get());
        result = osildp.isSpdxLicenseOsiApproved("apache-2.0");
        assertTrue(result.isPresent());
        assertTrue(result.get());
        result = osildp.isSpdxLicenseOsiApproved("invalidid");
        assertFalse(result.isPresent());
        // All of the current OSI licenses have an OSI approved keyword - no test for the negative
    }

}
