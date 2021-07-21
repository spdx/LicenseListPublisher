/**
 * Copyright (c) 2011 Source Auditor Inc.
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
package org.spdx.htmltemplates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringEscapeUtils;
import org.spdx.crossref.Live;
import org.spdx.crossref.Timestamp;
import org.spdx.crossref.Valid;
import org.spdx.crossref.Wayback;
import org.spdx.library.InvalidSPDXAnalysisException;
import org.spdx.library.model.license.CrossRef;
import org.spdx.library.model.license.SpdxListedLicense;
import org.spdx.licenseTemplate.InvalidLicenseTemplateException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This class contains a formatted HTML file for a given license.  Specific
 * formatting information is contained in this file.
 * @author Gary O'Neall
 *
 */
public class LicenseHTMLFile {

	static final String TEMPLATE_CLASS_PATH = "resources" + "/" + "htmlTemplate";
	static final String TEMPLATE_ROOT_PATH = "resources" + File.separator + "htmlTemplate";
	static final String TEMPLATE_FILE_NAME = "LicenseHTMLTemplate.html";
	static final boolean USE_SITE = false;	// set to true to use the site name for the link of external web pages

	static Comparator<CrossRef> licenseComparator =	new Comparator<CrossRef>() {

        @Override
        public int compare(CrossRef o1, CrossRef o2) {
            Optional<Integer> order1;
            try {
                order1 = o1.getOrder();
            } catch (InvalidSPDXAnalysisException e) {
                order1 = Optional.empty();
            }
            Optional<Integer> order2;
            try {
                order2 = o2.getOrder();
            } catch (InvalidSPDXAnalysisException e) {
                order2 = Optional.empty();;
            }
            if (order1.isPresent()) {
                if (order2.isPresent()) {
                    return order1.get().compareTo(order2.get());
                } else {
                    return -1;
                }
            } else {
                if (order2.isPresent()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }
    };
    
	/**
	 * Parses a URL and stores the site name and the original URL
	 * @author Gary O'Neall
	 *
	 */
	public static class FormattedUrl {
		String url;
		/*		
		 * license crossref information in the form of an array of strings. 
		 * With the strings being of the form "{a:b, c:b}"
		*/
		String[] licenseCrossRefs;
		
		Boolean isValid;
		Boolean isLive;
		Boolean isWayBackLink;
		String match;
		String timestamp;
		
		public FormattedUrl(String url) {
			this.url = url;
			this.licenseCrossRefs = null;
		}
		public FormattedUrl(String url, String [] licenseCrossRefs) {
			this.url = url;
			this.licenseCrossRefs = licenseCrossRefs;
		}
		public FormattedUrl(Optional<String> url, Optional<Boolean> isValid, Optional<Boolean> isLive, 
				Optional<Boolean> isWayBackLink, Optional<String> match, Optional<String> timestamp) {
			this.url = (url.isPresent()) ? url.get() : "N/A";
			this.isValid = (isValid.isPresent()) ? isValid.get() : false;
			this.isLive = (isLive.isPresent()) ? isLive.get() : false;
			this.isWayBackLink = (isWayBackLink.isPresent()) ? isWayBackLink.get() : false;
			this.match = (match.isPresent()) ? match.get() : "N/A";
			this.timestamp = (timestamp.isPresent()) ? timestamp.get() : "N/A";
		}
		public String getUrl() {
			return this.url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		
		public boolean getIsValid() {
			if(isValid != null) {
				return isValid;
			}
			return Valid.urlValidator(url);
		}

		public boolean getIsLive() {
			if(isLive != null) {
				return isLive;
			}
			return Live.urlLinkExists(url);
		}

		public String getMatch() {
			if(match != null) {
				return match;
			}
			return "--";
		}

		public boolean getIsWayBackLink() {
			if(isWayBackLink != null) {
				return isWayBackLink;
			}
			return Wayback.isWayBackUrl(url);
		}

		public String getTimestamp() {
			if(timestamp != null) {
				return timestamp;
			}
			return Timestamp.getTimestamp();
		}

	}
	private SpdxListedLicense license;
	/**
	 * @param license Listed license to be used
	 */
	public LicenseHTMLFile(SpdxListedLicense license) {
		this.license = license;
	}

	public LicenseHTMLFile() {
		this(null);
	}

	/**
	 * @return the license
	 */
	public SpdxListedLicense getLicense() {
		return license;
	}

	/**
	 * @param license the license to set
	 */
	public void setLicense(SpdxListedLicense license) {
		this.license = license;
	}

	public void writeToFile(File htmlFile, String tableOfContentsReference) throws IOException, MustacheException, InvalidLicenseTemplateException, InvalidSPDXAnalysisException {
		FileOutputStream stream = null;
		OutputStreamWriter writer = null;
		if (!htmlFile.exists()) {
			if (!htmlFile.createNewFile()) {
				throw(new IOException("Can not create new file "+htmlFile.getName()));
			}
		}
		String templateDirName = TEMPLATE_ROOT_PATH;
		File templateDirectoryRoot = new File(templateDirName);
		if (!(templateDirectoryRoot.exists() && templateDirectoryRoot.isDirectory())) {
			templateDirName = TEMPLATE_CLASS_PATH;
		}
		try {
			stream = new FileOutputStream(htmlFile);
			writer = new OutputStreamWriter(stream, "UTF-8");
			DefaultMustacheFactory builder = new DefaultMustacheFactory(templateDirName);
	        Map<String, Object> mustacheMap = buildMustachMap();
	        Mustache mustache = builder.compile(TEMPLATE_FILE_NAME);
	        mustache.execute(writer, mustacheMap);
		} finally {
			if (writer != null) {
				writer.close();
			}
			if (stream != null) {
				stream.close();
			}
		}
	}
	
	/**
	 * @return
	 * @throws InvalidSPDXAnalysisException 
	 * @throws
	 * @throws LicenseTemplateRuleException
	 */
	private Map<String, Object> buildMustachMap() throws InvalidLicenseTemplateException, InvalidSPDXAnalysisException {
		Map<String, Object> retval = Maps.newHashMap();
		if (license != null) {
			retval.put("licenseId", license.getLicenseId());
			String licenseTextHtml = license.getLicenseTextHtml();
			retval.put("licenseText", licenseTextHtml);
			retval.put("licenseName", license.getName());
			String notes;
			if (license.getComment() != null && !license.getComment().isEmpty()) {
				notes = license.getComment();
			} else {
				notes = null;
			}
			String templateText = license.getStandardLicenseTemplate();
			if (templateText == null) {
				templateText = StringEscapeUtils.escapeHtml4(license.getLicenseText());
			}
			retval.put("standardLicenseTemplate", templateText);
			retval.put("notes", notes);
			retval.put("osiApproved", license.isOsiApproved());
			retval.put("fsfLibre", license.isFsfLibre());
			retval.put("notFsfLibre", license.isNotFsfLibre());
			List<FormattedUrl> otherWebPages = Lists.newArrayList();
			try {
				List<CrossRef> crossRefCopy = new ArrayList<>();
				for (CrossRef crossRef:license.getCrossRef()) {
					crossRefCopy.add(crossRef);
				}
				Collections.sort(crossRefCopy, licenseComparator);

					for (CrossRef crossRef:crossRefCopy) {
						otherWebPages.add(new FormattedUrl(crossRef.getUrl(), crossRef.getValid(), 
								crossRef.getLive(), crossRef.getIsWayBackLink(), 
								crossRef.getMatch(), crossRef.getTimestamp()));
					}
			} catch (InvalidSPDXAnalysisException e) {
				throw new InvalidLicenseTemplateException("Error getting crossRefs",e);
			}
			if (otherWebPages.size() == 0) {
				otherWebPages = null;	// Force the template to print None
			}
			retval.put("otherWebPages", otherWebPages);
			retval.put("title", license.getName());
			String header = license.getLicenseHeaderHtml();
			if (header != null && header.trim().isEmpty()) {
				header = null;	// so the template will appropriately skip the header text
			}
			retval.put("licenseHeader", header);
		    retval.put("deprecated", this.license.isDeprecated());
	        retval.put("deprecatedVersion", this.license.getDeprecatedVersion());
		}
		return retval;
	}

}
