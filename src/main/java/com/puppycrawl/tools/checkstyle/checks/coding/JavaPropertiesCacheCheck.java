package com.puppycrawl.tools.checkstyle.checks.coding;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.CacheAware;
import com.puppycrawl.tools.checkstyle.CustomCacheFile;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class JavaPropertiesCacheCheck extends AbstractCheck implements CacheAware {
    private static final Pattern pattern = Pattern.compile("[a-zA-Z0-9.-_]+");

    public static Set<String> strings = new HashSet<String>();

    private CustomCacheFile<Set<String>> cache;
    private File currentFile;

    @Override
    public int[] getDefaultTokens() {
        return new int[] {
            TokenTypes.STRING_LITERAL
        };
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
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
    public boolean canSkipCachedFile(File file) {
        return (this.cache.get(file) != null);
    }

    @Override
    public void skipCachedFile(File file) {
        strings.addAll(this.cache.get(file));
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
    public void beginTree(DetailAST rootAST) {
        this.currentFile = new File(getFileContents().getFileName());
        this.cache.put(this.currentFile, new HashSet<String>());
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getParent().getType() != TokenTypes.PLUS) {
            String text = ast.getText();
            text = text.substring(1, text.length() - 1);

            if (isMatchProperty(text)) {
                this.cache.get(this.currentFile).add(text);
                strings.add(text);
            }
        }
    }

    private static boolean isMatchProperty(String text) {
        return pattern.matcher(text).matches();
    }

    @Override
    public void finishProcessing() {
        // do nothing
    }
}
