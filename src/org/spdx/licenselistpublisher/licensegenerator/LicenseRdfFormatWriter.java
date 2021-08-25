/**
 * Copyright (c) 2017 Source Auditor Inc.
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
 */
package org.spdx.licenselistpublisher.licensegenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxConstants;
import org.spdx.library.model.license.ListedLicenseException;
import org.spdx.library.model.license.SpdxListedLicense;
import org.spdx.licenselistpublisher.LicenseGeneratorException;
import org.spdx.spdxRdfStore.OutputFormat;
import org.spdx.spdxRdfStore.RdfStore;

/**
 * Write RDF formats for the licenses
 *
 * @author Gary O'Neall
 *
 */
public class LicenseRdfFormatWriter implements ILicenseFormatWriter {

	private File rdfXml;
	private File rdfTurtle;
	private File rdfNt;
	private RdfStore rdfStore;
	private File rdfJsonLd;

	/**
	 * @param rdfXml File to store RDF XML formatted license list
	 * @param rdfTurtle File to store RDF Turtle formatted license list
	 * @param rdfNt File to store RDF Nt formatted license list
	 * @param rdfJsonLd File to store JSON-LD formatted license list
	 */
	public LicenseRdfFormatWriter(File rdfXml, File rdfTurtle, File rdfNt, File rdfJsonLd) {
		this.rdfXml = rdfXml;
		this.rdfTurtle = rdfTurtle;
		this.rdfNt = rdfNt;
		this.rdfJsonLd = rdfJsonLd;
		rdfStore = new RdfStore();// Create store to hold licenses and exceptions
	}

	/**
	 * @return the rdfXml
	 */
	public File getRdfXml() {
		return rdfXml;
	}

	/**
	 * @param rdfXml the rdfXml to set
	 */
	public void setRdfXml(File rdfXml) {
		this.rdfXml = rdfXml;
	}

	/**
	 * @return the rdfTurtle
	 */
	public File getRdfTurtle() {
		return rdfTurtle;
	}

	/**
	 * @param rdfTurtle the rdfTurtle to set
	 */
	public void setRdfTurtle(File rdfTurtle) {
		this.rdfTurtle = rdfTurtle;
	}

	/**
	 * @return the rdfNt
	 */
	public File getRdfNt() {
		return rdfNt;
	}

	/**
	 * @param rdfNt the rdfNt to set
	 */
	public void setRdfNt(File rdfNt) {
		this.rdfNt = rdfNt;
	}

	@Override
	public void writeLicense(SpdxListedLicense license, boolean deprecated, String deprecatedVersion) throws IOException, LicenseGeneratorException, InvalidSPDXAnalysisException {
		RdfStore onlyThisLicense = new RdfStore();
		ModelCopyManager copyManager = new ModelCopyManager();
		copyManager.copy(onlyThisLicense, SpdxConstants.LISTED_LICENSE_URL, license.getModelStore(), license.getDocumentUri(), 
				license.getId(), license.getType());
		String licBaseFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(license.getLicenseId());
		writeRdf(onlyThisLicense, SpdxConstants.LISTED_LICENSE_URL, rdfXml, rdfTurtle, rdfNt, rdfJsonLd, licBaseFileName);
		// Copy to the table of contents
		copyManager.copy(rdfStore, SpdxConstants.LISTED_LICENSE_URL, license.getModelStore(), license.getDocumentUri(), 
				license.getId(), license.getType());
	}

	/**
	 * Write the RDF representations of the licenses and exceptions
	 * @param rdfStore Store with the licenses and exceptions
	 * @param documentUri Document URI containing the elements to be written
	 * @param rdfXml Folder for the RdfXML representation
	 * @param rdfTurtle Folder for the Turtle representation
	 * @param rdfNt Folder for the NT representation
	 * @param rdfJsonLd Folder for the JSON-LD representation
	 * @param name Name of the file
	 * @throws LicenseGeneratorException
	 */
	private static void writeRdf(RdfStore rdfStore, String documentUri, File rdfXml, File rdfTurtle,
			File rdfNt, File rdfJsonLd, String name) throws LicenseGeneratorException {
		if (rdfXml != null) {
			writeRdf(rdfStore, documentUri, rdfXml.getPath() + File.separator + name + ".rdf", OutputFormat.XML_ABBREV);
		}
		if (rdfTurtle != null) {
			writeRdf(rdfStore, documentUri, rdfTurtle.getPath() + File.separator + name + ".ttl", OutputFormat.TURTLE);
		}
		if (rdfNt != null) {
			writeRdf(rdfStore, documentUri, rdfNt.getPath() + File.separator + name + ".nt", OutputFormat.N_TRIPLET);
		}
		if (rdfJsonLd != null) {
			writeRdf(rdfStore, documentUri, rdfJsonLd.getPath() + File.separator + name + ".jsonld", OutputFormat.JSON_LD);
		}
	}

	/**
	 * Write an RDF file for for all elements in the container
	 * @param rdfStore Store for the RDF elements
	 * @param documentUri Document URI containing the elements to be written
	 * @param fileName File name to write the elements to
	 * @param format Jena RDF format
	 * @throws LicenseGeneratorException
	 */
	public static void writeRdf(RdfStore rdfStore, String documentUri, String fileName, OutputFormat format) throws LicenseGeneratorException {
		File outFile = new File(fileName);
		if (!outFile.exists()) {
			try {
				if (!outFile.createNewFile()) {
					throw new LicenseGeneratorException("Can not create RDF output file "+fileName);
				}
			} catch (IOException e) {
				throw new LicenseGeneratorException("Can not create RDF output file "+fileName);
			}
		}

		rdfStore.setOutputFormat(format);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outFile);
			rdfStore.serialize(documentUri, out);
		} catch (FileNotFoundException e1) {
			throw new LicenseGeneratorException("Can not create RDF output file "+fileName);
		} catch (InvalidSPDXAnalysisException e) {
			throw new LicenseGeneratorException("SPDX analysis exception generating the RDF output", e);
		} catch (IOException e) {
			throw new LicenseGeneratorException("I/O generating the RDF output", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("Warning - unable to close RDF output file "+fileName);
				}
			}
		}
	}

	@Override
	public void writeToC() throws IOException, LicenseGeneratorException {
		writeRdf(rdfStore, SpdxConstants.LISTED_LICENSE_URL, rdfXml, rdfTurtle, rdfNt, rdfJsonLd, "licenses");
	}

	@Override
	public void writeException(ListedLicenseException exception)
			throws IOException, LicenseGeneratorException, InvalidSPDXAnalysisException {
		String exceptionHtmlFileName = LicenseHtmlFormatWriter.formLicenseHTMLFileName(exception.getLicenseExceptionId());
		RdfStore onlyThisException = new RdfStore();	
		ModelCopyManager copyManager = new ModelCopyManager();
		copyManager.copy(onlyThisException, SpdxConstants.LISTED_LICENSE_URL, exception.getModelStore(), exception.getDocumentUri(), 
				exception.getId(), exception.getType());
		writeRdf(onlyThisException, SpdxConstants.LISTED_LICENSE_URL, rdfXml, rdfTurtle, rdfNt, rdfJsonLd, exceptionHtmlFileName);
		// Copy to the table of contents
		copyManager.copy(rdfStore, SpdxConstants.LISTED_LICENSE_URL, exception.getModelStore(), exception.getDocumentUri(), 
				exception.getId(), exception.getType());
	}
}
