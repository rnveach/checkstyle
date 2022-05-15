///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
///////////////////////////////////////////////////////////////////////////////////////////////

package com.google.checkstyle.test.base;

import org.checkstyle.base.AbstractItModuleTestSupport;

import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

public abstract class AbstractGoogleModuleTestSupport extends AbstractItModuleTestSupport {

    private static final String XML_NAME = "/google_checks.xml";

    private static final Configuration CONFIGURATION;

    static {
        try {
            CONFIGURATION = ConfigurationLoader.loadConfiguration(XML_NAME,
                    new PropertiesExpander(System.getProperties()));
        }
        catch (CheckstyleException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Returns {@link Configuration} instance for the given module name.
     * This implementation uses {@link #getModuleConfig(String)} method inside.
     *
     * @param moduleName module name.
     * @param moduleId module id.
     * @return {@link Configuration} instance for the given module name.
     * @throws IllegalStateException if there is a problem retrieving the module or config.
     */
    protected static Configuration getModuleConfig(String moduleName) {
        return getModuleConfig(CONFIGURATION, moduleName);
    }

    /**
     * Returns a {@link Configuration} instance for the given module IDs.
     *
     * @param moduleIds module IDs.
     * @return {@link Configuration} instance.
     * @throws CheckstyleException if there is an error with the config.
     */
    protected static Configuration getModuleConfigById(String... moduleIds)
            throws CheckstyleException {
        return getModuleConfigById(CONFIGURATION, moduleIds);
    }

}
