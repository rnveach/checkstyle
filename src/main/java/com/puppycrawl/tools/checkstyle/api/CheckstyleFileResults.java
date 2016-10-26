package com.puppycrawl.tools.checkstyle.api;

import java.util.SortedSet;

public final class CheckstyleFileResults {
    private final FileText fileText;
    private final SortedSet<LocalizedMessage> messages;

    public CheckstyleFileResults(FileText fileText, SortedSet<LocalizedMessage> messages) {
        this.fileText = fileText;
        this.messages = messages;
    }

    public FileText getFileText() {
        return fileText;
    }

    public SortedSet<LocalizedMessage> getMessages() {
        return messages;
    }
}
