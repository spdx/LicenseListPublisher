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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.core.IModelCopyManager;
import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.SpdxConstantsCompatV2;
import org.spdx.library.model.v3_0_1.expandedlicensing.ListedLicense;
import org.spdx.library.model.v3_0_1.expandedlicensing.ListedLicenseException;
import org.spdx.licenselistpublisher.ListedExceptionContainer;
import org.spdx.licenselistpublisher.ListedLicenseContainer;
import org.spdx.storage.IModelStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Parses and provides access to a License XML document
 * @author Gary O'Neall
 *
 */
public class LicenseXmlDocument {
	static final Logger logger = LoggerFactory.getLogger(LicenseXmlDocument.class.getName());

	public static final String PROP_SCHEMA_FILENAME = "listedLicenseSchema";
	public static final String LICENSE_XML_SCHEMA_URL = "https://raw.githubusercontent.com/spdx/license-list-XML/master/schema/ListedLicense.xsd";
	public static final String LICENSE_XML_SCHEMA_LOCATION = "org/spdx/licensexml/ListedLicense.xsd";

	private static Schema _schema = null;	// cache of the license XML schema
	private Document xmlDocument;
	private IModelStore v2ModelStore;
	private IModelStore v3ModelStore;
	private IModelCopyManager copyManager;

	/**
	 * @param file XML file for the License
	 * @param copyManager Copy manager for both model store
	 * @param v3ModelStore model store for SPDX Spec version 3 liccense and exceptions
	 * @param v2ModelStore model store for SPDX Spec version 2 liccense and exceptions
	 */
	public LicenseXmlDocument(File file, IModelStore v2ModelStore, IModelStore v3ModelStore, IModelCopyManager copyManager) throws LicenseXmlException {
		this.v2ModelStore = v2ModelStore;
		this.v3ModelStore = v3ModelStore;
		this.copyManager = copyManager;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Disable external access to prevent confidential file disclosures or SSRFs.
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // Disable external access to prevent confidential file disclosures or SSRFs.
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Parser configuration error creating document builder",e);
			throw(new LicenseXmlException("Error creating parser for license XML file"));
		}
		try {
			this.xmlDocument = builder.parse(file);
		} catch (SAXException e) {
			logger.error("Error parsing license XML document",e);
			throw(new LicenseXmlException("Unable to parse license XML file: "+e.getMessage()));
		} catch (IOException e) {
			logger.error("I/O Error reading license XML file",e);
			throw(new LicenseXmlException("I/O Error reading XML file: "+e.getMessage()));
		}
		assertValid(file);
	}

	/**
	 * @return listed license XML schema
	 * @throws LicenseXmlException
	 */
	private synchronized Schema getSchema() throws LicenseXmlException {
		if (_schema == null) {
			InputStream schemaIs = null;
			try {
				String schemaFilePath = System.getProperty(PROP_SCHEMA_FILENAME);
				if (schemaFilePath != null) {
					try {
					    schemaIs = new FileInputStream(schemaFilePath);
					} catch (IOException e) {
						logger.error("IO Exception opening specified schema file "+schemaFilePath,e);
						throw new LicenseXmlException("Invalid license XML schema file");
					}
				} else {
					try {
						URL schemaUrl = new URL(LICENSE_XML_SCHEMA_URL);
						schemaIs = schemaUrl.openStream();
					} catch (Exception e) {
						logger.warn("Unable to open license XML schema URL, using cached copy",e);
					}
					if (schemaIs == null) {
						schemaIs = LicenseXmlDocument.class.getClassLoader().getResourceAsStream(LICENSE_XML_SCHEMA_LOCATION);
					}
				}
				Source schemaSource = new StreamSource(schemaIs);
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				_schema = schemaFactory.newSchema(schemaSource);
			} catch (SAXException e) {
				logger.error("Invalid schema file",e);
				throw new LicenseXmlException("Invalid Listed License Schema",e);
			} finally {
				if (schemaIs != null) {
					try {
						schemaIs.close();
					} catch (IOException e) {
						logger.warn("Unable to close Schema stream",e);
					}
				}
			}
		}
		return _schema;
	}

	/**
	 * Checks the xmlDocument for a valid file and throws a LicenseXmlException if not valid
	 */
	private void assertValid(File licenseXmlFile) throws LicenseXmlException {
		try {
			Source xmlSource = new StreamSource(licenseXmlFile);
			Schema schema = getSchema();
			if (Objects.isNull(schema)) {
			    throw new LicenseXmlException("Unable to open schema file for validation");
			}
			Validator validator = schema.newValidator();
			validator.validate(xmlSource);
		} catch (MalformedURLException e) {
			logger.error("Unable to open License List XML schema file",e);
			throw new LicenseXmlException("Unable to open License List XML schema file");
		} catch (SAXParseException e) {
			logger.error("Invalid license XML file "+licenseXmlFile.getName(),e);
			throw new LicenseXmlException("Parsing error in XML file "+licenseXmlFile.getName()+ " at line "+e.getLineNumber()+", column "+e.getColumnNumber()+":"+e.getMessage());
		} catch (SAXException e) {
			logger.error("Invalid license XML file "+licenseXmlFile.getName(),e);
			throw new LicenseXmlException("Invalid XML file "+licenseXmlFile.getName()+":"+e.getMessage());
		} catch (IOException e) {
			logger.error("IO Error validating license XML file",e);
			throw new LicenseXmlException("IO Error validating license XML file");
		}
	}

	public LicenseXmlDocument(Document xmlDocument) throws LicenseXmlException {
		this.xmlDocument = xmlDocument;
	}

	/**
	 * Will skip deprecated licenses
	 * @return
	 * @throws InvalidSPDXAnalysisException
	 * @throws LicenseXmlException
	 */
	public List<ListedLicenseContainer> getListedLicenses() throws InvalidSPDXAnalysisException, LicenseXmlException {
		List<ListedLicenseContainer> retval = new ArrayList<ListedLicenseContainer>();
		Element rootElement = this.xmlDocument.getDocumentElement();
		NodeList licenseElements = rootElement.getElementsByTagName(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_LICENSE);
		for (int i = 0; i < licenseElements.getLength(); i++) {
			Element licenseElement = (Element)(licenseElements.item(i));
			retval.add(getListedLicense(licenseElement));
		}
		return retval;
	}

	private ListedLicenseContainer getListedLicense(Element licenseElement) throws InvalidSPDXAnalysisException, LicenseXmlException {
		String name = licenseElement.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_NAME);
		String id = licenseElement.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_ID);
		boolean deprecated = licenseElement.hasAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_DEPRECATED_VERSION);
		String deprecatedVersion = null;
		if (deprecated) {
			deprecatedVersion = licenseElement.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_DEPRECATED_VERSION);
		}
		NodeList textNodes = licenseElement.getElementsByTagName(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TEXT);
		if (textNodes.getLength() != 1) {
			throw new LicenseXmlException("Invalid number of text elements.  Expected 1 - found "+textNodes.getLength());
		}
		Element textElement = (Element)textNodes.item(0);
		String text = LicenseXmlHelper.getLicenseText(textElement);
		NodeList notes = licenseElement.getElementsByTagName(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_NOTES);
		String comment = null;
		if (notes.getLength() > 0) {
			StringBuilder commentBuilder = new StringBuilder(LicenseXmlHelper.getNoteText((Element)(notes.item(0))));
			for (int i = 1; i < notes.getLength(); i++) {
				commentBuilder.append("; ");
				commentBuilder.append(LicenseXmlHelper.getNoteText((Element)(notes.item(i))));
			}
			comment = commentBuilder.toString();
		}
		NodeList urlNodes = licenseElement.getElementsByTagName(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_CROSS_REF);
		ArrayList<String> sourceUrls = new ArrayList<>();
		for (int i = 0; i < urlNodes.getLength(); i++) {
			String sourceUrl = urlNodes.item(i).getTextContent().trim();
			sourceUrls.add(sourceUrl);
		}
		String licenseHeader = null;
		String licenseHeaderTemplate = null;
		String licenseHeaderTemplateHtml = null;
		NodeList headerNodes = licenseElement.getElementsByTagName(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
		if (headerNodes.getLength() > 0) {
			StringBuilder sbText = new StringBuilder();
			StringBuilder sbTemplate = new StringBuilder();
			StringBuilder sbHtml = new StringBuilder();
			sbText.append(LicenseXmlHelper.getHeaderText((Element)headerNodes.item(0)));
			sbTemplate.append(LicenseXmlHelper.getHeaderTemplate((Element)headerNodes.item(0)));
			sbHtml.append(LicenseXmlHelper.getHeaderTextHtml((Element)headerNodes.item(0)));
			for (int i = 1; i < headerNodes.getLength(); i++) {
				sbText.append('\n');
				sbText.append(LicenseXmlHelper.getHeaderText((Element)headerNodes.item(i)));
				sbTemplate.append('\n');
				sbTemplate.append(LicenseXmlHelper.getHeaderTemplate((Element)headerNodes.item(i)));
				sbHtml.append("<br />\n");
				sbHtml.append(LicenseXmlHelper.getHeaderTextHtml((Element)headerNodes.item(i)));
			}
			licenseHeader = sbText.toString();
			licenseHeaderTemplate = sbTemplate.toString();
			licenseHeaderTemplateHtml = sbHtml.toString();
		}
		String template = LicenseXmlHelper.getLicenseTemplate(textElement);
		boolean osiApproved;
		if (licenseElement.hasAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_OSI_APPROVED)) {
			osiApproved = "true".equals(licenseElement.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_OSI_APPROVED).toLowerCase());
		} else {
			osiApproved = false;
		}
		boolean fsfLibre;
		if (licenseElement.hasAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_FSF_LIBRE)) {
			fsfLibre = "true".equals(licenseElement.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_FSF_LIBRE).toLowerCase());
		} else {
			fsfLibre = false;
		}
		String licenseHtml = LicenseXmlHelper.getLicenseTextHtml(textElement);
		org.spdx.library.model.v2.license.SpdxListedLicense licv2 = new org.spdx.library.model.v2.license.SpdxListedLicense(v2ModelStore, 
				SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX, id, copyManager, true);
		ListedLicense licv3 = new ListedLicense(v3ModelStore, SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX + id,
				copyManager, true, null);
		licv2.setName(name);
		licv3.setName(name);
		licv2.setLicenseText(text);
		licv3.setLicenseText(text);
		licv2.setSeeAlso(sourceUrls);
		licv3.getSeeAlsos().addAll(sourceUrls);
		licv2.setComment(comment);
		licv3.setComment(comment);
		licv2.setStandardLicenseHeader(licenseHeader);
		licv3.setStandardLicenseHeader(licenseHeader);
		licv2.setStandardLicenseTemplate(template);
		licv3.setStandardLicenseTemplate(template);
		licv2.setOsiApproved(osiApproved);
		licv3.setIsOsiApproved(osiApproved);
		licv2.setFsfLibre(fsfLibre);
		licv3.setIsFsfLibre(fsfLibre);
		licv2.setLicenseTextHtml(licenseHtml);
		licv2.setDeprecated(deprecated);
		licv3.setIsDeprecatedLicenseId(deprecated);
		licv2.setDeprecatedVersion(deprecatedVersion);
		licv3.setDeprecatedVersion(deprecatedVersion);
		licv2.setLicenseHeaderHtml(licenseHeaderTemplateHtml);
		licv2.setStandardLicenseHeaderTemplate(licenseHeaderTemplate);
		int i = 0;
		for (String sourceUrl:sourceUrls) {
			org.spdx.library.model.v2.license.CrossRef crossRef = licv2.createCrossRef(sourceUrl)
					.setOrder(i++)
					.build();
			licv2.getCrossRef().add(crossRef);
		}
		return new ListedLicenseContainer(licv2, licv3);
	}

	/**
	 * @return
	 * @throws LicenseXmlException
	 * @throws InvalidSPDXAnalysisException 
	 */
	public List<ListedExceptionContainer> getLicenseExceptions() throws LicenseXmlException, InvalidSPDXAnalysisException {
		List<ListedExceptionContainer> retval = new ArrayList<>();
		Element rootElement = this.xmlDocument.getDocumentElement();
		NodeList exceptionElements = rootElement.getElementsByTagName(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_EXCEPTION);
		for (int i = 0; i < exceptionElements.getLength(); i++) {
			Element exceptionElement = (Element)(exceptionElements.item(i));
			retval.add(getException(exceptionElement));
		}
		return retval;
	}

	private ListedExceptionContainer getException(Element exceptionElement) throws LicenseXmlException, InvalidSPDXAnalysisException {
		String name = exceptionElement.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_NAME);
		String id = exceptionElement.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_ID);
		boolean deprecated = exceptionElement.hasAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_DEPRECATED_VERSION);
		String deprecatedVersion = null;
		if (deprecated) {
			deprecatedVersion = exceptionElement.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_DEPRECATED_VERSION);
		}
		NodeList textNodes = exceptionElement.getElementsByTagName(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TEXT);
		if (textNodes.getLength() != 1) {
			throw new LicenseXmlException("Invalid number of text elements.  Expected 1 - found "+textNodes.getLength());
		}
		Element textElement = (Element)textNodes.item(0);
		String text = LicenseXmlHelper.getLicenseText(textElement);
		String template = LicenseXmlHelper.getLicenseTemplate(textElement);
		String html = LicenseXmlHelper.getLicenseTextHtml(textElement);
		NodeList notes = exceptionElement.getElementsByTagName(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_NOTES);
		String comment = null;
		if (notes.getLength() > 0) {
			StringBuilder commentBuilder = new StringBuilder(LicenseXmlHelper.getNoteText((Element)(notes.item(0))));
			for (int i = 1; i < notes.getLength(); i++) {
				commentBuilder.append("; ");
				commentBuilder.append(LicenseXmlHelper.getNoteText((Element)(notes.item(i))));
			}
			comment = commentBuilder.toString();
		}
		NodeList urlNodes = exceptionElement.getElementsByTagName(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_CROSS_REF);
		List<String> sourceUrls = new ArrayList<>();
		for (int i = 0; i < urlNodes.getLength(); i++) {
			sourceUrls.add(urlNodes.item(i).getTextContent().trim());
		}
		org.spdx.library.model.v2.license.ListedLicenseException exceptionV2 = new org.spdx.library.model.v2.license.ListedLicenseException(v2ModelStore, 
				SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX, id, copyManager, true);
		ListedLicenseException exceptionV3 = new ListedLicenseException(v3ModelStore, SpdxConstantsCompatV2.LISTED_LICENSE_NAMESPACE_PREFIX + id,
				copyManager, true, null);
		exceptionV2.setName(name);
		exceptionV3.setName(name);
		exceptionV2.setLicenseExceptionText(text);
		exceptionV3.setAdditionText(text);
		exceptionV2.setLicenseExceptionTemplate(template);
		exceptionV3.setStandardAdditionTemplate(template);
		exceptionV2.setSeeAlso(sourceUrls);
		exceptionV3.getSeeAlsos().addAll(sourceUrls);
		exceptionV2.setComment(comment);
		exceptionV3.setComment(comment);
		exceptionV2.setExceptionTextHtml(html);
		exceptionV2.setDeprecated(deprecated);
		exceptionV3.setIsDeprecatedAdditionId(deprecated);
		exceptionV2.setDeprecatedVersion(deprecatedVersion);
		exceptionV3.setDeprecatedVersion(deprecatedVersion);
		return new ListedExceptionContainer(exceptionV2, exceptionV3);
	}

}
