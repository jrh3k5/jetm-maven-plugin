package com.google.code.jetm.maven.data;

import java.util.Collections;
import java.util.Map;

import etm.core.aggregation.Aggregate;

public class AggregateSummary implements Aggregate, Comparable<AggregateSummary> {
    private double min;
    private double max;
    private double total;
    private long measurements;
    private String name;

    public AggregateSummary(String name) {
        this.name = name;
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
    public double getMax() {
        return max;
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
    public long getMeasurements() {
        return measurements;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    public void add(Aggregate aggregate) {
        this.min += aggregate.getMin();
        this.max += aggregate.getMax();
        this.total += aggregate.getTotal();
        this.measurements += aggregate.getMeasurements();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(AggregateSummary o) {
        return getName().compareTo(o.getName());
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
    public boolean hasChilds() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    public Map getChilds() {
        return Collections.emptyMap();
    }
}
