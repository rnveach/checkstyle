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
//////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class VerifyModifiers {
    private static final List<Violation> VIOLATIONS = new ArrayList<Violation>();

    private static boolean DEBUG = true;

    public static void main(String[] args) throws Exception {
        readInput();
        runLogic();
    }

    // ////////////////////////////////////////////////////////////////////////////////

    private static void readInput() throws Exception {
        final File file = new File("M:\\output.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                processLine(line);
            }
        }
    }

    private static void processLine(String line) {
        int pos = line.indexOf("Violation - ");
        if (pos == -1)
            return;

        pos += 12;

        VIOLATIONS.add(new Violation(line, pos));
    }

    // ////////////////////////////////////////////////////////////////////////////////

    private static void runLogic() throws Exception {
        // String previousFile = null;
        // String previousFileContents = null;
        // String[] previousFileLines = null;

        for (Violation violation : VIOLATIONS) {
            System.out.println("Starting: " + violation.toString());

            try {
                final File file = new File(violation.file);
                final byte[] originalBytes;
                final String originalContents;
                final String[] fileLines;

                // if (violation.file.equals(previousFile)) {
                // originalContents = new String(previousFileContents);
                // fileLines = Arrays.copyOf(previousFileLines,
                // previousFileLines.length);
                // }
                // else {
                originalBytes = Files.readAllBytes(file.toPath());
                originalContents = new String(originalBytes);
                fileLines = originalContents.split(System.lineSeparator());
                // }

                if (originalContents.length() == 0)
                    System.out.println("ERROR: File is empty");
                if (fileLines.length <= 5)
                    System.out.println("ERROR: File is has too little lines");

                replaceFileContents(file, fileLines, violation);

                if (compileTest()) {
                    System.out.println("Passed Change: " + violation.toString());
                }
                else {
                    System.out.println("Failed Change: " + violation.toString());
                }

                if (originalContents.length() == 0)
                    System.out.println("ERROR: File is empty");
                if (fileLines.length <= 5)
                    System.out.println("ERROR: File is has too little lines");

                // restore original file
                file.delete();
                Files.write(file.toPath(), originalBytes, StandardOpenOption.CREATE);
                Thread.sleep(1000);

                if (file.length() != originalBytes.length) {
                    System.out.println("ERROR: file restoration doesn't match expected amount by "
                            + (file.length() - originalContents.length()) + " bytes");
                }

                // save memory if same file
                // previousFile = violation.file;
                // previousFileContents = originalContents;
                // previousFileLines = fileLines;
            }
            catch (NoSuchFileException ex) {
                System.out.println("ERROR: No such file");
                // previousFile = null;
                // previousFileContents = null;
                // previousFileLines = null;
            }
        }
    }

    private static void replaceFileContents(File file, String[] fileLines, Violation violation)
            throws Exception {
        final long beforeLength = file.length();

        if (fileLines.length < violation.lineStart)
            System.out.println("ERROR: failed to change the file.");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                file)))) {
            int linePosition = 1;
            for (String line : fileLines) {
                if (linePosition == violation.lineStart) {
                    if ((line.length() < violation.columnStart)
                            || (line.length() < violation.columnEnd)) {
                        System.out.println("ERROR: Line is too short for replacement");
                    }
                    else {
                        bw.write(line.substring(0, violation.columnStart));
                        bw.write("private");
                        bw.write(line.substring(violation.columnEnd, line.length()));
                    }
                }
                else {
                    bw.write(line);
                }

                bw.newLine();

                linePosition++;
            }

            bw.flush();
        }

        Thread.sleep(1000);

        final long afterLength = file.length();

        if (afterLength + 5 < beforeLength) {
            System.out.println("ERROR: file shrunk unexpected amount by "
                    + (afterLength - beforeLength) + " bytes");
        }
    }

    private static boolean compileTest() throws Exception {
        final Runtime runtTime = Runtime.getRuntime();
        final Process process = runtTime.exec("cmd /c mvn --batch-mode test", null,
                getWorkingDirectory());

        if (DEBUG) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()))) {
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }

        final boolean result = process.waitFor() == 0;

        if (DEBUG) {
            final String errorStream = toString(process.getErrorStream());

            System.out.println(errorStream);
            System.out.println("RC: " + process.exitValue());

            System.out.println("-----------------------------------------");
        }

        return result;
    }

    private static String toString(InputStream inputStream) throws IOException {
        final StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line + System.getProperty("line.separator"));
            }
        }

        return sb.toString();
    }

    private static File getWorkingDirectory() throws IOException {
        final File path = new File(".");
        final File directory = new File(path.getCanonicalPath());

        return directory;
    }

    // ////////////////////////////////////////////////////////////////////////////////

    private static class Violation {
        private String file;

        private int lineStart;
        private int lineEnd;

        private int columnStart;
        private int columnEnd;

        public Violation(String line, int pos) {
            file = getFile(line);

            final String[] split = line.substring(pos).split(" ");

            final String[] start = split[0].split(",");
            final String[] end = split[2].split(",");

            lineStart = Integer.parseInt(start[0]);
            columnStart = Integer.parseInt(start[1]);

            lineEnd = Integer.parseInt(end[0]);
            columnEnd = Integer.parseInt(end[1]);
        }

        private static String getFile(String line) {
            final int end = line.indexOf(":", 20);

            return line.substring(8, end);
        }

        @Override
        public String toString() {
            return "{" + this.file + ", " + this.lineStart + ", " + this.columnStart + ", "
                    + this.lineEnd + ", " + this.columnEnd + "}";
        }
    }
}
