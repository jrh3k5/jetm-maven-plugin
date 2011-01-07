package com.google.code.jetm.maven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import com.google.code.jetm.maven.data.AggregateSummary;
import com.google.code.jetm.reporting.xml.XmlAggregateBinder;

import etm.core.aggregation.Aggregate;

/**
 * A mojo used to create a report that displays the collective JETM timings that
 * were collected and rendered using an {@link XmlAggregateBinder}.
 * 
 * @author jrh3k5
 * @goal timing-report
 * @phase site
 */

public class TimingReportMojo extends AbstractMavenReport {
    /**
     * Directory where reports will go.
     * 
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    private String outputDirectory;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

    /**
     * {@inheritDoc}
     */
    public String getOutputName() {
        return "jetm-timing-report";
    }

    /**
     * {@inheritDoc}
     */
    public String getName(Locale locale) {
        return "JETM Timing Report";
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription(Locale locale) {
        return "A collective report of all JETM timings that were collected and rendered.";
    }

    /**
     * {@inheritDoc}
     */
    protected Renderer getSiteRenderer() {
        return siteRenderer;
    }

    /**
     * {@inheritDoc}
     */
    protected String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * {@inheritDoc}
     */
    protected MavenProject getProject() {
        return project;
    }

    /**
     * {@inheritDoc}
     */
    protected void executeReport(Locale locale) throws MavenReportException {
        final List<Aggregate> aggregates = getAggregates();
        final List<AggregateSummary> summaries = aggregate(aggregates);
        final Sink sink = getSink();
        try {
            sink.head();
            sink.title();
            sink.text(getName(locale));
            sink.title_();
            sink.head_();

            sink.body();
            sink.section1();
            sink.text(getName(locale));
            sink.section1_();

            sink.section2();
            sink.text("Summary");
            sink.section2_();

            if (summaries.isEmpty()) {
                sink.text("There are no JETM timings available for reporting.");
                return;
            }

            sink.table();
            tableHeaderCell(sink, "Name");
            tableHeaderCell(sink, "Average");
            tableHeaderCell(sink, "Measurements");
            tableHeaderCell(sink, "Minimum");
            tableHeaderCell(sink, "Maximum");
            tableHeaderCell(sink, "Total");
            for (AggregateSummary summary : summaries) {
                sink.tableRow();
                tableCell(sink, summary.getName());
                tableCell(sink, Double.toString(summary.getTotal() / summary.getMeasurements()));
                tableCell(sink, Long.toString(summary.getMeasurements()));
                tableCell(sink, Double.toString(summary.getMin()));
                tableCell(sink, Double.toString(summary.getMax()));
                tableCell(sink, Double.toString(summary.getTotal()));
                sink.tableRow_();
            }
            sink.table_();
        } finally {
            sink.body_();

            sink.flush();
            sink.close();
        }
    }

    private void tableHeaderCell(Sink sink, String text) {
        sink.tableHeaderCell();
        sink.text(text);
        sink.tableHeaderCell_();
    }

    private void tableCell(Sink sink, String text) {
        sink.tableCell();
        sink.text(text);
        sink.tableCell_();
    }

    private List<Aggregate> getAggregates() {
        // TODO
        return Collections.emptyList();
    }

    private List<AggregateSummary> aggregate(Collection<Aggregate> aggregates) {
        if (aggregates.isEmpty())
            return Collections.emptyList();

        final Map<String, AggregateSummary> summaries = new HashMap<String, AggregateSummary>();
        for (Aggregate aggregate : aggregates) {
            final String name = aggregate.getName();
            if (!summaries.containsKey(name))
                summaries.put(name, new AggregateSummary(name));

            final AggregateSummary summary = summaries.get(name);
            summary.add(aggregate);
        }

        final List<AggregateSummary> summaryList = new ArrayList<AggregateSummary>(summaries.size());
        for (AggregateSummary summary : summaries.values())
            summaryList.add(summary);

        return summaryList;

    }
}
