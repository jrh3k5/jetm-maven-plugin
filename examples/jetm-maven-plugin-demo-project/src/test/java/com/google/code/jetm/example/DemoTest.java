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

public class DemoTest {
    private final EtmMonitor monitor = EtmManager.getEtmMonitor();
    private final String pointAName = "ThreadA";
    private final String pointBName = "ThreadB";

    @BeforeClass
    public static void setUpBeforeClass() {
        BasicEtmConfigurator.configure();
    }
    
    @Before
    public void setUp() {
        monitor.start();
    }

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

    @Test
    public void testOneHundred() throws Exception {
        run(100);
    }

    @Test
    public void testTwoHundred() throws Exception {
        run(2000);
    }

    private void run(long milliseconds) throws Exception {
        final EtmPoint pointA = monitor.createPoint("Thread.A");
        Thread.sleep(milliseconds);
        pointA.collect();

        final EtmPoint pointB = monitor.createPoint("Thread.B");
        Thread.sleep(milliseconds * 2);
        pointB.collect();
    }
}
