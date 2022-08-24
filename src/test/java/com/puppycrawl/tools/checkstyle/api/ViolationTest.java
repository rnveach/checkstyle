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

package com.puppycrawl.tools.checkstyle.api;

import static com.google.common.truth.Truth.assertWithMessage;
import static com.puppycrawl.tools.checkstyle.utils.CommonUtil.EMPTY_OBJECT_ARRAY;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.EqualsVerifierReport;

/**
 * Custom class loader is needed to pass URLs to pretend these are loaded from the classpath
 * though we can't add/change the files for testing. The class loader is nested in this class,
 * so the custom class loader we are using is safe.
 *
 * @noinspection ClassLoaderInstantiation
 * @noinspectionreason ClassLoaderInstantiation - Custom class loader is needed to
 *      pass URLs for testing
 */
public class ViolationTest {

    @DefaultLocale("fr")
    @Test
    public void testEnforceEnglishLanguageBySettingRootLocale() {
        Violation.setLocale(Locale.ROOT);
        final Violation violation = createSampleViolation();

        assertWithMessage("Invalid violation")
            .that(violation.getViolation())
            .isEqualTo("Empty statement.");
    }

    private static Violation createSampleViolation() {
        return createSampleViolationWithId("module");
    }

    private static Violation createSampleViolationWithId(String id) {
        return new Violation(1, "com.puppycrawl.tools.checkstyle.checks.coding.messages",
                "empty.statement", EMPTY_OBJECT_ARRAY, id, Violation.class, null);
    }

    /**
     * Sets the English locale for all tests.
     * Otherwise, some tests failed in other locales.
     */
    @BeforeAll
    public static void setEnglishLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @BeforeEach
    public void tearDown() {
        Violation.clearCache();
        Violation.setLocale(Locale.getDefault());
    }

}
