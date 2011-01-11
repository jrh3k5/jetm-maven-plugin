package com.google.code.jetm.maven.util;

import java.io.File;
import java.util.Locale;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * An {@link IOFileFilter} used to exclude everything but XML files.
 * 
 * @author jrh3k5
 * 
 */

public class XmlIOFileFilter extends AbstractFileFilter {

    @Override
    public boolean accept(File file) {
        return file.getAbsolutePath().toLowerCase(Locale.getDefault()).endsWith(".xml");
    }
}
