/*
RequireThis
checkFields = (default)true
checkMethods = (default)true
validateOnlyOverlapping = false


*/

// someexamples of 1.5 extensions
package com.puppycrawl.tools.checkstyle.checks.coding.requirethis; // ok

@interface MyAnnotation1 {
    String name();
    int version();
}

@MyAnnotation1(name = "ABC", version = 1)
public class InputRequireThis15Extensions
{

}

enum Enum2
{
    A, B, C;
    Enum2() {}
    public String toString() {
        return ""; //some custom implementation
    }
}

interface TestRequireThisEnum
{
    enum DAY_OF_WEEK
    {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY
    }
}
public class NestedClass {
    protected RuntimeException exception = new RuntimeException() {};

    public void anonEx2() {
        RuntimeException exception = new RuntimeException();
        try {
            //some code
            String re = "lol";
        } catch (Exception e) {
            throw exception;
        }
    }
}
