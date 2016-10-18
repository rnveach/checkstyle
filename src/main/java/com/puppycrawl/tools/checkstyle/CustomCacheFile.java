package com.puppycrawl.tools.checkstyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CustomCacheFile<V> {
    public static String cacheLocation = "";

    /** Location of the file to save the cache to. */
    private final File location;
    /** Used to keep track of files to trim contents if files are deleted. */
    private final Set<File> usedFiles = new TreeSet<File>();
    /** Custom cache to be used by filesets/checks. */
    private final Map<File, V> contents = new HashMap<File, V>();

    public CustomCacheFile(String fileName) {
        location = new File(cacheLocation + fileName);
    }

    public void put(File key, V value) {
        usedFiles.add(key);
        contents.put(key, value);
    }

    public V get(File key) {
        usedFiles.add(key);
        return contents.get(key);
    }

    public void trimFiles() {
        // get hash map keys
        final Set<File> keys = contents.keySet();
        // remove hash map keys on files we used this run
        keys.removeAll(usedFiles);
        // remove what is left over, deleted/renamed/moved files that don't exist anymore
        for (File key : keys) {
            contents.remove(key);
        }
    }

    public void reset() {
        usedFiles.clear();
        contents.clear();
    }

    public void save() throws IOException {
        final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(location));
        try {
            out.writeObject(contents);
        }
        finally {
            out.close();
        }
    }

    @SuppressWarnings("unchecked")
    public void load() throws Exception {
        contents.clear();

        if (location.exists()) {
            final ObjectInputStream out = new ObjectInputStream(new FileInputStream(location));
            try {
                contents.putAll(((Map<File, V>) out.readObject()));
            }
            finally {
                out.close();
            }
        }
    }

    public void delete() {
        location.delete();
    }
}
