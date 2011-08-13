package com.google.code.jetm.maven.data;

import com.google.code.jetm.maven.TimingReportMojo;

/**
 * Enumerations of the time units supported by this report.
 * 
 * @author jrh3k5
 * 
 */

public enum TimeUnit {
    /**
     * Milliseconds.
     */
    MILLISECONDS("ms", 1),
    /**
     * Seconds
     */
    SECONDS("sec", 1000);

    /**
     * Get a time unit from the appropriate {@link TimingReportMojo} abbreviate. The comparison is case-insensitive.
     * 
     * @param abbreviation
     *            The abbreviation from which to obtain the corresponding enumeration.
     * @return A {@link TimeUnit} enumeration corresponding to the given abbreviation:
     *         <ul>
     *         <li><b>MILLIS</b>: returns {@link #MILLISECONDS}.</li>
     *         <li><b>SECS</b>: returns {@link #SECONDS}</li>
     * @throws IllegalArgumentException
     *             If the given abbreviation is not known.
     */
    public static TimeUnit fromMojoAbbreviation(String abbreviation) {
        if ("SECS".equalsIgnoreCase(abbreviation))
            return SECONDS;
        else if ("MILLIS".equalsIgnoreCase(abbreviation))
            return MILLISECONDS;

        throw new IllegalArgumentException("Unrecognized time unit abbreviation: " + abbreviation);
    }

    private final long division;
    private final String displayName;

    /**
     * Create a time unit enumeration.
     * 
     * @param displayName
     *            The name to be displayed on the reports.
     * @param division
     *            The amount by which microseconds are to be divided in order to convert the microseconds into this unit's measurement.
     */
    private TimeUnit(String displayName, long division) {
        this.displayName = displayName;
        this.division = division;
    }
    
    /**
     * Convert microseconds to this unit's value.
     * 
     * @param micros
     *            The number of microseconds.
     * @return The given number of microseconds in this unit's measurement.
     */
    public double fromMilliseconds(double micros) {
        return micros / division;
    }

    /**
     * Get the display name of this time unit.
     * 
     * @return The display name of the time unit.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the value by which this unit divides in order to convert from milliseconds to this unit.
     * 
     * @return The division value.
     */
    public long getDivisionValue() {
        return division;
    }
}
