package com.puppycrawl.tools.checkstyle.checks.coding.toomanyreassignments;

public class InputTooManyReassignmentsInvalid {
    public void method1() {
        int a;
        a = 1;
        a = 2;
        a = 3;
        a = 4;
    }

    public void method2() {
        int a = 1;
        a = 2;
        a = 3;
        a = 4;
    }

    int field;

    public void method3() {
        field = 1;
        field = 2;
        field = 3;
        this.field = 4;
    }
}
