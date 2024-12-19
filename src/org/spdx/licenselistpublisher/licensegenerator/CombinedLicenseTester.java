/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Source Auditor Inc. 2024.
 *
 */

package org.spdx.licenselistpublisher.licensegenerator;

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;
import org.spdx.utility.compare.SpdxCompareException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Combines a simple license tester and the standard license tester into a single tester
 */
public class CombinedLicenseTester implements ILicenseTester {

    private final ILicenseTester simpleLicenseTester;
    private final ILicenseTester licenseTester;

    /**
     * Create a license tester that will test both simple licenses and standard licenses
     * @param simpleLicenseTestDir Directory containing license texts in the format [license-id].txt
     * @param licenseTestDir Directory of license text files for comparison in the form {license-id}/(license|header|exception)/(good|bad)/{test-id}.txt
     */
    public CombinedLicenseTester(File simpleLicenseTestDir, File licenseTestDir) {
        simpleLicenseTester = new SimpleLicenseTester(simpleLicenseTestDir);
        licenseTester = new LicenseTester(licenseTestDir);
    }

    @Override
    public List<String> testException(ListedExceptionContainer exceptionContainer) throws IOException, InvalidSPDXAnalysisException {
        List<String> retval = simpleLicenseTester.testException(exceptionContainer);
        retval.addAll(licenseTester.testException(exceptionContainer));
        return retval;
    }

    @Override
    public List<String> testLicense(ListedLicenseContainer licenseContainer) throws IOException, SpdxCompareException, InvalidSPDXAnalysisException {
        List<String> retval = simpleLicenseTester.testLicense(licenseContainer);
        retval.addAll(licenseTester.testLicense(licenseContainer));
        return retval;
    }

    @Nullable
    @Override
    public String getLicenseTestText(String licenseId) throws IOException {
        return simpleLicenseTester.getLicenseTestText(licenseId);
    }

    @Nullable
    @Override
    public String getExceptionTestText(String licenseExceptionId) throws IOException {
        return simpleLicenseTester.getExceptionTestText(licenseExceptionId);
    }
}
