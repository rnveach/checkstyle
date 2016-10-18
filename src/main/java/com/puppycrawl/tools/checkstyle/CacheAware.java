package com.puppycrawl.tools.checkstyle;

import java.io.File;

public interface CacheAware {
    public void onCacheReset();

    public boolean canSkipCachedFile(File file);

    public void skipCachedFile(File file);
}
