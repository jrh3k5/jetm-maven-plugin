# Introduction #

This plugin uses the [jetm-reporting-utilities](http://code.google.com/p/jetm-reporting-utilities/) library - specifically, its XmlAggregateBinder - to read written-out timing data.

# Generating a Report #

Assume that you have a test like this:

```
package foo;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;

import com.google.code.jetm.reporting.BindingMeasurementRenderer;
import com.google.code.jetm.reporting.xml.XmlAggregateBinder;

public class XmlAggregateBinderDemoTest {
    private static EtmMonitor monitor;

    /**
     * Configure JETM
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        BasicEtmConfigurator.configure();

        monitor = EtmManager.getEtmMonitor();
        monitor.start();
    }

    /**
     * Write out the results of all of the test runs. This writes out 
     * the collected point data to an XML file located in target/jetm
     * beneath the working directory.
     * 
     * @throws Exception
     *             If any errors occur during the write-out.
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (monitor != null) {
            monitor.stop();

            final File timingDirectory = new File("target/jetm");
            FileUtils.forceMkdir(timingDirectory);

            final File timingFile = new File(timingDirectory, XmlAggregateBinderDemoTest.class.getSimpleName() + ".xml");
            final FileWriter writer = new FileWriter(timingFile);
            try {
                monitor.render(new BindingMeasurementRenderer(new XmlAggregateBinder(), writer));
            } finally {
                writer.close();
            }
        }
    }

    @Test
    public void doStuff() {
       // execute some code here that starts and stops JETM collection points
    }
}
```

Within your project POM, place the following:
```
<project>
    <reporting>
        <plugins>
            <plugin>
                <groupId>com.google.code.jetm</groupId>
                <artifactId>jetm-maven-plugin</artifactId>
                <version>1.0.1</version>
                <configuration>
                    <timings>
                        <timing>${project.build.directory}/jetm</timing>
                    </timings>
                </configuration>
            </plugin>
        </plugins>
    </reporting>
</project>
```

Then simply run:

```
mvn test site
```

...and you will get a generated report of all of your collected timings.

# Generating an Example Report #

The plugin has some example projects that you can use to see the report in action. Run the following from your command line to check out the example projects:

```
svn checkout http://jetm-maven-plugin.googlecode.com/svn/tags/jetm-maven-plugin-1.0.1/src/it/resources/example-projects/
```

Navigate within any of the projects and run the following:

```
mvn test site -Djetm.plugin.version=1.0.1
```

...and you will see a "JETM Timing Report" in the site, detailing the collected timings.