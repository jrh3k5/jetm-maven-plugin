package com.google.code.jetm.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import com.google.code.jetm.maven.data.AggregateSummary;
import com.google.code.jetm.maven.util.XmlIOFileFilter;
import com.google.code.jetm.reporting.AggregateBinder;
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
     * The directories containing the timing report XML files.
     * 
     * @parameter
     * @required
     */
    private File[] timings;

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

    private DecimalFormat decimalFormatter = new DecimalFormat("0.00");
    private final AggregateBinder binder = new XmlAggregateBinder();
    private final XmlIOFileFilter xmlFileFilter = new XmlIOFileFilter();

    /**
     * {@inheritDoc}
     */
    protected void executeReport(Locale locale) throws MavenReportException {
        final Map<File, List<Aggregate>> aggregates = getAggregates();
        final List<AggregateSummary> summaries = aggregate(aggregates);
        Collections.sort(summaries);

        final Sink sink = getSink();
        try {
            sink.head();
            sink.title();
            sink.text(getName(locale));
            sink.title_();
            sink.head_();

            sink.body();
            sink.sectionTitle1();
            sink.text(getName(locale));
            sink.sectionTitle1_();

            sink.sectionTitle2();
            sink.text("Summary");
            sink.sectionTitle2_();

            sink.text("This is a summary, by measurement name, of the measurements taken.");

            if (summaries.isEmpty()) {
                sink.text(" There are no JETM timings available for reporting.");
                return;
            }

            print(sink, summaries);

            sink.sectionTitle2();
            sink.text("File Breakdown");
            sink.sectionTitle2_();

            sink.text("This is a list of, per XML file, the measurements taken.");

            for (Entry<File, List<Aggregate>> entry : aggregates.entrySet()) {
                sink.sectionTitle3();
                sink.text(entry.getKey().getName());
                sink.sectionTitle3_();

                print(sink, entry.getValue());
            }
        } finally {
            sink.body_();

            sink.flush();
            sink.close();
        }
    }

    private void print(Sink sink, Collection<? extends Aggregate> aggregates) {
        sink.table();
        tableHeaderCell(sink, "Name");
        tableHeaderCell(sink, "Average (sec)");
        tableHeaderCell(sink, "Measurements");
        tableHeaderCell(sink, "Minimum (sec)");
        tableHeaderCell(sink, "Maximum (sec)");
        tableHeaderCell(sink, "Total");
        for (Aggregate aggregate : aggregates) {
            sink.tableRow();
            tableCell(sink, aggregate.getName());
            tableCell(sink, decimalFormatter.format((aggregate.getTotal() / aggregate
                    .getMeasurements()) / 1000.0));
            tableCell(sink, Long.toString(aggregate.getMeasurements()));
            tableCell(sink, decimalFormatter.format(aggregate.getMin() / 1000.0));
            tableCell(sink, decimalFormatter.format(aggregate.getMax() / 1000.0));
            tableCell(sink, decimalFormatter.format(aggregate.getTotal() / 1000.0));
            sink.tableRow_();
        }
        sink.table_();
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

    private Map<File, List<Aggregate>> getAggregates() throws MavenReportException {
        final Map<File, List<Aggregate>> aggregates = new LinkedHashMap<File, List<Aggregate>>();
        for (File directory : timings) {
            for (File file : FileUtils.listFiles(directory, xmlFileFilter, TrueFileFilter.TRUE)) {
                final List<Aggregate> aggregateList = new LinkedList<Aggregate>();
                FileReader reader;
                try {
                    reader = new FileReader(file);
                } catch (FileNotFoundException e) {
                    throw new MavenReportException("File not found: " + file, e);
                }
                try {
                    aggregateList.addAll(binder.unbind(reader));
                } finally {
                    IOUtils.closeQuietly(reader);
                }
                aggregates.put(file, aggregateList);
            }
        }
        return aggregates;
    }

    private List<AggregateSummary> aggregate(Map<File, List<Aggregate>> aggregates) {
        if (aggregates.isEmpty())
            return Collections.emptyList();

        final Map<String, AggregateSummary> summaries = new HashMap<String, AggregateSummary>();
        for (List<Aggregate> aggregateList : aggregates.values())
            for (Aggregate aggregate : aggregateList) {
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
