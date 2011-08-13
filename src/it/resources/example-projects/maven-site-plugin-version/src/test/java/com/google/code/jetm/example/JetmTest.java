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

public class JetmTest {
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
     * Write timing data out.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testWriteOut() throws Exception {
        monitor.createPoint("point").collect();
    }
}
