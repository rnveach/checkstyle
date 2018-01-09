////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2018 the original author or authors.
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
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.utils;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;

import com.google.common.reflect.ClassPath;
import com.puppycrawl.tools.checkstyle.TreeWalkerFilter;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.BeforeExecutionFileFilter;
import com.puppycrawl.tools.checkstyle.api.Filter;
import com.puppycrawl.tools.checkstyle.api.RootModule;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;

/**
 * Contains utility methods for module reflection.
 * @author LuoLiangchen
 */
public final class ModuleReflectionUtils {

    private static final Set<String> CHECK_PROPERTIES =
            getAllModuleProperties(AbstractCheck.class);
    private static final Set<String> JAVADOC_CHECK_PROPERTIES =
            getAllModuleProperties(AbstractJavadocCheck.class);
    private static final Set<String> FILESET_PROPERTIES =
            getAllModuleProperties(AbstractFileSetCheck.class);

    private static final List<String> UNDOCUMENTED_PROPERTIES = Arrays.asList(
            "Checker.classLoader",
            "Checker.classloader",
            "Checker.moduleClassLoader",
            "Checker.moduleFactory",
            "TreeWalker.classLoader",
            "TreeWalker.moduleFactory",
            "TreeWalker.cacheFile",
            "TreeWalker.upChild",
            "SuppressWithNearbyCommentFilter.fileContents",
            "SuppressionCommentFilter.fileContents"
    );

    /** Prevent instantiation. */
    private ModuleReflectionUtils() {
    }

    /**
     * Gets checkstyle's modules (directly, not recursively) in the given packages.
     * @param packages the collection of package names to use
     * @param loader the class loader used to load Checkstyle package names
     * @return the set of checkstyle's module classes
     * @throws IOException if the attempt to read class path resources failed
     * @see #isCheckstyleModule(Class)
     */
    public static Set<Class<?>> getCheckstyleModules(
            Collection<String> packages, ClassLoader loader) throws IOException {
        final ClassPath classPath = ClassPath.from(loader);
        return packages.stream()
                .flatMap(pkg -> classPath.getTopLevelClasses(pkg).stream())
                .map(ClassPath.ClassInfo::load)
                .filter(ModuleReflectionUtils::isCheckstyleModule)
                .collect(Collectors.toSet());
    }

    /**
     * Checks whether a class may be considered as a checkstyle module. Checkstyle's modules are
     * non-abstract classes, which are either checkstyle's checks, file sets, filters, file filters,
     * {@code TreeWalker} filters, audit listener, or root module.
     * @param clazz class to check.
     * @return true if the class may be considered as the checkstyle module.
     */
    public static boolean isCheckstyleModule(Class<?> clazz) {
        return isValidCheckstyleClass(clazz)
            && (isTreeWalkerCheck(clazz)
                    || isFileSetModule(clazz)
                    || isFilterModule(clazz)
                    || isFileFilterModule(clazz)
                    || isTreeWalkerFilterModule(clazz)
                    || isAuditListener(clazz)
                    || isRootModule(clazz));
    }

    /**
     * Checks whether a class extends 'AutomaticBean', is non-abstract, and has a default
     * constructor.
     * @param clazz class to check.
     * @return true if a class may be considered a valid production class.
     */
    public static boolean isValidCheckstyleClass(Class<?> clazz) {
        return AutomaticBean.class.isAssignableFrom(clazz)
                && !Modifier.isAbstract(clazz.getModifiers())
                && hasDefaultConstructor(clazz);
    }

    /**
     * Checks if the class has a default constructor.
     * @param clazz class to check
     * @return true if the class has a default constructor.
     */
    private static boolean hasDefaultConstructor(Class<?> clazz) {
        boolean result = false;
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Checks whether a class may be considered as the checkstyle check
     * which has TreeWalker as a parent.
     * Checkstyle's checks are classes which implement 'AbstractCheck' interface.
     * @param clazz class to check.
     * @return true if a class may be considered as the checkstyle check.
     */
    public static boolean isTreeWalkerCheck(Class<?> clazz) {
        return AbstractCheck.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether a class may be considered as the checkstyle file set.
     * Checkstyle's file sets are classes which implement 'AbstractFileSetCheck' interface.
     * @param clazz class to check.
     * @return true if a class may be considered as the checkstyle file set.
     */
    public static boolean isFileSetModule(Class<?> clazz) {
        return AbstractFileSetCheck.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether a class may be considered as the checkstyle filter.
     * Checkstyle's filters are classes which implement 'Filter' interface.
     * @param clazz class to check.
     * @return true if a class may be considered as the checkstyle filter.
     */
    public static boolean isFilterModule(Class<?> clazz) {
        return Filter.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether a class may be considered as the checkstyle file filter.
     * Checkstyle's file filters are classes which implement 'BeforeExecutionFileFilter' interface.
     * @param clazz class to check.
     * @return true if a class may be considered as the checkstyle file filter.
     */
    public static boolean isFileFilterModule(Class<?> clazz) {
        return BeforeExecutionFileFilter.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether a class may be considered as the checkstyle audit listener module.
     * Checkstyle's audit listener modules are classes which implement 'AuditListener' interface.
     * @param clazz class to check.
     * @return true if a class may be considered as the checkstyle audit listener module.
     */
    public static boolean isAuditListener(Class<?> clazz) {
        return AuditListener.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether a class may be considered as the checkstyle root module.
     * Checkstyle's root modules are classes which implement 'RootModule' interface.
     * @param clazz class to check.
     * @return true if a class may be considered as the checkstyle root module.
     */
    public static boolean isRootModule(Class<?> clazz) {
        return RootModule.class.isAssignableFrom(clazz);
    }

    /**
     * Checks whether a class may be considered as the checkstyle {@code TreeWalker} filter.
     * Checkstyle's {@code TreeWalker} filters are classes which implement 'TreeWalkerFilter'
     * interface.
     * @param clazz class to check.
     * @return true if a class may be considered as the checkstyle {@code TreeWalker} filter.
     */
    public static boolean isTreeWalkerFilterModule(Class<?> clazz) {
        return TreeWalkerFilter.class.isAssignableFrom(clazz);
    }

    public static String getModuleSimpleName(Class<?> clazz) {
        String name = clazz.getSimpleName();

        if (name.endsWith("Check")) {
            name = name.substring(0, name.length() - 5);
        }

        return name;
    }

    public static Set<String> getAllModuleProperties(Class<?> module) {
        final Set<String> result = new TreeSet<>();
        final PropertyDescriptor[] map = PropertyUtils.getPropertyDescriptors(module);

        for (PropertyDescriptor p : map) {
            if (p.getWriteMethod() != null) {
                result.add(p.getName());
            }
        }

        return result;
    }

    public static Set<String> getUserModuleProperties(Object module) {
        final Class<?> moduleClass = module.getClass();
        final Set<String> properties = getAllModuleProperties(moduleClass);

        if (isTreeWalkerCheck(moduleClass)) {
            if (AbstractJavadocCheck.class.isAssignableFrom(moduleClass)) {
                properties.removeAll(JAVADOC_CHECK_PROPERTIES);

                // override
                properties.add("violateExecutionOnNonTightHtml");

                final AbstractJavadocCheck check = (AbstractJavadocCheck) module;

                final int[] acceptableJavadocTokens = check.getAcceptableJavadocTokens();
                Arrays.sort(acceptableJavadocTokens);
                final int[] defaultJavadocTokens = check.getDefaultJavadocTokens();
                Arrays.sort(defaultJavadocTokens);
                final int[] requiredJavadocTokens = check.getRequiredJavadocTokens();
                Arrays.sort(requiredJavadocTokens);

                if (!Arrays.equals(acceptableJavadocTokens, defaultJavadocTokens)
                        || !Arrays.equals(acceptableJavadocTokens, requiredJavadocTokens)) {
                    properties.add("javadocTokens");
                }
            }
            else {
                properties.removeAll(CHECK_PROPERTIES);
            }

            final AbstractCheck check = (AbstractCheck) module;

            final int[] acceptableTokens = check.getAcceptableTokens();
            Arrays.sort(acceptableTokens);
            final int[] defaultTokens = check.getDefaultTokens();
            Arrays.sort(defaultTokens);
            final int[] requiredTokens = check.getRequiredTokens();
            Arrays.sort(requiredTokens);

            if (!Arrays.equals(acceptableTokens, defaultTokens)
                    || !Arrays.equals(acceptableTokens, requiredTokens)) {
                properties.add("tokens");
            }
        } else if (isFileSetModule(moduleClass)) {
            properties.removeAll(FILESET_PROPERTIES);

            // override
            properties.add("fileExtensions");
        }

        // remove undocumented properties
        new HashSet<>(properties).stream()
            .filter(prop -> UNDOCUMENTED_PROPERTIES.contains(moduleClass.getSimpleName() + "." + prop))
            .forEach(properties::remove);

        return properties;
    }

    public static String getTreeWalkerCheckAcceptableTokensText(AbstractCheck check) {
        return TokenUtils.getTokenNames(check.getAcceptableTokens(), check.getRequiredTokens());
    }

    public static String getTreeWalkerCheckDefaultTokensText(AbstractCheck check) {
        return TokenUtils.getTokenNames(check.getDefaultTokens(), check.getRequiredTokens());
    }

    public static String getTreeWalkerCheckAcceptableJavadocTokensText(AbstractJavadocCheck check) {
        return JavadocUtils.getTokenNames(check.getAcceptableJavadocTokens(),
                check.getRequiredJavadocTokens());
    }

    public static String getTreeWalkerCheckDefaultJavadocTokensText(AbstractJavadocCheck check) {
        return JavadocUtils.getTokenNames(check.getDefaultJavadocTokens(),
                check.getRequiredJavadocTokens());
    }

    public static Class<?> getModulePropertyType(Object module, String propertyName) throws Exception {
        final Field field = getField(module.getClass(), propertyName);
        final Class<?> result;

        if (field == null) {
            result = null;
        }
        else {
            result = field.getType();
        }

        return result;
    }

    private static Field getField(Class<?> clss, String propertyName) {
        Field result = null;

        if (clss != null) {
            try {
                result = clss.getDeclaredField(propertyName);
                result.setAccessible(true);
            }
            catch (NoSuchFieldException ignored) {
                result = getField(clss.getSuperclass(), propertyName);
            }
        }

        return result;
    }
}
