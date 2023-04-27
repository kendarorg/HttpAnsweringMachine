package org.kendar.replayer.engine.db;

import org.kendar.janus.cmd.interfaces.JdbcCommand;
import org.kendar.janus.results.JdbcResult;
import org.kendar.replayer.storage.ReplayerRow;

public class DbRow {
    private final String initiator;
    private final long connectionId;
    private long traceId;
    private ReplayerRow row;
    private JdbcCommand request;
    private JdbcResult response;
    private boolean visited = false;
    public DbRow(ReplayerRow row, JdbcCommand request, JdbcResult response) {
        this.row = row;
        this.request = request;
        this.response = response;
        var httpRequest = row.getRequest();
        this.initiator = httpRequest.getPathParameter("targetType");
        this.connectionId = Long.parseLong(httpRequest.getHeader("X-Connection-Id"));
        this.traceId = -1L;
        if (httpRequest.getPathParameter("targetId") != null) {
            this.traceId = Long.parseLong(httpRequest.getPathParameter("targetId"));
        }
    }

    public long getConnectionId() {
        return connectionId;
    }

    public long getTraceId() {
        return traceId;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public ReplayerRow getRow() {
        return row;
    }

    public void setRow(ReplayerRow row) {
        this.row = row;
    }

    public JdbcCommand getRequest() {
        return request;
    }

    public void setRequest(JdbcCommand request) {
        this.request = request;
    }

    public JdbcResult getResponse() {
        return response;
    }

    public void setResponse(JdbcResult response) {
        this.response = response;
    }
}
