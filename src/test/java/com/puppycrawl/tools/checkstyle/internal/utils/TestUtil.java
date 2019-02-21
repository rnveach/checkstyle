////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2019 the original author or authors.
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

package com.puppycrawl.tools.checkstyle.internal.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.powermock.reflect.Whitebox;

import com.puppycrawl.tools.checkstyle.PackageNamesLoader;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.TreeWalkerAuditEvent;
import com.puppycrawl.tools.checkstyle.TreeWalkerFilter;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

public final class TestUtil {

    private TestUtil() {
    }

    /**
     * Verifies that utils class has private constructor and invokes it to satisfy code coverage.
     * @param utilClass class to test for c-tor
     * @param checkConstructorIsPrivate flag to skip check for private visibility, it is useful
     *                                  for Classes that are mocked by PowerMockRunner that make
     *                                  private c-tors as public
     * @return true if constructor is expected.
     * @noinspection BooleanParameter
     */
    public static boolean isUtilsClassHasPrivateConstructor(final Class<?> utilClass,
                                                             boolean checkConstructorIsPrivate)
            throws ReflectiveOperationException {
        final Constructor<?> constructor = utilClass.getDeclaredConstructor();
        final boolean result;
        if (checkConstructorIsPrivate && !Modifier.isPrivate(constructor.getModifiers())) {
            result = false;
        }
        else {
            constructor.setAccessible(true);
            constructor.newInstance();
            result = true;
        }
        return result;
    }

    /**
     * Checks if stateful field is cleared during {@link AbstractCheck#beginTree} in check.
     *
     * @param check      check object which field is to be verified
     * @param astToVisit ast to pass into check methods
     * @param fieldName  name of the field to be checked
     * @param isClear    function for checking field state
     * @return {@code true} if state of the field is cleared
     */
    public static boolean isStatefulFieldClearedDuringBeginTree(AbstractCheck check,
                                                                DetailAST astToVisit,
                                                                String fieldName,
                                                                Predicate<Object> isClear) {
        check.beginTree(astToVisit);
        check.visitToken(astToVisit);
        check.beginTree(null);
        return isClear.test(Whitebox.getInternalState(check, fieldName));
    }

    /**
     * Checks if stateful static field is cleared during {@link AbstractCheck#beginTree} in check.
     *
     * @param check      check object which field is to be verified
     * @param astToVisit ast to pass into check methods
     * @param fieldName  name of the field to be checked
     * @param isClear    function for checking field state
     * @return {@code true} if state of the field is cleared
     */
    public static boolean isStatefulStaticFieldClearedDuringBeginTree(AbstractCheck check,
                                                                DetailAST astToVisit,
                                                                String fieldName,
                                                                Predicate<Object> isClear) {
        check.beginTree(astToVisit);
        check.visitToken(astToVisit);
        check.beginTree(null);
        return isClear.test(Whitebox.getInternalState(check.getClass(), fieldName));
    }

    /**
     * Checks if stateful field is cleared during {@link AutomaticBean}'s finishLocalSetup.
     *
     * @param filter filter object which field is to be verified
     * @param event event to pass into filter methods
     * @param fieldName name of the field to be checked
     * @param isClear function for checking field state
     * @return {@code true} if state of the field is cleared
     * @throws Exception if there was an error.
     */
    public static boolean isStatefulFieldClearedDuringLocalSetup(
            TreeWalkerFilter filter, TreeWalkerAuditEvent event,
            String fieldName, Predicate<Object> isClear) throws Exception {
        filter.accept(event);
        Whitebox.invokeMethod(filter, "finishLocalSetup");
        return isClear.test(Whitebox.getInternalState(filter, fieldName));
    }

    /**
     * Checks if stateful static field is cleared during {@link AutomaticBean}'s finishLocalSetup.
     *
     * @param filter filter object which field is to be verified
     * @param event event to pass into filter methods
     * @param fieldName name of the field to be checked
     * @param isClear function for checking field state
     * @return {@code true} if state of the field is cleared
     * @throws Exception if there was an error.
     */
    public static boolean isStatefulStaticFieldClearedDuringLocalSetup(
            TreeWalkerFilter filter, TreeWalkerAuditEvent event,
            String fieldName, Predicate<Object> isClear) throws Exception {
        filter.accept(event);
        Whitebox.invokeMethod(filter, "finishLocalSetup");
        return isClear.test(Whitebox.getInternalState(filter.getClass(), fieldName));
    }

    /**
     * Returns the default PackageObjectFactory with the default package names.
     * @return the default PackageObjectFactory.
     */
    public static PackageObjectFactory getPackageObjectFactory() throws CheckstyleException {
        final ClassLoader cl = TestUtil.class.getClassLoader();
        final Set<String> packageNames = PackageNamesLoader.getPackageNames(cl);
        return new PackageObjectFactory(packageNames, cl);
    }

    /**
     * Finds node of specified type among root children, siblings, siblings children
     * on any deep level.
     * @param root      DetailAST
     * @param predicate predicate
     * @return {@link Optional} of {@link DetailAST} node which matches the predicate.
     */
    public static Optional<DetailAST> findTokenInAstByPredicate(DetailAST root,
                                                                Predicate<DetailAST> predicate) {
        DetailAST curNode = root;
        while (!predicate.test(curNode)) {
            DetailAST toVisit = curNode.getFirstChild();
            while (curNode != null && toVisit == null) {
                toVisit = curNode.getNextSibling();
                if (toVisit == null) {
                    curNode = curNode.getParent();
                }
            }

            if (curNode == toVisit || curNode == root.getParent()) {
                curNode = null;
                break;
            }

            curNode = toVisit;
        }
        return Optional.ofNullable(curNode);
    }

}
