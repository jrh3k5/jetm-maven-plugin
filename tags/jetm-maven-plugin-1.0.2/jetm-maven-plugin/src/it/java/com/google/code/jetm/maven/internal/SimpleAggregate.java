package com.google.code.jetm.maven.internal;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import etm.core.aggregation.Aggregate;

/**
 * A simple {@link Aggregate} bean implementation.
 * 
 * @author jrh3k5
 * 
 */

public class SimpleAggregate implements Aggregate {
    private final String name;
    private final double average;
    private final double min;
    private final double max;
    private final long measurements;
    private final double total;
    private final boolean hasChilds;
    private final Map<?, ?> childs;

    /**
     * Create an aggregate.
     * 
     * @param name
     *            The name of the aggregate.
     * @param average
     *            The average of the measurements.
     * @param min
     *            The lowest measurement.
     * @param max
     *            The highest measurement.
     * @param measurements
     *            The number of measurements.
     * @param total
     *            The total of all measurements collected.
     */
    public SimpleAggregate(String name, double average, double min, double max, long measurements, double total) {
        this.name = name;
        this.average = average;
        this.min = min;
        this.max = max;
        this.measurements = measurements;
        this.total = total;
        this.hasChilds = false;
        this.childs = Collections.emptyMap();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof Aggregate))
            return false;

        final Aggregate other = (Aggregate) obj;
        return getName().equals(other.getName()) && getAverage() == other.getAverage() && getChilds().equals(other.getChilds()) && getMax() == other.getMax()
                && getMeasurements() == other.getMeasurements() && getMin() == other.getMin() && getTotal() == other.getTotal() && hasChilds() == other.hasChilds();
    }

    /**
     * {@inheritDoc}
     */
    public double getAverage() {
        return average;
    }

    /**
     * {@inheritDoc}
     */
    public Map<?, ?> getChilds() {
        return childs;
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
        return hasChilds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(average);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(max);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (int) (measurements ^ (measurements >>> 32));
        temp = Double.doubleToLongBits(min);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        temp = Double.doubleToLongBits(total);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
