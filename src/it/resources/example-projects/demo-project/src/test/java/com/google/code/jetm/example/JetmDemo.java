package com.google.code.jetm.example;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.jetm.reporting.BindingMeasurementRenderer;
import com.google.code.jetm.reporting.xml.XmlAggregateBinder;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;

/**
 * A simple test that writes out timings to the disk as part of the test
 * execution.
 * 
 * @author jrh3k5
 * 
 */

public class JetmDemo {
    private final EtmMonitor monitor = EtmManager.getEtmMonitor();

    /**
     * Configure JETM.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        BasicEtmConfigurator.configure();
    }

    /**
     * Start the monitor.
     */
    @Before
    public void setUp() {
        monitor.start();
    }

    /**
     * Write out the timing reports to XML files.
     * 
     * @throws Exception
     *             If any errors occur during the write-out.
     */
    @After
    public void tearDown() throws Exception {
        final File destination = new File("target/jetm/demo-"
                + Long.toString(System.currentTimeMillis()) + ".xml");
        FileUtils.forceMkdir(destination.getParentFile());

        final FileWriter writer = new FileWriter(destination);
        try {
            final BindingMeasurementRenderer renderer = new BindingMeasurementRenderer(
                    new XmlAggregateBinder(), writer);
            monitor.render(renderer);
        } finally {
            writer.close();
        }

        monitor.stop();
    }

    /**
     * Gather 100-millisecond timings.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testOneHundred() throws Exception {
        run(100);
    }

    /**
     * Gather 200-millisecond timings.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testTwoHundred() throws Exception {
        run(2000);
    }

    /**
     * Gather timings.
     * 
     * @param milliseconds
     *            The number of milliseconds that each monitor is to collect.
     * @throws Exception
     *             If any errors occur during the runs execution.
     */
    private void run(long milliseconds) throws Exception {
        final EtmPoint pointA = monitor.createPoint("Thread.A");
        Thread.sleep(milliseconds);
        pointA.collect();

        final EtmPoint pointB = monitor.createPoint("Thread.B");
        Thread.sleep(milliseconds * 2);
        pointB.collect();
    }
}
