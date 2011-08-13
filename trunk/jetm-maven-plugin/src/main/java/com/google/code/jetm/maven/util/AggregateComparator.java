package com.google.code.jetm.maven.util;

import java.io.Serializable;
import java.util.Comparator;

import etm.core.aggregation.Aggregate;

/**
 * A {@link Comparator} that compares two {@link Aggregate} objects by their {@link Aggregate#getName()}; it will first do a case-insensitive comparison and, if the yields the same result, will return
 * the case-sensitive comparison of their two names.
 * 
 * @author jrh3k5
 * 
 */

public class AggregateComparator implements Comparator<Aggregate>, Serializable {
    private static final long serialVersionUID = -7535778376868300149L;

    /**
     * {@inheritDoc}
     */
    public int compare(Aggregate o1, Aggregate o2) {
        final int caseIgnoreDiff = o1.getName().compareToIgnoreCase(o2.getName());
        return caseIgnoreDiff == 0 ? o1.getName().compareTo(o2.getName()) : caseIgnoreDiff;
    }

}
