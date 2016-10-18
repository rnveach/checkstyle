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
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import com.puppycrawl.tools.checkstyle.CacheAware;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * Provides common functionality for many FileSetChecks.
 *
 * @author lkuehne
 * @author oliver
 */
public abstract class AbstractFileSetCheck
    extends AbstractViolationReporter
    implements FileSetCheck, CacheAware {

    /** Collects the error messages. */
    private final LocalizedMessages messageCollector = new LocalizedMessages();

    /** The dispatcher errors are fired to. */
    private MessageDispatcher messageDispatcher;

    /** The file extensions that are accepted by this filter. */
    private String[] fileExtensions = CommonUtils.EMPTY_STRING_ARRAY;

    /**
     * Called to process a file that matches the specified file extensions.
     * @param file the file to be processed
     * @param lines an immutable list of the contents of the file.
     * @throws CheckstyleException if error condition within Checkstyle occurs.
     */
    protected abstract void processFiltered(File file, List<String> lines)
            throws CheckstyleException;

    /** Called when all the files have been processed. */
    protected abstract void finishProcessFiltered();

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
    public void onCacheReset() {
        // No code by default, should be overridden only by demand at subclasses
    }

    protected boolean canSkipCachedFileFiltered(File file) {
        return true;
    }

    @Override
    public final boolean canSkipCachedFile(File file) {
        // Process only what interested in
        if (CommonUtils.matchesFileExtension(file, fileExtensions)) {
            return canSkipCachedFileFiltered(file);
        }
        return true;
    }

    protected void skipCachedFileFiltered(File file) {
        // No code by default, should be overridden only by demand at subclasses
    }

    @Override
    public void skipCachedFile(File file) {
        // Process only what interested in
        if (CommonUtils.matchesFileExtension(file, fileExtensions)) {
            skipCachedFileFiltered(file);
        }
    }

    @Override
    public final SortedSet<LocalizedMessage> process(File file, List<String> lines)
            throws CheckstyleException {
        messageCollector.reset();
        // Process only what interested in
        if (CommonUtils.matchesFileExtension(file, fileExtensions)) {
            processFiltered(file, lines);
        }
        return messageCollector.getMessages();
    }

    @Override
    public final SortedSet<LocalizedMessage> finishProcessing() {
        messageCollector.reset();
        finishProcessFiltered();
        return messageCollector.getMessages();
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
        logExternal(null, lineNo, colNo, key, args);
    }

    @Override
    public final void logExternal(String fileName, int line, String key, Object... args) {
        logExternal(fileName, line, 0, key, args);
    }
    
    @Override
    public void logExternal(String fileName, int lineNo, int colNo, String key,
            Object... args) {
        messageCollector.add(
                new LocalizedMessage(
                    fileName,
                    lineNo,
                    colNo,
                    getMessageBundle(),
                    key,
                    args,
                    getSeverityLevel(),
                    getId(),
                    getClass(),
                    getCustomMessages().get(key)));
    }
}
