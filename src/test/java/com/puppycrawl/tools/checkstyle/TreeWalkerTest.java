////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
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

package com.puppycrawl.tools.checkstyle;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.checks.indentation.CommentsIndentationCheck;
import com.puppycrawl.tools.checkstyle.checks.naming.TypeNameCheck;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TreeWalker.class)
public class TreeWalkerTest extends BaseCheckTestSupport {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testBehaviourWithOnlyCommentChecks() throws Exception {
        final TreeWalker treeWalkerSpy = spy(new TreeWalker());
        final Class<?> classAstState =
                Class.forName("com.puppycrawl.tools.checkstyle.AstState");
        final PackageObjectFactory factory = new PackageObjectFactory(
                new HashSet<>(), Thread.currentThread().getContextClassLoader());
        treeWalkerSpy.configure(createCheckConfig(CommentsIndentationCheck.class));
        treeWalkerSpy.setModuleFactory(factory);
        treeWalkerSpy.setupChild(createCheckConfig(CommentsIndentationCheck.class));
        spy(TreeWalker.class);
        doNothing().when(treeWalkerSpy, "walk",
                any(DetailAST.class), any(FileContents.class), any(classAstState));
        when(treeWalkerSpy, "appendHiddenCommentNodes", any(DetailAST.class))
                .thenReturn(null);
        treeWalkerSpy.processFiltered(temporaryFolder.newFile("file.java"), new ArrayList<>());
        verifyPrivate(treeWalkerSpy, times(1))
                .invoke("appendHiddenCommentNodes", any(DetailAST.class))
                ;
        verifyPrivate(treeWalkerSpy, times(1)).invoke("walk",
                any(DetailAST.class), any(FileContents.class), any(classAstState));
    }

//    @Test
    public void testBehaviourWithOrdinaryAndCommentChecks() throws Exception {
        final TreeWalker treeWalkerSpy = spy(new TreeWalker());
        final Class<?> classAstState =
                Class.forName("com.puppycrawl.tools.checkstyle.TreeWalker$AstState");
        final PackageObjectFactory factory = new PackageObjectFactory(
                new HashSet<>(), Thread.currentThread().getContextClassLoader());
        treeWalkerSpy.configure(new DefaultConfiguration("TreeWalkerTest"));
        treeWalkerSpy.setModuleFactory(factory);
        treeWalkerSpy.setupChild(createCheckConfig(CommentsIndentationCheck.class));
        treeWalkerSpy.setupChild(createCheckConfig(TypeNameCheck.class));
        spy(TreeWalker.class);
        doNothing().when(treeWalkerSpy, "walk",
                any(DetailAST.class), any(FileContents.class), any(classAstState));
        when(TreeWalker.class, "appendHiddenCommentNodes", any(DetailAST.class))
                .thenReturn(null);
        treeWalkerSpy.processFiltered(temporaryFolder.newFile("file.java"), new ArrayList<>());
        verifyPrivate(TreeWalker.class, times(1))
                .invoke("appendHiddenCommentNodes", any(DetailAST.class));
        verifyPrivate(treeWalkerSpy, times(2)).invoke("walk",
                any(DetailAST.class), any(FileContents.class), any(classAstState));
        verifyStatic(times(1));
        TreeWalker.parse(any(FileContents.class));
    }
}
