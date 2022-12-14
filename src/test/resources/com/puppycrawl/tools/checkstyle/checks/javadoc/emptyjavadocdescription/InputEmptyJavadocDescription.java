/*
EmptyJavadocDescription
scope = (default)private
excludeScope = (default)null


*/

package com.puppycrawl.tools.checkstyle.checks.javadoc.emptyjavadocdescription;

/**
 * Abstract superclass of all test-suite builders for collection interfaces.
 *
 * @author George van den Driessche
 */
@GwtIncompatible
public abstract class AbstractCollectionTestSuiteBuilder<
         B extends AbstractCollectionTestSuiteBuilder<B, E>, E>
     extends PerCollectionSizeTestSuiteBuilder<B, TestCollectionGenerator<E>, Collection<E>, E> {

    /** @see AbstractContainerTester#resetContainer() */
    protected void resetCollection() {
      resetContainer();
    }
}