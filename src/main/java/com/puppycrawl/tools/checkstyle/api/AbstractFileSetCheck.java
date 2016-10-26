////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
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

package com.puppycrawl.tools.checkstyle.api;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.puppycrawl.tools.checkstyle.utils.CommonUtils;
import com.rits.cloning.Cloner;
import com.rits.cloning.IDeepCloner;
import com.rits.cloning.IFastCloner;

/**
 * Provides common functionality for many FileSetChecks.
 *
 * @author lkuehne
 * @author oliver
 */
public abstract class AbstractFileSetCheck
    extends AbstractViolationReporter
    implements FileSetCheck, Cloneable {
    private static Cloner cloner = new Cloner();
    private static boolean addedFinalCloneClasses;

    /** Class loader to resolve classes with. **/
    private ClassLoader classLoader;

    /** Collects the error messages. */
    private final LocalizedMessages messageCollector = new LocalizedMessages();

    /** The dispatcher errors are fired to. */
    private MessageDispatcher messageDispatcher;

    /** The file extensions that are accepted by this filter. */
    private String[] fileExtensions = CommonUtils.EMPTY_STRING_ARRAY;

    static {
        // cloner.setDumpClonedClasses(true);
        cloner.nullInsteadOfClone(Cloner.class);
        cloner.dontCloneInstanceOf(MessageDispatcher.class);
        cloner.registerFastCloner(TreeSet.class, new IFastCloner() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Object clone(Object t, IDeepCloner deepCloner, Map<Object, Object> clones) {
                final TreeSet<Object> m = (TreeSet) t;
                final TreeSet result = new TreeSet(m.comparator());
                for (final Object v : m)
                {
                    final Object value = deepCloner.deepClone(v, clones);
                    result.add(value);
                }
                return result;
            }
        });
        cloner.registerFastCloner(ArrayDeque.class, new IFastCloner() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public Object clone(Object t, IDeepCloner deepCloner, Map<Object, Object> clones) {
                final ArrayDeque<Object> m = (ArrayDeque) t;
                final ArrayDeque result = new ArrayDeque();
                for (final Object v : m)
                {
                    final Object value = deepCloner.deepClone(v, clones);
                    result.add(value);
                }
                return result;
            }
        });
    }

    /**
     * Called to process a file that matches the specified file extensions.
     * @param file the file to be processed
     * @param lines an immutable list of the contents of the file.
     * @throws CheckstyleException if error condition within Checkstyle occurs.
     */
    protected abstract void processFiltered(File file, List<String> lines)
            throws CheckstyleException;

    /**
     * Retrieves the class loader.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * @param classLoader class loader to resolve classes with.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void init() {
        // No code by default, should be overridden only by demand at subclasses
    }

    @Override
    public void destroy() {
        // No code by default, should be overridden only by demand at subclasses
    }

    @Override
    public void beginProcessing(String charset) {
        // No code by default, should be overridden only by demand at subclasses
    }

    @Override
    public final SortedSet<LocalizedMessage> process(File file, List<String> lines)
            throws CheckstyleException {
        messageCollector.reset();
        // Process only what interested in
        if (CommonUtils.matchesFileExtension(file, fileExtensions)) {
//            System.out.println("File '" + file.getAbsolutePath() + "' matches extensions " + Arrays.toString(fileExtensions) + " in " + this.getClass().getSimpleName());
            processFiltered(file, lines);
        }
        return messageCollector.getMessages();
    }

    @Override
    public void finishProcessing() {
        // No code by default, should be overridden only by demand at subclasses
    }

    @Override
    public final void setMessageDispatcher(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    /**
     * A message dispatcher is used to fire violation messages to
     * interested audit listeners.
     *
     * @return the current MessageDispatcher.
     */
    protected final MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }

    /**
     * @return file extensions that identify the files that pass the
     *     filter of this FileSetCheck.
     */
    public String[] getFileExtensions() {
        return Arrays.copyOf(fileExtensions, fileExtensions.length);
    }

    /**
     * Sets the file extensions that identify the files that pass the
     * filter of this FileSetCheck.
     * @param extensions the set of file extensions. A missing
     *         initial '.' character of an extension is automatically added.
     * @throws IllegalArgumentException is argument is null
     */
    public final void setFileExtensions(String... extensions) {
        if (extensions == null) {
            throw new IllegalArgumentException("Extensions array can not be null");
        }

        fileExtensions = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            final String extension = extensions[i];
            if (CommonUtils.startsWithChar(extension, '.')) {
                fileExtensions[i] = extension;
            }
            else {
                fileExtensions[i] = "." + extension;
            }
        }
    }

    /**
     * Returns the collector for violation messages.
     * Subclasses can use the collector to find out the violation
     * messages to fire via the message dispatcher.
     *
     * @return the collector for localized messages.
     */
    protected final LocalizedMessages getMessageCollector() {
        return messageCollector;
    }

    @Override
    public final void log(int line, String key, Object... args) {
        log(line, 0, key, args);
    }

    @Override
    public final void log(int lineNo, int colNo, String key,
            Object... args) {
        messageCollector.add(
                new LocalizedMessage(lineNo,
                        colNo,
                        getMessageBundle(),
                        key,
                        args,
                        getSeverityLevel(),
                        getId(),
                        getClass(),
                        getCustomMessages().get(key)));
    }

    /**
     * Notify all listeners about the errors in a file.
     * Calls {@code MessageDispatcher.fireErrors()} with
     * all logged errors and than clears errors' list.
     * @param fileName the audited file
     */
    protected final void fireErrors(String fileName) {
        final SortedSet<LocalizedMessage> errors = messageCollector
                .getMessages();
        messageCollector.reset();
        getMessageDispatcher().fireErrors(fileName, errors);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        if (!addedFinalCloneClasses) {
            cloner.registerFastCloner(classLoader.getClass(), new IFastCloner() {
                @Override
                public Object clone(Object t, IDeepCloner cloner, Map<Object, Object> clones) {
                    return t;
                }
            });
            addedFinalCloneClasses = true;
        }

        final AbstractFileSetCheck clone = cloner.deepClone(this);

        clone.messageDispatcher = messageDispatcher;

        return clone;
    }
}
