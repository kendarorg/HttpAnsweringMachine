package org.kendar.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public interface FileResourcesUtils {
    InputStream getFileFromResourceAsStream(String fileName);
    byte[] getFileFromResourceAsByteArray(String fileName) throws IOException;
    List<String> getFileFromResourceAsString(String fileName);
    File getFileFromResource(String fileName) throws URISyntaxException;
    String buildPath(String ... paths);
    String buildPathRelative(String ... paths);
    HashMap<String,Object> loadResources(Object clazz,String path) throws  IOException;
}
