package org.kendar.replayer.engine;

import org.kendar.replayer.storage.DbRecording;
import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;
import org.kendar.utils.LoggerBuilder;

import java.util.Map;

public interface ReplayerEngine {
    String getId();
    void loadDb(Long recordingId) throws Exception;
    Response findRequestMatch(Request req, String contentHash, Map<String, String> params) throws Exception;
    ReplayerEngine create(LoggerBuilder logger);

    boolean isValidPath(Request path);

    boolean isValidRoundTrip(Request req, Response res, Map<String, String> specialParams);

    void setParams(Map<String, String> params);

    void setupStaticCalls(DbRecording recording) throws Exception;

    void updateReqRes(Request req, Response res, Map<String, String> specialParams);
}
