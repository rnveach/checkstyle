package com.puppycrawl.tools.checkstyle;

/**
 * State of AST.
 * Indicates whether tree contains certain nodes.
 */
public enum AstState {
    /**
     * Ordinary tree.
     */
    ORDINARY,

    /**
     * AST contains comment nodes.
     */
    WITH_COMMENTS
    }
