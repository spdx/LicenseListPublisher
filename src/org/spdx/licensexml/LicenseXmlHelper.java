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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.library.model.v2.SpdxConstantsCompatV2;
import org.spdx.licenseTemplate.HtmlTemplateOutputHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Static helper class for License XML parsing
 * @author Gary O'Neall
 *
 */
public class LicenseXmlHelper {
	static final Logger logger = LoggerFactory.getLogger(LicenseXmlHelper.class);
	
	/**
	 * Maximum number of characters in a copyright
	 */
	private static final int MAX_COPYRIGHT_LENGTH = 5000;
	/**
	 * Maximum number of characters in a bullet
	 */
	private static final int MAX_BULLET_LENGTH = 20;

	private static final String INDENT_STRING = "   ";

	private static final String BULLET_ALT_MATCH = ".{0," + Integer.toString(MAX_BULLET_LENGTH) + "}";

	private static final String BULLET_ALT_NAME = "bullet";

	private static final String COPYRIGHT_ALT_MATCH = ".{0," + Integer.toString(MAX_COPYRIGHT_LENGTH) + "}";

	private static final String COPYRIGHT_ALT_NAME = "copyright";

	//Spacing attributes
	//TODO: Move these to SpdxConstantsCompatV2.java
	private static final String SPACING_ATTRIBUTE = "spacing";
	private static final String SPACING_BOTH = "both";
	private static final String SPACING_BEFORE = "before";
	private static final String SPACING_AFTER = "after";
	private static final String SPACING_NONE = "none";
	private static final String SPACING_DEFAULT = "default";

	/**
	 * The text under these elements are included without modification
	 */
	static HashSet<String> LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS = new HashSet<String>();
	static {
		LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_COPYRIGHT_TEXT);
		LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_ITEM);
		LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TEXT);
		LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
	}

	/**
	 * The text under these elements are included without modification
	 */
	static Set<String> HEADER_UNPROCESSED_TAGS = new HashSet<>();
	static {
		HEADER_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_COPYRIGHT_TEXT);
		HEADER_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TITLE_TEXT);
		HEADER_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_ITEM);
		HEADER_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_BULLET);
		HEADER_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER);
	}

	static Set<String> NOTES_UNPROCESSED_TAGS = new HashSet<>();
	static {
		NOTES_UNPROCESSED_TAGS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_NOTES);
	}

	static Set<String> FLOW_CONTROL_ELEMENTS = new HashSet<>();
	static {
		FLOW_CONTROL_ELEMENTS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_LIST);
		FLOW_CONTROL_ELEMENTS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_PARAGRAPH);
	}

	/**
	 * Elements which will create alt text
	 */
	static Set<String> ALT_ELEMENTS = new HashSet<>();
	static {
		ALT_ELEMENTS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_ALT);
		ALT_ELEMENTS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_COPYRIGHT_TEXT);
		ALT_ELEMENTS.add(SpdxConstantsCompatV2.LICENSEXML_ELEMENT_BULLET);
	}

	static String DOUBLE_QUOTES_REGEX = "(\\u201C|\\u201D)";
	static String SINGLE_QUOTES_REGEX = "(\\u2018|\\u2019)";

	/**
	 * Convert a node to text which contains various markup information and appends it to the sb
	 * @param node node to convert
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @param noSpace if true, do not add any spaces before the text
	 * @return true if no space should be added to the next text element
	 * @throws LicenseXmlException
	 */
	private static boolean appendNodeText(Node node, boolean useTemplateFormat, StringBuilder sb, int indentCount, Set<String> unprocessedTags,
			boolean includeHtmlTags, boolean noSpace) throws LicenseXmlException {
		boolean noNextSpace = false;
		if (node.getNodeType() == Node.TEXT_NODE) {
			if (includeHtmlTags) {
				sb.append(StringEscapeUtils.escapeXml10(fixUpText(node.getNodeValue())));
			} else {
				appendNormalizedWhiteSpaceText(sb, node.getNodeValue(), noSpace);
			}
		} else if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element)node;
			String tagName = element.getTagName();
			if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_LIST.equals(tagName)) {
				appendListElements(element, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags);
			} else if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_ALT.equals(tagName)) {
				if (!element.hasAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_ALT_NAME)) {
					throw(new LicenseXmlException("Missing name attribute for variable text"));
				}
				String altName = element.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_ALT_NAME);
				if (!element.hasAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_ALT_MATCH)) {
					throw(new LicenseXmlException("Missing match attribute for variable text"));
				}
				String match = element.getAttribute(SpdxConstantsCompatV2.LICENSEXML_ATTRIBUTE_ALT_MATCH);
				noNextSpace = appendAltText(element, altName, match, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags, noNextSpace);
			} else if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_OPTIONAL.equals(tagName)) {
				noNextSpace = appendOptionalText(element, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags, noSpace);
			} else if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_BREAK.equals(tagName)) {
				if (includeHtmlTags) {
					sb.append("<br />");
				}
				addNewline(sb, indentCount);
				// There really shouldn't be any children as the tag must be an empty element for HTML, but this currently isn't enforced in the schema
				if (element.getChildNodes().getLength() > 0) {
					throw new LicenseXmlException("Non-empty <br> tag found");
				}
			} else if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_PARAGRAPH.equals(tagName)) {
				if (includeHtmlTags) {
					appendParagraphTag(sb, indentCount);
				} else if (sb.length() > 1) {
					addNewline(sb, indentCount);
				}
				appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags);
				if (includeHtmlTags) {
					sb.append("</p>\n");
				} else {
				    addNewline(sb, indentCount);
				    addNewline(sb, indentCount);	// extra lines between paragraphs
				}
			} else if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TITLE_TEXT.equals(tagName)) {
				if (!inALtBlock(element)) {
					appendOptionalText(element, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags, noSpace);
					noNextSpace = false;
				} else {
					appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags);
				}
			} else if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_COPYRIGHT_TEXT.equals(tagName)) {
				if (!inALtBlock(element)) {
					appendAltText(element, COPYRIGHT_ALT_NAME, COPYRIGHT_ALT_MATCH, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags, noNextSpace);
					noNextSpace = false;
				} else {
					appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags);
				}
			} else if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_BULLET.equals(tagName)) {
				if (!inALtBlock(element)) {
					appendAltText(element, BULLET_ALT_NAME, BULLET_ALT_MATCH, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags, noNextSpace);
					noNextSpace = false;
				} else {
					appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags);
				}
			} else if (unprocessedTags.contains(tagName)) {
				appendElementChildrenText(element, useTemplateFormat, sb, indentCount, unprocessedTags, includeHtmlTags);
			} else {
				throw(new LicenseXmlException("Unknown license element tag name: "+tagName));
			}
		}
		return noNextSpace;
	}

	/**
	 * @param element
	 * @return true if the element is a child of an alt block
	 */
	private static boolean inALtBlock(Element element) {
		Node parent = element.getParentNode();
		while (parent != null) {
			if (parent.getNodeType() == Node.ELEMENT_NODE && ALT_ELEMENTS.contains(((Element)(parent)).getTagName())) {
				return true;
			}
			parent = parent.getParentNode();
		}
		return false;
	}

	/**
	 * Create a paragraph tag with the appropriate indentation
	 * @param sb
	 * @param indentCount
	 */
	private static void appendParagraphTag(StringBuilder sb, int indentCount) {
		sb.append("<p>");
	}

	/**<
	 * Appends text removing any extra whitespace and linefeed information
	 * @param sb String builds to append to
	 * @param text text to append
	 * @param noSpace if true, do not add any spaces before the text
	 */
	private static void appendNormalizedWhiteSpaceText(StringBuilder sb, String text, boolean noSpace) {
		boolean endsInWhiteSpace = sb.length() == 0 || Character.isWhitespace(sb.charAt(sb.length()-1));
		List<String> tokens = tokenize(text);
		if (tokens.size() > 0) {
			if (!endsInWhiteSpace && !noSpace) {
				sb.append(' ');
			}
			sb.append(tokens.get(0));
			for (int i = 1; i < tokens.size(); i++) {
				sb.append(' ');
				sb.append(tokens.get(i));
			}
		}
	}

	/**
	 * Tokenize a string based on the Character whitespace
	 * @param text
	 * @return
	 */
	private static List<String> tokenize(String text) {
		List<String> result = new ArrayList<String>();
		int loc = 0;
		while (loc < text.length()) {
			while (loc < text.length() && Character.isWhitespace(text.charAt(loc))) {
				loc++;
			}
			if (loc < text.length()) {
				StringBuilder sb = new StringBuilder();
				while (loc < text.length() && !Character.isWhitespace(text.charAt(loc))) {
					sb.append(text.charAt(loc++));
				}
				result.add(sb.toString());
			}
		}
		return result;
	}

	/**
	 * Appends the text for all the child nodes in the element
	 * @param element Element to convert
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @throws LicenseXmlException
	 */
	private static void appendElementChildrenText(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount, Set<String> unprocessedTags,
			boolean includeHtmlTags) throws LicenseXmlException {
		NodeList licenseChildNodes = element.getChildNodes();
		boolean noSpace = false;
		for (int i = 0; i < licenseChildNodes.getLength(); i++) {
			noSpace = appendNodeText(licenseChildNodes.item(i),useTemplateFormat, sb, indentCount,
					unprocessedTags, includeHtmlTags, noSpace);
		}
	}

	/**
	 * Add a newline to the stringbuilder and indent per the indent count
	 * @param sb Stringbuild to append to
	 * @param indentCount
	 */
	private static void addNewline(StringBuilder sb, int indentCount) {
		sb.append('\n');
		for (int i = 0; i < indentCount; i ++) {
			sb.append(INDENT_STRING);
		}
	}

	/**
	 * Append optional text
	 * @param element Element element containing the optional text
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @param noSpace true if no space should be added prior to this text element
	 * @return true if no space should be added before the next text element
	 * @throws LicenseXmlException
	 */
	private static boolean appendOptionalText(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount, Set<String> unprocessedTags,
			boolean includeHtmlTags, boolean noSpace) throws LicenseXmlException {
		StringBuilder childSb = new StringBuilder();
		String spacing = SPACING_DEFAULT;
		if (element.hasAttribute(SPACING_ATTRIBUTE)) {
			spacing = element.getAttribute(SPACING_ATTRIBUTE);
		}
		if (!(SPACING_DEFAULT.equals(spacing) || SPACING_BOTH.equals(spacing) || SPACING_BEFORE.equals(spacing) || SPACING_AFTER.equals(spacing) || SPACING_NONE.equals(spacing))) {
			throw new LicenseXmlException("Invalid spacing attribute for optional text: "+spacing);
		}
		if (element.hasChildNodes()) {
			appendElementChildrenText(element, useTemplateFormat, childSb, indentCount, unprocessedTags, includeHtmlTags);
		} else {
			childSb.append(element.getTextContent());
		}
		if (useTemplateFormat) {
			boolean appendLeadingSpace = false;
			if (childSb.length() > 0 && childSb.charAt(0) == ' ') {
				appendLeadingSpace = true;
				childSb.delete(0, 1);
			} else if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1)) &&
					!noSpace && (SPACING_BOTH.equals(spacing) || SPACING_BEFORE.equals(spacing) || SPACING_DEFAULT.equals(spacing))) {
				appendLeadingSpace = true;
			}
			sb.append("<<beginOptional>>");
			if (appendLeadingSpace) sb.append(' ');
			sb.append(childSb);
			if (SPACING_BOTH.equals(spacing) || SPACING_AFTER.equals(spacing)) {
				sb.append(' ');
			}
			sb.append("<<endOptional>>");
		} else if (includeHtmlTags) {
			if (includesFlowControl(element)) {
				sb.append("<div class=\"");
			} else {
				sb.append("<var class=\"");
			}

			sb.append(HtmlTemplateOutputHandler.OPTIONAL_LICENSE_TEXT_CLASS);
			sb.append("\">");
			if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1)) &&
                    !noSpace && (SPACING_BOTH.equals(spacing) || SPACING_BEFORE.equals(spacing) || SPACING_DEFAULT.equals(spacing))) {
                sb.append(' ');
            }
			sb.append(childSb.toString());
			if (SPACING_BOTH.equals(spacing) || SPACING_AFTER.equals(spacing)) {
                sb.append(' ');
            }
			if (includesFlowControl(element)) {
				sb.append("</div>");
			} else {
				sb.append("</var>");
			}
		} else {
			if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1)) &&
					!noSpace && (SPACING_BOTH.equals(spacing) || SPACING_BEFORE.equals(spacing) || SPACING_DEFAULT.equals(spacing))) {
				sb.append(' ');
			}
			sb.append(childSb);
			if (SPACING_BOTH.equals(spacing) || SPACING_AFTER.equals(spacing)) {
				sb.append(' ');
			}
		}
		return SPACING_BEFORE.equals(spacing) || SPACING_NONE.equals(spacing);
	}

	/**
	 * @param element parent element
	 * @return true if the element includes any flow control content per https://www.w3.org/TR/2014/REC-html5-20141028/dom.html#phrasing-content-1
	 */
	private static boolean includesFlowControl(Element element) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element eChild = (Element)child;
				if (FLOW_CONTROL_ELEMENTS.contains(eChild.getTagName())) {
					return true;
				} else {
					if (includesFlowControl(eChild)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Add text for an alternative expression
	 * @param element Element containing the alternative expression
	 * @param altName Name for the alt / var text
	 * @param match Regex pattern match string for the alternate text
	 * @param useTemplateFormat If true, convert any optional or variable elements into the template markup language
	 * if false, translate to the equivalent text
	 * @param sb Stringbuilder to append the text to
	 * @param indentCount number of indentations (e.g. number of embedded lists)
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @param noSpace true if no space should be added prior to this text element
	 * @return true if no space should be added before the next text element
	 * @throws LicenseXmlException
	 */
	private static boolean appendAltText(Element element, String altName, String match,
			boolean useTemplateFormat, StringBuilder sb, int indentCount, Set<String> unprocessedTags,
			boolean includeHtmlTags, boolean noSpace) throws LicenseXmlException {
		String spacing = SPACING_DEFAULT;
		if (element.hasAttribute(SPACING_ATTRIBUTE)) {
			spacing = element.getAttribute(SPACING_ATTRIBUTE);
		}
		if (!(SPACING_DEFAULT.equals(spacing) || SPACING_BOTH.equals(spacing) || SPACING_BEFORE.equals(spacing) || SPACING_AFTER.equals(spacing) || SPACING_NONE.equals(spacing))) {
			throw new LicenseXmlException("Invalid spacing attribute for optional text: "+spacing);
		}
		StringBuilder originalSb = new StringBuilder();
		if (element.hasChildNodes()) {
			appendElementChildrenText(element, useTemplateFormat, originalSb, indentCount,
					unprocessedTags, includeHtmlTags);
		} else {
			originalSb.append(element.getTextContent());
		}
		if (useTemplateFormat) {
			if (originalSb.length() > 0 && originalSb.charAt(0) == ' ') {
				sb.append(' ');
				originalSb.delete(0, 1);
			} else if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1)) &&
					!noSpace && (SPACING_BOTH.equals(spacing) || SPACING_BEFORE.equals(spacing) || SPACING_DEFAULT.equals(spacing))) {
				sb.append(' ');
			}
			sb.append("<<var;name=\"");
			sb.append(altName);
			sb.append("\";original=\"");
			sb.append(originalSb.toString().replaceAll("\n", " "));  // Remove any new lines
			sb.append("\";match=\"");
			sb.append(match);
			sb.append("\">>");
			if (SPACING_BOTH.equals(spacing) || SPACING_AFTER.equals(spacing)) {
				sb.append(' ');
			}
		} else if (includeHtmlTags) {
			if (includesFlowControl(element)) {
				sb.append("<div class=\"");
			} else {
				sb.append("<var class=\"");
			}
			sb.append(HtmlTemplateOutputHandler.REPLACEABLE_LICENSE_TEXT_CLASS);
			sb.append("\"><span title=\"can be replaced with the pattern ");
			sb.append(match);
			sb.append("\">");
			if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1)) &&
                    !noSpace && (SPACING_BOTH.equals(spacing) || SPACING_BEFORE.equals(spacing) || SPACING_DEFAULT.equals(spacing))) {
                sb.append(' ');
            }
			sb.append(originalSb);
			if (SPACING_BOTH.equals(spacing) || SPACING_AFTER.equals(spacing)) {
                sb.append(' ');
            }
			sb.append("</span>");
			if (includesFlowControl(element)) {
				sb.append("</div>");
			} else {
				sb.append("</var>");
			}
		} else {
			if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length()-1)) &&
					!noSpace && (SPACING_BOTH.equals(spacing) || SPACING_BEFORE.equals(spacing) || SPACING_DEFAULT.equals(spacing))) {
				sb.append(' ');
			}
			sb.append(originalSb);
			if (SPACING_BOTH.equals(spacing) || SPACING_AFTER.equals(spacing)) {
				sb.append(' ');
			}
		}
		return SPACING_BEFORE.equals(spacing) || SPACING_NONE.equals(spacing);
	}

	/**
	 * Appends a list element to the stringbuilder sb
	 * @param element
	 * @param useTemplateFormat
	 * @param sb
	 * @param indentCount Number of indentations for the text
	 * @param unprocessedTags Tags that do not require any process - text of the children of that tag should just be appended.
	 * @param includeHtmlTags if true, include HTML tags for creating an HTML fragment including the formatting from the original XML element
	 * @throws LicenseXmlException
	 */
	private static void appendListElements(Element element,
			boolean useTemplateFormat, StringBuilder sb, int indentCount, Set<String> unprocessedTags,
			boolean includeHtmlTags) throws LicenseXmlException {
		if (!SpdxConstantsCompatV2.LICENSEXML_ELEMENT_LIST.equals(element.getTagName())) {
			throw(new LicenseXmlException("Invalid list element tag - expected 'list', found '"+element.getTagName()+"'"));
		}
		if (includeHtmlTags) {
			sb.append("\n<ul style=\"list-style:none\">");
		}
		NodeList listItemNodes = element.getChildNodes();
		for (int i = 0; i < listItemNodes.getLength(); i++) {
			if (listItemNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element listItem = (Element)listItemNodes.item(i);
				if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_ITEM.equals(listItem.getTagName())) {
					if (includeHtmlTags) {
						sb.append("\n<li>");
						appendNodeText(listItem, useTemplateFormat, sb, indentCount + 1, unprocessedTags, includeHtmlTags, false);
						sb.append("</li>");
					} else {
						addNewline(sb, indentCount+1);
						appendNodeText(listItem, useTemplateFormat, sb, indentCount + 1, unprocessedTags, includeHtmlTags, false);
					}

				} else if (SpdxConstantsCompatV2.LICENSEXML_ELEMENT_LIST.equals(listItem.getTagName())) {
					appendListElements(listItem, useTemplateFormat, sb, indentCount+1,
							unprocessedTags, includeHtmlTags);
				} else {
					throw(new LicenseXmlException("Expected only list item tags ('item') or lists ('list') in a list, found "+listItem.getTagName()));
				}
			} else if (listItemNodes.item(i).getNodeType() == Node.TEXT_NODE) {
				appendNodeText(listItemNodes.item(i), useTemplateFormat, sb, indentCount, unprocessedTags,
				        includeHtmlTags, false);
			} else {
			    throw(new LicenseXmlException("Expected only element children for a list element"));
			}
		}
		if (includeHtmlTags) {
			sb.append("\n</ul>");
		}
	}

	/**
	 * Gets the license template text from the license element
	 * @param licenseElement
	 * @return
	 * @throws LicenseXmlException
	 */
	public static String getLicenseTemplate(Element licenseElement) throws LicenseXmlException {
		if (!SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TEXT.equals(licenseElement.getTagName())) {
			throw(new LicenseXmlException("Invalid element tag name - expected '"+SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TEXT+"'"+licenseElement.getTagName()+"'"));
		}
		StringBuilder sb = new StringBuilder();
		appendNodeText(licenseElement, true, sb, 0, LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS, false, false);
		return fixUpText(sb.toString());
	}

	/**
	 * Format note text taking into account line breaks, paragraphs etc.
	 * @param licenseElement
	 * @return
	 * @throws LicenseXmlException
	 */
	public static String getNoteText(Element licenseElement) throws LicenseXmlException {
		if (!SpdxConstantsCompatV2.LICENSEXML_ELEMENT_NOTES.equals(licenseElement.getTagName())) {
			throw(new LicenseXmlException("Invalid element tag name - expected '"+SpdxConstantsCompatV2.LICENSEXML_ELEMENT_NOTES+"'"+licenseElement.getTagName()+"'"));
		}
		StringBuilder sb = new StringBuilder();
		appendNodeText(licenseElement, false, sb, 0, NOTES_UNPROCESSED_TAGS, false, false);
		return sb.toString();
	}

	/**
	 * Gets license text from the license element
	 * @param licenseElement
	 * @return
	 * @throws LicenseXmlException
	 */
	public static String getLicenseText(Element licenseElement) throws LicenseXmlException {
		if (!SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TEXT.equals(licenseElement.getTagName())) {
			throw(new LicenseXmlException("Invalid element tag name - expected '"+SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TEXT+"'"+licenseElement.getTagName()+"'"));
		}
		StringBuilder sb = new StringBuilder();
		appendNodeText(licenseElement, false, sb, 0, LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS, false, false);
		return fixUpText(sb.toString());
	}

	public static String dumpLicenseDom(Element licenseElement) {
		StringBuilder sb = new StringBuilder();
		appendNode(licenseElement, sb, 0);
		return sb.toString();
	}

	/**
	 * @param licenseElement
	 * @param sb
	 */
	private static void appendNode(Node node,
			StringBuilder sb, int indent) {
		for (int i = 0; i  < indent; i++) {
			sb.append(INDENT_STRING);
		}
		sb.append("Node Type: ");
		sb.append(node.getNodeType());
		sb.append(", Node Name: ");
		sb.append(node.getNodeName());
		sb.append(", Node Value: '");
		sb.append(node.getNodeValue());
		sb.append('\'');
		sb.append(", Node Text: '");
		sb.append(node.getTextContent());
		sb.append("'\n");
		if (node.hasChildNodes()) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				appendNode(children.item(i), sb, indent+1);
			}
		}
	}

	/**
	 * @param headerElement
	 * @return header text where headerNode is the root element
	 * @throws LicenseXmlException
	 */
	public static Object getHeaderText(Element headerElement) throws LicenseXmlException {
		if (!SpdxConstantsCompatV2.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER.equals(headerElement.getTagName())) {
			throw(new LicenseXmlException("Invalid element tag name - expected '"+SpdxConstantsCompatV2.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER+"'"+headerElement.getTagName()+"'"));
		}
		StringBuilder sb = new StringBuilder();
		appendNodeText(headerElement, false, sb, 0, HEADER_UNPROCESSED_TAGS, false, false);
		return fixUpText(sb.toString());
	}

	/**
	 * @param headerElement
	 * @return header template where headerNode is the root element
	 * @throws LicenseXmlException
	 */
	public static Object getHeaderTemplate(Element headerElement) throws LicenseXmlException {
		if (!SpdxConstantsCompatV2.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER.equals(headerElement.getTagName())) {
			throw(new LicenseXmlException("Invalid element tag name - expected '"+SpdxConstantsCompatV2.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER+"'"+headerElement.getTagName()+"'"));
		}
		StringBuilder sb = new StringBuilder();
		appendNodeText(headerElement, true, sb, 0, HEADER_UNPROCESSED_TAGS, false, false);
		return fixUpText(sb.toString());
	}

	/**
	 * @param headerElement
	 * @return header html fragment where headerNode is the root element
	 * @throws LicenseXmlException
	 */
	public static Object getHeaderTextHtml(Element headerElement) throws LicenseXmlException {
		if (!SpdxConstantsCompatV2.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER.equals(headerElement.getTagName())) {
			throw(new LicenseXmlException("Invalid element tag name - expected '"+SpdxConstantsCompatV2.LICENSEXML_ELEMENT_STANDARD_LICENSE_HEADER+"'"+headerElement.getTagName()+"'"));
		}
		StringBuilder sb = new StringBuilder();
		appendNodeText(headerElement, false, sb, 0, HEADER_UNPROCESSED_TAGS, true, false);
		return fixUpText(sb.toString());
	}

	/**
	 * @param string
	 * @return Text normalized for different character variations
	 */
	private static String fixUpText(String string) {
		return string.replaceAll(DOUBLE_QUOTES_REGEX, "\"")
		        .replaceAll(SINGLE_QUOTES_REGEX, "'")
		        .replaceAll("\\n\\s*\\n\\s*\\n", "\n\n");
	}

	/**
	 * Get the HTML fragment representing the license text from the license body
	 * @param licenseElement root element containing the license text
	 * @return
	 * @throws LicenseXmlException
	 */
	public static String getLicenseTextHtml(Element licenseElement) throws LicenseXmlException {
		if (!SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TEXT.equals(licenseElement.getTagName())) {
			throw(new LicenseXmlException("Invalid element tag name - expected '"+SpdxConstantsCompatV2.LICENSEXML_ELEMENT_TEXT+"'"+licenseElement.getTagName()+"'"));
		}
		StringBuilder sb = new StringBuilder();
		appendNodeText(licenseElement, false, sb, 0, LICENSE_AND_EXCEPTION_UNPROCESSED_TAGS, true, false);
		return fixUpText(sb.toString());
	}

}
