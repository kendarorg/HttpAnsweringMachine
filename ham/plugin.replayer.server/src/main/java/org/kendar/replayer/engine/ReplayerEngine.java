package org.kendar.replayer.engine;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;

public interface ReplayerEngine {
    String getId();
    void loadDb(Long recordingId) throws Exception;
    Response findRequestMatch(Request req, String contentHash) throws Exception;
    ReplayerEngine create(LoggerBuilder logger);
}
