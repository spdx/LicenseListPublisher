/**
 * 
 */
package org.spdx.licenselistpublisher.licensegenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.spdx.html.InvalidLicenseTemplateException;
import org.spdx.licenselistpublisher.LicenseGeneratorException;
import org.spdx.licenselistpublisher.LicensePublisherHelper;
import org.spdx.rdfparser.license.ListedLicenseException;
import org.spdx.rdfparser.license.SpdxListedLicense;

/**
 * @author Gary O'Neall
 * 
 * Write Java source code to implement a static Java class file containing all listed licenses and exceptions
 * as well as supporting interface definitions.
 * 
 * The output of this writer can be treated as a source directory which can be run through a java compiler
 * to produce a static class which support the <code>ISpdxListedException</code> and <code>ISpdxListedLicense</code>
 * interfaces.
 *
 */
public class LicenseJavaSourceWriter implements ILicenseFormatWriter {
	
	private static final String LISTED_LICENSES_FILE_NAME = "SpdxListedLicenses.java";
	private static final String LISTED_EXCEPTIONS_FILE_NAME = "SpdxListedExceptions.java";
	private static final String LISTED_LICENSE_INTERFACE_FILE_NAME = "ISpdxListedLicense.java";
	private static final String LISTED_LICENSE_INTERFACE_TEMPLATE = "resources/javatemplate/ISpdxListedLicense.java";
	private static final String LISTED_EXCEPTION_INTERFACE_FILE_NAME = "ISpdxListedException.java";
	private static final String LISTED_EXCEPTION_INTERFACE_TEMPLATE = "resources/javatemplate/ISpdxListedException.java";
	private File sourceDir;
	ListedLicensesJavaSource listedLicenseSource;
	ListedExceptionsJavaSource listedExceptionSource;
	

	/**
	 * @param sourceDir Directory where the Java source files will be written
	 */
	public LicenseJavaSourceWriter(File sourceDir, String version) {
		this.sourceDir = sourceDir;
		listedLicenseSource = new ListedLicensesJavaSource(version);
		listedExceptionSource = new ListedExceptionsJavaSource();
	}

	@Override
	public void writeLicense(SpdxListedLicense license, boolean deprecated, String deprecatedVersion)
			throws IOException, LicenseGeneratorException {
		listedLicenseSource.addLicense(license);
	}

	@Override
	public void writeToC() throws IOException, LicenseGeneratorException {
		Path sourcePath = sourceDir.toPath();
		listedLicenseSource.writeFile(sourcePath.resolve(LISTED_LICENSES_FILE_NAME).toFile());
		listedExceptionSource.writeFile(sourcePath.resolve(LISTED_EXCEPTIONS_FILE_NAME).toFile());
		LicensePublisherHelper.copyResourceFile(LISTED_LICENSE_INTERFACE_TEMPLATE, 
				sourcePath.resolve(LISTED_LICENSE_INTERFACE_FILE_NAME).toFile());
		LicensePublisherHelper.copyResourceFile(LISTED_EXCEPTION_INTERFACE_TEMPLATE, 
				sourcePath.resolve(LISTED_EXCEPTION_INTERFACE_FILE_NAME).toFile());
	}

	@Override
	public void writeException(ListedLicenseException exception)
			throws IOException, LicenseGeneratorException, InvalidLicenseTemplateException {
		listedExceptionSource.addException(exception);
	}

}
