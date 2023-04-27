package org.kendar.replayer.generator;

import org.kendar.servers.http.Request;
import org.kendar.servers.http.Response;

import java.util.List;

public interface SelectedGenerator {
    String getId();

    void generate(int recordingId, Request req, Response res, List<Long> ids) throws Exception;
}
