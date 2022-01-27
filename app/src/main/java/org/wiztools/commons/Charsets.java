/*
 * Copyright WizTools.org
 * Licensed under the Apache License, Version 2.0:
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.wiztools.commons;

import java.nio.charset.Charset;

/**
 * Defines common charsets supported in all Java platforms.
 * @author subwiz
 */
public interface Charsets{
    public Charset US_ASCII =  Charset.forName("US-ASCII");
    public Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public Charset UTF_8 = Charset.forName("UTF-8");
    public Charset UTF_16BE = Charset.forName("UTF-16BE");
    public Charset UTF_16LE = Charset.forName("UTF-16LE");
    public Charset UTF_16 = Charset.forName("UTF-16");
    public Charset DEFAULT = Charset.defaultCharset();

    public Charset[] ALL = new Charset[]{US_ASCII, ISO_8859_1, UTF_8, UTF_16BE, UTF_16LE, UTF_16};
}