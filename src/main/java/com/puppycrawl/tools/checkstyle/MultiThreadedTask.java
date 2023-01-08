///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2023 the original author or authors.
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

package com.puppycrawl.tools.checkstyle;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.puppycrawl.tools.checkstyle.api.FileSetCheck;
import com.puppycrawl.tools.checkstyle.api.MessageDispatcher;
import com.puppycrawl.tools.checkstyle.api.Violation;
import com.rits.cloning.Cloner;
import com.rits.cloning.IDeepCloner;
import com.rits.cloning.IFastCloner;

/**
 * This class provides the functionality to perform one set of work on a file in a multi-threaded
 * environment.
 *
 * @author Richard Veach
 */
public class MultiThreadedTask implements Callable<SortedSet<Violation>> {
    private static Cloner cloner = new Cloner();

    private Future<SortedSet<Violation>> future;

    private boolean haltOnException;
    private File file;
    private String charset;
    private List<FileSetCheck> fileSetChecks;

    static {
        // cloner.setDumpClonedClasses(true);
        cloner.nullInsteadOfClone(Cloner.class);
        cloner.dontCloneInstanceOf(GlobalStatefulCheck.class, ThreadLocal.class, MessageDispatcher.class,
                org.apache.commons.logging.Log.class,
                ClassLoader.class,
                java.util.concurrent.ConcurrentHashMap.class,
                net.sf.saxon.sxpath.XPathExpression.class,
                java.lang.ref.WeakReference.class);
        cloner.registerFastCloner(TreeSet.class, new IFastCloner() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Object clone(Object t, IDeepCloner deepCloner, Map<Object, Object> clones) {
                final TreeSet<Object> original = (TreeSet) t;
                final TreeSet result = new TreeSet(original.comparator());
                for (final Object originalValue : original) {
                    final Object value = deepCloner.deepClone(originalValue, clones);
                    result.add(value);
                }
                return result;
            }
        });
        cloner.registerFastCloner(ArrayDeque.class, new IFastCloner() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Object clone(Object t, IDeepCloner deepCloner, Map<Object, Object> clones) {
                final ArrayDeque<Object> original = (ArrayDeque) t;
                final ArrayDeque result = new ArrayDeque();
                for (final Object originalValue : original) {
                    final Object value = deepCloner.deepClone(originalValue, clones);
                    result.add(value);
                }
                return result;
            }
        });
    }

    /**
     * Initializes the class.
     *
     * @param charset
     *            Name of a charset.
     * @param fileSetChecks
     *            List of fileset checks.
     */
    public void init(boolean haltOnException, String charset, List<FileSetCheck> fileSetChecks) {
        this.haltOnException = haltOnException;
        this.charset = charset;
        this.fileSetChecks = copy(fileSetChecks);
    }

    /**
     * Make a deep copy of the list.
     *
     * @param list
     *            Deep list of elements to copy.
     * @return New list of elements.
     * @throws CloneNotSupportedException if there is an error.
     */
    private static List<FileSetCheck> copy(List<FileSetCheck> list) {
        final List<FileSetCheck> result = new ArrayList<FileSetCheck>();

        for (FileSetCheck fsc : list) {
            result.add((FileSetCheck) cloner(fsc));
        }

        return result;
    }

    /**
     * Starts running the task with the specified file input.
     *
     * @param executor
     *            The multi-thread service.
     * @param file
     *            The file to work on.
     */
    public void start(ExecutorService executor, File file) {
        this.file = file;
        future = executor.submit(this);
    }

    @Override
    public SortedSet<Violation> call() throws Exception {
        return SingleChecker.processFile(file, charset, fileSetChecks, haltOnException);
    }

    /**
     * Checks if the current task is executing any work.
     *
     * @return True if the task is still working, otherwise false.
     */
    public boolean isWorking() {
        return future != null && !future.isDone();
    }

    /**
     * Checks if the current task has any results to return.
     *
     * @return True if the task has results to return.
     */
    public boolean hasResults() {
        return future != null && future.isDone();
    }

    /**
     * Retrieves the list of messages generated by this task.
     *
     * @return The sorted list of messages.
     * @throws InterruptedException
     *             if the task threw an exception
     * @throws ExecutionException
     *             if the current task was interrupted while waiting
     */
    public SortedSet<Violation> getResult() throws InterruptedException, ExecutionException {
        final SortedSet<Violation> result = future.get();

        future = null;

        return result;
    }

    /**
     * Retrieves the file the task was working on.
     *
     * @return The file.
     */
    public File getFile() {
        return file;
    }

    public static Object cloner(Object instance) {
        Object clone = null;

        try {
            clone = cloner.deepClone(instance);
        }
        catch (Throwable throwable) {
            throw new IllegalStateException(
                    "Error occurred when cloning: " + instance.getClass().getName(), throwable);
        }

        return clone;
    }
}
