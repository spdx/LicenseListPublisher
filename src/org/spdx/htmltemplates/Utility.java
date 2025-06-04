/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) Source Auditor Inc. 2025.
 *
 */

package org.spdx.htmltemplates;

import com.github.mustachejava.MustacheResolver;
import com.github.mustachejava.resolver.ClasspathResolver;
import com.github.mustachejava.resolver.FileSystemResolver;

import java.io.File;

/**
 * Utility class for HTML templates - static methods for common code across different templates
 */
public class Utility {

    static final String TEMPLATE_CLASS_PATH = "resources" + "/" + "htmlTemplate";
    static final String TEMPLATE_ROOT_PATH = "resources" + File.separator + "htmlTemplate";

    /**
     * Gets the file system based resolver if it exists, otherwise returns a class path resolver
     * @return resolver based on if the file system match exists
     */
    static MustacheResolver getMustacheResolver() {
        File templateDirectoryRoot = new File(TEMPLATE_ROOT_PATH);
        if (templateDirectoryRoot.exists() && templateDirectoryRoot.isDirectory()) {
            return new FileSystemResolver(templateDirectoryRoot);
        } else {
            return new ClasspathResolver(TEMPLATE_CLASS_PATH);
        }
    }
}
