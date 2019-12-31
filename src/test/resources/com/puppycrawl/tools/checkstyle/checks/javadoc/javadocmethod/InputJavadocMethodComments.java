package com.puppycrawl.tools.checkstyle.checks.javadoc.javadocmethod;

public class InputJavadocMethodComments
{
    /**
     * A Javadoc comment.
     */
    //@ A JML Annotation
    public int foo() { return 0; } 

    /**
     * A Javadoc comment.
     */
    /*@ A JML Annotation */
    public int foo2() { return 0; } 
}
