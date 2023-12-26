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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.licenselistpublisher.LicenseGeneratorException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Singleton class which parses the OSI JSON file for OSI license data: https://api.opensource.org/licenses/
 * 
 * Schema documentation can be found at https://github.com/OpenSourceOrg/api/blob/master/doc/endpoints.md
 * 
 * @author Gary O'Neall
 *
 */
public class OsiLicenseDataParser {
    
    static final Logger logger = LoggerFactory.getLogger(OsiLicenseDataParser.class);
    
    static final String PROP_USE_ONLY_LOCAL_FILE = "LocalOsiJson";
    static final String PROP_OSI_JSON_URL = "OsiJsonUrl";
    
    static final String OSI_JSON_URL = "https://api.opensource.org/licenses/";
    
    static final String OSI_JSON_FILE_PATH = "resources" + File.separator + "osi-licenses.json";
    static final String OSI_JSON_CLASS_PATH = "osi-licenses.json";
    
    private Map<String, Boolean> licenseIdToOsiApproved;
    private boolean useOnlyLocalFile = false;
    String licenseJsonUrl;
    
    private static OsiLicenseDataParser _instance = null;
    
    private OsiLicenseDataParser() throws LicenseGeneratorException {
        licenseIdToOsiApproved = new HashMap<>();
        useOnlyLocalFile = Boolean.parseBoolean(System.getProperty(PROP_USE_ONLY_LOCAL_FILE, "false"));
        licenseJsonUrl = System.getProperty(PROP_OSI_JSON_URL, OSI_JSON_URL);
        Reader reader = null;
        ClassLoader oldContextCL = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader newClassLoader = getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(newClassLoader);
            InputStream input = null;
            if (!useOnlyLocalFile) {
                // First, try the URL
                try {
                    URL url = new URL(licenseJsonUrl);
                    input = url.openStream();
                } catch (MalformedURLException e) {
                    logger.warn("Invalid OSI JSON URL - using file system");
                    input = null;
                } catch (IOException e) {
                    input = null;
                }
            }
            if (input == null) {
                // try the file system
                try {
                    input = new FileInputStream(OSI_JSON_FILE_PATH);
                } catch (FileNotFoundException e) {
                    logger.warn("Unable to open OSI JSON file, using class path option");
                    input = null;
                }
            }
            if (input == null) {
                try {
                    input = this.getClass().getResourceAsStream(OSI_JSON_CLASS_PATH);
                } catch (Exception e) {
                    logger.error("Unable to open OSI JSON file");
                }
            }
            if (input == null) {
                throw new LicenseGeneratorException("Unable to open input JSON file for OSI License Data");
            }
            reader = new InputStreamReader(input);
            Gson gson = new Gson();
            Type osiLicensesType = new TypeToken<List<OsiLicense>>(){}.getType();

            List<OsiLicense> osiLicenses = gson.fromJson(reader, osiLicensesType);
            for (OsiLicense osiLicense:osiLicenses) {
                List<OsiIdentifier> identifiers = osiLicense.getIdentifiers();
                for (OsiIdentifier identifier:identifiers) {
                    if ("SPDX".equals(identifier.scheme)) {
                        if (licenseIdToOsiApproved.containsKey(identifier.identifier.toLowerCase())) {
                            logger.warn("Duplicate SPDX ID in OSI licenses: "+identifier.identifier);
                        }
                        List<String> keywords = osiLicense.getKeywords();
                        boolean osiApproved = keywords.contains("osi-approved");
                        licenseIdToOsiApproved.put(identifier.identifier.toLowerCase(), osiApproved);
                    }
                }
            }
        } finally {
            if (Objects.nonNull(reader)) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.warn("IO error closing OSI JSON input stream",e);
                }
            }
            Thread.currentThread().setContextClassLoader(oldContextCL);
        }
    }

    /**
     * @return Singleton instance of the OsiLicenseDataParser
     * @throws LicenseGeneratorException
     */
    public static OsiLicenseDataParser getOsiLicenseDataParser() throws LicenseGeneratorException {
        if (Objects.isNull(_instance)) {
            _instance = new OsiLicenseDataParser();
        }
        return _instance;
    }

    /**
     * @param licenseId
     * @return optional present value true if license is OSI approved, optional present value false if the license
     * is listed and not OSI approved, optional empty if the license ID is not listed in the OSI API
     */
    public Optional<Boolean> isSpdxLicenseOsiApproved(String licenseId) {
        return Optional.ofNullable(licenseIdToOsiApproved.get(licenseId.toLowerCase()));
    }

    /**
     * Used for testing - clear the instance
     */
    protected static void reset() {
        _instance = null;
    }

}
