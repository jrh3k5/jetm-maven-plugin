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
     * The directories containing the timing report XML files. If not set, then a default of "${project.build.directory}/jetm" will be used instead.
     * 
     * @parameter
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
     * The build directory for the Maven project.
     * 
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private File buildDirectory;

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

            if (summaries.isEmpty()) {
                sink.text(" There are no JETM timings available for reporting.");
                return;
            }

            sink.sectionTitle2();
            sink.text("Summary");
            sink.sectionTitle2_();

            sink.text("This is a summary, by measurement name, of the measurements taken.");

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

    /**
     * Print a table containing information within a given set of aggregates.
     * 
     * @param sink
     *            The {@link Sink} used to render out the table.
     * @param aggregates
     *            A {@link Collection} of {@link Aggregate} objects representing
     *            the data to be written out.
     */
    private void print(Sink sink, Collection<? extends Aggregate> aggregates) {
        sink.table();
        sink.tableRows(null, false);
        sink.tableRow();
        tableHeaderCell(sink, "Name");
        tableHeaderCell(sink, "Average (sec)");
        tableHeaderCell(sink, "Measurements");
        tableHeaderCell(sink, "Minimum (sec)");
        tableHeaderCell(sink, "Maximum (sec)");
        tableHeaderCell(sink, "Total");
        sink.tableRow_();
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
        sink.tableRows_();
        sink.table_();
    }

    /**
     * Create a table header cell.
     * 
     * @param sink
     *            The {@link Sink} used to render out the header.
     * @param text
     *            The text to be printed within the table header.
     */
    private void tableHeaderCell(Sink sink, String text) {
        sink.tableHeaderCell();
        sink.text(text);
        sink.tableHeaderCell_();
    }

    /**
     * Create a table cell.
     * 
     * @param sink
     *            The {@link Sink} used to render out the table cell.
     * @param text
     *            The text to be written inside the cell.
     */
    private void tableCell(Sink sink, String text) {
        sink.tableCell();
        sink.text(text);
        sink.tableCell_();
    }

    /**
     * Get aggregates.
     * 
     * @return A {@link Map}. Its keys are the files that contain aggregate
     *         data; the values are {@link List}s of {@link Aggregate} objects
     *         representing the timings read within each file.
     *         <p />
     *         If a file contains no timing data, it will not be returned in
     *         this map.
     * @throws MavenReportException
     *             If any errors occur while reading the file.
     */
    private Map<File, List<Aggregate>> getAggregates() throws MavenReportException {
        final Map<File, List<Aggregate>> aggregates = new LinkedHashMap<File, List<Aggregate>>();
        for (File directory : getTimings()) {
            if (!directory.exists())
                continue;

            for (File file : FileUtils.listFiles(directory, xmlFileFilter, TrueFileFilter.TRUE)) {
                final List<Aggregate> aggregateList = new LinkedList<Aggregate>();
                FileReader reader;
                try {
                    reader = new FileReader(file);
                } catch (FileNotFoundException e) {
                    throw new MavenReportException("File not found: " + file, e);
                }
                try {
                    final Collection<Aggregate> unbound = binder.unbind(reader);
                    if (unbound.isEmpty())
                        continue;
                    aggregateList.addAll(unbound);
                } finally {
                    IOUtils.closeQuietly(reader);
                }
                aggregates.put(file, aggregateList);
            }
        }
        return aggregates;
    }

    /**
     * Get the timings directories.
     * 
     * @return An array of {@link File} objects representing the configured timing directories; if none are configured, then "${project.build.directory}/jetm" will be used as a default.
     */
    private File[] getTimings() {
        return timings == null ? new File[] { new File(buildDirectory, "jetm") } : timings;
    }

    /**
     * Create aggregate summaries.
     * 
     * @param aggregates
     *            A {@link Map} containing the aggregate data to be summarized.
     * @return A {@link List} of {@link AggregateSummary} objects representing
     *         the entirety of aggregate data, summarized by name.
     * @see #getAggregates()
     */
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
