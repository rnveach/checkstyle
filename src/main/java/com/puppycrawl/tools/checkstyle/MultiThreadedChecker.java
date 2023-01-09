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
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Violation;

/**
 * This class provides the functionality to check a set of files in a multi-threaded environment.
 *
 * @author Richard Veach
 */
public class MultiThreadedChecker extends SingleChecker {
    private ExecutorService executor;
    private MultiThreadedTask[] tasks;
    private int tasksRunning;

    private int numberOfThreads;

    @Override
    public int process(List<File> files) throws CheckstyleException {
        // init hack override
        init();

        return super.process(files);
    }

    /**
     * Initializes the class.
     */
    public void init() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(numberOfThreads);
        }
        if (tasks == null) {
            tasks = new MultiThreadedTask[numberOfThreads];
        }

        tasksRunning = 0;
    }

    @Override
    protected void processFiles(List<File> files) throws CheckstyleException {
        // init tasks
        for (int iindex = 0; iindex < tasks.length; iindex++) {
            if (tasks[iindex] == null) {
                tasks[iindex] = new MultiThreadedTask();
            }

            try {
                tasks[iindex].init(haltOnException, charset, fileSetChecks);
            }
            catch (Throwable t) {
                throw new CheckstyleException("Failed to deep copy a file set", t);
            }
        }

        super.processFiles(files);

        // keep waiting for tasks to finish before returning
        try {
            while (tasksRunning > 0) {
                processTasks(null);

                if (tasksRunning > 0) {
                    Thread.sleep(100);
                }
            }
        }
        catch (Exception ex) {
            throw new CheckstyleException("Multi-threaded sleep exception", ex);
        }
    }

    @Override
    protected void workFile(File file, long timestamp) throws Exception {
        final String fileName = file.getAbsolutePath();
        fireFileStarted(fileName);

        processTasks(file);
    }

    private void processTasks(File file) throws CheckstyleException {
        try {
            do {
                for (MultiThreadedTask task : tasks) {
                    if (!task.isWorking()) {
                        if (task.hasResults()) {
                            final SortedSet<Violation> taskresults = task.getResult();
                            final File taskFile = task.getFile();

                            tasksRunning--;

                            endWorkingFile(taskFile.getAbsolutePath(), taskresults,
                                    taskFile.lastModified());
                        }

                        if (file != null) {
                            task.start(executor, file);
                            tasksRunning++;
                            return;
                        }
                    }
                }

                if (file == null || tasksRunning == 0) {
                    break;
                }

                // keep waiting for a thread to free up
                Thread.sleep(100);
            } while (true);
        }
        catch (Exception ex) {
            throw new CheckstyleException("Multi-threaded process tasks exception", ex);
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        executor.shutdownNow();
    }

    /**
     * Sets the number of threads to use.
     *
     * @param numberOfThreads
     *            The number of threads to use.
     */
    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
}
