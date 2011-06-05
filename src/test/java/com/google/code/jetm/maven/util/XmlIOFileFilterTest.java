package com.google.code.jetm.maven.util;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;

/**
 * Unit tests for {@link XmlIOFileFilter}.
 * 
 * @author jrh3k5
 * 
 */

public class XmlIOFileFilterTest {
    /**
     * Test that {@link XmlIOFileFilter#accept(File)} only accepts XML files.
     */
    @Test
    public void testAcceptFile() {
        final XmlIOFileFilter fileFilter = new XmlIOFileFilter();

        final String xmlFileName = "me.xml";
        final File xmlFile = mock(File.class);
        when(xmlFile.getAbsolutePath()).thenReturn(xmlFileName);
        assertThat(fileFilter.accept(xmlFile)).isTrue();

        final String txtFileName = "you.txt";
        final File txtFile = mock(File.class);
        when(txtFile.getAbsolutePath()).thenReturn(txtFileName);
        assertThat(fileFilter.accept(txtFile)).isFalse();
    }

}
