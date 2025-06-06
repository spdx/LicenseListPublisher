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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.license.SpdxListedLicense;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;

/**
 * This class holds a formatted HTML file for a license table of contents
 * @author Gary O'Neall
 *
 */
public class LicenseTOCHTMLFile {

	static final String HTML_TEMPLATE = "TocHTMLTemplate.html";

	public static class DeprecatedLicense {
		private String reference;
		private String refNumber;
		private String licenseId;
		private String licenseName;
		private String deprecatedVersion;

		public DeprecatedLicense(String reference, String refNumber,
				String licenseId, String licenseName, String deprecatedVersion) {
			this.reference = reference;
			this.refNumber = refNumber;
			this.licenseId = licenseId;
			this.licenseName = licenseName;
			this.deprecatedVersion = deprecatedVersion;
		}

		/**
		 * @return the reference
		 */
		public String getReference() {
			return reference;
		}

		/**
		 * @param reference the reference to set
		 */
		public void setReference(String reference) {
			this.reference = reference;
		}

		/**
		 * @return the refNumber
		 */
		public String getRefNumber() {
			return refNumber;
		}

		/**
		 * @param refNumber the refNumber to set
		 */
		public void setRefNumber(String refNumber) {
			this.refNumber = refNumber;
		}

		/**
		 * @return the licenseId
		 */
		public String getLicenseId() {
			return licenseId;
		}

		/**
		 * @param licenseId the licenseId to set
		 */
		public void setLicenseId(String licenseId) {
			this.licenseId = licenseId;
		}

		/**
		 * @return the licenseName
		 */
		public String getLicenseName() {
			return licenseName;
		}

		/**
		 * @param licenseName the licenseName to set
		 */
		public void setLicenseName(String licenseName) {
			this.licenseName = licenseName;
		}

		/**
		 * @return the deprecatedVersion
		 */
		public String getDeprecatedVersion() {
			return deprecatedVersion;
		}

		/**
		 * @param deprecatedVersion the deprecatedVersion to set
		 */
		public void setDeprecatedVersion(String deprecatedVersion) {
			this.deprecatedVersion = deprecatedVersion;
		}
	}

	public static class ListedSpdxLicense {
		private String reference;
		private String refNumber;
		private String licenseId;
		private String osiApproved;
		private final String fsfLibre;
		private String licenseName;

		public ListedSpdxLicense() {
			reference = null;
			refNumber = null;
			licenseId = null;
			osiApproved = null;
			licenseName = null;
			fsfLibre = null;
		}

		public ListedSpdxLicense(String reference, String refNumber,
				String licenseId, boolean isOsiApproved, Boolean fsfLibre, String licenseName) {
			this.reference = reference;
			this.refNumber = refNumber;
			this.licenseId = licenseId;
			if (isOsiApproved) {
				this.osiApproved = "Y";
			} else {
				this.osiApproved = "";
			}
			if (fsfLibre != null && fsfLibre) {
				this.fsfLibre = "Y";
			} else {
				this.fsfLibre = "";
			}
			this.licenseName = licenseName;
		}

		/**
		 * @return the reference
		 */
		public String getReference() {
			return reference;
		}

		/**
		 * @param reference the reference to set
		 */
		public void setReference(String reference) {
			this.reference = reference;
		}

		/**
		 * @return the refNumber
		 */
		public String getRefNumber() {
			return refNumber;
		}

		/**
		 * @param refNumber the refNumber to set
		 */
		public void setRefNumber(String refNumber) {
			this.refNumber = refNumber;
		}

		/**
		 * @return the licenseId
		 */
		public String getLicenseId() {
			return licenseId;
		}

		/**
		 * @param licenseId the licenseId to set
		 */
		public void setLicenseId(String licenseId) {
			this.licenseId = licenseId;
		}

		/**
		 * @return the osiApproved
		 */
		public String getOsiApproved() {
			return osiApproved;
		}

		public String getFsfLibre() {
			return fsfLibre;
		}

		/**
		 * @param osiApproved the osiApproved to set
		 */
		public void setOsiApproved(String osiApproved) {
			this.osiApproved = osiApproved;
		}

		/**
		 * @return the licenseName
		 */
		public String getLicenseName() {
			return licenseName;
		}

		/**
		 * @param licenseName the licenseName to set
		 */
		public void setLicenseName(String licenseName) {
			this.licenseName = licenseName;
		}
	}

	List<ListedSpdxLicense> listedLicenses = new ArrayList<>();
	List<DeprecatedLicense> deprecatedLicenses = new ArrayList<>();

      private int currentRefNumber = 1;

      String version;
      String releaseDate;

      private String generateVersionString(String version, String releaseDate) {
    	  if (version == null || version.trim().isEmpty()) {
    		  return "";
    	  }
    	  String retval = version.trim();
    	  if (releaseDate != null && !releaseDate.trim().isEmpty()) {
    		  retval = retval + " "+ releaseDate.trim();
    	  }
    	  return retval;
      }
      public LicenseTOCHTMLFile(String version, String releaseDate) {
    	  this.version = version;
    	  this.releaseDate = releaseDate.substring(0, 10);
      }

	public void addLicense(SpdxListedLicense license, String licHTMLReference) throws InvalidSPDXAnalysisException {
		listedLicenses.add(new ListedSpdxLicense(licHTMLReference, String.valueOf(this.currentRefNumber),
				license.getLicenseId(), license.isOsiApproved(), license.getFsfLibre(), license.getName()));
		currentRefNumber++;
	}

	public void writeToFile(File htmlFile) throws IOException, MustacheException {
        if (!htmlFile.exists()) {
			if (!htmlFile.createNewFile()) {
				throw(new IOException("Can not create new file "+htmlFile.getName()));
			}
		}

        try (FileOutputStream stream = new FileOutputStream(htmlFile); OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8")) {
            DefaultMustacheFactory builder = new DefaultMustacheFactory(Utility.getMustacheResolver());
            Map<String, Object> mustacheMap = buildMustachMap();
            Mustache mustache = builder.compile(HTML_TEMPLATE);
            mustache.execute(writer, mustacheMap);
        }
	}
	/**
	 * Build the a hash map to map the variables in the template to the values
	 * @return the map build from the license
	 */
	private Map<String, Object> buildMustachMap() {
		Map<String, Object> retval = new HashMap<>();
		retval.put("version", generateVersionString(version, releaseDate));
		this.listedLicenses.sort(new Comparator<ListedSpdxLicense>() {

			@Override
			public int compare(ListedSpdxLicense arg0, ListedSpdxLicense arg1) {
				return arg0.getLicenseId().compareToIgnoreCase(arg1.getLicenseId());
			}

		});
		retval.put("listedLicenses", this.listedLicenses);
		this.deprecatedLicenses.sort(new Comparator<DeprecatedLicense>() {

			@Override
			public int compare(DeprecatedLicense arg0, DeprecatedLicense arg1) {
				return arg0.getLicenseId().compareToIgnoreCase(arg1.getLicenseId());
			}
			
		});
		retval.put("deprecatedLicenses", this.deprecatedLicenses);
		return retval;
	}
	/**
	 * @param deprecatedLicense
	 * @param licHTMLReference
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void addDeprecatedLicense(SpdxListedLicense deprecatedLicense,
			String licHTMLReference) throws InvalidSPDXAnalysisException {
		deprecatedLicenses.add(new DeprecatedLicense(licHTMLReference, String.valueOf(this.currentRefNumber),
				deprecatedLicense.getLicenseId(),
				deprecatedLicense.getName(),
				deprecatedLicense.getDeprecatedVersion()));
		currentRefNumber++;
	}
}
