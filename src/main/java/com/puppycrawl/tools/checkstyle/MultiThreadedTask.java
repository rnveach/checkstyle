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

package com.puppycrawl.tools.checkstyle;

import java.io.File;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.puppycrawl.tools.checkstyle.api.FileSetCheck;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;

/**
 * This class provides the functionality to perform one set of work on a file in a multi-threaded
 * environment.
 *
 * @author Richard Veach
 */
public class MultiThreadedTask implements Callable<SortedSet<LocalizedMessage>> {
    private Future<SortedSet<LocalizedMessage>> future;

    private File file;
    private String charset;
    private List<FileSetCheck> fileSetChecks;

    /**
     * Initializes the class.
     * 
     * @param charset
     *            Name of a charset.
     * @param fileSetChecks
     *            List of fileset checks.
     */
    public void init(String charset, List<FileSetCheck> fileSetChecks) {
        this.charset = charset;

        // TODO: copy filesets here
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
    public SortedSet<LocalizedMessage> call() throws Exception {
        return Checker.processFile(file, charset, fileSetChecks);
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
    public SortedSet<LocalizedMessage> getResult() throws InterruptedException, ExecutionException {
        final SortedSet<LocalizedMessage> result = future.get();

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
}
