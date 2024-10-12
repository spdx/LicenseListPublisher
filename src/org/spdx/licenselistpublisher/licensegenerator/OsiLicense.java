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

import java.util.List;

/**
 * OSI Licenses based on the OSI licenses schema: https://github.com/OpenSourceOrg/api/blob/master/doc/endpoints.md
 * 
 * @author Gary O'Neall
 *
 */
public class OsiLicense {
    
    String id;
    String name;
    String superseded_by;
    List<OsiIdentifier> identifiers;
    List<String> keywords;
    List<Object> links;
    List<Object> other_names;
    List<Object> text;
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * @return the identifiers
     */
    public List<OsiIdentifier> getIdentifiers() {
        return identifiers;
    }
    /**
     * @param identifiers the identifiers to set
     */
    public void setIdentifiers(List<OsiIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return the superseded_by
     */
    public String getSuperseded_by() {
        return superseded_by;
    }
    /**
     * @param superseded_by the superseded_by to set
     */
    public void setSuperseded_by(String superseded_by) {
        this.superseded_by = superseded_by;
    }
    /**
     * @return the keywords
     */
    public List<String> getKeywords() {
        return keywords;
    }
    /**
     * @param keywords the keywords to set
     */
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    /**
     * @return the links
     */
    public List<Object> getLinks() {
        return links;
    }
    /**
     * @param links the links to set
     */
    public void setLinks(List<Object> links) {
        this.links = links;
    }
    /**
     * @return the other_names
     */
    public List<Object> getOther_names() {
        return other_names;
    }
    /**
     * @param other_names the other_names to set
     */
    public void setOther_names(List<Object> other_names) {
        this.other_names = other_names;
    }
    /**
     * @return the text
     */
    public List<Object> getText() {
        return text;
    }
    /**
     * @param text the text to set
     */
    public void setText(List<Object> text) {
        this.text = text;
    }

}
