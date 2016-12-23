package com.puppycrawl.tools.checkstyle;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.checks.coding.JavaPropertiesCacheCheck;

public class PropertyUsedCheck extends AbstractFileSetCheck {
    public Set<String> properties = new HashSet<String>();

    private CustomCacheFile<Set<String>> cache;

    public PropertyUsedCheck() {
        setFileExtensions("properties");
    }

    @Override
    public void init() {
        this.cache = new CustomCacheFile<Set<String>>(this.getClass().getSimpleName() + ".dat");
        try {
            this.cache.load();
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onCacheReset() {
        this.cache.reset();
    }

    @Override
    protected boolean canSkipCachedFileFiltered(File file) {
        return (this.cache.get(file) != null);
    }

    @Override
    protected void skipCachedFileFiltered(File file) {
        this.properties.addAll(this.cache.get(file));
    }

    @Override
    public void destroy() {
        this.cache.trimFiles();
        try {
            this.cache.save();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void processFiltered(File file, List<String> lines) throws CheckstyleException {
        final Set<String> list = new HashSet<String>();

        for (String line : lines) {
            if (!line.isEmpty() && line.contains("=")) {
                list.add(line.split("=", 2)[0].trim());
            }
        }

        this.cache.put(file, list);
        this.properties.addAll(list);
    }

    @Override
    protected void finishProcessFiltered() {
        for (String property : this.properties) {
            if (!JavaPropertiesCacheCheck.strings.contains(property)) {
                // fileName is missing because this is just an example
                logExternal("fileName", 0, "property.notused", property);
            }
        }
    }
}
