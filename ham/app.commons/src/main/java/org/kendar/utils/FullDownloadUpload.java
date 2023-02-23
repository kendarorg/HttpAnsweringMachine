package org.kendar.utils;

import java.util.Map;

public interface FullDownloadUpload {
    Map<String, byte[]> retrieveItems() throws Exception;

    String getId();
}
