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

package com.puppycrawl.tools.checkstyle.filters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.puppycrawl.tools.checkstyle.XmlLoader;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.FilterSet;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Loads a filter chain of suppressions.
 */
public final class SuppressionsLoader extends XmlLoader {

    /** The public ID for the configuration dtd. */
    private static final String DTD_PUBLIC_ID_1_0 =
        "-//Puppy Crawl//DTD Suppressions 1.0//EN";
    /** The new public ID for version 1_0 configuration dtd. */
    private static final String DTD_PUBLIC_CS_ID_1_0 =
        "-//Checkstyle//DTD SuppressionFilter Configuration 1.0//EN";
    /** The resource for the configuration dtd. */
    private static final String DTD_SUPPRESSIONS_NAME_1_0 =
        "com/puppycrawl/tools/checkstyle/suppressions_1_0.dtd";
    /** The public ID for the configuration dtd. */
    private static final String DTD_PUBLIC_ID_1_1 =
        "-//Puppy Crawl//DTD Suppressions 1.1//EN";
    /** The new public ID for version 1_1 configuration dtd. */
    private static final String DTD_PUBLIC_CS_ID_1_1 =
        "-//Checkstyle//DTD SuppressionFilter Configuration 1.1//EN";
    /** The resource for the configuration dtd. */
    private static final String DTD_SUPPRESSIONS_NAME_1_1 =
        "com/puppycrawl/tools/checkstyle/suppressions_1_1.dtd";
    /** The public ID for the configuration dtd. */
    private static final String DTD_PUBLIC_ID_1_2 =
        "-//Puppy Crawl//DTD Suppressions 1.2//EN";
    /** The new public ID for version 1_2 configuration dtd. */
    private static final String DTD_PUBLIC_CS_ID_1_2 =
        "-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN";
    /** The resource for the configuration dtd. */
    private static final String DTD_SUPPRESSIONS_NAME_1_2 =
        "com/puppycrawl/tools/checkstyle/suppressions_1_2.dtd";

    /** String literal for element name. **/
    private static final String ELEMENT_NAME = "suppress";

    /** String literal for attribute name. **/
    private static final String ATTRIBUTE_NAME_FILES = "files";
    /** String literal for attribute name. **/
    private static final String ATTRIBUTE_NAME_CHECKS = "checks";
    /** String literal for attribute name. **/
    private static final String ATTRIBUTE_NAME_MESSAGE = "message";
    /** String literal for attribute name. **/
    private static final String ATTRIBUTE_NAME_ID = "id";
    /** String literal for attribute name. **/
    private static final String ATTRIBUTE_NAME_LINES = "lines";
    /** String literal for attribute name. **/
    private static final String ATTRIBUTE_NAME_COLUMNS = "columns";

    /**
     * The filter chain to return in getAFilterChain(),
     * configured during parsing.
     */
    private final FilterSet filterChain = new FilterSet();

    /**
     * Creates a new {@code SuppressionsLoader} instance.
     *
     * @throws ParserConfigurationException if an error occurs
     * @throws SAXException if an error occurs
     */
    private SuppressionsLoader()
            throws ParserConfigurationException, SAXException {
        super(createIdToResourceNameMap());
    }

    @Override
    public void startElement(String namespaceUri,
                             String localName,
                             String qName,
                             Attributes attributes)
            throws SAXException {
        if (ELEMENT_NAME.equals(qName)) {
            final SuppressFilterElement suppress = getSuppressElement(attributes);
            filterChain.addFilter(suppress);
        }
        else if (!"suppressions".equals(qName)) {
            throw new IllegalStateException("Unknown name:" + qName + ".");
        }
    }

    /**
     * Returns the suppress element, initialized from given attributes.
     *
     * @param attributes the attributes of xml-tag "&lt;suppress&gt;&lt;/suppress&gt;",
     *                   specified inside suppression file.
     * @return the suppress element
     * @throws SAXException if an error occurs.
     */
    private static SuppressFilterElement getSuppressElement(Attributes attributes)
            throws SAXException {
        final String checks = attributes.getValue(ATTRIBUTE_NAME_CHECKS);
        final String modId = attributes.getValue(ATTRIBUTE_NAME_ID);
        final String message = attributes.getValue(ATTRIBUTE_NAME_MESSAGE);
        if (checks == null && modId == null && message == null) {
            // -@cs[IllegalInstantiation] SAXException is in the overridden method signature
            throw new SAXException("missing checks or id or message attribute");
        }
        final SuppressFilterElement suppress;
        try {
            final String files = attributes.getValue(ATTRIBUTE_NAME_FILES);
            final String lines = attributes.getValue(ATTRIBUTE_NAME_LINES);
            final String columns = attributes.getValue(ATTRIBUTE_NAME_COLUMNS);
            suppress = new SuppressFilterElement(files, checks, message, modId, lines, columns);
        }
        catch (final PatternSyntaxException ex) {
            // -@cs[IllegalInstantiation] SAXException is in the overridden method signature
            throw new SAXException("invalid files or checks or message format", ex);
        }
        return suppress;
    }

    /**
     * Returns the suppression filters in a specified file.
     *
     * @param filename name of the suppressions file.
     * @return the filter chain of suppression elements specified in the file.
     * @throws CheckstyleException if an error occurs.
     */
    public static FilterSet load(String filename)
            throws CheckstyleException {
        // figure out if this is a File or a URL
        final URI uri = CommonUtil.getUriByFilename(filename);
        final InputSource source = new InputSource(uri.toString());
        return load(source, filename);
    }

    /**
     * Returns the suppression filters in a specified source.
     *
     * @param source the source for the suppressions.
     * @param sourceName the name of the source.
     * @return the filter chain of suppression elements in source.
     * @throws CheckstyleException if an error occurs.
     */
    private static FilterSet load(InputSource source, String sourceName)
            throws CheckstyleException {
        try {
            final SuppressionsLoader instance = new SuppressionsLoader();
            instance.parseInputSource(source);
            return instance.filterChain;
        }
        catch (final FileNotFoundException ex) {
            throw new CheckstyleException("Unable to find: " + sourceName, ex);
        }
        catch (final ParserConfigurationException | SAXException ex) {
            final String message = String.format(Locale.ROOT, "Unable to parse %s - %s",
                    sourceName, ex.getMessage());
            throw new CheckstyleException(message, ex);
        }
        catch (final IOException ex) {
            throw new CheckstyleException("Unable to read " + sourceName, ex);
        }
        catch (final NumberFormatException ex) {
            final String message = String.format(Locale.ROOT, "Number format exception %s - %s",
                    sourceName, ex.getMessage());
            throw new CheckstyleException(message, ex);
        }
    }

    /**
     * Creates mapping between local resources and dtd ids.
     *
     * @return map between local resources and dtd ids.
     */
    private static Map<String, String> createIdToResourceNameMap() {
        final Map<String, String> map = new HashMap<>();
        map.put(DTD_PUBLIC_ID_1_0, DTD_SUPPRESSIONS_NAME_1_0);
        map.put(DTD_PUBLIC_ID_1_1, DTD_SUPPRESSIONS_NAME_1_1);
        map.put(DTD_PUBLIC_ID_1_2, DTD_SUPPRESSIONS_NAME_1_2);
        map.put(DTD_PUBLIC_CS_ID_1_0, DTD_SUPPRESSIONS_NAME_1_0);
        map.put(DTD_PUBLIC_CS_ID_1_1, DTD_SUPPRESSIONS_NAME_1_1);
        map.put(DTD_PUBLIC_CS_ID_1_2, DTD_SUPPRESSIONS_NAME_1_2);
        return map;
    }

}
