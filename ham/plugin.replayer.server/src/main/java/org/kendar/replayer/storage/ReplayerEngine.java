package org.kendar.replayer.storage;

import org.kendar.servers.http.Request;

public interface ReplayerEngine {
    String getId();
    void loadDb(Long recordingId) throws Exception;
    ReplayerRow findRequestMatch(Request req,String contentHash) throws Exception;
    ReplayerEngine create();
}
