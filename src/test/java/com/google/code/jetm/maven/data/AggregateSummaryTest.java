package com.google.code.jetm.maven.data;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import etm.core.aggregation.Aggregate;

/**
 * Unit tests for {@link AggregateSummary}.
 * 
 * @author jrh3k5
 * 
 */

public class AggregateSummaryTest {
    /**
     * Test the summary of two aggregates in a single {@link AggregateSummary}
     * object.
     */
    @Test
    public void testAdd() {
        final Aggregate aggregateOne = mock(Aggregate.class);
        when(aggregateOne.getMin()).thenReturn(Double.valueOf(1.0));
        when(aggregateOne.getMax()).thenReturn(Double.valueOf(2.0));
        when(aggregateOne.getTotal()).thenReturn(Double.valueOf(3.0));
        when(aggregateOne.getMeasurements()).thenReturn(Long.valueOf(4));

        final Aggregate aggregateTwo = mock(Aggregate.class);
        when(aggregateTwo.getMin()).thenReturn(Double.valueOf(5.0));
        when(aggregateTwo.getMax()).thenReturn(Double.valueOf(6.0));
        when(aggregateTwo.getTotal()).thenReturn(Double.valueOf(7.0));
        when(aggregateTwo.getMeasurements()).thenReturn(Long.valueOf(8));

        final AggregateSummary summary = new AggregateSummary("a summary");
        summary.add(aggregateOne);
        summary.add(aggregateTwo);

        assertThat(summary.getMin()).isEqualTo(aggregateOne.getMin());
        assertThat(summary.getMax()).isEqualTo(aggregateTwo.getMax());
        assertThat(summary.getTotal()).isEqualTo(aggregateOne.getTotal() + aggregateTwo.getTotal());
        assertThat(summary.getMeasurements()).isEqualTo(
                aggregateOne.getMeasurements() + aggregateTwo.getMeasurements());
    }

    /**
     * Test the comparison of two {@link AggregateSummary} objects. Two
     * summaries by the same name should match; those with different names
     * should not match.
     */
    @Test
    public void testCompareTo() {
        final AggregateSummary summaryA = new AggregateSummary("summaryA");
        final AggregateSummary summaryACopy = new AggregateSummary(summaryA.getName());
        
        assertThat(summaryA.compareTo(summaryACopy)).isZero();
        assertThat(summaryACopy.compareTo(summaryA)).isZero();
        
        final AggregateSummary summaryB = new AggregateSummary("summaryB");
        assertThat(summaryA.compareTo(summaryB)).isLessThan(0);
        assertThat(summaryB.compareTo(summaryA)).isGreaterThan(0);
    }

}
