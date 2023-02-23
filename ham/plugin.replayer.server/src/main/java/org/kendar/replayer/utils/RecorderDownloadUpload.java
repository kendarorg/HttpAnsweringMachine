package org.kendar.replayer.utils;

import org.kendar.utils.FullDownloadUpload;

import java.util.HashMap;
import java.util.Map;

public class RecorderDownloadUpload implements FullDownloadUpload {
    @Override
    public Map<String, byte[]> retrieveItems() throws Exception {

        Map<String, byte[]> result = new HashMap<>();

        return result;
    }

    @Override
    public String getId() {
        return "recorder";
    }

    @Override
    public void uploadItems(HashMap<String, byte[]> data) throws Exception {

    }
}
