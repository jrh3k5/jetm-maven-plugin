package com.google.code.jetm.maven.data;

import etm.core.aggregation.Aggregate;

public class AggregateSummary {
    private double min;
    private double max;
    private double total;
    private long measurements;
    private String name;

    public AggregateSummary(String name) {
        this.name = name;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getTotal() {
        return total;
    }

    public long getMeasurements() {
        return measurements;
    }

    public String getName() {
        return name;
    }

    public void add(Aggregate aggregate) {
        this.min += aggregate.getMin();
        this.max += aggregate.getMax();
        this.total += aggregate.getTotal();
        this.measurements += aggregate.getMeasurements();
    }
}
