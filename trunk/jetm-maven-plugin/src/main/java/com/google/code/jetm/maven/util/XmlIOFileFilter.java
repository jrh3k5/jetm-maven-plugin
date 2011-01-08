package com.google.code.jetm.maven.util;

import java.io.File;
import java.util.Locale;

import org.apache.commons.io.filefilter.AbstractFileFilter;

public class XmlIOFileFilter extends AbstractFileFilter {

    @Override
    public boolean accept(File file) {
        return file.getAbsolutePath().toLowerCase(Locale.getDefault()).endsWith(".xml");
    }
}
