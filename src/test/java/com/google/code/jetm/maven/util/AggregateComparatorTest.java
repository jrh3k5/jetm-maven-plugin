package com.google.code.jetm.maven.util;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import etm.core.aggregation.Aggregate;

/**
 * Unit tests for {@link AggregateComparator}.
 * 
 * @author jrh3k5
 * 
 */

public class AggregateComparatorTest {
    private final AggregateComparator comparator = new AggregateComparator();

    /**
     * Test the comparison of two {@link Aggregate} objects by their names when they are different. Normally, the one with the lower-case letters would be the greater one but, because of the initial
     * case-insensitive comparison, the lower-case one is considered to be the lesser ("C" < "D").
     */
    @Test
    public void testCompareDifferentNames() {
        final Aggregate lesser = mock(Aggregate.class);
        when(lesser.getName()).thenReturn("abc");

        final Aggregate greater = mock(Aggregate.class);
        when(greater.getName()).thenReturn("ABD");
        
        assertThat(comparator.compare(lesser, greater)).isNegative();
        assertThat(comparator.compare(greater, lesser)).isPositive();
        assertThat(comparator.compare(greater, greater)).isZero();
        assertThat(comparator.compare(lesser, lesser)).isZero();
    }
}
