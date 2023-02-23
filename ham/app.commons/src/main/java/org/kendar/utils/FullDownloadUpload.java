package org.kendar.utils;

import java.util.HashMap;
import java.util.Map;

public interface FullDownloadUpload {
    Map<String, byte[]> retrieveItems() throws Exception;

    String getId();

    void uploadItems(HashMap<String, byte[]> data) throws Exception;
}
