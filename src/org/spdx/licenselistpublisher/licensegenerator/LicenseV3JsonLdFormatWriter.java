/**
 * SpdxLicenseIdentifier: Apache-2.0
 * 
 * Copyright (c) 2024 Source Auditor Inc.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.spdx.core.IModelCopyManager;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.core.TypedValue;
import org.spdx.library.ModelCopyManager;
import org.spdx.library.SpdxModelFactory;
import org.spdx.library.model.v2.SpdxConstantsCompatV2;
import org.spdx.library.model.v3_0_1.SpdxConstantsV3;
import org.spdx.library.model.v3_0_1.core.CreationInfo;
import org.spdx.library.model.v3_0_1.core.ElementCollection;
import org.spdx.library.model.v3_0_1.core.ProfileIdentifierType;
import org.spdx.library.model.v3_0_1.core.SpdxDocument;
import org.spdx.library.model.v3_0_1.expandedlicensing.ExternalListedLicense;
import org.spdx.library.model.v3_0_1.expandedlicensing.ListedLicense;
import org.spdx.library.model.v3_0_1.expandedlicensing.ListedLicenseException;
import org.spdx.licenseTemplate.InvalidLicenseTemplateException;
import org.spdx.licenselistpublisher.LicenseGeneratorException;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;
import org.spdx.licensexml.LicenseXmlDocument;
import org.spdx.licensexml.XmlLicenseProvider;
import org.spdx.storage.IModelStore.IdType;
import org.spdx.storage.compatv2.CompatibleModelStoreWrapper;
import org.spdx.storage.simple.InMemSpdxStore;
import org.spdx.v3jsonldstore.JsonLDStore;

/**
 * Writes SPDX JsonLD files compliant with the SPDX Spec version 3
 * 
 * @author Gary O'Neall
 *
 */
public class LicenseV3JsonLdFormatWriter implements ILicenseFormatWriter {
	
	public static final String SPDX_V3_FOLDER_NAME = "SPDXv3";
	public static final String SPDX_V3_JSON_LD_FOLDER_NAME = "v3jsonld";
	private static final String LICENSE_TOC_FILE = "licenses.json";
	private static final String EXCEPTION_TOC_FILE = "exceptions.json";
	private static final CharSequence LOCATION_PREFIX = SpdxConstantsCompatV2.LISTED_LICENSE_URL + SPDX_V3_FOLDER_NAME + "/" + SPDX_V3_FOLDER_NAME;
	private static final String FILE_SUFFIX = ".json";
	private File jsonLdFolder;
	private JsonLDStore licenseTocStore;
	private JsonLDStore exceptionTocStore;
	private SpdxDocument licenseTocDoc;
	private SpdxDocument exceptionTocDoc;
	private ElementCollection licenseTocCollection;
	private ElementCollection exceptionTocCollection;
	private IModelCopyManager copyManager = new ModelCopyManager();
	
	/**
	 * @param version License list version
	 * @param releaseDate release date for the license list
	 * @param jsonLDFolder Folder to output the license TOC file, the exception TOC file, all license and exception details
	 * @throws InvalidSPDXAnalysisException on error creating TOC stores and/or objects
	 */
	public LicenseV3JsonLdFormatWriter(String version, String releaseDate, File jsonLdFolder) throws InvalidSPDXAnalysisException {
		this.jsonLdFolder = jsonLdFolder;
		this.licenseTocStore = new JsonLDStore(new InMemSpdxStore(), true);
		CreationInfo licTocCreation = XmlLicenseProvider.createCreationInfo(this.licenseTocStore, 
				copyManager, releaseDate, version);
		this.licenseTocCollection = licTocCreation.createBundle(SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX + "license-collection-" + version.replace('.','_'))
				.setContext("list of all SPDX listed licenses for license list version +version")
				.build();
		this.licenseTocDoc = licTocCreation.createSpdxDocument(SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX + "license-doc-" + version.replace('.','_'))
				.setDescription("Document containing the list of all SPDX listed licenses for license list version "+version)
				.addRootElement(this.licenseTocCollection)
				.addProfileConformance(ProfileIdentifierType.CORE)
				.addProfileConformance(ProfileIdentifierType.SOFTWARE)
				.build();
		this.exceptionTocStore = new JsonLDStore(new InMemSpdxStore(), true);
		CreationInfo exceptionTocCreation = XmlLicenseProvider.createCreationInfo(this.exceptionTocStore, 
				copyManager, releaseDate, version);
		this.exceptionTocCollection = exceptionTocCreation.createBundle(SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX + "exception-collection-" + version.replace('.','_'))
				.setContext("list of all SPDX listed license exception for license list version " + version)
				.build();
		this.exceptionTocDoc = exceptionTocCreation.createSpdxDocument(SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX + "exception-doc-" + version.replace('.','_'))
				.setDescription("Document containing the list of all SPDX listed license exceptions for license list version "+version)
				.addRootElement(this.exceptionTocCollection)
				.addProfileConformance(ProfileIdentifierType.CORE)
				.addProfileConformance(ProfileIdentifierType.SOFTWARE)
				.build();
	}
	
	/**
	 * @param store JSONLD store containing the data to write
	 * @param filePath file path for the resultant file
	 * @param pretty if true, the JSON LD file will be formatted to be (more) human readable
	 * @throws LicenseGeneratorException on any IO or SPDX parsing errors
	 */
	public static void writeV3JsonLD(JsonLDStore store, Path filePath, boolean pretty) throws LicenseGeneratorException {
		if (!Files.exists(filePath)) {
			try {
				Files.createFile(filePath);
			} catch (IOException e) {
				throw new LicenseGeneratorException("Can not create JSONLD output file "+filePath);
			}
		}
		store.setPretty(pretty);
		try (OutputStream stream = new FileOutputStream(filePath.toFile())) {
			store.serialize(stream);
		} catch (FileNotFoundException e) {
			throw new LicenseGeneratorException("Unexpected file not found error for "+filePath);
		} catch (IOException e) {
			throw new LicenseGeneratorException("I/O error writing JSONLD output file "+filePath, e);
		} catch (InvalidSPDXAnalysisException e) {
			throw new LicenseGeneratorException("SPDX exception creating JSONLD output file "+filePath, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeLicense(org.spdx.licenselistpublisher.ListedLicenseContainer, boolean, java.lang.String)
	 */
	@Override
	public void writeLicense(ListedLicenseContainer licenseContainer,
			boolean deprecated, String deprecatedVersion)
			throws IOException, LicenseGeneratorException,
			InvalidSPDXAnalysisException, InvalidLicenseTemplateException {
		ListedLicense license = licenseContainer.getV3ListedLicense();
		JsonLDStore onlyThisLicenseStore = new JsonLDStore(new InMemSpdxStore(), true);
		ModelCopyManager onlyLicenseCopyManager = new ModelCopyManager();
		TypedValue tv = onlyLicenseCopyManager.copy(onlyThisLicenseStore, license.getModelStore(), 
				license.getObjectUri(), license.getSpecVersion(), license.getIdPrefix());
		String licenseId = CompatibleModelStoreWrapper.objectUriToId(false, license.getObjectUri(), SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX);
		ListedLicense copiedLicense = (ListedLicense)SpdxModelFactory.inflateModelObject(onlyThisLicenseStore, tv.getObjectUri(), tv.getType(), onlyLicenseCopyManager, 
				tv.getSpecVersion(), false, license.getIdPrefix());
		List<String> verify = copiedLicense.createSpdxDocument(license.getObjectUri() + "_document")
				.setName(licenseId+" document")
				.setComment("SPDX Document For "+licenseId)
				.addRootElement(copiedLicense)
				.build().verify();
		if (!verify.isEmpty()) {
			throw new LicenseGeneratorException("Invalid license data generated for "+licenseId+": "+verify.get(0));
		}
		licenseTocDoc.getSpdxImports().add(licenseTocDoc.createExternalMap(licenseTocStore.getNextId(IdType.Anonymous))
				.setExternalSpdxId(license.getObjectUri())
				.setLocationHint(license.getObjectUri().replace(SpdxConstantsV3.SPDX_LISTED_LICENSE_NAMESPACE, LOCATION_PREFIX) + FILE_SUFFIX)
				// TODO - we can write the file and get a hash to add a verified using
				.build());
		writeV3JsonLD(onlyThisLicenseStore, jsonLdFolder.toPath().resolve(
				LicenseHtmlFormatWriter.formLicenseHTMLFileName(licenseId) + FILE_SUFFIX), true);
		licenseTocCollection.getElements().add(new ExternalListedLicense(licenseTocCollection.getModelStore(),
				license.getObjectUri(), licenseTocCollection.getCopyManager(), true, 
				SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX));
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeToC()
	 */
	@Override
	public void writeToC() throws IOException, LicenseGeneratorException {
		writeV3JsonLD(this.licenseTocStore, jsonLdFolder.toPath().resolve(LICENSE_TOC_FILE), true);
		List<String> verify = licenseTocDoc.verify();
		if (!verify.isEmpty()) {
			throw new LicenseGeneratorException("Invalid license TOC data generated: "+verify.get(0));
		}
		writeV3JsonLD(this.exceptionTocStore, jsonLdFolder.toPath().resolve(EXCEPTION_TOC_FILE), true);
		verify = exceptionTocDoc.verify();
		if (!verify.isEmpty()) {
			throw new LicenseGeneratorException("Invalid exception TOC data generated: "+verify.get(0));
		}
	}

	/* (non-Javadoc)
	 * @see org.spdx.licenselistpublisher.licensegenerator.ILicenseFormatWriter#writeException(org.spdx.licenselistpublisher.ListedExceptionContainer)
	 */
	@Override
	public void writeException(ListedExceptionContainer exceptionContainer)
			throws IOException, LicenseGeneratorException,
			InvalidLicenseTemplateException, InvalidSPDXAnalysisException {
		ListedLicenseException exception = exceptionContainer.getV3Exception();
		JsonLDStore onlyThisExceptionStore = new JsonLDStore(new InMemSpdxStore(), true);
		ModelCopyManager onlyExceptionCopyManager = new ModelCopyManager();
		TypedValue tv = onlyExceptionCopyManager.copy(onlyThisExceptionStore, exception.getModelStore(), 
				exception.getObjectUri(), exception.getSpecVersion(), exception.getIdPrefix());
		String exceptionId = CompatibleModelStoreWrapper.objectUriToId(false, exception.getObjectUri(), 
				SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX);
		ListedLicenseException copiedException = (ListedLicenseException)SpdxModelFactory.inflateModelObject(onlyThisExceptionStore, 
				tv.getObjectUri(), tv.getType(), onlyExceptionCopyManager, tv.getSpecVersion(), false, exception.getIdPrefix());
		List<String> verify = copiedException.createSpdxDocument(exception.getObjectUri() + "_document")
				.setName(exceptionId+" document")
				.setComment("SPDX Document For "+exceptionId)
				.addRootElement(copiedException)
				.build().verify();
		if (!verify.isEmpty()) {
			throw new LicenseGeneratorException("Invalid exception data generated for "+exceptionId+": "+verify.get(0));
		}
		exceptionTocDoc.getSpdxImports().add(exceptionTocDoc.createExternalMap(exceptionTocStore.getNextId(IdType.Anonymous))
				.setExternalSpdxId(exception.getObjectUri())
				.setLocationHint(exception.getObjectUri().replace(SpdxConstantsV3.SPDX_LISTED_LICENSE_NAMESPACE, LOCATION_PREFIX) + FILE_SUFFIX)
				// TODO - we can write the file and get a hash to add a verified using
				.build());
		writeV3JsonLD(onlyThisExceptionStore, jsonLdFolder.toPath().resolve(
				LicenseHtmlFormatWriter.formLicenseHTMLFileName(exceptionId) + FILE_SUFFIX), true);
		exceptionTocCollection.getElements().add(new ExternalListedLicense(exceptionTocCollection.getModelStore(),
				exception.getObjectUri(), exceptionTocCollection.getCopyManager(), true, 
				SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX));
	}

}
