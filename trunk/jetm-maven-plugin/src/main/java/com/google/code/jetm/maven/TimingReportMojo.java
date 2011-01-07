package com.google.code.jetm.maven;

import java.util.Locale;

import org.apache.maven.doxia.siterenderer.DefaultSiteRenderer;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import com.google.code.jetm.reporting.xml.XmlAggregateBinder;

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
    private DefaultSiteRenderer siteRenderer;

    /**
     * {@inheritDoc}
     */
    public String getOutputName() {
        return "JETM Timing Report";
    }

    /**
     * {@inheritDoc}
     */
    public String getName(Locale locale) {
        return getOutputName();
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
        // TODO Auto-generated method stub

    }

}
