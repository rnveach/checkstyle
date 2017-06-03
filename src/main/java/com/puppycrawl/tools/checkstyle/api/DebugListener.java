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

package com.puppycrawl.tools.checkstyle.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class DebugListener extends AutomaticBean implements AuditListener {
    private final Map<String, AtomicLong> checkTime = new TreeMap<String, AtomicLong>();
    private final Map<String, AtomicLong> checkUses = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> checkMin = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> checkMax = new HashMap<String, AtomicLong>();

    private final Map<String, AtomicLong> fileSetTime = new TreeMap<String, AtomicLong>();
    private final Map<String, AtomicLong> fileSetUses = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> fileSetMin = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> fileSetMax = new HashMap<String, AtomicLong>();

    private final Map<String, AtomicLong> fileTime = new TreeMap<String, AtomicLong>();
    private final Map<String, AtomicLong> fileUses = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> fileMin = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> fileMax = new HashMap<String, AtomicLong>();

    private final Map<String, AtomicLong> parseTime = new TreeMap<String, AtomicLong>();
    private final Map<String, AtomicLong> parseUses = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> parseMin = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> parseMax = new HashMap<String, AtomicLong>();

    private final Map<String, AtomicLong> customTime = new TreeMap<String, AtomicLong>();
    private final Map<String, AtomicLong> customUses = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> customMin = new HashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> customMax = new HashMap<String, AtomicLong>();
    private final Stack<Long> customStartTimeMemory = new Stack<Long>();
    private final Stack<String> customNamesMemory = new Stack<String>();

    private long startTime;
    private long fileStartTime;
    private long fileSetStartTime;
    private long checkStartTime;
    private long parseStartTime;
    private long javaDocParseStartTime;
    private long customStartTime;

    @Override
    public void auditStarted(AuditEvent event) {
        this.startTime = System.nanoTime();
        this.fileStartTime = 0;
        this.fileSetStartTime = 0;
        this.checkStartTime = 0;
        this.customStartTime = 0;

        this.checkTime.clear();
        this.checkUses.clear();
        this.checkMin.clear();
        this.checkMax.clear();
        this.fileSetTime.clear();
        this.fileSetUses.clear();
        this.fileSetMin.clear();
        this.fileSetMax.clear();
        this.fileTime.clear();
        this.fileUses.clear();
        this.fileMin.clear();
        this.fileMax.clear();
        this.parseTime.clear();
        this.parseUses.clear();
        this.parseMin.clear();
        this.parseMax.clear();
        this.customTime.clear();
        this.customUses.clear();
        this.customMin.clear();
        this.customMax.clear();
        this.customStartTimeMemory.clear();
        this.customNamesMemory.clear();
    }

    @Override
    public void auditFinished(AuditEvent event) {
        System.out.println("------------------");
        System.out.println("Run Time: " + format(System.nanoTime() - this.startTime, 1));
        System.out.println();

        System.out.println("Files: (" + fileTime.size() + ")");

        for (String key : fileTime.keySet()) {
            System.out.println(key + "\t" + fileUses.get(key).get() + "\t"
                    + format(fileTime.get(key).get(), 1)
                    + "\t" //
                    + format(fileMin.get(key).get(), 1)
                    + "\t" //
                    + format(fileMax.get(key).get(), 1) + "\t"
                    + format(fileTime.get(key).get(), fileUses.get(key).get()));
        }

        System.out.println();
        System.out.println("FileSets: (" + fileSetTime.size() + ")");

        for (String key : fileSetTime.keySet()) {
            System.out.println(key + "\t" + fileSetUses.get(key).get() + "\t"
                    + format(fileSetTime.get(key).get(), 1)
                    + "\t" //
                    + format(fileSetMin.get(key).get(), 1)
                    + "\t" //
                    + format(fileSetMax.get(key).get(), 1) + "\t"
                    + format(fileSetTime.get(key).get(), fileSetUses.get(key).get()));
        }

        System.out.println();
        System.out.println("Checks: (" + checkTime.size() + ")");

        for (String key : checkTime.keySet()) {
            System.out.println(key + "\t" + checkUses.get(key).get() + "\t"
                    + format(checkTime.get(key).get(), 1)
                    + "\t" //
                    + format(checkMin.get(key).get(), 1)
                    + "\t" //
                    + format(checkMax.get(key).get(), 1) + "\t"
                    + format(checkTime.get(key).get(), checkUses.get(key).get()));
        }

        System.out.println();
        System.out.println("Parses: (" + parseTime.size() + ")");

        for (String key : parseTime.keySet()) {
            System.out.println(key + "\t" + parseUses.get(key).get() + "\t"
                    + format(parseTime.get(key).get(), 1)
                    + "\t" //
                    + format(parseMin.get(key).get(), 1)
                    + "\t" //
                    + format(parseMax.get(key).get(), 1) + "\t"
                    + format(parseTime.get(key).get(), parseUses.get(key).get()));
        }

        System.out.println();
        System.out.println();
        System.out.println("Customs: (" + customTime.size() + ")");

        for (String key : customTime.keySet()) {
            System.out.println(key + "\t" + customUses.get(key).get() + "\t"
                    + format(customTime.get(key).get(), 1)
                    + "\t" //
                    + format(customMin.get(key).get(), 1)
                    + "\t" //
                    + format(customMax.get(key).get(), 1) + "\t"
                    + format(customTime.get(key).get(), customUses.get(key).get()));
        }

        System.out.println("------------------");
    }

    private static String format(long i, long j) {
        double d;

        if (j == 1)
            d = i;
        else
            d = (double) i / j;

        double temp = d / 1000000000;

        return String.valueOf((long) (temp * 1000) / 1000d);
    }

    @Override
    public void fileStarted(AuditEvent event) {
        this.fileStartTime = System.nanoTime();
    }

    @Override
    public void fileSetStarted(AuditEvent event) {
        this.fileSetStartTime = System.nanoTime();
    }

    @Override
    public void checkStarted(AuditEvent event) {
        this.checkStartTime = System.nanoTime();
    }

    @Override
    public void parseStarted(AuditEvent event) {
        this.parseStartTime = System.nanoTime();
    }

    @Override
    public void JavaDocParseStarted(AuditEvent event) {
        this.javaDocParseStartTime = System.nanoTime();
    }

    @Override
    public void CustomStarted(AuditEvent event) {
        if (this.customStartTime != 0)
            customStartTimeMemory.push(this.customStartTime);
        this.customNamesMemory.push(event.getSource().toString());

        this.customStartTime = System.nanoTime();
    }

    @Override
    public void checkFinished(AuditEvent event) {
        if (this.checkStartTime == 0)
            return;

        final String src = event.getSource().getClass().getSimpleName();
        final long d = System.nanoTime() - this.checkStartTime;
        this.checkStartTime = 0;

        if (this.checkTime.get(src) == null) {
            this.checkTime.put(src, new AtomicLong());
            this.checkUses.put(src, new AtomicLong());
            this.checkMin.put(src, new AtomicLong(Long.MAX_VALUE));
            this.checkMax.put(src, new AtomicLong(Long.MIN_VALUE));
        }

        final AtomicLong min = this.checkMin.get(src);
        if (d < min.get())
            min.set(d);

        final AtomicLong max = this.checkMax.get(src);
        if (d > max.get())
            max.set(d);

        this.checkTime.get(src).addAndGet(d);
        this.checkUses.get(src).addAndGet(1);

        // reduce time from fileset as this is part of its process
        if (this.fileSetStartTime != 0) {
            this.fileSetStartTime += d;
        }
    }

    @Override
    public void fileSetFinished(AuditEvent event) {
        if (this.fileSetStartTime == 0)
            return;

        final String src = event.getSource().getClass().getSimpleName();
        final long d = System.nanoTime() - this.fileSetStartTime;
        this.fileSetStartTime = 0;

        if (this.fileSetTime.get(src) == null) {
            this.fileSetTime.put(src, new AtomicLong());
            this.fileSetUses.put(src, new AtomicLong());
            this.fileSetMin.put(src, new AtomicLong(Long.MAX_VALUE));
            this.fileSetMax.put(src, new AtomicLong(Long.MIN_VALUE));
        }

        final AtomicLong min = this.fileSetMin.get(src);
        if (d < min.get())
            min.set(d);

        final AtomicLong max = this.fileSetMax.get(src);
        if (d > max.get())
            max.set(d);

        this.fileSetTime.get(src).addAndGet(d);
        this.fileSetUses.get(src).addAndGet(1);
    }

    @Override
    public void fileFinished(AuditEvent event) {
        if (this.fileStartTime == 0)
            return;

        final String src = event.getFileName();
        final long d = System.nanoTime() - this.fileStartTime;
        this.fileStartTime = 0;

        if (this.fileTime.get(src) == null) {
            this.fileTime.put(src, new AtomicLong());
            this.fileUses.put(src, new AtomicLong());
            this.fileMin.put(src, new AtomicLong(Long.MAX_VALUE));
            this.fileMax.put(src, new AtomicLong(Long.MIN_VALUE));
        }

        final AtomicLong min = this.fileMin.get(src);
        if (d < min.get())
            min.set(d);

        final AtomicLong max = this.fileMax.get(src);
        if (d > max.get())
            max.set(d);

        this.fileTime.get(src).addAndGet(d);
        this.fileUses.get(src).addAndGet(1);
    }

    @Override
    public void parseFinished(AuditEvent event) {
        if (this.parseStartTime == 0)
            return;

        final String src = "Java";
        final long d = System.nanoTime() - this.parseStartTime;
        this.parseStartTime = 0;

        if (this.parseTime.get(src) == null) {
            this.parseTime.put(src, new AtomicLong());
            this.parseUses.put(src, new AtomicLong());
            this.parseMin.put(src, new AtomicLong(Long.MAX_VALUE));
            this.parseMax.put(src, new AtomicLong(Long.MIN_VALUE));
        }

        final AtomicLong min = this.parseMin.get(src);
        if (d < min.get())
            min.set(d);

        final AtomicLong max = this.parseMax.get(src);
        if (d > max.get())
            max.set(d);

        this.parseTime.get(src).addAndGet(d);
        this.parseUses.get(src).addAndGet(1);

        // reduce time from fileset as this is part of its process
        if (this.fileSetStartTime != 0) {
            this.fileSetStartTime += d;
        }
    }

    @Override
    public void JavaDocParseFinished(AuditEvent event) {
        if (this.javaDocParseStartTime == 0)
            return;

        final String src = "JavaDoc";
        final long d = System.nanoTime() - this.javaDocParseStartTime;
        this.javaDocParseStartTime = 0;

        if (this.parseTime.get(src) == null) {
            this.parseTime.put(src, new AtomicLong());
            this.parseUses.put(src, new AtomicLong());
            this.parseMin.put(src, new AtomicLong(Long.MAX_VALUE));
            this.parseMax.put(src, new AtomicLong(Long.MIN_VALUE));
        }

        final AtomicLong min = this.parseMin.get(src);
        if (d < min.get())
            min.set(d);

        final AtomicLong max = this.parseMax.get(src);
        if (d > max.get())
            max.set(d);

        this.parseTime.get(src).addAndGet(d);
        this.parseUses.get(src).addAndGet(1);

        // reduce time from check as this is part of its process
        if (this.checkStartTime != 0) {
            this.checkStartTime += d;
        }
    }

    @Override
    public void CustomFinished(AuditEvent event) {
        if (this.customStartTime == 0)
            return;

        final String src = event.getSource().toString();
        final String startSrc = this.customNamesMemory.pop();

        if (!startSrc.equals(src)) {
            System.err.println("Custom name mis-match: " + src + " vs " + startSrc);
        }
        
        final long d = System.nanoTime() - this.customStartTime;

        if (this.customStartTimeMemory.size() > 0)
            this.customStartTime = this.customStartTimeMemory.pop() + d;
        else
            this.customStartTime = 0;

        if (this.customTime.get(src) == null) {
            this.customTime.put(src, new AtomicLong());
            this.customUses.put(src, new AtomicLong());
            this.customMin.put(src, new AtomicLong(Long.MAX_VALUE));
            this.customMax.put(src, new AtomicLong(Long.MIN_VALUE));
        }

        final AtomicLong min = this.customMin.get(src);
        if (d < min.get())
            min.set(d);

        final AtomicLong max = this.customMax.get(src);
        if (d > max.get())
            max.set(d);

        this.customTime.get(src).addAndGet(d);
        this.customUses.get(src).addAndGet(1);
    }

    @Override
    public void addError(AuditEvent event) {
        // nothing
    }

    @Override
    public void addException(AuditEvent event, Throwable throwable) {
        // nothing
    }

    @Override
    protected void finishLocalSetup() throws CheckstyleException {
        // nothing
    }
}
