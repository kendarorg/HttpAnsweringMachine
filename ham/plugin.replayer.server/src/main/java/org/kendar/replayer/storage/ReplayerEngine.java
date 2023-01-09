package org.kendar.replayer.storage;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

public interface ReplayerEngine {
    String getId();
    void loadDb(Long recordingId) throws Exception;
    Response findRequestMatch(Request req, String contentHash) throws Exception;
    ReplayerEngine create();
}
