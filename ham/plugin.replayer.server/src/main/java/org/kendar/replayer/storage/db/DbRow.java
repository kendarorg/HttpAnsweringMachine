package org.kendar.replayer.storage.db;

import org.kendar.janus.cmd.JdbcCommand;
import org.kendar.janus.results.JdbcResult;
import org.kendar.replayer.storage.ReplayerRow;

public class DbRow {
    private final String initiator;
    private long connectionId;

    public long getConnectionId() {
        return connectionId;
    }

    public long getTraceId() {
        return traceId;
    }

    private long traceId;
    private ReplayerRow row;
    private JdbcCommand request;
    private JdbcResult response;

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    private boolean visited = false;

    public DbRow(ReplayerRow row, JdbcCommand request, JdbcResult response) {
        this.row = row;
        this.request = request;
        this.response = response;
        var httpRequest = row.getRequest();
        this.initiator = httpRequest.getPathParameter("targetType");
        this.connectionId = Long.parseLong(httpRequest.getHeader("X-Connection-Id"));
        this.traceId = -1L;
        if(httpRequest.getPathParameter("targetId")!=null){
            this.traceId = Long.parseLong(httpRequest.getPathParameter("targetId"));
        }
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