/**
 * Copyright (c) 2014 Source Auditor Inc.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.library.model.v2.license.ListedLicenseException;
import org.spdx.licenseTemplate.InvalidLicenseTemplateException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;


/**
 * Manages the production of an HTML file based on an SpdxLicenseRestriction (a.k.a License Exception)
 * @author Gary O'Neall
 *
 */
public class ExceptionHtml {

	static final String HTML_TEMPLATE = "ExceptionHTMLTemplate.html";

	Map<String, Object> mustacheMap = new HashMap<>();

	/**
	 * @param exception
	 * @throws InvalidLicenseTemplateException
	 * @throws InvalidSPDXAnalysisException 
	 */
	public ExceptionHtml(ListedLicenseException exception) throws InvalidLicenseTemplateException, InvalidSPDXAnalysisException {
		List<String> alSourceUrls = new ArrayList<>();
		for (String sourceUrl: exception.getSeeAlso()) {
			alSourceUrls.add(sourceUrl);
		}
		mustacheMap.put("name", exception.getName());
		mustacheMap.put("id", exception.getLicenseExceptionId());
		mustacheMap.put("text", exception.getExceptionTextHtml());
		mustacheMap.put("getSourceUrl", alSourceUrls);
		mustacheMap.put("notes", exception.getComment());
		mustacheMap.put("deprecated", exception.isDeprecated());
		mustacheMap.put("deprecatedVersion", exception.getDeprecatedVersion());
	}

	/**
	 * @param exceptionHtmlFile
	 * @param exceptionHtmlTocReference
	 * @throws IOException
	 * @throws MustacheException
	 */
	public void writeToFile(File exceptionHtmlFile,
			String exceptionHtmlTocReference) throws IOException, MustacheException {
		mustacheMap.put("exceptionTocReference", exceptionHtmlTocReference);
        if (!exceptionHtmlFile.exists()) {
			if (!exceptionHtmlFile.createNewFile()) {
				throw(new IOException("Can not create new file "+exceptionHtmlFile.getName()));
			}
		}

        try (FileOutputStream stream = new FileOutputStream(exceptionHtmlFile); OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8")) {
            DefaultMustacheFactory builder = new DefaultMustacheFactory(Utility.getMustacheResolver());
            Mustache mustache = builder.compile(HTML_TEMPLATE);
            mustache.execute(writer, mustacheMap);
        }
	}
}
