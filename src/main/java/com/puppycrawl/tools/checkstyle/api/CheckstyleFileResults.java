package com.puppycrawl.tools.checkstyle.api;

import java.util.SortedSet;

public final class CheckstyleFileResults {
    private final FileContents fileContents;
    private final SortedSet<LocalizedMessage> messages;

    public CheckstyleFileResults(FileContents fileContents, SortedSet<LocalizedMessage> messages) {
        this.fileContents = fileContents;
        this.messages = messages;
    }

    public FileContents getFileContents() {
        return fileContents;
    }

    public SortedSet<LocalizedMessage> getMessages() {
        return messages;
    }
}
