package org.kendar.utils;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

public interface FileResourcesUtils {
    InputStream getFileFromResourceAsStream(String fileName);
    List<String> getFileFromResourceAsString(String fileName);
    File getFileFromResource(String fileName) throws URISyntaxException;
}
