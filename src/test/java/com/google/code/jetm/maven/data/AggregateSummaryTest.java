package com.google.code.jetm.maven.data;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import etm.core.aggregation.Aggregate;

/**
 * Unit tests for {@link AggregateSummary}.
 * 
 * @author jrh3k5
 * 
 */

public class AggregateSummaryTest {
    /**
     * A {@link Rule} used to test for thrown exceptions.
     */
    @Rule
    public ExpectedException expected = ExpectedException.none();

    /**
     * Given a {@code null} name, construction should fail.
     */
    @Test
    public void testConstructNullName() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Name cannot be null.");
        new AggregateSummary(null);
    }

    /**
     * Two summaries by the same name should be equal.
     */
    @Test
    public void testEquals() {
        final AggregateSummary summary = new AggregateSummary("test name");
        final AggregateSummary other = new AggregateSummary(summary.getName());
        assertThat(summary).isEqualTo(other);
        assertThat(other).isEqualTo(summary);
    }

    /**
     * An aggregate summary should not be equal to an object that is not an
     * aggregate summary.
     */
    @Test
    public void testEqualsNotAggregateSummary() {
        assertThat(new AggregateSummary("a summary")).isNotEqualTo(new Object());
    }

    /**
     * An aggregate summary should not be equal to {@code null}.
     */
    @Test
    public void testEqualsNull() {
        assertThat(new AggregateSummary("not null")).isNotEqualTo(null);
    }

    /**
     * An aggregate summary should be equal to itself.
     */
    @Test
    public void testEqualsSelf() {
        final AggregateSummary summary = new AggregateSummary("self");
        assertThat(summary).isEqualTo(summary);
    }

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
        assertThat(summary.getMeasurements()).isEqualTo(aggregateOne.getMeasurements() + aggregateTwo.getMeasurements());
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

    /**
     * A summary's hash code should be simply the hash code of its name.
     */
    @Test
    public void testHashCode() {
        final String name = "this will be a hash code";
        assertThat(new AggregateSummary(name).hashCode()).isEqualTo(name.hashCode());
    }
}
