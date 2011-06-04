package com.google.code.jetm.maven.data;

import java.util.Collections;
import java.util.Map;

import etm.core.aggregation.Aggregate;

/**
 * A bean to store summary of aggregate data.
 * 
 * @author jrh3k5
 * 
 */

public class AggregateSummary implements Aggregate, Comparable<AggregateSummary> {
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private double total;
    private long measurements;
    private String name;

    /**
     * Create a summary.
     * 
     * @param name
     *            The name of the summary.
     * @throws IllegalArgumentException
     *             If the given name is {@code null}.
     */
    public AggregateSummary(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null.");

        this.name = name;
    }

    /**
     * Add an aggregate to the summary.
     * 
     * @param aggregate
     *            The {@link Aggregate} to be added to the summary.
     */
    public void add(Aggregate aggregate) {
        this.min = Math.min(aggregate.getMin(), getMin());
        this.max = Math.max(aggregate.getMax(), getMax());
        this.total += aggregate.getTotal();
        this.measurements += aggregate.getMeasurements();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(AggregateSummary o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof AggregateSummary))
            return false;

        return getName().equals(((AggregateSummary) o).getName());
    }

    /**
     * {@inheritDoc}
     */
    public double getAverage() {
        return getTotal() / getMeasurements();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public Map getChilds() {
        return Collections.emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    public double getMax() {
        return max;
    }

    /**
     * {@inheritDoc}
     */
    public long getMeasurements() {
        return measurements;
    }

    /**
     * {@inheritDoc}
     */
    public double getMin() {
        return min;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public double getTotal() {
        return total;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChilds() {
        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
