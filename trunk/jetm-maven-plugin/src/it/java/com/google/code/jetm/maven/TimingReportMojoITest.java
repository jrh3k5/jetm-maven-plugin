package com.google.code.jetm.maven;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.test.plugin.BuildTool;
import org.codehaus.plexus.util.FileUtils;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.code.jetm.maven.data.TimeUnit;
import com.google.code.jetm.maven.internal.SimpleAggregate;
import com.google.code.jetm.reporting.xml.XmlAggregateBinder;

import etm.core.aggregation.Aggregate;

/**
 * Integration tests for the timing report mojo.
 * 
 * @author jrh3k5
 * 
 */

public class TimingReportMojoITest {
    /**
     * A {@link Rule} used to retrieve the test name.
     */
    @Rule
    public TestName testName = new TestName();

    private static final Properties originalSystemProperties = System.getProperties();
    private final List<String> cleanTestSite = Arrays.asList("clean", "test", "site");
    private WebDriver driver;
    private BuildTool build;

    /**
     * Set the {@code $maven.home} value in the system properties for the
     * {@link BuildTool} object.
     * 
     * @throws Exception
     *             If any errors occur during the setup.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final ResourceBundle systemPropsBundle = ResourceBundle.getBundle("system");
        System.setProperty("maven.home", systemPropsBundle.getString("maven.home"));
    }

    /**
     * Create and initialize a {@link BuildTool} object to invoke Maven.
     * 
     * @throws Exception
     *             If any errors occur during the setup.
     */
    @Before
    public void setUp() throws Exception {
        build = new BuildTool();
        build.initialize();

        driver = new HtmlUnitDriver();
    }

    /**
     * Clean up resources used by the (possibly) instantiated {@link BuildTool}
     * object.
     * 
     * @throws Exception
     *             If any errors occur during the teardown.
     */
    @After
    public void tearDown() throws Exception {
        if (build != null)
            build.dispose();

        if (driver != null)
            driver.close();
    }

    /**
     * Restore the system properties to their original values.
     * 
     * @throws Exception
     *             If any errors occur during the teardown.
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        System.setProperties(originalSystemProperties);
    }

    /**
     * Test the creation of a report for a demo project.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDemoProject() throws Exception {
        final String projectName = "demo-project";
        final File logFile = getLogFile();
        final InvocationResult result = build.executeMaven(getPom(projectName), null, cleanTestSite, logFile);
        assertThat(result.getExitCode()).isZero();
    
        final WebDriver driver = getDriver();
        driver.get(getSiteIndexLocation(projectName));
        openJetmTimingReport(driver);
    
        final Collection<Aggregate> reportData = getAggregateData();
        for (File reportFile : (List<File>) FileUtils.getFiles(FileUtils.toFile(getClass().getResource("/example-projects/" + projectName + "/target/jetm")), "**/*.xml", null, true)) {
            final Collection<Aggregate> aggregates = getAggregates(reportFile, TimeUnit.SECONDS);
            // Find the entry for the file
            assertHasSection(reportFile.getName());
            // Make sure the data's in the report
            assertThat(reportData).contains(aggregates.toArray());
        }
    }

    /**
     * Test the creation of an empty report.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testEmptyReport() throws Exception {
        final String projectName = "empty-project";
        final File logFile = getLogFile();
        final InvocationResult result = build.executeMaven(getPom(projectName), null, cleanTestSite, logFile);
        assertThat(result.getExitCode()).isZero();

        final File siteIndex = new File(URI.create(getSiteIndexLocation(projectName)));
        assertThat(new File(siteIndex.getParentFile(), "jetm-timing-report.html")).doesNotExist();
    }

    /**
     * Versions of maven-site-plugin past 2.0.x bring in a newer version of
     * doxia which made for stricter requirements in the usage of the doxia API.
     * This manifested in the {@code <th />} tags being incorrectly generated
     * and placed outside of the {@code <table />} tag.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testMavenSitePluginCompatibility() throws Exception {
        final String projectName = "maven-site-plugin-version";
        final File baseLogFile = getLogFile();

        for (String sitePluginVersion : new String[] { "2.0", "2.1", "2.2" }) {
            final File logFile = new File(baseLogFile.getParentFile(), sitePluginVersion + "-" + baseLogFile.getName());

            final Properties mavenProps = new Properties();
            mavenProps.setProperty("site.plugin.version", sitePluginVersion);

            final InvocationResult result = build.executeMaven(getPom(projectName), mavenProps, cleanTestSite, logFile);
            assertThat(result.getExitCode()).as("Execution for version " + sitePluginVersion + " failed.").isZero();

            final InputStream resourceStream = getClass().getResourceAsStream("/example-projects/maven-site-plugin-version/target/site/jetm-timing-report.html");
            assertThat(resourceStream).as("Could not find timing report for plugin version " + sitePluginVersion).isNotNull();
            try {
                final Document document = getDocument(IOUtils.toString(resourceStream));
                @SuppressWarnings("unchecked")
                final Iterator<Element> headerElements = document.getDescendants(new ElementFilter("th"));
                assertThat(headerElements.hasNext()).as("No header elements in version " + sitePluginVersion).isTrue();
                while (headerElements.hasNext()) {
                    final Element tableHeader = headerElements.next();
                    final Element tableRow = tableHeader.getParentElement();
                    assertThat(tableRow.getName()).isEqualTo("tr");

                    /*
                     * doxia used by 2.0 wraps the body of the table in a <tbody
                     * /> tag
                     */
                    Element tableElement = null;
                    if ("2.0".equals(sitePluginVersion)) {
                        final Element tableBody = tableRow.getParentElement();
                        assertThat(tableBody.getName()).isEqualTo("tbody");
                        assertThat((tableElement = tableBody.getParentElement()).getName()).isEqualTo("table");
                    } else
                        assertThat((tableElement = tableRow.getParentElement()).getName()).isEqualTo("table");

                    /*
                     * To ensure some consistent styling, there should be zero
                     * border
                     */
                    final Attribute borderAttribute = tableElement.getAttribute("border");
                    if (borderAttribute != null)
                        assertThat(borderAttribute.getValue()).isEqualTo("0");
                }
            } finally {
                IOUtils.closeQuietly(resourceStream);
            }

            driver.close();
        }
    }

    /**
     * Test the rendering of a report in milliseconds.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRenderInMilliseconds() throws Exception {
        final String projectName = "millisecond-project";
        final File logFile = getLogFile();
        final InvocationResult result = build.executeMaven(getPom(projectName), null, cleanTestSite, logFile);
        assertThat(result.getExitCode()).isZero();

        final WebDriver driver = getDriver();
        driver.get(getSiteIndexLocation(projectName));
        openJetmTimingReport(driver);

        final Collection<Aggregate> reportData = getAggregateData();
        for (File reportFile : (List<File>) FileUtils.getFiles(FileUtils.toFile(getClass().getResource("/example-projects/" + projectName + "/target/jetm")), "**/*.xml", null, true)) {
            final Collection<Aggregate> aggregates = getAggregates(reportFile, TimeUnit.MILLISECONDS);
            // Find the entry for the file
            assertHasSection(reportFile.getName());
            // Make sure the data's in the report
            assertThat(reportData).contains(aggregates.toArray());
        }
    }

    /**
     * Assert that the section by the given name exists in the report.
     * 
     * @param sectionName
     *            The section name whose existence is to be verified.
     */
    private void assertHasSection(String sectionName) {
        for (WebElement element : driver.findElements(By.tagName("h4")))
            if (sectionName.equals(element.getText()))
                return;

        throw new IllegalArgumentException("No section name found: " + sectionName + ". Page source: " + driver.getPageSource());
    }

    /**
     * Parse aggregate data from the report.
     * 
     * @return A {@link Collection} of {@link Aggregate} objects representing
     *         the data in the report.
     */
    private Collection<Aggregate> getAggregateData() {
        final Collection<Aggregate> aggregates = new LinkedList<Aggregate>();
        final List<WebElement> htmlRows = driver.findElements(By.tagName("tr"));
        for (WebElement htmlRow : htmlRows) {
            final List<WebElement> htmlCells = htmlRow.findElements(By.tagName("td"));
            if (htmlCells.size() == 6) {
                final double average = Double.parseDouble(htmlCells.get(1).getText());
                final double min = Double.parseDouble(htmlCells.get(3).getText());
                final double max = Double.parseDouble(htmlCells.get(4).getText());
                final double total = Double.parseDouble(htmlCells.get(5).getText());
                final long measurements = Long.parseLong(htmlCells.get(2).getText());
                aggregates.add(new SimpleAggregate(htmlCells.get(0).getText(), average, min, max, measurements, total));
            }
        }
        return aggregates;
    }

    /**
     * Attempt to parse a given XML document into a JDOM document object. This
     * method tries five times, because there seem to be intermittent issues
     * with reading these generated HTML files - inability to read XSD files,
     * unexpected end of file, and other fun things. In the event that all
     * attempts have been exhausted, all of the collected exceptions will be
     * printed and then another exception will be thrown to interrupt the test
     * execution.
     * <p />
     * Admittedly, this is a hack, but it's a workaround to unstable sources of
     * information (such as the XSD) that should not cause the test to fail.
     * 
     * @param xml
     *            The XML document that is to be parsed.
     * @return A {@link Document} representing the given XML document.
     * @throws IllegalStateException
     *             If all attempts to parse the document are exhausted.
     */
    private Document getDocument(String xml) {
        final int maxAttempts = 5;
        final Exception[] caught = new Exception[maxAttempts];
        for (int i = 0; i < maxAttempts; i++) {
            try {
                return new SAXBuilder().build(new StringReader(xml));
            } catch (Exception e) {
                caught[i] = e;
            }
        }

        for (int i = 0; i < caught.length && caught[i] != null; i++)
            caught[i].printStackTrace();

        throw new IllegalStateException("Too many errors were encountered while trying to parse the document.");
    }

    /**
     * Get a web driver to explore the generated Maven site.
     * 
     * @return A {@link WebDriver}. Each invocation will close whatever driver
     *         was previously returned and return an entirely new driver.
     */
    private WebDriver getDriver() {
        if (driver != null)
            driver.close();

        return driver = new HtmlUnitDriver();
    }

    /**
     * Get the aggregate data from the given file.
     * 
     * @param aggregateData
     *            A {@link File} containing the raw aggregate data to be
     *            collected.
     * @return A {@link Collection} of {@link Aggregate} data parsed from the
     *         given file and rounded to a precision as displayed in the report.
     * @throws IOException
     *             If any errors occur during the collection of the data.
     */
    private Collection<Aggregate> getAggregates(File aggregateData, TimeUnit timeUnit) throws IOException {
        final FileReader reader = new FileReader(aggregateData);
        try {
            final Collection<Aggregate> aggregates = new LinkedList<Aggregate>();
            for (Aggregate original : new XmlAggregateBinder().unbind(reader)) {
                /*
                 * Divide each by a thousand to convert from milliseconds to
                 * seconds
                 */
                final double average = round(original.getAverage() / timeUnit.getDivisionValue());
                final double min = round(original.getMin() / timeUnit.getDivisionValue());
                final double max = round(original.getMax() / timeUnit.getDivisionValue());
                final double total = round(original.getTotal() / timeUnit.getDivisionValue());
                aggregates.add(new SimpleAggregate(original.getName(), average, min, max, original.getMeasurements(), total));
            }
            return aggregates;
        } finally {
            reader.close();
        }
    }

    /**
     * Get a log file to be used in the Maven build.
     * 
     * @return A {@link File} to be used to store Maven build output.
     * @throws IOException
     *             If any errors occur while generating the log file path.
     */
    private File getLogFile() throws IOException {
        final File logFile = new File("target/failsafe-reports/" + testName.getMethodName() + "-maven.log");
        FileUtils.forceMkdir(logFile.getParentFile());
        return logFile;
    }

    /**
     * Retrieve a POM file.
     * 
     * @param artifactId
     *            The artifact ID of the project whose POM is to be retrieved.
     * @return A {@link File} reference to the given artifact ID's POM.
     * @throws IllegalArgumentException
     *             If the POM cannot be found.
     */
    private File getPom(String artifactId) {
        final URL pomUrl = getClass().getResource("/example-projects/" + artifactId + "/pom.xml");
        if (pomUrl == null)
            throw new IllegalArgumentException("No POM found for artifact: " + artifactId);
        return FileUtils.toFile(pomUrl);
    }

    /**
     * Get the location of the index.html for a Maven project's site.
     * 
     * @param artifactId
     *            The artifact ID of the project whose site index is to be
     *            retrieved.
     * @return A {@code String} representing the absolute location of the site
     *         index.
     * @throws IllegalArgumentException
     *             If the site index cannot be found.
     */
    private String getSiteIndexLocation(String artifactId) {
        final URL pomUrl = getClass().getResource("/example-projects/" + artifactId + "/target/site/index.html");
        if (pomUrl == null)
            throw new IllegalArgumentException("No index.html found for artifact: " + artifactId);
        return pomUrl.toExternalForm();
    }

    /**
     * Open the JETM timing report.
     * 
     * @param driver
     *            The {@link WebDriver} to use.
     */
    private void openJetmTimingReport(WebDriver driver) {
        driver.findElement(By.linkText("Project Reports")).click();
        driver.findElement(By.linkText("JETM Timing Report")).click();
    }

    /**
     * Round a floating point value to two decimal places.
     * 
     * @param value
     *            The decimal value to be rounded.
     * @return The given floating point value, rounded to two places.
     */
    private double round(double value) {
        return Double.parseDouble(new DecimalFormat("0.00").format(value));
    }
}
